package SplitSystem.services;

import SplitSystem.models.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    Map<String, User> userMap;

    public UserService() {
        this.userMap = new ConcurrentHashMap<>();
    }
    public User createUser(String name, String email) throws Exception {
        // validators here
        if (name.isBlank() || email.isBlank()) {
            throw new Exception("User name/email cannot be blank");
        }
        User user = new User(name, email);
        userMap.put(user.getId(), user);
        return user;
    }

    public User getUser(String userId) {
        return userMap.getOrDefault(userId, null);
    }

    public boolean DeleteUser(String userId) {
        if (userMap.containsKey(userId)) {
            userMap.remove(userId);
            return true;
        }
        System.out.printf("User not found with id : %s \n", userId);
        return false;
    }
}
