package SplitSystem.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** A named set of users with its own expense history and who-owes-whom ledger. */
public class Group {
    private final String id;
    private final String name;
    private final Set<String> memberIds = new LinkedHashSet<>();
    private final List<User> members = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final BalanceSheet balanceSheet = new BalanceSheet();

    public Group(String name, List<User> users) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be blank");
        }
        if (users == null || users.size() < 2) {
            throw new IllegalArgumentException("A group needs at least two members");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        users.forEach(this::addMember);
    }

    public boolean addMember(User user) {
        if (memberIds.add(user.getId())) {
            members.add(user);
            return true;
        }
        return false;
    }

    public boolean isMember(User user) {
        return memberIds.contains(user.getId());
    }

    /** Records the expense in the history and updates the group's ledger. All users involved must be members. */
    public void addExpense(Expense expense) {
        if (!isMember(expense.getPaidBy())) {
            throw new IllegalArgumentException(expense.getPaidBy().getName() + " is not a member of " + name);
        }
        for (Split split : expense.getSplits()) {
            if (!isMember(split.getUser())) {
                throw new IllegalArgumentException(split.getUser().getName() + " is not a member of " + name);
            }
        }
        expenses.add(expense);
        balanceSheet.applyExpense(expense);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public BalanceSheet getBalanceSheet() {
        return balanceSheet;
    }
}
