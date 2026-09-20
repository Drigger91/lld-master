package SplitSystem.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Who-owes-whom ledger.
 * sheet[A][B] = net amount B owes A. Positive: B owes A. Negative: A owes B.
 * Both directions are kept in sync so "what does X owe / collect" is a single map lookup.
 */
public class BalanceSheet {
    private static final double EPS = 0.005;

    private final Map<String, Map<String, Double>> sheet = new HashMap<>();
    private final Map<String, User> usersById = new LinkedHashMap<>();

    /** Every non-payer split becomes a debt from the split's user to the payer. */
    public void applyExpense(Expense expense) {
        User payer = expense.getPaidBy();
        for (Split split : expense.getSplits()) {
            if (split.getUser().getId().equals(payer.getId())) {
                continue; // the payer's own share cancels out
            }
            adjust(payer, split.getUser(), split.getAmount());
        }
    }

    /**
     * payer hands `amount` to payee. Only allowed up to what payer currently owes payee
     * (the system does not let you overpay and flip the debt).
     */
    public void settle(User payer, User payee, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Settlement amount must be positive");
        }
        double owed = amountOwed(payer, payee);
        if (amount > owed + EPS) {
            throw new IllegalArgumentException(String.format(
                    "%s owes %s only %.2f, cannot settle %.2f", payer.getName(), payee.getName(), owed, amount));
        }
        // paying the creditor is equivalent to the payer lending that amount back
        adjust(payer, payee, amount);
    }

    /** How much `debtor` owes `creditor` right now (0 if nothing or if it is the other way round). */
    public double amountOwed(User debtor, User creditor) {
        double v = sheet.getOrDefault(creditor.getId(), Map.of()).getOrDefault(debtor.getId(), 0.0);
        return v > EPS ? v : 0.0;
    }

    /** Net position of one user: positive value => other user owes them; negative => they owe. */
    public Map<User, Double> balancesFor(User user) {
        Map<User, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : sheet.getOrDefault(user.getId(), Map.of()).entrySet()) {
            if (Math.abs(e.getValue()) > EPS) {
                result.put(usersById.get(e.getKey()), e.getValue());
            }
        }
        return result;
    }

    /** Human readable "X owes Y amount" lines; empty list when everyone is settled up. */
    public List<String> summary() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> creditorEntry : sheet.entrySet()) {
            User creditor = usersById.get(creditorEntry.getKey());
            for (Map.Entry<String, Double> e : creditorEntry.getValue().entrySet()) {
                if (e.getValue() > EPS) {
                    User debtor = usersById.get(e.getKey());
                    lines.add(String.format("%s owes %s %.2f", debtor.getName(), creditor.getName(), e.getValue()));
                }
            }
        }
        return lines;
    }

    public boolean isSettled() {
        return summary().isEmpty();
    }

    private void adjust(User creditor, User debtor, double amount) {
        usersById.putIfAbsent(creditor.getId(), creditor);
        usersById.putIfAbsent(debtor.getId(), debtor);
        sheet.computeIfAbsent(creditor.getId(), k -> new LinkedHashMap<>())
                .merge(debtor.getId(), amount, Double::sum);
        sheet.computeIfAbsent(debtor.getId(), k -> new LinkedHashMap<>())
                .merge(creditor.getId(), -amount, Double::sum);
    }
}
