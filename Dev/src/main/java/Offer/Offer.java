package Offer;

import store.Item;
import user.Subscriber;

import javax.persistence.*;
import java.util.Collection;
import java.util.LinkedList;
@Entity
@IdClass(OfferId.class)
public class Offer {

    @ManyToOne
    private Subscriber subscriber;
    @ManyToOne
    private Item item;
    private int quantity;
    private double price;
    private boolean approved;
    @ManyToMany
    @CollectionTable(name = "offer_approved_owners")
    private Collection<Subscriber> approvedOwners;
    @ManyToMany
    @CollectionTable(name = "offer_countered_owners")
    private Collection<Subscriber> counteredOwners;
    @Id
    private Integer id;
    @Id
    private Integer store_id;



    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setApprovedOwners(Collection<Subscriber> approvedOwners) {
        this.approvedOwners = approvedOwners;
    }

    public void setCounteredOwners(Collection<Subscriber> counteredOwners) {
        this.counteredOwners = counteredOwners;
    }

    public Integer getStore_id() {
        return store_id;
    }

    public void setStore_id(Integer store_id) {
        this.store_id = store_id;
    }

    public Offer(Subscriber subscriber, Item item, int quantity, double price) {
        this.subscriber = subscriber;
        this.item = item;
        this.quantity = quantity;
        this.price = price;
        this.approved = false;
        this.approvedOwners = new LinkedList<>();
        this.counteredOwners = new LinkedList<>();
    }

    public Offer() {

    }

    public Subscriber getSubscriber() { return subscriber; }

    public Item getItem() { return item; }

    public int getQuantity() { return quantity; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public String toString() {
        return ", user: " + subscriber.getUserName() + ", item: " + item.getName() + ", quantity: " + quantity + ", price: " + price;
    }

    public void approve() { this.approved = true; }

    public boolean isApproved() { return this.approved; }

    public void addApprovedOwner(Subscriber owner) { this.approvedOwners.add(owner); }

    public int getApprovedOwners() { return this.approvedOwners.size(); }

    public void addCounteredOwner(Subscriber owner) { this.counteredOwners.add(owner); }

    public int getCounteredOwners() { return this.counteredOwners.size(); }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
