package SplitSystem.services;

import SplitSystem.models.Expense;
import SplitSystem.models.Group;
import SplitSystem.models.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupService {
    private final Map<String, Group> groups = new ConcurrentHashMap<>();

    public Group createGroup(List<User> users, String name) {
        Group group = new Group(name, users);
        groups.put(group.getId(), group);
        return group;
    }

    public Group getGroup(String groupId) {
        return groups.get(groupId);
    }

    public boolean addMember(Group group, User user) {
        return group.addMember(user);
    }

    /** Records the expense in the group; returns false (and prints why) if any participant is not a member. */
    public boolean addExpenseToGroup(Group group, Expense expense) {
        try {
            group.addExpense(expense);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add expense: " + e.getMessage());
            return false;
        }
    }

    /** payer pays payee `amount` inside the group's ledger. */
    public void settleUp(Group group, User payer, User payee, double amount) {
        if (!group.isMember(payer) || !group.isMember(payee)) {
            throw new IllegalArgumentException("Both users must be members of " + group.getName());
        }
        group.getBalanceSheet().settle(payer, payee, amount);
    }

    /** Prints "X owes Y amount" for every outstanding pair in the group. */
    public void printBalances(Group group) {
        System.out.println("Balances for group '" + group.getName() + "':");
        List<String> lines = group.getBalanceSheet().summary();
        if (lines.isEmpty()) {
            System.out.println("  everyone is settled up");
        }
        for (String line : lines) {
            System.out.println("  " + line);
        }
    }
}
