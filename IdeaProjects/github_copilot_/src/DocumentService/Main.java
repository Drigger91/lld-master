package DocumentService;

public class Main {
    public static void main(String[] args) {
        User user1 = new User();
        User user2 = new User();
        System.out.printf("User 1 -> %s %s%n", user1.getUserId(), user1.getDocuments().toString());
        System.out.printf("User 2 -> %s %s%n", user2.getUserId(), user2.getDocuments().toString());
        user1.createDocumentForUser("Test User 1");
        System.out.printf("User 1 -> %s %s%n", user1.getUserId(), user1.getDocuments());
        Document document1 = user1.getDocuments().get(0);
        System.out.println("Document1 id : " + document1.getDocumentId());
        document1.editDocument(user2.getUserId(), "Test");
        System.out.println(document1.getContent(user2.getUserId()));
        user1.giveAccessForDocument(1, 2, AccessLevel.READ);
        System.out.println(document1.getContent(user2.getUserId()));
        document1.editDocument(user2.getUserId(), "Test");
        user1.giveAccessForDocument(1, 2, AccessLevel.EDIT);
        document1.editDocument(user2.getUserId(), "Test");
        System.out.println(document1.getContent(user2.getUserId()));
    }
}
