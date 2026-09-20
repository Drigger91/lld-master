package SplitSystem.services;

import SplitSystem.enums.SplitType;
import SplitSystem.models.BalanceSheet;
import SplitSystem.models.Expense;
import SplitSystem.models.Split;
import SplitSystem.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds expenses and keeps the "personal" (non-group) ledger: 1:1 expenses between users
 * and their settlements. Group expenses use the group's own ledger via GroupService.
 */
public class ExpenseService {
    private final SplitService splitService;
    private final BalanceSheet personalLedger = new BalanceSheet();
    private final List<Expense> history = new ArrayList<>();

    public ExpenseService(SplitService splitService) {
        this.splitService = splitService;
    }

    /** Builds an Expense (splits computed by the strategy) without recording it anywhere. */
    public Expense createExpense(String name, double totalAmount, User paidBy, SplitType type,
                                 List<User> participants, List<Double> values) {
        List<Split> splits = splitService.createSplits(type, totalAmount, participants, values);
        return new Expense(name, totalAmount, paidBy, type, splits);
    }

    /** Builds the expense and records it on the personal ledger. */
    public Expense addExpense(String name, double totalAmount, User paidBy, SplitType type,
                              List<User> participants, List<Double> values) {
        Expense expense = createExpense(name, totalAmount, paidBy, type, participants, values);
        history.add(expense);
        personalLedger.applyExpense(expense);
        return expense;
    }

    /** payer pays payee `amount` against what they owe on the personal ledger. */
    public void settleUp(User payer, User payee, double amount) {
        personalLedger.settle(payer, payee, amount);
    }

    public double amountOwed(User debtor, User creditor) {
        return personalLedger.amountOwed(debtor, creditor);
    }

    public BalanceSheet getPersonalLedger() {
        return personalLedger;
    }

    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(history);
    }
}
