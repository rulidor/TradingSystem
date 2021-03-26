package Domain;

import User.User;

import java.util.Collection;

public class Basket {

    private String store;
    private User user;
    private Collection<String> items;

    public Basket(String store, User user) {
        this.store = store;
        this.user = user;
    }

    public void addItem(String item, int amount)
    {

    }

    public Collection<String> getItems()
    {
        return this.items;
    }

    public void deleteItem(String item)
    {
        items.remove(item);
    }

    public void changeAmount(String item, int amount)
    {

    }
}
