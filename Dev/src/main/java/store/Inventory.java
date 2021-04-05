



package store;

import exceptions.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory {

    private ConcurrentHashMap<Item, Integer> items;
    private AtomicInteger id = new AtomicInteger(1);

    public Inventory() {
        this.items = new ConcurrentHashMap<>();
    }
//    /**
//     * this adds a new item and it's amount to the inventory os a store
//     * @param name - the name of the new item
//     * @param price - the price of the new item
//     * @param category - the category of the new item
//     * @param subCategory - the sub category of the new item
//     * @param rating - the rating of the new item
//     * @param amount the amount in the store for the new item
//     * @exception  WrongName,WrongPrice,WrongRating,WrongAmount,WrongCategory,ItemAlreadyExists  */
//    public void addItem(String name, double price, String category, String subCategory, double rating, int amount) throws Exception {
//        if(name == null || name.isEmpty() || name.trim().isEmpty())
//            throw new WrongName("item name is null or contains only white spaces");
//        if(name.charAt(0) >= '0' && name.charAt(0) <= '9')
//            throw new WrongName("item name cannot start with a number");
//        if(price < 0)
//            throw new WrongPrice("item price cannot be negative");
//        if(rating < 0)
//            throw new WrongRating("item rating cannot be negative");
//        if(amount < 0)
//            throw new WrongAmount("item amount should be 0 or more than that");
//        for (Item item: items.keySet())
//            if(item.getName().equals(name) && item.getCategory().equals(category) && item.getSubCategory().equals(subCategory))
//                throw new ItemAlreadyExists("item already exists");
//        if(category.charAt(0) >= '0' && category.charAt(0) <= '9')// add check to category need to add tests
//            throw new WrongCategory("item category cannot start with a number");
//
//        items.putIfAbsent(new Item(id.get(), name, price, category, subCategory, rating), amount);
//        id.getAndIncrement();
//    }

    /**
     * this adds a new item and it's amount to the inventory os a store
     * @param name - the name of the new item
     * @param price - the price of the new item
     * @param category - the category of the new item
     * @param subCategory - the sub category of the new item
     * @param amount the amount in the store for the new item
     * @exception WrongNameException,WrongPriceException,WrongAmountException,WrongCategoryException,ItemAlreadyExistsException  */
    public int addItem(String name, double price, String category, String subCategory, int amount) throws ItemException {
        if(name == null || name.isEmpty() || name.trim().isEmpty())
            throw new WrongNameException("item name is null or contains only white spaces");
        if(name.charAt(0) >= '0' && name.charAt(0) <= '9')
            throw new WrongNameException("item name cannot start with a number");
        if(price < 0)
            throw new WrongPriceException("item price cannot be negative");
        if(amount < 0)
            throw new WrongAmountException("item amount should be 0 or more than that");
        for (Item item: items.keySet())
            if(item.getName().equals(name) && item.getCategory().equals(category) && item.getSubCategory().equals(subCategory))
                throw new ItemAlreadyExistsException("item already exists");
        if(category.charAt(0) >= '0' && category.charAt(0) <= '9')// add check to category need to add tests
            throw new WrongCategoryException("item category cannot start with a number");
        int newItemId=id.get();
        items.putIfAbsent(new Item(newItemId, name, price, category, subCategory, 0), amount);
        id.getAndIncrement();
        return newItemId;
    }

    /**
     * This method is used to search the inventory for items that matches the param name.
     * @param name - the name of the wanted item
     * @exception ItemNotFoundException - On non existing item with param name*/
    public ConcurrentLinkedQueue<Item> searchItemByName(String name)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: items.keySet())
            if(item.getName().toLowerCase().equals(name.toLowerCase()))
                foundItems.add(item);
        return foundItems;
    }

    /**
     * This method is used to search the inventory for items that matches the param category.
     * @param category - the category of the wanted item
     * @exception ItemNotFoundException - On non existing item with param category*/
    public ConcurrentLinkedQueue<Item> searchItemByCategory(String category)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: items.keySet())
            if(item.getCategory().toLowerCase().equals(category.toLowerCase()))
                foundItems.add(item);

        return foundItems;
    }

    /**
     * This method is used to search the inventory for items that matches the param keyword.
     * @param keyword - the keyword of the wanted item
     * @exception ItemNotFoundException - On non existing item with param keyword*/
    public ConcurrentLinkedQueue<Item> searchItemByKeyWord(String keyword)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: items.keySet())
            if(item.getName().toLowerCase().contains(keyword.toLowerCase()) || item.getCategory().toLowerCase().contains(keyword.toLowerCase()) ||
                    item.getSubCategory().toLowerCase().contains(keyword.toLowerCase()))
                foundItems.add(item);
        return foundItems;
    }

    /**
     * This method searches the inventory by name, category and sub-Category
     * @param name - name of the wanted item
     * @param category - the category of the wanted item
     * @param subCategory - the sub category of the wanted item
     * @exception ItemNotFoundException - when there are no item that matches the giving parameters.*/
    public Item getItem(String name, String category, String subCategory) throws ItemException
    {
        for (Item item: items.keySet())
            if(item.getName().toLowerCase().equals(name.toLowerCase()) && item.getCategory().toLowerCase().equals(category.toLowerCase())
                    && item.getSubCategory().toLowerCase().equals(subCategory.toLowerCase()))
                return item;
        throw new ItemNotFoundException("item not found");
    }

    public Item searchItem(int itemId) throws ItemException{
        for (Item item: items.keySet())
            if(item.getId()==itemId)
                return item;
        throw new ItemNotFoundException("item not found");

    }

    /**
     * This method is used to filter the inventory for items that matches the params startPrice and endPrice.
     * @param startPrice - the startPrice of the items price
     * @param endPrice - the endPrice of the items price */
    public ConcurrentLinkedQueue<Item> filterByPrice(double startPrice, double endPrice)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: items.keySet())
            if(item.getPrice() >= startPrice && item.getPrice() <= endPrice)
                foundItems.add(item);

        return foundItems;
    }

    /**
     * This method is used to filter the inventory for items that matches the params startPrice and endPrice.
     * @param startPrice - the startPrice of the items price
     * @param endPrice - the endPrice of the items price */
    public ConcurrentLinkedQueue<Item> filterByPrice(ConcurrentLinkedQueue<Item> itemsList,double startPrice, double endPrice)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: itemsList)
            if(item.getPrice() >= startPrice && item.getPrice() <= endPrice)
                foundItems.add(item);

        return foundItems;
    }

    /**
     * This method is used to filter the inventory for items that matches the param rating.
     * @param rating - the keyword of the wanted item */
    public ConcurrentLinkedQueue<Item> filterByRating(double rating)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: items.keySet())
            if(item.getRating() >= rating)
                foundItems.add(item);

        return foundItems;
    }

    /**
     * This method is used to filter the inventory for items that matches the param rating.
     * @param rating - the keyword of the wanted item
     * @exception ItemNotFoundException - On non existing item with param rating or greater*/
    public ConcurrentLinkedQueue<Item> filterByRating(ConcurrentLinkedQueue<Item> itemsList,double rating)
    {
        ConcurrentLinkedQueue<Item> foundItems = new ConcurrentLinkedQueue();
        for (Item item: itemsList)
            if(item.getRating() >= rating)
                foundItems.add(item);

        return foundItems;
    }

    /**
     * This method changes the amount of an item in the inventory
     * @param itemId- id of the wanted item
     * @param amount - the new amount fo the item
     * @exception WrongAmountException when the amount is illegal*/
    public void changeQuantity(int itemId, int amount) throws ItemException {
        if(amount < 0)
            throw new WrongAmountException("item amount should be 0 or more than that");

        items.replace(searchItem(itemId), amount);
    }

    /**
     * This method checks if there is enough amount of an item in the inventory
     * @param itemId - id of the item in the inventory
     * @param amount - the amount of the item to check
     * @exception WrongAmountException when the amount is illegal*/
    public boolean checkAmount(int itemId, int amount) throws ItemException {
        if(amount > items.get(searchItem(itemId)))
            throw new WrongAmountException("there is not enough from the item");
        if(amount<0)
            throw new WrongAmountException("amount can't be a negative number");
        return true;
    }

    /**
     * This method decreases the amount of the item by param quantity.
     * @param itemID- id of the wanted item
     * @param quantity - the quantity of the wanted item
     * @exception WrongAmountException - when the amount is illegal */
    public void decreaseByQuantity(int itemID, int quantity) throws ItemException {
        Item item = searchItem(itemID);
        if(items.get(item) -quantity< 0)
            throw new WrongAmountException("cannot decrease the quantity of an item with amount of 0");
        items.replace(item, items.get(item) - quantity);
    }

    /**
     *  This method removes an item
     * @param itemID - the id of the item
     * @exception ItemNotFoundException - when the wanted item does not exist in the inventory */
    public Item removeItem(int itemID) throws ItemException {
        Item item=searchItem(itemID);
       items.remove(item);
       return item;

    }

    public ConcurrentHashMap<Item, Integer> getItems() {
        return items;
    }

//    /**
//     * This method changes an item's price in the inventory
//     * @param name - the name of the item
//     * @param price - the price of the item
//     * @param category - the category of the item
//     * @param subCategory - the sub category of the item
//     * @param price- the new price of the item
//     * @exception  ItemNotFound,WrongPrice  */
//    public void setItemPrice(String name, String category, String subCategory, double price) throws Exception {
//        searchItem(name, category, subCategory).setPrice(price);
//    }

    public String toString() {
        String itemsDisplay = "";
        for (Item item: items.keySet())
            itemsDisplay += item.toString();
        return itemsDisplay;
    }

    public void changeItemDetails(int itemID, String newSubCategory, Integer newQuantity, Double newPrice) throws ItemException {
        for ( Item item: items.keySet()) {
            if(item.getId()==itemID)
            {
                if(newSubCategory != null && !newSubCategory.isEmpty() && !newSubCategory.trim().isEmpty())
                    item.setSubCategory(newSubCategory);

                if(newQuantity !=null)
                    changeQuantity(itemID,newQuantity);

                if(newPrice != null)
                    item.setPrice(newPrice);

                return;
            }
        }
        throw new ItemNotFoundException("no item in inventory matching item id");

    }
}
