package user;

import store.Store;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class RemovePermissionPermission extends StorePermission
{
    private final Subscriber target;

    private RemovePermissionPermission(Subscriber target, Store store) {
        super(store);
        this.target = target;
    }

    public static RemovePermissionPermission getInstance(Subscriber target, Store store) {

        RemovePermissionPermission key = new RemovePermissionPermission(target, store);
        return (RemovePermissionPermission)pool.computeIfAbsent(key, k -> new WeakReference<>(key)).get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o || (o != null && getClass() == o.getClass())) return true;
        if (!super.equals(o)) return false;
        RemovePermissionPermission that = (RemovePermissionPermission) o;
        return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), target);
    }

    @Override
    public String toString() {
        return "RemovePermissionPermission{" +
                "store=" + store.getName() +
                "target=" + target.getUserName() +
                '}';
    }
}
