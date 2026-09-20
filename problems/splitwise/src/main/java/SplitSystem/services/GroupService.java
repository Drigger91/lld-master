package SplitSystem.services;

import SplitSystem.models.Expense;
import SplitSystem.models.Group;
import SplitSystem.models.User;

import java.util.List;
import java.util.Map;

public class GroupService {
    Map<String, Group> groups;

    public Group createGroup(List<User> users, String name) {
        // create group logic here
    }
    public boolean addExpenseToGroup(Group group, Expense expense) {
        // expense add to group
        return true;
    }
}
