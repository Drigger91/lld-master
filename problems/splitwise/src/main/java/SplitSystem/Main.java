package SplitSystem;

import SplitSystem.enums.SplitType;
import SplitSystem.models.Expense;
import SplitSystem.models.Group;
import SplitSystem.models.User;
import SplitSystem.services.ExpenseService;
import SplitSystem.services.GroupService;
import SplitSystem.services.SplitService;
import SplitSystem.services.UserService;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        UserService userService = new UserService();
        SplitService splitService = new SplitService();
        ExpenseService expenseService = new ExpenseService(splitService);
        GroupService groupService = new GroupService();

        User alice = userService.createUser("Alice", "alice@example.com");
        User bob = userService.createUser("Bob", "bob@example.com");
        User carol = userService.createUser("Carol", "carol@example.com");

        // ---------- 1:1 flow ----------
        System.out.println("=== 1:1 expenses ===");
        // Alice pays 50 for groceries, custom split 30 (Alice) / 20 (Bob) -> Bob owes Alice 20
        Expense groceries = expenseService.addExpense("Groceries", 50, alice, SplitType.EXACT,
                List.of(alice, bob), List.of(30.0, 20.0));
        System.out.println(groceries);
        // Bob pays 30 for a cab, split equally -> Alice owes Bob 15; net Bob owes Alice 5
        Expense cab = expenseService.addExpense("Cab", 30, bob, SplitType.EQUAL, List.of(alice, bob), null);
        System.out.println(cab);
        printLedger("Personal ledger", expenseService);

        System.out.println("Bob tries to settle 10 (owes only 5):");
        try {
            expenseService.settleUp(bob, alice, 10);
        } catch (IllegalArgumentException e) {
            System.out.println("  rejected: " + e.getMessage());
        }
        System.out.println("Bob settles 5 with Alice");
        expenseService.settleUp(bob, alice, 5);
        printLedger("Personal ledger", expenseService);

        // ---------- group flow ----------
        System.out.println("\n=== Group expenses ===");
        Group trip = groupService.createGroup(List.of(alice, bob, carol), "Road trip");
        System.out.println("Created group '" + trip.getName() + "' with " + trip.getMembers().size() + " members");

        // Alice pays 900 for the hotel, split equally -> Bob and Carol each owe Alice 300
        Expense hotel = expenseService.createExpense("Hotel", 900, alice, SplitType.EQUAL,
                List.of(alice, bob, carol), null);
        groupService.addExpenseToGroup(trip, hotel);
        System.out.println(hotel);

        // Bob pays 300 for fuel, 50/25/25 percent -> Alice owes Bob 150, Carol owes Bob 75
        Expense fuel = expenseService.createExpense("Fuel", 300, bob, SplitType.PERCENT,
                List.of(alice, bob, carol), List.of(50.0, 25.0, 25.0));
        groupService.addExpenseToGroup(trip, fuel);
        System.out.println(fuel);

        // Equal split with a remainder: 100 over 3 -> 33.34 / 33.33 / 33.33
        Expense snacks = expenseService.createExpense("Snacks", 100, carol, SplitType.EQUAL,
                List.of(alice, bob, carol), null);
        groupService.addExpenseToGroup(trip, snacks);
        System.out.println(snacks);

        System.out.println("Group has " + trip.getAllExpenses().size() + " expenses");
        groupService.printBalances(trip);

        double carolOwesBob = trip.getBalanceSheet().amountOwed(carol, bob);
        System.out.printf("Carol settles everything she owes Bob (%.2f)%n", carolOwesBob);
        groupService.settleUp(trip, carol, bob, carolOwesBob);
        groupService.printBalances(trip);

        // ---------- validation ----------
        System.out.println("\n=== Validation ===");
        User dave = userService.createUser("Dave", "dave@example.com");
        Expense outsider = expenseService.createExpense("Tickets", 60, dave, SplitType.EQUAL, List.of(dave, alice), null);
        System.out.println("Adding an expense paid by a non-member: " + groupService.addExpenseToGroup(trip, outsider));

        expectFailure("EXACT split that does not add up", () ->
                expenseService.createExpense("Dinner", 100, alice, SplitType.EXACT, List.of(alice, bob), List.of(60.0, 30.0)));
        expectFailure("PERCENT split that is not 100", () ->
                expenseService.createExpense("Dinner", 100, alice, SplitType.PERCENT, List.of(alice, bob), List.of(70.0, 40.0)));
        expectFailure("negative split value", () ->
                expenseService.createExpense("Dinner", 100, alice, SplitType.EXACT, List.of(alice, bob), List.of(120.0, -20.0)));
        expectFailure("zero amount expense", () ->
                expenseService.createExpense("Nothing", 0, alice, SplitType.EQUAL, List.of(alice, bob), null));
        expectFailure("settling when nothing is owed", () -> expenseService.settleUp(alice, bob, 1));
        expectFailure("group with a single member", () -> groupService.createGroup(List.of(alice), "Solo"));
    }

    private static void printLedger(String title, ExpenseService expenseService) {
        System.out.println(title + ":");
        List<String> lines = expenseService.getPersonalLedger().summary();
        if (lines.isEmpty()) {
            System.out.println("  everyone is settled up");
        }
        lines.forEach(line -> System.out.println("  " + line));
    }

    private static void expectFailure(String what, Runnable action) {
        try {
            action.run();
            System.out.println(what + ": NOT rejected (unexpected)");
        } catch (IllegalArgumentException e) {
            System.out.println(what + ": rejected -> " + e.getMessage());
        }
    }
}
