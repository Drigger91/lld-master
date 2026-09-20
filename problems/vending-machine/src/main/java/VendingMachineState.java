// Refer State Design Pattern for clear understanding

// IdleState -> ReadyState -> DispenseState -> ReturnChangeState

public interface VendingMachineState {
    void selectProduct(Product product);

    void insertCoin(Coin coin);

    void insertNote(Note note);

    void dispenseProduct();

    void returnChange();
}
