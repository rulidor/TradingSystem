package util;


import io.javalin.http.Context;

public class RequestUtil {

    public static String getQueryLocale(Context ctx) {
        return ctx.queryParam("locale");
    }

    public static String getParamIsbn(Context ctx) {
        return ctx.pathParam("isbn");
    }

    public static String getQueryUsername(Context ctx) {
        return ctx.formParam("username");
    }

    public static String getConnectionID(Context ctx) {
        return ctx.formParam("connectionID");
    }

    public static String getQueryPassword(Context ctx) {
        return ctx.formParam("password");
    }

    public static String getStoreID(Context ctx) {
        return ctx.formParam("storeID");
    }

    public static String getProductID(Context ctx) {
        return ctx.formParam("productID");
    }

    public static String getProduceName(Context ctx) {
        return ctx.formParam("productName");
    }

    public static String getCategory(Context ctx) {
        return ctx.formParam("category");
    }

    public static String getSubCategory(Context ctx) {
        return ctx.formParam("subCategory");
    }

    public static String getManagerUserName(Context ctx) {
        return ctx.formParam("managerUserName");
    }

    public static Double getPrice(Context ctx) {
        return Double.parseDouble(ctx.formParam("price"));
    }

    public static String getSearchBox(Context ctx) {
        return ctx.formParam("searchBox");
    }

    public static int getAmount(Context ctx) {
        return Integer.parseInt(ctx.formParam("amount"));
    }

    public static String getQueryLoginRedirect(Context ctx) {
        return ctx.queryParam("loginRedirect");
    }

    public static String getSessionLocale(Context ctx) {
        return (String) ctx.sessionAttribute("locale");
    }
    public static String getStoreName(Context ctx) {
        return ctx.formParam("storeName");
    }

    public static String getSessionCurrentUser(Context ctx) {
        return (String) ctx.sessionAttribute("currentUser");
    }

    public static boolean removeSessionAttrLoggedOut(Context ctx) {
        String loggedOut = ctx.sessionAttribute("loggedOut");
        ctx.sessionAttribute("loggedOut", null);
        return loggedOut != null;
    }

    public static String removeSessionAttrLoginRedirect(Context ctx) {
        String loginRedirect = ctx.sessionAttribute("loginRedirect");
        ctx.sessionAttribute("loginRedirect", null);
        return loginRedirect;
    }

}