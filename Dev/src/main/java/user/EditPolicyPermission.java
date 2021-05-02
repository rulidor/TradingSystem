package user;

import store.Store;

import java.lang.ref.WeakReference;

public class EditPolicyPermission extends StorePermission
{
    private EditPolicyPermission(Store store) {
        super(store);
    }

    public static EditPolicyPermission getInstance(Store store) {

        return (EditPolicyPermission)pool.computeIfAbsent(new EditPolicyPermission(store), WeakReference::new).get();
    }

    @Override
    public String toString() {
        return "EditPolicyPermission{" +
                "store=" + (store == null ? null : store.getName()) +
                '}';
    }
}