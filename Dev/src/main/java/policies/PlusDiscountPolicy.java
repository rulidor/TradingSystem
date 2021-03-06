package policies;

import exceptions.PolicyException;
import store.Item;
import user.Basket;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
@Entity
public class PlusDiscountPolicy extends CompoundDiscountPolicy {

    public PlusDiscountPolicy(int id, Collection<DiscountPolicy> discountPolicies) {
        super(id, discountPolicies);
        for (DiscountPolicy discountPolicy: discountPolicies) {
            items.addAll(discountPolicy.getItems());
        }
    }

    public PlusDiscountPolicy() {
    }

    @Override
    public double cartTotalValue(Basket purchaseBasket) throws PolicyException {
        double value = 0;
        if(discountPolicies.size() == 0) {
            for(Map.Entry<Item, Integer> itemsAndQuantity: purchaseBasket.getItems().entrySet())
            {
                Item item = itemsAndQuantity.getKey();
                int quantity = itemsAndQuantity.getValue();
                value += (item.getPrice() * quantity);
            }
            return value;
        }
        if(discountPolicies.size() == 1)
            return discountPolicies.stream().toList().get(0).cartTotalValue(purchaseBasket);
        for(Map.Entry<Item, Integer> itemsAndQuantity: purchaseBasket.getItems().entrySet()) {
            Item item = itemsAndQuantity.getKey();
            int quantity = itemsAndQuantity.getValue();
            int totalDiscount = 0;
            Collection<Item> items = new LinkedList<>();
            items.add(item);
            for (DiscountPolicy discountPolicy : discountPolicies) {
                discountPolicy.cartTotalValue(purchaseBasket);
                if(discountPolicy.getItems().contains(item))
                    totalDiscount += discountPolicy.getDiscount();
            }
            value += ((((100 - (double)totalDiscount) / 100) * item.getPrice()) * quantity);
        }
        return value;
    }
}
