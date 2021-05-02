package notifications;

import exceptions.ItemException;
import org.testng.annotations.Test;
import store.Store;
import user.Subscriber;

public class NotificationsTests {

    private Store store1;
    private Subscriber subscriber1;

    void createUser1(){
        subscriber1 = new Subscriber(10, "user1");
    }

    void openStoreAndAddItem() throws ItemException {
        createUser1();
        store1 = new Store();
        store1.subscribe(subscriber1);
        store1.addItem("item1", 10, "category1", null, 20);
    }

//    @Test
//    void subscribeOpenStore() throws ItemException {
//        openStoreAndAddItem();
//        assertTrue(store1.getObservable().getObservers().contains(subscriber1));
//    }

//    @Test
//    void notifyPurchase() throws ItemException {
//        openStoreAndAddItem();
//        Subscriber subscriber2 = new Subscriber(20, "subscriber2");
//        //todo: addToBasket -> purchaseCart
//    }

    @Test
    void notificationAddedToPending(){
        //todo: test notification Added To Pending when user not logged in
    }

    @Test
    void notifyPostponed(){
        //todo: test notify Postponed notifications
    }
}


