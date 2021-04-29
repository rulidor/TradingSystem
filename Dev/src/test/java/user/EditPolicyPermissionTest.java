package user;

import org.junit.jupiter.api.Test;
import store.Store;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class EditPolicyPermissionTest {

    private final Store store = mock(Store.class);
    private final Store differentStore = mock(Store.class);
    private final StorePermission permission = EditPolicyPermission.getInstance(store);
    private final StorePermission samePermission = EditPolicyPermission.getInstance(store);
    private final StorePermission differentPermission = EditPolicyPermission.getInstance(differentStore);

    @Test
    void testPermissionSame() {
        assertSame(permission, samePermission);
    }

    @Test
    void testPermissionNotSame() {
        assertNotSame(permission, differentPermission);
    }
}