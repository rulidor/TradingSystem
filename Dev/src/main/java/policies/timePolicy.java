package policies;

import user.Basket;

public class timePolicy extends simplePurchasePolicy {

    @Override
    public boolean isValidPurchase(Basket purchaseBasket) {
        return false;
    }
}
