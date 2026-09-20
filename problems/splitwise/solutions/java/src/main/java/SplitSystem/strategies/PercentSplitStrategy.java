package SplitSystem.strategies;

import SplitSystem.models.Split;
import SplitSystem.models.User;

import java.util.ArrayList;
import java.util.List;

/** Caller states a percentage per participant; the percentages must sum to 100. */
public class PercentSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> computeSplits(double totalAmount, List<User> participants, List<Double> values) {
        if (values == null || values.size() != participants.size()) {
            throw new IllegalArgumentException("PERCENT split needs one percentage per participant");
        }
        double percentSum = 0;
        for (double p : values) {
            percentSum += p;
        }
        if (Math.abs(percentSum - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                    String.format("PERCENT split percentages (%.2f) must add up to 100", percentSum));
        }
        // Allocate in paise so the shares add up to the total exactly; last participant absorbs rounding.
        long totalPaise = Math.round(totalAmount * 100);
        long allocated = 0;
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            long share = (i == participants.size() - 1)
                    ? totalPaise - allocated
                    : Math.round(totalPaise * values.get(i) / 100.0);
            allocated += share;
            splits.add(new Split(participants.get(i), share / 100.0));
        }
        return splits;
    }
}
