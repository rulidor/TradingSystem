package user;

import exceptions.*;
import externalServices.DeliveryData;
import externalServices.DeliverySystem;
import externalServices.PaymentData;
import externalServices.PaymentSystem;
import notifications.Observable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import persistence.RepoMock;
import policies.DefaultDiscountPolicy;
import policies.DefaultPurchasePolicy;
import store.Item;
import store.Store;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.*;

public class UserTest {

    private User user;

    @Mock private PaymentSystem paymentSystem;
    @Mock private DeliverySystem deliverySystem;
    @Mock private PaymentData paymentData;
    @Mock private DeliveryData deliveryData;

    private ConcurrentHashMap<Store, Basket> baskets;
    private Basket basket;
    private Store store;
    private ConcurrentHashMap<Item, Integer> items;
    private Item item;

    @BeforeClass
    public void beforeClass() {
        RepoMock.enable();
    }

    @BeforeMethod
    void setUp() {
        MockitoAnnotations.openMocks(this);
        items = new ConcurrentHashMap<>();
        store = new Store();
        item = new Item();
        baskets = spy(new ConcurrentHashMap<>());
        user = new User(baskets);

        basket = new Basket(user, store, items); // do not make this a spy (Mockito doesn't handle records properly)
        store.setObservable(new Observable());
        store.setPurchasePolicy(new DefaultPurchasePolicy());
        store.setDiscountPolicy(DefaultDiscountPolicy.getInstance());
    }

    @Test
    void makeCart_WhenEmpty() {
        User from = mock(User.class);
        user.makeCart(from);
        verify(baskets).putAll(any());
    }

    @Test
    void makeCart_WhenNotEmpty() {
        when(baskets.isEmpty()).thenReturn(false);
        User from = mock(User.class);
        user.makeCart(from);
        verify(baskets, never()).putAll(any());
    }

    @Test
    void getSubscriber() {
        assertThrows(NotLoggedInException.class, () -> user.getSubscriber());
    }

    @Test
    void getNewBasket() {
        Store store = mock(Store.class);
        assertNotNull(user.getBasket(store));
    }

    @Test
    void getExistingBasket() {
        Store store = mock(Store.class);
        Basket basket = mock(Basket.class);
        baskets.put(store, basket);
        assertSame(basket, user.getBasket(store));
    }

    @Test
    void purchaseCart() throws InvalidActionException {
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        item = store.searchItemById(0);
        baskets.put(store, basket);

        items.put(item, 3);
        assertEquals(1, user.getCart().size());
        assertEquals(5, store.getItems().get(item.getItem_id()).getAmount());
        user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData);
        assertEquals(0, user.getCart().size()); // checks that the cart is empty after the purchase
        assertEquals(2, store.getItems().get(item.getItem_id()).getAmount()); // checks that the inventory quantity updated
    }

    @Test
    void purchaseCartPaymentCheck() throws InvalidActionException {
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        item = store.searchItemById(0);
        baskets.put(store, basket);

        items.put(item, 3);
        doThrow(PaymentSystemException.class).when(paymentSystem).pay(paymentData);
        user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData);
        assertEquals(1, user.getCart().size());
        assertTrue(user.getCart().get(store).getItems().containsKey(item));
        assertEquals(5, store.getItems().get(item.getItem_id()).getAmount());
    }

    @Test
    void purchaseCartDeliveryCheck() throws InvalidActionException {
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        item = store.searchItemById(0);
        baskets.put(store, basket);

        items.put(item, 3);
        doThrow(DeliverySystemException.class).when(deliverySystem).deliver(deliveryData);
        user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData);
        assertEquals(1, user.getCart().size());
        assertTrue(user.getCart().get(store).getItems().containsKey(item));
        assertEquals(5, store.getItems().get(item.getItem_id()).getAmount());
    }

    @Test
    void purchaseCartNegativeQuantity() throws ItemException {
        // trying to purchase negative quantity
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        baskets.put(store, basket);
        items.put(item, -2);

        assertThrows(WrongAmountException.class, () -> user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData));
    }

    @Test
    void purchaseCartBigQuantityThanAvailable() throws ItemException {
        // trying to purchase more quantity than available
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        baskets.put(store, basket);
        items.put(item, 10);

        assertThrows(WrongAmountException.class, () -> user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData));
    }

    @Test
    void purchaseCartCorrectValueCalculation() throws InvalidActionException {
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        baskets.put(store, basket);
        item = store.searchItemById(0);
        items.put(item, 3);

        user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData);
        assertTrue(store.getPurchaseHistory().toString().contains("21.0")); // checks that the purchase value correct
    }

    @Test
    void purchaseCartPurchaseHistoryUpdated() throws InvalidActionException {
        store.addItem("cheese", 7.0, "cat1", "sub1", 5);
        baskets.put(store, basket);
        item = store.searchItemById(0);
        items.put(item, 3);

        user.purchaseCart(paymentSystem, deliverySystem, paymentData, deliveryData);
        assertTrue(store.getPurchaseHistory().toString().contains("cheese")); // checks that the purchase added to store history
    }
}