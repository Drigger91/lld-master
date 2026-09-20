package SplitSystem.models;

/** One participant's share of an expense: this user owes {@code amount} to the expense's payer. */
public class Split {
    private final User user;
    private final double amount;

    public Split(User user, double amount) {
        if (user == null) {
            throw new IllegalArgumentException("Split must belong to a user");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Split amount must be positive, got " + amount);
        }
        this.user = user;
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("%s -> %.2f", user.getName(), amount);
    }
}
