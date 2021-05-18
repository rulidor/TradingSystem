package user;

import store.Store;

import java.lang.ref.WeakReference;
import java.util.Objects;
import javax.persistence.*;
@Entity
public class OwnerPermission extends StorePermission
{
    private OwnerPermission(Store store) {
        super(store);
    }

    public OwnerPermission() {

    }

    public static OwnerPermission getInstance(Store store) {

        return (OwnerPermission)pool.computeIfAbsent(new OwnerPermission(store), WeakReference::new).get();
    }

    @Override
    public String toString() {
        return "OwnerPermission{" +
                "store=" + (store == null ? null : store.getName()) +
                '}';
    }
}
