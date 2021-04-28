package service;

import exceptions.InvalidActionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

public interface TradingSystemService {
    
    
    // ***********************************************************************
    // Topics: system, guest, subscriber
    // ***********************************************************************

    /* Initialize system and login to a system manager.
    params: system manager details
    preconditions: user details corresponds to a system manager user that is registered in the user authenticator. */
//    void initializeSystem(String userName, String pass) throws InvalidActionException;

    //returns a connectId.
    String connect() throws InvalidActionException;

    /* Register to system
       preconditions: userName, pass not null; userName not already exist. */
    void register(String userName, String password) throws InvalidActionException;

    /* Login to system */
    void login(String connectID, String userName, String pass) throws InvalidActionException;

    /* Logout from system */
    void logout(String connectID) throws InvalidActionException;

    /* Get product by filter, uses spellchecking. */
    Collection<String> getItems(String keyWord, String productName, String category, String subCategory, Double ratingItem, Double ratingStore, Double maxPrice, Double minPrice) throws InvalidActionException;
    // the String in the collection represent item.toString()
    // TODO use spellChecking

    /* Save product in basket of a store. */
    void addItemToBasket(String userID, String storeId, String productId, int amount) throws InvalidActionException;

    /* get cart's products. */
    Collection<String> showCart(String userID) throws InvalidActionException;

    /* get basket's products. */
    Collection<String> showBasket(String userID, String storeId) throws InvalidActionException;

    /* updates the amount of a product for user from a specific store. if new amount = 0 then the product will be deleted from the basket.
    * If trying to update an item which not exist in the basket, the amount will be updated. */
    void updateProductAmountInBasket(String userID, String storeId, String productId, int newAmount) throws InvalidActionException;

    /* make purchase for every product in all of the user's baskets */
    //each purchase matches to an item from a store with the appropriate quantity.
    void purchaseCart(String userID) throws InvalidActionException;

    /* get purchase history of a user by permissions: user himself / system manager.
    * every purchase represents buying of a cart.
    * for example, if userId1 bought 3 "milk" products and 2 "eggs" products from storeId1, there will be 1 purchases for the user. */
    Collection<String> getPurchaseHistory(String userID) throws InvalidActionException;

    /* enables user to write an opinion on a product he has purchased.
    preconditions: 1. the user has purchased the product
                   2. productId belongs to storeId (even if quantity in inventory is 0)
                   3. desc is neither null, nor empty. */
    void writeOpinionOnProduct(String userID, String storeID, String productId, String desc) throws InvalidActionException;


    // ***********************************************************************
    // Topics: store owner, store manager, system manager
    // ***********************************************************************


    /* Get info of all stores owners and managers, and the products in every store
    preconditions: invoker is a system manager. */
    Collection<String> getStoresInfo(String userID) throws InvalidActionException;

    /* Get all products of the store, with store id.
    preconditions: invoker is the owner/manager of the store or is a system manager.*/
    //each String element in the collection represents an item in the store.
    Collection<String> getItemsByStore(String userID, String storeId) throws InvalidActionException;

    /* creates a new store. username is the founder and owner.
       pre-condition: 1. storeName is not null or empty
                      2.userId is a subscriber and not a guest
        returns storeId; */
    String openNewStore(String userID, String newStoreName) throws InvalidActionException;

    /* appoints a new store manager. assignor is an owner of the store, assignee is the username of the new store manager
     precondition: assignee is not a manager in this store and is a subscriber (not guest)
     poscondition: assignee have the permissions of a new store manager, i.e the basic permissions for a manager, which are:
                   get info about roles in the store and their permissions, get info about products in the store,
                   get requests from users and answer them.*/
    void appointStoreManager(String userID, String assigneeUserName, String storeId) throws InvalidActionException;

    /* adds a product to a store.
    // returns the product ID
    preconditions: invoker is the store owner or is a manager of it, with permissions to make changes in products. */
    //category and subCategory can be null or empty string. productName cannot be null or empty string. quantity and price cannot be < 0.
    String addProductToStore(String userID, String storeId, String productName, String category, String subCategory, int quantity, double price) throws InvalidActionException;

    /* deletes a product from a store 
    preconditions: invoker is the store owner or is a manager of it, with permissions to make changes in products. */
    void deleteProductFromStore(String userID, String storeId, String productID) throws InvalidActionException;

    /* updates a product details of a store.
    // if there is null, no need to update the field. productId cannot be changed.
    preconditions: invoker is the store owner or is a manager of it, with permissions to make changes in products.*/
    void updateProductDetails(String userID, String storeId, String productID, String newSubCategory, Integer newQuantity, Double newPrice) throws InvalidActionException;

    /* appoints a new store owner. assignor is an owner of the store, assignee is the username of a new store owner
     * pre-condition: assignee is not an owner in this store and is a subscriber (not guest) */
    void appointStoreOwner(String userID, String assigneeUserName, String storeId) throws InvalidActionException;

    /*The next block of functions deals with store manager permissions. A new store manager has only the
        basic permissions in the store. */
    //******************************************************************************
    
    /* allows manager to add, delete amd update product in a specific store.
     precondition: assignor is the assignor of the manager, assignee is a manager of the store
     postcondition: the manager has permissions to add, delete amd update product in the store. */
    void allowManagerToUpdateProducts(String userID, String storeId, String managerUserName) throws InvalidActionException;
    
    /* disables a manager from adding, deleting amd updating product in a specific store.
     pre-condition: assignor is the assignor of the manager
     postcondition: the manager DOESN'T have permissions to add, delete amd update product in the store. */
    void disableManagerFromUpdateProducts(String userID, String storeId, String managerUserName) throws InvalidActionException ;
    
    /* allows manager to get info and edit purchase and discount policies in a specific store.
     precondition: assignor is the assignor of the manager.
     postcondition: the manager has permissions to get info and edit purchase and discount policies in the store. */
    void allowManagerToEditPolicies(String userID, String storeId, String managerUserName) throws InvalidActionException;
    
    /* disables a manager from getting info and editing purchase and discount policies in a specific store.
     pre-condition: assignor is the assignor of the manager
     postcondition: the manager DOESN'T have permissions to get info and edit purchase and discount policies in the store. */
    void disableManagerFromEditPolicies(String userID, String storeId, String managerUserName) throws InvalidActionException;
    
    /* allows manager to get purchases history of the store.
     precondition: assignor is the assignor of the manager. managerUserName is a subscriber and a manager of the store.
     postcondition: the manager has permissions to get purchases history of the store. */
    void allowManagerToGetHistory(String userID, String storeId, String managerUserName) throws InvalidActionException;
    
    /* disables a manager from getting purchases history of the store.
     pre-condition: assignor is the assignor of the manager
     postcondition: the manager DOESN'T have permissions to get purchases history of the store. */
    void disableManagerFromGetHistory(String userID, String storeId, String managerUserName) throws InvalidActionException;
    
    //end of block dealing with store manager permissions
    //******************************************************************************

        
    /* removes a user from a store manager role.
     * pre-condition: the invoker is an owner of the store and is the assignor of the manager*/
    //returns true if manager removed, else returns false.
    boolean removeManager(String userID, String storeId, String managerUserName) throws InvalidActionException;

    /* removes a user from a store owner role.
     * pre-condition: the invoker is an owner of the store and is the assignor of the owner */
    //returns true if manager removed, else returns false.
    boolean removeOwner(String connId, String storeId, String targetUserName) throws InvalidActionException;

    /* shows store staff information and their permissions in the store
    precondition: invoker has the permissions to get the info. */
    //every string element in the collection represents one staff member username and his permissions.
    Collection<String> showStaffInfo(String userID, String storeId) throws InvalidActionException;

    /* shows sales History of a specific store by permissions: system manager / store owner / store manager.
    precondition: invoker has the permissions to get the info. */
    //every string element in the collection represents a purchase of a basket, with the quantity that was sale to a specific user.
    Collection<String> getSalesHistoryByStore(String userID, String storeId) throws InvalidActionException;

    //TODO permissions for policies and discounts

    /* get all policies of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to create store policies.*/
    Collection<Integer> getStorePolicies(String userID, String storeId) throws InvalidActionException;

    /* assign a policy to a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to create store policies.*/
    void assignPolicy(int policy, String userID, String storeId) throws InvalidActionException;

    /* remove policy of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    void removePolicy(String userID, String storeId, int policy) throws InvalidActionException;

    /* create quantity policy of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    int makeQuantityPolicy(String userID, String storeId, Collection<String> items, int minQuantity, int maxQuantity) throws InvalidActionException;

    /* create minimum basket purchase value policy of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    int makeBasketPurchasePolicy(String userID, String storeId, int minBasketValue) throws InvalidActionException;

    /* create time policy of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    int makeTimePolicy(String userID, String storeId, Collection<String> items, String time) throws InvalidActionException;

    /* create and policy between two policies of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    int andPolicy(String userID, String storeId, int policy1, int policy2) throws InvalidActionException;

    /* create or policy between two policies of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    int orPolicy(String userID, String storeId, int policy1, int policy2) throws InvalidActionException;

    /* create xor policy between two policies of a store.
    preconditions: invoker is the store owner or is a manager of it, with permissions to remove store policies.*/
    int xorPolicy(String userID, String storeId, int policy1, int policy2) throws InvalidActionException;

    //TODO add all discount policies options here


    // ***********************************************************************
    // Topics: service level, external systems
    // ***********************************************************************
    
    /* shows the event log.
       every string element represents an event, which is an application to the system and its parameters.
       precondition: invoker has the permissions to get the info - only system manager. */
    Collection<String> getEventLog(String userID) throws InvalidActionException, IOException;
    
    /* shows the error log.
       every string element represents an error.
       precondition: invoker has the permissions to get the info - only system manager. */
    Collection<String> getErrorLog(String userID) throws InvalidActionException;
}
