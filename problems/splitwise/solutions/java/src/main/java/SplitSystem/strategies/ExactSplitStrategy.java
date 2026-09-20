package SplitSystem.strategies;

import SplitSystem.models.Split;
import SplitSystem.models.User;

import java.util.ArrayList;
import java.util.List;

/** Caller states exactly how much each participant owes; the amounts must sum to the total. */
public class ExactSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> computeSplits(double totalAmount, List<User> participants, List<Double> values) {
        if (values == null || values.size() != participants.size()) {
            throw new IllegalArgumentException("EXACT split needs one amount per participant");
        }
        double sum = 0;
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            double amount = SplitStrategy.round2(values.get(i));
            sum += amount;
            splits.add(new Split(participants.get(i), amount));
        }
        if (Math.abs(sum - totalAmount) > 0.01) {
            throw new IllegalArgumentException(
                    String.format("EXACT split amounts (%.2f) must add up to the total (%.2f)", sum, totalAmount));
        }
        return splits;
    }
}
