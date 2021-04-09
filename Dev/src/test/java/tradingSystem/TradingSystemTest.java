package tradingSystem;

import authentication.UserAuthentication;
import exceptions.*;
import externalServices.DeliverySystem;
import externalServices.PaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.Item;
import store.Store;
import user.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingSystemTest {

    @Mock private UserAuthentication auth;
    @Mock private PaymentSystem paymentSystem;
    @Mock private DeliverySystem deliverySystem;
    @Mock private Map<String, Subscriber> subscribers;
    @Mock private Collection<Subscriber> staff;
    @Mock private Map<String, User> connections;
    @Mock private Map<Integer, Store> stores;
    @Mock private Subscriber subscriber;
    @Mock private User user;
    @Mock private Store store;
    @Mock private Item item;

    private final String connectionId = "9034580392580932458093248590324850932485";
    private final String userName = "Johnny";
    private final String password = "Cash";
    private final int storeId = 984585;

    private TradingSystem tradingSystem;

    @BeforeEach
    void setUp() throws SubscriberDoesNotExistException, WrongPasswordException {
        tradingSystem = TradingSystem.createTradingSystem("Roni", "roni12", paymentSystem, deliverySystem,
                auth, subscribers, connections, stores);
    }

    @Test
    void getUserByConnectionId() throws ConnectionIdDoesNotExistException {
        when(connections.get(connectionId)).thenReturn(user);
        assertSame(tradingSystem.getUserByConnectionId(connectionId), user);
    }

    @Test
    void getUserByConnectionId_ConnectionIdDoesNotExist() {
        assertThrows(ConnectionIdDoesNotExistException.class, () -> tradingSystem.getUserByConnectionId(connectionId));
    }

    @Test
    void getStore() throws InvalidStoreIdException {
        when(stores.get(storeId)).thenReturn(store);
        assertSame(store, tradingSystem.getStore(storeId));
    }

    @Test
    void getStore_InvalidStoreId() {
        assertThrows(InvalidStoreIdException.class, () -> tradingSystem.getStore(storeId));
    }

    @Test
    void register() throws SubscriberAlreadyExistsException {
        tradingSystem.register(userName, password);
        verify(auth).register(userName, password);
        verify(subscribers).put(eq(userName), any(Subscriber.class));
    }

    @Test
    void connect() {
        String connectionId = tradingSystem.connect();
        verify(connections).put(anyString(), any(User.class));
        String uuid = java.util.UUID.randomUUID().toString();
        assertNotEquals(uuid, connectionId); // verify we got a new uuid
        assertEquals(uuid.length(), connectionId.length()); // verify we got the correct length
    }

    @Test
    void login() throws ConnectionIdDoesNotExistException, SubscriberDoesNotExistException, WrongPasswordException {
        when(connections.get(connectionId)).thenReturn(user);
        when(subscribers.get(userName)).thenReturn(subscriber);
        tradingSystem.login(connectionId, userName, password);
        verify(subscriber).makeCart(user);
        verify(connections).put(connectionId, subscriber);
    }

    @Test
    void logoutSubscriber() throws ConnectionIdDoesNotExistException, NotLoggedInException {
        when(connections.get(connectionId)).thenReturn(subscriber);
        when(subscriber.getSubscriber()).thenReturn(subscriber);
        tradingSystem.logout(connectionId, user);
        verify(user).makeCart(subscriber);
        verify(connections).put(connectionId, user);
    }

    @Test
    void logoutGuest() throws ConnectionIdDoesNotExistException, NotLoggedInException {
        when(connections.get(connectionId)).thenReturn(user);
        tradingSystem.logout(connectionId, user);
        verify(connections, never()).put(anyString(), isA(User.class));
    }

    @Test
    void newStore() throws ItemException {

        tradingSystem.newStore(subscriber, "Toy Story");
        verify(subscriber).addPermission(OwnerPermission.getInstance(store));
        verify(subscriber).addPermission(ManagerPermission.getInstance(store));
        verify(subscriber).addPermission(ManageInventoryPermission.getInstance(store));
    }

    @Test
    void getStoreStaff() throws NoPermissionException {
        Collection<Subscriber> allSubscribers = new HashSet<>();
        allSubscribers.add(subscriber);
        when(subscribers.values()).thenReturn(allSubscribers);
        when(subscriber.havePermission(ManagerPermission.getInstance(store))).thenReturn(true);
        tradingSystem.getStoreStaff(subscriber, store, staff);
        verify(staff).add(subscriber);
    }

    @Test
    void getItems() {
        String s = "S";
        Double d = 1.0;
        Collection<Store> storesCollection = new LinkedList<>();
        storesCollection.add(store);
        when(stores.values()).thenReturn(storesCollection);
        ConcurrentLinkedQueue<Item> items = new ConcurrentLinkedQueue<>(); // TODO why ConcurrentLinkedQueue!?
        items.add(item);
        when(store.searchAndFilter(s, s, s, d, d, d, d)).thenReturn(items);
        Collection<String> result = tradingSystem.getItems(s, s, s, s, d, d, d, d);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void purchaseCart() {
        //TODO add a test here
    }
}