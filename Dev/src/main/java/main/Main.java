package main;

import Logger.Log;
import authentication.UserAuthentication;
import exceptions.InvalidActionException;
import exceptions.SubscriberAlreadyExistsException;
import exceptions.WrongPasswordException;
import externalServices.DeliverySystem;
import externalServices.PaymentSystem;
import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;
import io.javalin.websocket.WsContext;
import notifications.Notification;
import notifications.Observer;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import persistence.DatabaseFetcher;
import persistence.Repo;
import policies.DiscountPolicy;
import policies.PurchasePolicy;
import presenatation.TradingSystem;
import service.TradingSystemService;
import service.TradingSystemServiceImpl;
import store.Store;
import tradingSystem.TradingSystemBuilder;
import tradingSystem.TradingSystemImpl;
import user.AdminPermission;
import user.Subscriber;
import user.Visitors;
import util.Filters;
import util.HerokuUtil;
import util.Path;
import util.ViewUtil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    private static class WsObserver implements Observer {

        private WsContext ctx;

        public WsObserver(WsContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void notify(Notification notification) {
            if(ctx.session.isOpen())
                ctx.send(notification.print());
        }
    }
    // Declare dependencies
    public static presenatation.TradingSystem tradingSystem;
    private static Log errorLog;

    public static void main(String[] args) throws InvalidActionException {
        errorLog = new Log();
        PropertyConfigurator.configure("Dev/log4j.properties");
        Config cfg = new Config();
        String configPath = "Dev/config/config.properties";
        if(args.length > 1)
            configPath = args[1];
        try (InputStream input = new FileInputStream(configPath)) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            cfg.adminName = prop.getProperty("system.admin.name");
            cfg.adminPassword = prop.getProperty("system.admin.password");
            cfg.port = Integer.parseInt(prop.getProperty("port"));
            cfg.sslPort = Integer.parseInt(prop.getProperty("sslPort"));
            cfg.stateFileAddress = prop.getProperty("stateFileAddress");
            cfg.startupScript = prop.getProperty("startupScript");
            cfg.paymentSystem = prop.getProperty("paymentSystem");
            cfg.deliverySystem = prop.getProperty("deliverySystem");
            Repo.setPersistence_unit(prop.getProperty("persistence.unit"));
        }catch (FileNotFoundException e) {
            errorLog.errorToLogger("initialization of the system made with non-exist properties file");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("How to initialize the system?");
//        System.out.println("0 - startup script");
//        System.out.println("1 - from existing DB");
//        Scanner scan = new Scanner(System.in);
//        int flag = scan.nextInt();
        int flag;
        if(Repo.isDBEmpty() == true)
            flag = 0; //use startup script
        else
            flag = 1; //use data from DB
        run(cfg, flag);


//        run(cfg);
    }

    public static void run(Config cfg, int flag) throws InvalidActionException {

        UserAuthentication userAuthentication;
        Visitors visitors;
        // work around for the system initialization
        if(flag == 0) {
            userAuthentication = new UserAuthentication();
            visitors = new Visitors();
        }
        else {
            userAuthentication = Repo.getAuthentication();
            visitors = Repo.getVisitors();
        }
        try{
            userAuthentication.register(cfg.adminName, cfg.adminPassword);
        }catch (SubscriberAlreadyExistsException e){
            try {
                userAuthentication.authenticate(cfg.adminName, cfg.adminPassword);
            }catch (WrongPasswordException w){
                errorLog.errorToLogger("Wrong password for admin, name: " + cfg.adminName + ", password: ******");
            }
        }
        ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();
        AtomicInteger subscriberIdCounter = new AtomicInteger();
        Subscriber admin = new Subscriber(subscriberIdCounter.getAndIncrement(), cfg.adminName);
        admin.addPermission(AdminPermission.getInstance());
        subscribers.put(cfg.adminName, admin);
        PaymentSystem paymentSystem;
        DeliverySystem deliverySystem;
        try {
            Class<?> cls = Class.forName(cfg.paymentSystem, true, ClassLoader.getSystemClassLoader());
            paymentSystem = (PaymentSystem) cls.getConstructor().newInstance();
            cls = Class.forName(cfg.deliverySystem, true, ClassLoader.getSystemClassLoader());
            deliverySystem = (DeliverySystem) cls.getConstructor().newInstance();

        }catch (ClassNotFoundException e){
            errorLog.errorToLogger("paymentSystem or deliverySystem in config.properties are wrong. paymentSystem: " + cfg.paymentSystem + ", deliverySystem: " + cfg.deliverySystem);
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        tradingSystem.TradingSystem tradingSystem;
        if(flag == 0) {
            tradingSystem = new TradingSystemBuilder().setUserName(cfg.adminName).setPassword(cfg.adminPassword)
                    .setSubscriberIdCounter(subscriberIdCounter).setSubscribers(subscribers).setAuth(userAuthentication)
                    .setPaymentSystem(paymentSystem).setDeliverySystem(deliverySystem).build();
            //map.clear();
        }else
        {
            DatabaseFetcher fetcher = new DatabaseFetcher();

            ConcurrentHashMap<String, Subscriber> subscribers_from_DB = fetcher.getSubscribers();
            for (String username: subscribers.keySet()) {
                subscribers_from_DB.put(username, subscribers.get(username));
            }
            ConcurrentHashMap<Integer, Store> stores_fromDB = fetcher.getStores();
//            ConcurrentHashMap<Integer, DiscountPolicy> discounts = fetcher.getDiscountPolicies();
//            ConcurrentHashMap<Integer, PurchasePolicy> purchases = fetcher.getPurchasePolicies();
            ConcurrentHashMap<Store, Collection<Integer>> stores_discounts = fetcher.getStoresDiscountPolicies();
            ConcurrentHashMap<Store, Collection<Integer>> stores_purchases = fetcher.getStoresPurchasePolicies();
            AtomicInteger subscribersIdCounter = fetcher.getSubscriberIdCounter();
            ConcurrentHashMap<Integer, PurchasePolicy> purchasesMap = fetcher.getPurchasePolicies();
            ConcurrentHashMap<Integer, DiscountPolicy> discountsMap = fetcher.getDiscountPolicies();


            tradingSystem = new TradingSystemBuilder().setUserName(cfg.adminName).setPassword(cfg.adminPassword)
                    .setSubscriberIdCounter(subscribersIdCounter).setSubscribers(subscribers_from_DB).setAuth(userAuthentication)
                    .setPaymentSystem(paymentSystem).setDeliverySystem(deliverySystem).setStores(stores_fromDB)
                    .setDiscountPolicies(discountsMap).setPurchasePolicies(purchasesMap)
                    .setStoresDiscountPolicies(stores_discounts).setStoresPurchasePolicies(stores_purchases)
                    .setVisitors_in_system(visitors).build();
        }



        TradingSystemService tradingSystemService = new TradingSystemServiceImpl(new TradingSystemImpl(tradingSystem));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try {
            compiler.run(null, null, null, cfg.stateFileAddress);
        }catch (NullPointerException e){
            errorLog.errorToLogger("State file is not exist. state file: " + cfg.stateFileAddress);
            throw new NullPointerException();
        }
        if(flag == 0) {
            try {
                Class<?> cls = Class.forName(cfg.startupScript, true, ClassLoader.getSystemClassLoader());
                Method method = cls.getMethod("run", TradingSystemService.class);
                method.invoke(null, tradingSystemService);

            } catch (ClassNotFoundException e) {
                errorLog.errorToLogger("Class for startupScript is not exist. startupScript: " + cfg.startupScript);
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                errorLog.errorToLogger("method for startupScript is not exist.");
                throw new RuntimeException(e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }



        // Instantiate your dependencies
        Main.tradingSystem = new TradingSystem(tradingSystemService);

        Javalin app = Javalin.create(config -> {
            config.server(() -> {
                Server server = new Server();
                ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory());
                sslConnector.setPort(cfg.sslPort);
                ServerConnector connector = new ServerConnector(server);
                connector.setPort(cfg.port);
                server.setConnectors(new Connector[]{sslConnector, connector});
                return server;
            });
            config.addStaticFiles("/public");
            config.registerPlugin(new RouteOverviewPlugin("/routes"));
        }).start(HerokuUtil.getHerokuAssignedPort());

        app.ws("/notifications", ws -> {
            ws.onClose(ctx -> {
                Subscriber subscriber = ctx.attribute("subscriber");
                subscriber.setObserver(null);
            });
            ws.onMessage(ctx -> {
                Subscriber subscriber = tradingSystem.getUserByConnectionId(ctx.message()).getSubscriber();
                ctx.attribute("subscriber", subscriber);
                Observer wsObserver = new WsObserver(ctx);
                subscriber.setObserver(wsObserver);
            });
        });

        app.ws("/visitors", ws -> {
            ws.onClose(ctx -> {
                Subscriber subscriber = ctx.attribute("subscriber");
                subscriber.setAdminObserver(null);
            });
            ws.onMessage(ctx -> {
                Subscriber subscriber = tradingSystem.getUserByConnectionId(ctx.message()).getSubscriber();
                ctx.attribute("subscriber", subscriber);
                Observer wsObserver = new WsObserver(ctx);
                subscriber.setAdminObserver(wsObserver);
            });
        });

        app.routes(() -> {
            before(Filters.handleLocaleChange);
            //before(LoginController.ensureLoginBeforeViewingBooks);
            get(Path.Web.HOME, Main.tradingSystem.serveHomePage);
            get(Path.Web.REGISTER, Main.tradingSystem.serveRegisterPage);
            post(Path.Web.REGISTER, Main.tradingSystem.handleRegisterPost);
            post(Path.Web.NotFound, Main.tradingSystem.handleNotFoundPost);
            get(Path.Web.ROOT, Main.tradingSystem.serveRootPage);
            post(Path.Web.ROOT, Main.tradingSystem.handleRootPost);
            //get(Path.Web.BOOKS, BookController.fetchAllBooks);
            //get(Path.Web.ONE_BOOK, BookController.fetchOneBook);
            post(Path.Web.PURCHASE, Main.tradingSystem.handlePurchasePost);
            get(Path.Web.LOGIN, Main.tradingSystem.serveLoginPage);
            get(Path.Web.SHOWBASKET, Main.tradingSystem.serveShowBasketPage);
            post(Path.Web.SHOWBASKET, Main.tradingSystem.handleShowBasketPost);
            get(Path.Web.UPDATEPRODUCTAMOUNTINBASKET, Main.tradingSystem.serveUpdateProductAmountInBasket);
            post(Path.Web.UPDATEPRODUCTAMOUNTINBASKET, Main.tradingSystem.handleUpdateProductAmountInBasketPost);
            get(Path.Web.CART, Main.tradingSystem.serveCartPage);
            post(Path.Web.CART, Main.tradingSystem.handleCartPost);
            get(Path.Web.PURCHASEHISTORY, Main.tradingSystem.servePurchaseHistoryPage);
            post(Path.Web.PURCHASEHISTORY, Main.tradingSystem.handlePurchaseHistoryPost);
            get(Path.Web.OPENNEWSTORE, Main.tradingSystem.serveOpenNewStorePage);
            post(Path.Web.OPENNEWSTORE, Main.tradingSystem.handleOpenNewStorePost);
            get(Path.Web.ADDITEMTOSTORE, Main.tradingSystem.serveAddItemToStorePage);

            post(Path.Web.ALLOWMANAGERTOUPDATEPRODUCTS, Main.tradingSystem.handleAllowManagerToUpdateProductsPost);
            post(Path.Web.DISABLEMANAGERFROMUPDATEPRODUCTS, Main.tradingSystem.handleDisableManagerFromUpdateProductsPost);
            post(Path.Web.ALLOWMANAGERTOEDITPOLICIES, Main.tradingSystem.handleAllowManagerToEditPoliciesPost);
            post(Path.Web.DISABLEMANAGERFROMEDITPOLICIES, Main.tradingSystem.handleDisableManagerFromEditPoliciesPost);
            post(Path.Web.ALLOWMANAGERTOGETHISTORY, Main.tradingSystem.handleAllowManagerToGetHistoryPost);
            post(Path.Web.DISABLEMANAGERFROMGETHISTORY, Main.tradingSystem.handleDisableManagerFromGetHistoryPost);
            get(Path.Web.PERMISSIONSFORMANAGER, Main.tradingSystem.servePermissionsForManagerPage);

            post(Path.Web.GETITEMS, Main.tradingSystem.handleGetItemsPost);
            get(Path.Web.GETITEMS, Main.tradingSystem.serveGetItemsPage);

            post(Path.Web.ADDITEMTOBASKET, Main.tradingSystem.handleAddItemToBasketPost);
            get(Path.Web.ADDITEMTOBASKET, Main.tradingSystem.serveAddItemToBasketPage);

            post(Path.Web.WRITEOPINIONONPRODUCT, Main.tradingSystem.handleWriteOpinionOnProductPost);
            get(Path.Web.WRITEOPINIONONPRODUCT, Main.tradingSystem.serveWriteOpinionOnProductPage);

            post(Path.Web.GETITEMSBYSTORE, Main.tradingSystem.handleGetItemsBtStorePost);
            post(Path.Web.SHOWSTAFFINFO, Main.tradingSystem.handleShowStaffPost);
            post(Path.Web.SALESHISTORY, Main.tradingSystem.handleGetSalesHistoryPost);
            post(Path.Web.STOREPOLICIES, Main.tradingSystem.handleGetStorePoliciesPost);
            get(Path.Web.GETSTOREDETAILS, Main.tradingSystem.serveGetStoreDetailsPage);

            post(Path.Web.GETSTORESINFO, Main.tradingSystem.handleGetStoresInfoPost);
            post(Path.Web.GETERRORLOG, Main.tradingSystem.handleGetErrorLogPost);
            post(Path.Web.GETEVENTLOG, Main.tradingSystem.handleGetEventLogPost);
            get(Path.Web.ADMINACTIONS, Main.tradingSystem.serveAdminActionsPage);

            post(Path.Web.ASSIGNSTOREPURCHASEPOLICY, Main.tradingSystem.handleAssignStorePurchasePolicyPost);
            post(Path.Web.REMOVEPOLICY, Main.tradingSystem.handleRemovePolicyPost);
            post(Path.Web.ASSIGNSTOREDISCOUNTPOLICY, Main.tradingSystem.handleAssignStoreDiscountPolicyPost);
            post(Path.Web.REMOVEDISCOUNT, Main.tradingSystem.handleRemoveDiscountPost);
            get(Path.Web.ASSIGNREMOVEPOLICY, Main.tradingSystem.serveAssignRemovePolicyPage);

            post(Path.Web.ANDPOLICY, Main.tradingSystem.handleAndPolicyPost);
            post(Path.Web.ORPOLICY, Main.tradingSystem.handleOrPolicyPost);
            post(Path.Web.XORPOLICY, Main.tradingSystem.handleXorPolicyPost);
            post(Path.Web.MAKEPLUSDISCOUNT, Main.tradingSystem.handleMakePlusDiscountPost);
            post(Path.Web.MAKEMAXDISCOUNT, Main.tradingSystem.handleMakeMaxDiscountPost);
            get(Path.Web.COMPLEXPOLICIES, Main.tradingSystem.serveComplexPoliciesPage);

            post(Path.Web.DELETEPRODUCTFROMSTORE, Main.tradingSystem.handleDeleteProductFromStorePost);
            get(Path.Web.DELETEPRODUCTFROMSTORE, Main.tradingSystem.serveDeleteProductFromStorePage);

            post(Path.Web.MAKEBASKETPURCHASEPOLICY, Main.tradingSystem.handleMakeBasketPurchasePolicyPost);
            get(Path.Web.MAKEBASKETPURCHASEPOLICY, Main.tradingSystem.serveMakeBasketPurchasePolicyPage);

            post(Path.Web.PURCHASECART, Main.tradingSystem.handlePurchaseCartPost);
            get(Path.Web.PURCHASECART, Main.tradingSystem.servePurchaseCartPage);

            post(Path.Web.ADDITEMTOBASKETBYOFFER, Main.tradingSystem.handleAddItemToBasketByOfferPost);
            get(Path.Web.ADDITEMTOBASKETBYOFFER, Main.tradingSystem.serveAddItemToBasketByOfferPage);

            post(Path.Web.GETOFFERSBYSTORE, Main.tradingSystem.handleGetOffersByStorePost);
            get(Path.Web.GETOFFERSBYSTORE, Main.tradingSystem.serveGetOffersByStorePage);

            post(Path.Web.APPROVEOFFER, Main.tradingSystem.handleApproveOfferPost);
            get(Path.Web.APPROVEOFFER, Main.tradingSystem.serveApproveOfferPage);

            post(Path.Web.GETSTOREDISCOUNTS, Main.tradingSystem.handleGetStoreDiscountsPost);
            get(Path.Web.GETSTOREDISCOUNTS, Main.tradingSystem.serveGetStoreDiscountsPage);

            post(Path.Web.GETTOTALINCOMEBYSTOREPERDAY, Main.tradingSystem.handleGetTotalIncomeByStorePerDayPost);
            get(Path.Web.GETTOTALINCOMEBYSTOREPERDAY, Main.tradingSystem.serveGetTotalIncomeByStorePerDayPage);

            post(Path.Web.GETTOTALINCOMEBYADMINPERDAY, Main.tradingSystem.handleGetTotalIncomeByAdminPerDayPost);
            get(Path.Web.GETTOTALINCOMEBYADMINPERDAY, Main.tradingSystem.serveGetTotalIncomeByAdminPerDayPage);

            post(Path.Web.GETTOTALVISITORSBYADMINPERDAY, Main.tradingSystem.handleGetTotalVisitorsByAdminPerDayPost);
            get(Path.Web.GETTOTALVISITORSBYADMINPERDAY, Main.tradingSystem.serveGetTotalVisitorsByAdminPerDay);

            post(Path.Web.UPDATEPRODUCTDETAILS, Main.tradingSystem.handleUpdateProductDetailsPost);
            get(Path.Web.UPDATEPRODUCTDETAILS, Main.tradingSystem.serveUpdateProductDetailsPage);

            post(Path.Web.MAKEQUANTITYPOLICY, Main.tradingSystem.handleMakeQuantityPolicyPost);
            post(Path.Web.DOQUANTITYPOLICY, Main.tradingSystem.handleDoQuantityPolicyPost);
            get(Path.Web.MAKEQUANTITYPOLICY, Main.tradingSystem.serveMakeQuantityPolicyPage);

            post(Path.Web.MAKETIMEPOLICY, Main.tradingSystem.handleMakeTimePolicyPost);
            post(Path.Web.DOTIMEPOLICY, Main.tradingSystem.handleDoTimePolicyPost);
            get(Path.Web.MAKETIMEPOLICY, Main.tradingSystem.serveMakeTimePolicyPage);

            post(Path.Web.MAKEQUANTITYDISCOUNT, Main.tradingSystem.handleMakeQuantityDiscountPost);
            post(Path.Web.DOQUANTITYDISCOUNT, Main.tradingSystem.handleDoQuantityDiscountPost);
            get(Path.Web.MAKEQUANTITYDISCOUNT, Main.tradingSystem.serveMakeQuantityDiscountPage);

            post(Path.Web.APPOINTSTOREOWNER, Main.tradingSystem.handleAppointStoreOwnerPost);
            post(Path.Web.APPOINTSTOREMANAGER, Main.tradingSystem.handleAppointStoreManagerPost);
            post(Path.Web.REMOVEOWNER, Main.tradingSystem.handleRemoveOwnerPost);
            post(Path.Web.REMOVEMANAGER, Main.tradingSystem.handleRemoveManagerPost);
            get(Path.Web.APPOINTREMOVEMANAGEROROWNER, Main.tradingSystem.serveAppointRemoveManagerOrOwnerPage);

            post(Path.Web.ADDITEMTOSTORE, Main.tradingSystem.handleAddItemToStorePost);
            post(Path.Web.LOGIN, Main.tradingSystem.handleLoginPost);
            post(Path.Web.LOGOUT, Main.tradingSystem.handleLogoutPost);
            post(Path.Web.SEARCH, Main.tradingSystem.handleSearchPost);
        });

        app.error(404, ViewUtil.NotFound);
    }

    private static SslContextFactory getSslContextFactory() {
        @SuppressWarnings("deprecation") SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreType("PKCS12");
        //noinspection ConstantConditions
        sslContextFactory.setKeyStorePath(Main.class.getResource("/keystore/localhost.p12").toExternalForm());
        sslContextFactory.setKeyStorePassword("password");
        return sslContextFactory;
    }
}