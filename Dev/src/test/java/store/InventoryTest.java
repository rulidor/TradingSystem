package store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    private static Inventory inventory = new Inventory();

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }
//    @Test
//    void addItemAndRating() throws Exception{
//        //checks that item name cannot be null
//        assertThrows(WrongName.class, () -> inventory.addItem(null, 20, "vegetables", "red", 3, 5));
//
//        //checks that item name cannot be with only white spaces
//        assertThrows(WrongName.class, () -> inventory.addItem("   ", 20, "vegetables", "red", 3, 5));
//
//        //checks that item cannot be with negative rating
//        assertThrows(WrongRating.class, () -> inventory.addItem("tomato", 20, "vegetables", "red", -1, 5));
//
//        inventory.addItem("tomato", 20, "vegetables", "red", 2, 5);
//        assertEquals(inventory.getItems().size(), 1);
//        assertEquals(inventory.getItems().keys().nextElement().getId(), 1);
//        inventory.addItem("cucumber", 15, "vegetables", "green", 2, 10);
//        assertEquals(inventory.getItems().size(), 2);
//        assertEquals(inventory.getItems().keys().nextElement().getId(), 2);
//    }

    @Test
    void addItemWithoutRating() throws ItemException{
        //checks that item name cannot start with a number
        assertThrows(WrongNameException.class, () -> inventory.addItem("12tomato", 20, "vegetables", "red", 5));

        //checks that item has a positive price
        assertThrows(WrongPriceException.class, () -> inventory.addItem("tomato", -5, "vegetables", "red", 5));

        //checks that item has a positive amount
        assertThrows(WrongAmountException.class, () -> inventory.addItem("tomato", 17, "vegetables", "red", -2));

        //checks that we cannot add an item that already exists
        int tomatoID=inventory.addItem("tomato", 20, "vegetables", "red", 5);

        assertThrows(ItemAlreadyExistsException.class, () -> inventory.addItem("tomato", 20, "vegetables", "red", 5));
        assertEquals(inventory.getItems().size(), 1);
        assertEquals(inventory.getItems().keys().nextElement().getId(),tomatoID);
    }

    @Test
    void searchItemByName() throws ItemException{
        inventory.addItem("tomato", 20, "vegetables", "red", 5);
        inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        assertTrue(inventory.searchItemByName("carrot").isEmpty());
        inventory.addItem("tomato", 20, "vegetables", "blue", 5);
        assertEquals(inventory.searchItemByName("tomato").size(), 2);
    }

    @Test
    void searchItemByCategory() throws ItemException{
        inventory.addItem("tomato", 20, "vegetables", "red", 5);
        inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        assertTrue(inventory.searchItemByCategory("camera").isEmpty());
        inventory.addItem("GoPro", 1200, "camera", "black", 5);
        assertEquals(inventory.searchItemByCategory("vegetables").size(), 2);
    }

    @Test
    void searchItemByKeyWord() throws ItemException{
        inventory.addItem("tomato", 20, "vegetables", "red", 5);
        inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        assertTrue(inventory.searchItemByKeyWord("blue").isEmpty());
        inventory.addItem("RedGoPro", 1200, "camera", "black", 5);
        assertEquals(inventory.searchItemByKeyWord("red").size(), 2);
    }

    @Test
    void searchItem() throws ItemException{
        int tomatoID= inventory.addItem("tomato", 20, "vegetables", "red", 5);
        int cucumberId= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        int tomato2ID= inventory.addItem("tomato", 20, "vegetables", "blue", 5);
        assertThrows(ItemNotFoundException.class, () -> inventory.searchItem(6));
        assertEquals(inventory.searchItem(tomato2ID).getId(), 3);
    }

    @Test
    void filterByPrice() throws ItemException{
        inventory.addItem("tomato", 20, "vegetables", "red", 5);
        inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        assertTrue(inventory.filterByPrice(30, 100).isEmpty());
        inventory.addItem("tomato", 20, "vegetables", "blue", 5);
        assertEquals(inventory.filterByPrice(15, 20).size(), 3);
    }

    @Test
    void filterByRating() throws ItemException{
        int tomatoId= inventory.addItem("tomato", 20, "vegetables", "red", 5);
        Item tomato = inventory.searchItem(tomatoId);
        tomato.setRating(2);
        int cucumberId= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        Item cucumber = inventory.searchItem(cucumberId);
        cucumber.setRating(3);
        assertTrue(inventory.filterByRating(4).isEmpty());
        assertEquals(inventory.filterByRating(2).size(), 2);
        assertEquals(inventory.filterByRating(3).size(), 1);
    }

    @Test
    void changeQuantity() throws ItemException{
        int tomatoId= inventory.addItem("tomato", 20, "vegetables", "red", 5);
        int cucumberID= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
      //  Item tomato = inventory.searchItem("tomato", "vegetables","red");

        //checks that the quantity must be 0 or greater
        assertThrows(WrongAmountException.class, () -> inventory.changeQuantity(tomatoId,-1));

        inventory.changeQuantity(tomatoId, 8);
        assertEquals(inventory.getItems().get(inventory.searchItem(tomatoId)), 8);
        inventory.changeQuantity(tomatoId, 2);
        assertEquals(inventory.getItems().get(inventory.searchItem(tomatoId)), 2);
    }

    @Test
    void decreaseByQuantity() throws ItemException{
        int cucumberId= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        int carrotId= inventory.addItem("carrot", 20, "vegetables", "orange", 0);
        Item cucumber = inventory.searchItem(cucumberId);

        //checks that the quantity must be 0 or greater
        assertThrows(WrongAmountException.class, () -> inventory.decreaseByQuantity(carrotId,4));

        inventory.decreaseByQuantity(cucumberId,1);
        assertEquals(inventory.getItems().get(cucumber), 9);
        inventory.decreaseByQuantity(cucumberId,2);
        assertEquals(inventory.getItems().get(cucumber), 7);
    }

    @Test
    void removeItem() throws ItemException{
        int cucumberId= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        int carrotId= inventory.addItem("carrot", 20, "vegetables", "orange", 0);
        assertEquals(inventory.getItems().size(), 2);

        //checks that only an existing item can be removed
        assertThrows(ItemNotFoundException.class, () -> inventory.removeItem(5));

        Item item =inventory.removeItem(carrotId);
        assertEquals(inventory.getItems().size(), 1);
        assertEquals(item.getId(),carrotId);
        assertThrows(ItemNotFoundException.class, () -> inventory.removeItem(carrotId));

    }

    @Test
    void checkAmount() throws ItemException {
        int tomatoId= inventory.addItem("tomato", 20, "vegetables", "red", 5);
        int cucumberID= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        assertThrows(WrongAmountException.class,()->inventory.checkAmount(tomatoId,8));
        assertTrue(inventory.checkAmount(cucumberID,4));
        assertThrows(WrongAmountException.class,() -> inventory.checkAmount(tomatoId,-1));

    }

    @Test
    void changeItemDetails() throws ItemException {
        int tomatoId= inventory.addItem("tomato", 20, "vegetables", "red", 5);
        int cucumberID= inventory.addItem("cucumber", 15, "vegetables", "green", 10);
        assertThrows(WrongAmountException.class,()->inventory.changeItemDetails(tomatoId,null,-9,null));
        assertNotEquals(inventory.searchItem(tomatoId).getSubCategory(),null);
        assertThrows(WrongAmountException.class,()->inventory.changeItemDetails(tomatoId,"",-9,null));
        assertNotEquals(inventory.searchItem(tomatoId).getPrice(),null);

        assertThrows(WrongPriceException.class, () -> inventory.changeItemDetails(cucumberID,null,null,-2.0));
        assertNotEquals(inventory.searchItem(tomatoId).getSubCategory(),null);

        inventory.changeItemDetails(tomatoId,"minPrice",null,null);
        assertEquals(inventory.searchItem(tomatoId).getSubCategory(),"minPrice");

        inventory.changeItemDetails(cucumberID,null,7,null);
        assertEquals(inventory.getItems().get(inventory.searchItem(cucumberID)),7);

        inventory.changeItemDetails(tomatoId,null,null,8.7);
        assertEquals(inventory.searchItem(tomatoId).getPrice(),8.7);

        inventory.changeItemDetails(cucumberID,"maxPrice",null,6.5);
        assertEquals(inventory.searchItem(cucumberID).getPrice(),6.5);
        assertEquals(inventory.searchItem(cucumberID).getSubCategory(),"maxPrice");


    }
//
//    @Test
//    void updateItemPrice() throws Exception{
//        inventory.addItem("cucumber", 15, "vegetables", "green", 10);
//        inventory.addItem("carrot", 20, "vegetables", "orange", 0);
//
//        //set a negative price for an item
//        assertThrows(WrongPriceException.class, () -> inventory.setItemPrice("carrot", "vegetables","orange", -20));
//
//        inventory.setItemPrice("carrot", "vegetables","orange", 50);
//        assertEquals(inventory.searchItem("carrot", "vegetables","orange").getPrice(), 50);
//        inventory.setItemPrice("carrot", "vegetables","orange", 34);
//        assertEquals(inventory.searchItem("carrot", "vegetables","orange").getPrice(), 34);
//    }


}
