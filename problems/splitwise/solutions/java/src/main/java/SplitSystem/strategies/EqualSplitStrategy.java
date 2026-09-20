package SplitSystem.strategies;

import SplitSystem.models.Split;
import SplitSystem.models.User;

import java.util.ArrayList;
import java.util.List;

/** Divides the total evenly; any leftover paise are spread over the first few participants. */
public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> computeSplits(double totalAmount, List<User> participants, List<Double> values) {
        long totalPaise = Math.round(totalAmount * 100);
        int n = participants.size();
        long base = totalPaise / n;
        long remainder = totalPaise % n;

        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long share = base + (i < remainder ? 1 : 0);
            splits.add(new Split(participants.get(i), share / 100.0));
        }
        return splits;
    }
}
