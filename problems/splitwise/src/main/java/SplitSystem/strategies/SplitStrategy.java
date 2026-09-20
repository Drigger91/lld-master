package SplitSystem.strategies;

import SplitSystem.models.Split;
import SplitSystem.models.User;

import java.util.List;

/**
 * Strategy: turns (total, participants, per-participant input values) into concrete Splits.
 * Implementations must return splits whose amounts add up to totalAmount (to the paisa).
 */
public interface SplitStrategy {
    /**
     * @param totalAmount  total expense amount, must be > 0
     * @param participants users who share the expense (payer included if they also consume a share)
     * @param values       strategy-specific input, one per participant (ignored for EQUAL)
     */
    List<Split> computeSplits(double totalAmount, List<User> participants, List<Double> values);

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
