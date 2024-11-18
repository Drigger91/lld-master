public class VendingMachineMain {
    public static void main(String[] args) {
        VendingMachine vendingMachine = VendingMachine.getInstance();

        // Add products to the inventory
        Product coke = new Product("Coke", 15);
        Product pepsi = new Product("Pepsi", 20);
        Product water = new Product("Water", 10);

        vendingMachine.getInventory().addProduct(coke, 5);
        vendingMachine.getInventory().addProduct(pepsi, 3);
        vendingMachine.getInventory().addProduct(water, 2);

        // Select a product
        vendingMachine.selectProduct(coke);

        // Insert coins
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);

        // Insert a note
        vendingMachine.insertNote(Note.TEN);

        // Dispense the product
        vendingMachine.dispenseProduct();

        // Return change
        vendingMachine.returnChange();

        // Select another product
        vendingMachine.selectProduct(pepsi);

        // Insert insufficient payment
        vendingMachine.insertCoin(Coin.ONE);

        // Try to dispense the product
        vendingMachine.dispenseProduct();

        // Insert more coins
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);

        // Dispense the product
        vendingMachine.dispenseProduct();

        // Return change
        vendingMachine.returnChange();
    }
}
