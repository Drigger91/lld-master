import java.math.BigDecimal;
import java.util.List;

public class DigitalWalletMain {
    public static void main(String[] args) {
        DigitalWallet digitalWallet = DigitalWallet.getInstance();

        // Create users
        User user1 = new User("U001", "John Doe", "john@example.com", "password123");
        User user2 = new User("U002", "Jane Smith", "jane@example.com", "password456");
        digitalWallet.createUser(user1);
        digitalWallet.createUser(user2);

        // Create accounts
        Account account1 = new Account("A001", user1, "1234567890", Currency.USD);
        Account account2 = new Account("A002", user2, "9876543210", Currency.EUR);
        digitalWallet.createAccount(account1);
        digitalWallet.createAccount(account2);

        // Add payment methods
        PaymentMethod creditCard = new CreditCard("PM001", user1, "1234567890123456", "12/25", "123");
        PaymentMethod bankAccount = new BankAccount("PM002", user2, "9876543210", "987654321");
        digitalWallet.addPaymentMethod(creditCard);
        digitalWallet.addPaymentMethod(bankAccount);

        // Deposit funds
        account1.deposit(new BigDecimal("1000.00"));
        account2.deposit(new BigDecimal("500.00"));

        // Transfer funds: 100 USD leaves account1, arrives as EUR in account2
        digitalWallet.transferFunds(account1, account2, new BigDecimal("100.00"), Currency.USD);
        System.out.println("Account 1 balance: " + account1.getBalance() + " " + account1.getCurrency());
        System.out.println("Account 2 balance: " + account2.getBalance() + " " + account2.getCurrency());

        // Transfer more than the balance -> rejected, balances unchanged
        try {
            digitalWallet.transferFunds(account2, account1, new BigDecimal("5000.00"), Currency.EUR);
        } catch (InsufficientFundsException e) {
            System.out.println("Transfer rejected: " + e.getMessage());
        }
        System.out.println("Account 2 balance after rejected transfer: " + account2.getBalance() + " " + account2.getCurrency());
        System.out.println();

        // Get transaction history
        List<Transaction> transactionHistory1 = digitalWallet.getTransactionHistory(account1);
        List<Transaction> transactionHistory2 = digitalWallet.getTransactionHistory(account2);

        // Print transaction history
        System.out.println("Transaction History for Account 1:");
        for (Transaction transaction : transactionHistory1) {
            System.out.println("Transaction ID: " + transaction.getId());
            System.out.println("Amount: " + transaction.getAmount() + " " + transaction.getCurrency());
            System.out.println("Timestamp: " + transaction.getTimestamp());
            System.out.println();
        }

        System.out.println("Transaction History for Account 2:");
        for (Transaction transaction : transactionHistory2) {
            System.out.println("Transaction ID: " + transaction.getId());
            System.out.println("Amount: " + transaction.getAmount() + " " + transaction.getCurrency());
            System.out.println("Timestamp: " + transaction.getTimestamp());
            System.out.println();
        }
    }
}
