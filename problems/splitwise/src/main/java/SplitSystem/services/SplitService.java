package SplitSystem.services;

import SplitSystem.enums.SplitType;
import SplitSystem.models.Split;
import SplitSystem.models.User;
import SplitSystem.strategies.EqualSplitStrategy;
import SplitSystem.strategies.ExactSplitStrategy;
import SplitSystem.strategies.PercentSplitStrategy;
import SplitSystem.strategies.SplitStrategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Picks the SplitStrategy for a SplitType and validates the common inputs. */
public class SplitService {
    private final Map<SplitType, SplitStrategy> strategies = new EnumMap<>(SplitType.class);

    public SplitService() {
        strategies.put(SplitType.EQUAL, new EqualSplitStrategy());
        strategies.put(SplitType.EXACT, new ExactSplitStrategy());
        strategies.put(SplitType.PERCENT, new PercentSplitStrategy());
    }

    /**
     * @param values per-participant amounts (EXACT) or percentages (PERCENT); may be null for EQUAL
     */
    public List<Split> createSplits(SplitType type, double totalAmount, List<User> participants, List<Double> values) {
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required");
        }
        if (values != null) {
            for (double v : values) {
                if (v <= 0) {
                    throw new IllegalArgumentException("Split values must be positive, got " + v);
                }
            }
        }
        SplitStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported split type " + type);
        }
        return strategy.computeSplits(totalAmount, participants, values);
    }
}
