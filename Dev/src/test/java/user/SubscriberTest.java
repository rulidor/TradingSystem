package user;

import exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.Store;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberTest {

    @Mock Permission permission;
    @Mock Map<Store, Basket> baskets;
    @Mock Set<Permission> permissions;
    @Mock Collection<Store> stores;

    private final Exception exception = mock(ItemException.class);
    private final Store store = mock(Store.class);
    private final Subscriber target = mock(Subscriber.class);
    private final Permission adminPermission = AdminPermission.getInstance();
    private final Permission managerPermission = ManagerPermission.getInstance(store);
    private final Permission ownerPermission = OwnerPermission.getInstance(store);
    private final Permission manageInventoryPermission = ManageInventoryPermission.getInstance(store);
    private final Permission removePermissionPermission = RemovePermissionPermission.getInstance(target, store);

    private final double price = 500.0;
    private final int quantity = 3;
    private final int itemId = 37373;
    private final String item = "PS5";
    private final String category = "Electronics";
    private final String subCategory = "Gaming Consoles";

    private Subscriber subscriber;

    @BeforeEach
    void setUp() {
        reset(store);
        reset(target);
        subscriber = new Subscriber("Shimshon", baskets, permissions);
    }

    @Test
    void validatePermissionHavePermission() throws NoPermissionException {
        when(permissions.contains(permission)).thenReturn(true);
        subscriber.validatePermission(permission);
    }

    @Test
    void validatePermissionNoPermission() {
        assertThrows(NoPermissionException.class, () -> subscriber.validatePermission(permission));
    }

    @Test
    void validateAtLeastOnePermissionHavePermission() throws NoPermissionException {
        when(permissions.contains(permission)).thenReturn(false);
        when(permissions.contains(adminPermission)).thenReturn(true);
        subscriber.validateAtLeastOnePermission(permission, adminPermission);
    }

    @Test
    void validateAtLeastOnePermissionNoPermission() {
        assertThrows(NoPermissionException.class, () -> subscriber.validateAtLeastOnePermission(adminPermission, permission));
    }

    @Test
    void getAllStores() throws NoPermissionException {
        when(permissions.contains(adminPermission)).thenReturn(true);
        assertEquals(stores, subscriber.getAllStores(stores));
    }

    @Test
    void getAllStoresNoPermission() {
        assertThrows(NoPermissionException.class, () -> subscriber.getAllStores(stores));
    }

    @Test
    void addManagerPermission() throws NoPermissionException, AlreadyOwnerException {

        when(permissions.contains(ownerPermission)).thenReturn(true);
        subscriber.addManagerPermission(target, store);
        verify(target).addPermission(managerPermission);
        verify(permissions).add(removePermissionPermission);
    }

    @Test
    void addManagerPermissionNoPermission() {

        assertThrows(NoPermissionException.class, () -> subscriber.addManagerPermission(target, store));
        verifyNoInteractions(target);
    }

    @Test
    void addManagerPermissionAlreadyManager() {

        when(permissions.contains(ownerPermission)).thenReturn(true);
        when(target.havePermission(managerPermission)).thenReturn(true);
        assertThrows(AlreadyOwnerException.class, () -> subscriber.addManagerPermission(target, store));
        verify(target, never()).addPermission(any());
        verify(permissions, never()).add(any());
    }

    @Test
    void addOwnerPermission() throws NoPermissionException, AlreadyOwnerException {

        when(permissions.contains(ownerPermission)).thenReturn(true);
        subscriber.addOwnerPermission(target, store);
        verify(target).addPermission(ownerPermission);
        verify(target).addPermission(managerPermission);
        verify(target).addPermission(manageInventoryPermission);
        verify(permissions).add(removePermissionPermission);
    }

    @Test
    void addOwnerPermissionNoPermission() {

        assertThrows(NoPermissionException.class, () -> subscriber.addOwnerPermission(target, store));
        verifyNoInteractions(target);
    }

    @Test
    void addOwnerPermissionAlreadyManager() {

        when(permissions.contains(ownerPermission)).thenReturn(true);
        when(target.havePermission(ownerPermission)).thenReturn(true);
        assertThrows(AlreadyOwnerException.class, () -> subscriber.addOwnerPermission(target, store));
        verify(target, never()).addPermission(any());
        verify(permissions, never()).add(any());
    }

    @Test
    void removeOwnerPermission() throws NoPermissionException {

        when(permissions.contains(removePermissionPermission)).thenReturn(true);
        subscriber.removeOwnerPermission(target, store);
        verify(target).removePermission(ownerPermission);
        verify(target).removePermission(manageInventoryPermission);
        verify(target).removePermission(managerPermission);
    }

    @Test
    void removeManagerPermission() throws NoPermissionException {

        when(permissions.contains(removePermissionPermission)).thenReturn(true);
        subscriber.removeManagerPermission(target, store);
        verify(target).removePermission(manageInventoryPermission);
        verify(target).removePermission(managerPermission);
    }

    @Test
    void removePermissionNoPermission() {

        assertThrows(NoPermissionException.class, () -> subscriber.removePermission(target, store, permission));
    }

    @Test
    void addStoreItem() throws Exception {

        when(permissions.contains(manageInventoryPermission)).thenReturn(true);
        subscriber.addStoreItem(itemId,store, item, category, subCategory, quantity, price);
        verify(store).addItem(itemId,item, price, category, subCategory, quantity);
    }

    @Test
    void addStoreItemNoPermission() throws Exception {

        assertThrows(NoPermissionException.class, () -> subscriber.addStoreItem(itemId,store, item, category, subCategory, quantity, price));
        verify(store, never()).addItem(any(),any(), anyDouble(), any(), any(), anyInt());
    }

    @Test
    void addStoreItemAddItemException() throws Exception {

        when(permissions.contains(manageInventoryPermission)).thenReturn(true);
        doThrow(exception).when(store).addItem(itemId,item, price, category, subCategory, quantity);
        Exception wrapper = assertThrows(AddStoreItemException.class,
                () -> subscriber.addStoreItem(itemId,store, item, category, subCategory, quantity, price));
        assertEquals(exception, wrapper.getCause());
    }

    @Test
    void removeStoreItem() throws Exception {

        when(permissions.contains(manageInventoryPermission)).thenReturn(true);
        subscriber.removeStoreItem(store, itemId);
        verify(store).removeItem(itemId);
    }

    @Test
    void removeStoreItemNoPermission() throws Exception {

        assertThrows(NoPermissionException.class, () -> subscriber.removeStoreItem(store, itemId));
        verify(store, never()).removeItem(anyInt());
    }

    @Test
    void removeStoreItemRemoveItemException() throws Exception {

        when(permissions.contains(manageInventoryPermission)).thenReturn(true);
        doThrow(exception).when(store).removeItem(itemId);
        Exception wrapper = assertThrows(RemoveStoreItemException.class, () -> subscriber.removeStoreItem(store, itemId));
        assertEquals(exception, wrapper.getCause());
    }

    @Test
    void updateStoreItem() throws Exception {

        when(permissions.contains(manageInventoryPermission)).thenReturn(true);
        subscriber.updateStoreItem(store, itemId, subCategory, quantity, price);
        verify(store).changeItem(itemId, subCategory, quantity, price);
    }

    @Test
    void updateStoreItemNoPermission() throws Exception {

        assertThrows(NoPermissionException.class,
                () -> subscriber.updateStoreItem(store, itemId, subCategory, quantity, price));
        verify(store, never()).changeItem(anyInt(), any(), anyInt(), any());
    }

    @Test
    void updateStoreItemChangeQuantityException() throws Exception {

        when(permissions.contains(manageInventoryPermission)).thenReturn(true);
        doThrow(exception).when(store).changeItem(itemId, subCategory, quantity, price);
        Exception wrapper = assertThrows(UpdateStoreItemException.class,
                () -> subscriber.updateStoreItem(store, itemId, subCategory, quantity, price));
        assertEquals(exception, wrapper.getCause());
    }
}