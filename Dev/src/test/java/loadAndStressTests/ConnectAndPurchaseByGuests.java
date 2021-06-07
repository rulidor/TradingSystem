package loadAndStressTests;

import authentication.UserAuthentication;
import exceptions.InvalidActionException;
import externalServices.DeliverySystemRealMock;
import externalServices.PaymentSystemRealMock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import service.TradingSystemServiceImpl;
import store.Store;
import tradingSystem.TradingSystemBuilder;
import tradingSystem.TradingSystemImpl;
import user.AdminPermission;
import user.Subscriber;
import user.User;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;

public class ConnectAndPurchaseByGuests {

    private TradingSystemServiceImpl tradingSystemService;
    private final String userName = "Admin";
    private final String password = "123";
    private ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, User> connections = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Store> stores = new ConcurrentHashMap<>();
    UserAuthentication auth = new UserAuthentication();
    private Subscriber admin;
    private PaymentSystemRealMock paymentSystem;
    private DeliverySystemRealMock deliverySystem;

    @BeforeClass
    void setUp() throws InvalidActionException {
        MockitoAnnotations.openMocks(this);
        auth.register(userName, password);
        admin = new Subscriber(0, userName);
        admin.addPermission(AdminPermission.getInstance());
        subscribers.put(userName, admin);
        paymentSystem = new PaymentSystemRealMock();
        deliverySystem = new DeliverySystemRealMock();
        tradingSystemService = spy(new TradingSystemServiceImpl(new TradingSystemImpl(new TradingSystemBuilder().setUserName(userName)
                .setPassword(password)
                .setSubscribers(subscribers)
                .setConnections(connections)
                .setStores(stores)
                .setAuth(auth)
                .setPaymentSystem(paymentSystem)
                .setDeliverySystem(deliverySystem).build())));

        String conn = tradingSystemService.connect();
        tradingSystemService.login(conn, userName, password);
        tradingSystemService.openNewStore(conn, "eBay");
        tradingSystemService.addProductToStore(conn, "0", "bamba", "snacks", "sub1", 2000, 5.5);
    }

    @Test (threadPoolSize = 1000, invocationCount = 1000, timeOut = 10000)
    public void test() throws InvalidActionException {
        String conn = tradingSystemService.connect();
        tradingSystemService.addItemToBasket(conn, "0", "0", 1);
        tradingSystemService.purchaseCart(conn, "1", 1, 2022, "1", "1", "1", "1", "1", "1", "1", 1);
    }

    @AfterClass
    public void tearDown() {
        System.out.println(paymentSystem.getTime());
        System.out.println(deliverySystem.getTime());
    }
}
