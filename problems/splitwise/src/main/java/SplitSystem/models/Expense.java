package SplitSystem.models;

import SplitSystem.enums.SplitType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** An amount paid by one user on behalf of several, already divided into Splits. Immutable. */
public class Expense {
    private final String id;
    private final String name;
    private final double totalAmount;
    private final User paidBy;
    private final SplitType splitType;
    private final List<Split> splits;
    private final LocalDateTime createdAt;

    public Expense(String name, double totalAmount, User paidBy, SplitType splitType, List<Split> splits) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Expense name cannot be blank");
        }
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive");
        }
        if (paidBy == null) {
            throw new IllegalArgumentException("Expense must have a payer");
        }
        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("Expense must have at least one split");
        }
        double sum = splits.stream().mapToDouble(Split::getAmount).sum();
        if (Math.abs(sum - totalAmount) > 0.01) {
            throw new IllegalArgumentException(
                    String.format("Splits (%.2f) do not add up to the expense total (%.2f)", sum, totalAmount));
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.splitType = splitType;
        this.splits = List.copyOf(splits);
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<Split> getSplits() {
        return Collections.unmodifiableList(splits);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Expense[%s, %.2f paid by %s, %s split %s]",
                name, totalAmount, paidBy.getName(), splitType, splits);
    }
}
