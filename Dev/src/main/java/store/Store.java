package store;

import Offer.Offer;
import exceptions.*;
import org.hibernate.annotations.Cascade;
import persistence.Repo;
import policies.DefaultDiscountPolicy;
import policies.DefaultPurchasePolicy;
import policies.DiscountPolicy;
import policies.PurchasePolicy;
import spellChecker.Spelling;
import user.Basket;
import notifications.Observable;
import review.Review;
import user.Subscriber;
import user.User;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
public class Store {

    @Id
    private Integer id = 0;
    private String name;
    private String description;
    private double rating;
    @OneToOne
    private DiscountPolicy discountPolicy;
    @ElementCollection
    private Collection<Integer> storeDiscountPolicies;
    @OneToOne
    private PurchasePolicy purchasePolicy;
    @ElementCollection
    private Collection<Integer> storePurchasePolicies;

    //private String founder;
    private boolean isActive = true;
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "id")
    @MapsId
    private Inventory inventory = new Inventory();
    @ElementCollection
    private final Collection<String> purchases = new LinkedList<>();
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Observable observable = new Observable(0);
    @ElementCollection
    private final Map<String, Double> totalValuePerDay = new HashMap<>();
    @OneToMany(cascade = CascadeType.ALL)
    private final Map<Integer, Offer> storeOffers = new HashMap<>();
    private final AtomicInteger offerIdCounter = new AtomicInteger();

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Collection<String> getPurchases() {
        return purchases;
    }

    public AtomicInteger getOfferIdCounter() {
        return offerIdCounter;
    }

    public Store() {
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Collection<Integer> getStoreDiscountPolicies() {
        return storeDiscountPolicies;
    }

    public void setStoreDiscountPolicies(Collection<Integer> storeDiscountPolicies) {
        this.storeDiscountPolicies = storeDiscountPolicies;
    }

    public Collection<Integer> getStorePurchasePolicies() {
        return storePurchasePolicies;
    }

    public void setStorePurchasePolicies(Collection<Integer> storePurchasePolicies) {
        this.storePurchasePolicies = storePurchasePolicies;
    }

    /**
     * This method opens a new store and create its inventory
     *
     * @param name        - the name of the new store
     * @param description - the price of the new store
     *                    //  * @param founder - the fonder of the new store
     * @throws WrongNameException
     */
    public Store(int id, String name, String description, PurchasePolicy purchasePolicy, DiscountPolicy discountPolicy) throws ItemException {
        if (name == null || name.isEmpty() || name.trim().isEmpty())
            throw new WrongNameException("store name is null or contains only white spaces");
        if (name.charAt(0) >= '0' && name.charAt(0) <= '9')
            throw new WrongNameException("store name cannot start with a number");
        if (description == null || description.isEmpty() || description.trim().isEmpty())
            throw new WrongNameException("store description is null or contains only white spaces");
        if (description.charAt(0) >= '0' && description.charAt(0) <= '9')
            throw new WrongNameException("store description cannot start with a number");
        this.id = id;
        this.name = name;
        this.description = description;
        this.rating = 0;
        if(purchasePolicy == null)
            this.purchasePolicy = DefaultPurchasePolicy.getInstance();
        else
            this.purchasePolicy = purchasePolicy;
        this.inventory = new Inventory(id);

        if(discountPolicy == null){
            DefaultDiscountPolicy defaultDiscountPolicy = DefaultDiscountPolicy.getInstance();
            this.discountPolicy = defaultDiscountPolicy;

        }
        else
            this.discountPolicy = discountPolicy;
        this.isActive = true;
        this.observable = new Observable(this.id);
        this.storePurchasePolicies = new LinkedList<>();
        this.storeDiscountPolicies = new LinkedList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) throws WrongRatingException {
        if (rating < 0)
            throw new WrongRatingException("rating must be a positive number");
        this.rating = rating;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * This method returns the items in the store's inventory
     */
    public Map<Integer, Item> getItems() {
        return this.inventory.getItems();
    }

    /**
     * this adds a new item and it's amount to the store's inventory
     *
     * @param name        - the name of the new item
     * @param price       - the price of the new item
     * @param category    - the category of the new item
     * @param subCategory - the sub category of the new item
     * @param amount      the amount in the store for the new item
     * @throws ItemException
     */
    public int addItem(String name, double price, String category, String subCategory, int amount) throws ItemException {
        return this.inventory.addItem(name, price, category, subCategory, amount);
    }

    public Collection<Item> searchAndFilter(String keyWord, String itemName, String category, Double ratingItem,
                                                       Double ratingStore, Double maxPrice, Double minPrice) {
        Spelling spelling = new Spelling();
        if(keyWord != null)
            keyWord = spelling.correct(keyWord.toLowerCase());
        if(itemName != null)
            itemName = spelling.correct(itemName.toLowerCase());
        if(category != null)
            category = spelling.correct(category.toLowerCase());
        Collection<Item> search = searchItems(keyWord, itemName, category);
        return filterItems(search, ratingItem, ratingStore, maxPrice, minPrice);
    }

    public Collection<Item> searchItems(String keyWord, String itemName, String category) {

        Collection<Item> result = new HashSet<>(inventory.getItems().values());
        boolean itemValue = itemName != null && !itemName.trim().isEmpty();
        boolean categoryValue = category != null && !category.trim().isEmpty();
        boolean keyWordValue = keyWord != null && !keyWord.trim().isEmpty();
        if(!itemValue && !categoryValue && !keyWordValue)
            return result;
        if (itemValue)
            result.retainAll(inventory.searchItemByName(itemName));
        if (categoryValue)
            result.retainAll(inventory.searchItemByCategory(category));
        if (keyWordValue)
            result.retainAll(inventory.searchItemByKeyWord(keyWord));
        return result;
    }


    public Collection<Item> filterItems(Collection<Item> items, Double ratingItem, Double ratingStore,
                                                   Double maxPrice, Double minPrice) {
        Collection<Item> result = new HashSet<>(items);
        if(ratingItem != null)
            result.retainAll(inventory.filterByRating(items, ratingItem));
        if(ratingStore != null && rating < ratingStore)
            result = new HashSet<>();
        if(minPrice != null && maxPrice != null)
            result.retainAll(inventory.filterByPrice(items, minPrice, maxPrice));
        return result;

    }

    /**
     * This method searches the inventory by name, category and sub-Category
     *
     * @param name        - name of the wanted item
     * @param category    - the category of the wanted item
     * @param subCategory - the sub category of the wanted item
     * @throws ItemNotFoundException - when there are no item that matches the giving parameters.
     */
    public Item getItem(String name, String category, String subCategory) throws ItemException {
        return inventory.getItem(name, category, subCategory);
    }

    /**
     * This method searches the store's inventory by name, category and sub-Category
     *
     * @param itemId- id of the wanted item
     * @throws ItemNotFoundException - when there are no item that matches the giving parameters.
     */
    public Item searchItemById(int itemId) throws ItemException {
        return this.inventory.searchItem(itemId);
    }

    public Collection<Item> filterByPrice(Collection<Item> items, double startPrice, double endPrice) {
        if (items != null)
            return this.inventory.filterByPrice(items, startPrice, endPrice);
        return inventory.filterByPrice(startPrice, endPrice);
    }

    public Collection<Item> filterByRating(Collection<Item> items, double rating) {
        if (items != null)
            return inventory.filterByRating(items, rating);
        return this.inventory.filterByRating(rating);
    }

    /**
     * This method checks if there is enough amount of an item in the inventory
     *
     * @param itemId - id of the item in the inventory
     * @param amount - the amount of the item to check
     * @throws WrongAmountException when the amount is illegal
     */
    public boolean checkAmount(int itemId, int amount) throws ItemException {
        return inventory.checkAmount(itemId, amount);
    }

    /**
     * This method removes an item from the store's inventory
     *
     * @param itemID- id of the item
     * @throws ItemNotFoundException - when the wanted item does not exist in the inventory
     */
    public Item removeItem(int itemID) throws ItemException {
        return this.inventory.removeItem(itemID);
    }

    /**
     * This method displays the items in the store's inventory
     * * @param name - name of the wanted item
     */
    public String toString() {
        return inventory.toString();
    }

    public PurchasePolicy getPurchasePolicy() {
        return purchasePolicy;
    }

    public DiscountPolicy getDiscountPolicy() {
        return discountPolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;

    }

    public void setPurchasePolicy(PurchasePolicy purchasePolicy) { this.purchasePolicy = purchasePolicy; }


    public void changeItem(int itemID, String newSubCategory, Integer newQuantity, Double newPrice) throws ItemException {
        inventory.changeItemDetails(itemID, newSubCategory, newQuantity, newPrice);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setNotActive(){
        if(isActive == false)
            return;
        this.isActive = false;
        observable.notifyStoreStatus(String.valueOf(id), isActive);
    }

    public void setActive(){
        if(isActive == true)
            return;
        this.isActive = true;
        observable.notifyStoreStatus(String.valueOf(id), isActive);
    }

    public double processBasketAndCalculatePrice(Basket basket, StringBuilder details, DiscountPolicy storeDiscountPolicy, Collection<Offer> userOffers) throws ItemException, PolicyException {
        if(userOffers != null) {
            for (Offer offer: userOffers) {
                if(offer.getQuantity() == 0)
                    basket.removeItem(offer.getItem());
                if (!basket.getItems().containsKey(offer.getItem())) {
                    userOffers.remove(offer);
                    storeOffers.values().remove(offer);
                }
            }
        }
        return inventory.calculate(basket, details, storeDiscountPolicy, userOffers);
    }

    public void rollBack(Map<Item, Integer> items) {
        synchronized (inventory.getItems()) {
            for (Map.Entry<Item, Integer> entry : items.entrySet()) {
                inventory.getItems().get(entry.getKey().getItem_id()).setAmount(entry.getKey().getAmount() + entry.getValue());
            }
        }
        unlockItems(items.keySet());
    }

    public void unlockItems(Set<Item> items) {
        for (Item item: items) {
            item.unlock();
        }
    }

    public Observable getObservable() {
        return observable;
    }

    public void addPurchase(String purchaseDetails) {
        purchases.add(purchaseDetails);
    }

    public void addTotalValuePerDay(String date, double value) {
        totalValuePerDay.put(date, value);
    }

    public void updateTotalValuePerDay(String date, double value) {
        totalValuePerDay.replace(date, value);
    }

    public Map<String, Double> getTotalValuePerDay() { return this.totalValuePerDay; }

    public Collection<String> getPurchaseHistory() { return purchases; }

    public void notifyPurchase( User buyer, Map<Item, Integer> basket) {
        observable.notifyPurchase(this, buyer, basket);
    }

    public void notifyNewOffer(Offer offer) {
        observable.notifyNewOffer(offer); }

    public void notifyApprovedOffer(Offer offer) { observable.notifyApprovedOffer(offer); }

    public void notifyDeclinedOffer(Offer offer) { observable.notifyDeclinedOffer(offer); }

    public void notifyCounterOffer(Offer offer) {
        observable.notifyCounterOffer(offer);
    }

    public void subscribe(Subscriber subscriber) {
        observable.subscribe(subscriber);
    }

    public void unsubscribe(Subscriber subscriber) {
        observable.unsubscribe(subscriber);
    }

    public void removeOwnerOrManager(Subscriber remover, Subscriber toRemove){
        observable.notifyRoleRemove(remover, toRemove, this.id);
    }

    public void notifyItemOpinion(Subscriber subscriber, Review review) {
        observable.notifyItemReview(subscriber, review);
    }

    public void setObservable(Observable observable) { this.observable = observable; }

    public void addOffer(Subscriber subscriber, Item item, int quantity, double price) {
        int id = offerIdCounter.getAndIncrement();
        Offer offer = new Offer(subscriber, item, quantity, price);
        this.storeOffers.put(id, offer);
        offer.setId(id);
        offer.setStore_id(this.id);

        Repo.persist(offer);
        Repo.merge(this);

    }

    public Collection<String> getOffers() {
        Collection<String> offers = new LinkedList<>();
        for (Map.Entry<Integer, Offer> offer: storeOffers.entrySet()) {
            offers.add("offer id: " + offer.getKey() + offer.getValue().toString());
        }
        return offers;
    }

    public Offer getOfferById(int offerId) throws OfferNotExistsException {
        if(!storeOffers.containsKey(offerId))
            throw new OfferNotExistsException();
        return storeOffers.get(offerId);
    }

    public Map<Integer, Offer> getStoreOffers() { return storeOffers; }

    //for appointing store owner or manager
    public void appointRole(Subscriber subscriber, Subscriber target, String role) {

        observable.notifyRoleAppointment(subscriber, target, this.id, role);

    }

    public Offer searchOfferByItemAndSubscriber(Subscriber subscriber, Item item) {
        for (Offer offer: this.storeOffers.values()) {
            if(offer.getSubscriber() == subscriber && offer.getItem() == item)
                return offer;
        }
        return null;
    }
}
