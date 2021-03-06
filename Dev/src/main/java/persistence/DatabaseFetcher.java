package persistence;

import authentication.UserAuthentication;
import policies.DiscountPolicy;
import policies.PurchasePolicy;
import store.Store;
import user.Subscriber;
import user.User;
import user.Visitors;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseFetcher {

    private ConcurrentHashMap<String, Subscriber> subscribers;
    private ConcurrentHashMap<Integer, Store> stores;
    private AtomicInteger subscriberIdCounter;
    private ConcurrentHashMap<Integer, PurchasePolicy> purchasePolicies;
    private ConcurrentHashMap<Integer, DiscountPolicy> discountPolicies;
    private ConcurrentHashMap<Store, Collection<Integer>> storesPurchasePolicies; //todo: why collection? store has only one purchase policy
    private ConcurrentHashMap<Store, Collection<Integer>> storesDiscountPolicies; //todo: same as with purchase policy
//    private ConcurrentHashMap<String, Map<String, Integer>> visitors; //todo - needed?
    private Map<String, Record> userAuthentication;


    public DatabaseFetcher() {
        Repo.getEm(); //initializing Entity Manager
        subscribers = new ConcurrentHashMap<>();
        stores = new ConcurrentHashMap<>();
        subscriberIdCounter = new AtomicInteger();
        purchasePolicies = new ConcurrentHashMap<>();
        discountPolicies = new ConcurrentHashMap<>();
        storesPurchasePolicies = new ConcurrentHashMap<>();
        storesDiscountPolicies = new ConcurrentHashMap<>();
        this.userAuthentication = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Subscriber> getSubscribers() {
        List<Subscriber> list = Repo.getSubscribers();
        for (Subscriber s:list ) {
            if(s.isLoggedIn() == true)
                s.setLoggedIn(false);
            subscribers.put(s.getUserName(), s);
        }
        return subscribers;
    }

    public UserAuthentication getAuthentication() {
        UserAuthentication ua = Repo.getAuthentication();
        return ua;
    }

    public Visitors getVisitors() {
        Visitors vis = Repo.getVisitors();
        return vis;
    }

    public ConcurrentHashMap<Integer, Store> getStores() {
        List<Store> list = Repo.getStores();
        for (Store s:list ) {
            stores.put(s.getId(), s);
        }
        return stores;
    }

    public AtomicInteger getSubscriberIdCounter() {
        List<Subscriber> list = Repo.getSubscribers();
        subscriberIdCounter.set(list.size());
        return subscriberIdCounter;
    }

    public ConcurrentHashMap<Store, Collection<Integer>> getStoresPurchasePolicies() {
        List<Store> list = Repo.getStores();
        ConcurrentHashMap<Store, Collection<Integer>> map = new ConcurrentHashMap<>();
        for (Store s:list ) {
            map.put(s, new LinkedList<>());
            map.get(s).addAll(s.getStorePurchasePolicies());
        }
        storesPurchasePolicies = map;
        return storesPurchasePolicies;
    }

    public ConcurrentHashMap<Store, Collection<Integer>> getStoresDiscountPolicies() {
        List<Store> list = Repo.getStores();
        ConcurrentHashMap<Store, Collection<Integer>> map = new ConcurrentHashMap<>();
        for (Store s:list ) {
            map.put(s, new LinkedList<>());
            map.get(s).addAll(s.getStoreDiscountPolicies());
        }
        storesDiscountPolicies = map;
        return storesDiscountPolicies;
    }

    public ConcurrentHashMap<Integer, PurchasePolicy> getPurchasePolicies() {
        return Repo.getPurchasePolicies();
    }

    public ConcurrentHashMap<Integer, DiscountPolicy> getDiscountPolicies() {
        return Repo.getDiscountPolicies();
    }
}
