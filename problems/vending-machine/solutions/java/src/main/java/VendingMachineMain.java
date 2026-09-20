public class VendingMachineMain {
    public static void main(String[] args) {
        VendingMachine vendingMachine = VendingMachine.getInstance();

        // Add products to the inventory
        Product coke = new Product("Coke", 15);
        Product pepsi = new Product("Pepsi", 20);
        Product water = new Product("Water", 10);
        Product juice = new Product("Juice", 25);

        vendingMachine.getInventory().addProduct(coke, 5);
        vendingMachine.getInventory().addProduct(pepsi, 3);
        vendingMachine.getInventory().addProduct(water, 1);
        // juice is never stocked

        // --- Purchase 1: exact payment with coins + a note ---
        vendingMachine.insertCoin(Coin.ONE);   // rejected: no product selected yet
        vendingMachine.selectProduct(coke);
        vendingMachine.selectProduct(pepsi);   // rejected: a product is already selected
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.insertNote(Note.TEN);   // total 15 == price -> ready to dispense
        vendingMachine.dispenseProduct();
        vendingMachine.returnChange();          // no change, back to idle
        System.out.println("Coke left: " + vendingMachine.getInventory().getQuantity(coke));

        // --- Purchase 2: insufficient payment first, then overpay and get change ---
        vendingMachine.selectProduct(pepsi);
        vendingMachine.insertCoin(Coin.ONE);
        vendingMachine.dispenseProduct();       // rejected: only 1 of 20 paid
        vendingMachine.insertNote(Note.TWENTY); // total 21 >= 20
        vendingMachine.insertCoin(Coin.FIVE);   // rejected: payment already complete
        vendingMachine.dispenseProduct();
        vendingMachine.returnChange();          // change 1

        // --- Cancel: partial payment is refunded in full ---
        vendingMachine.selectProduct(water);
        vendingMachine.insertCoin(Coin.FIVE);
        vendingMachine.returnChange();          // refund 5, back to idle

        // --- Out of stock: last unit sells, next selection is refused ---
        vendingMachine.selectProduct(water);
        vendingMachine.insertNote(Note.TEN);
        vendingMachine.dispenseProduct();
        vendingMachine.returnChange();
        vendingMachine.selectProduct(water);    // rejected: quantity 0
        vendingMachine.selectProduct(juice);    // rejected: never stocked
    }
}
