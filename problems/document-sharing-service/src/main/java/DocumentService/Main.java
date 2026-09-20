package DocumentService;

public class Main {
    public static void main(String[] args) {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        System.out.printf("User 1 -> %s %s%n", user1.getUserId(), user1.getDocuments().toString());
        System.out.printf("User 2 -> %s %s%n", user2.getUserId(), user2.getDocuments().toString());
        user1.createDocumentForUser("Test User 1");
        System.out.printf("User 1 -> %s %s%n", user1.getUserId(), user1.getDocuments());
        Document document1 = user1.getDocuments().get(0);
        int docId = document1.getDocumentId();
        System.out.println("Document1 id : " + docId);

        System.out.println("\n-- user2 has no access yet");
        document1.editDocument(user2.getUserId(), "Test");
        System.out.println(document1.getContent(user2.getUserId()));

        System.out.println("\n-- owner grants READ to user2");
        user1.giveAccessForDocument(docId, user2.getUserId(), AccessLevel.READ);
        System.out.println(document1.getContent(user2.getUserId()));
        document1.editDocument(user2.getUserId(), "Test");

        System.out.println("\n-- owner upgrades user2 to EDIT");
        user1.giveAccessForDocument(docId, user2.getUserId(), AccessLevel.EDIT);
        document1.editDocument(user2.getUserId(), "Edited by user2");
        System.out.println(document1.getContent(user2.getUserId()));
        System.out.println("Owner reads: " + document1.getContent(user1.getUserId()));

        System.out.println("\n-- user2 (editor, not owner) tries to share with user3");
        user2.giveAccessForDocument(docId, user3.getUserId(), AccessLevel.READ);
        System.out.println(document1.getContent(user3.getUserId()));

        System.out.println("\n-- owner downgrades user2 to NO_ACCESS, then revokes entirely");
        user1.giveAccessForDocument(docId, user2.getUserId(), AccessLevel.NO_ACCESS);
        System.out.println(document1.getContent(user2.getUserId()));
        user1.revokeAccessForDocument(docId, user2.getUserId());
        document1.editDocument(user2.getUserId(), "Should fail");
        System.out.println("user2 level now: " + document1.getAccessLevel(user2.getUserId()));

        System.out.println("\n-- sharing a document id that does not exist");
        user1.giveAccessForDocument(42, user2.getUserId(), AccessLevel.READ);
    }
}
