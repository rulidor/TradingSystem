package permissions;

import exceptions.ConnectionIdDoesNotExistException;
import exceptions.NoPermissionException;
import exceptions.SubscriberDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.Store;
import tradingSystem.TradingSystem;
import user.Subscriber;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static permissions.DeleteStoreItemCommand.newDeleteStoreItemCommand;

@ExtendWith(MockitoExtension.class)
class DeleteStoreItemCommandTest {

    @Mock TradingSystem tradingSystem;
    @Mock Subscriber user;
    @Mock Store store;

    private final String connectionId = "2345523532453245";
    private final int storeId = 2346758;
    private final int itemId = 4332423;

    @BeforeEach
    void setUp() throws ConnectionIdDoesNotExistException, SubscriberDoesNotExistException {
        when(tradingSystem.getStore(storeId)).thenReturn(store);
        when(tradingSystem.getSubscriberByConnectionId(connectionId)).thenReturn(user);
    }

    @Test
    void execute() throws Exception {

        Command cmd = newDeleteStoreItemCommand(tradingSystem, connectionId, storeId, itemId);
        when(user.havePermission(cmd.getRequiredPermission())).thenReturn(true);
        cmd.execute();
        verify(store).removeItem("" + itemId, null, null);
    }

    @Test
    void executeNoPermission() throws ConnectionIdDoesNotExistException, SubscriberDoesNotExistException {

        Command cmd = newDeleteStoreItemCommand(tradingSystem, connectionId, storeId, itemId);
        when(user.havePermission(cmd.getRequiredPermission())).thenReturn(false);
        assertThrows(NoPermissionException.class, cmd::execute);
    }
}