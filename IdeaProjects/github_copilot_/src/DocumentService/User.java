package DocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private final List<Document> documents;
    private final int userId;

    public User() {
        this.userId = ++Environment.totalUsers;
        this.documents = new ArrayList<>();
    }

    public int getUserId() {
        return this.userId;
    }

    public Document createDocumentForUser(String content) {
        Document newDocument = new Document(content, this.userId);
        this.documents.add(newDocument);
        return newDocument;
    }

    public List<Document> getDocuments() {
        return this.documents;
    }

    public boolean giveAccessForDocument(int documentId, int userId, AccessLevel level) {
        for (Document document : this.documents) {
            if (Objects.equals(documentId, document.getDocumentId())) {
                document.editAccessForDocument(userId, level);
                return true;
            }
        }
        System.out.println("No document found with given Id");
        return false;
    }

    public void addDocumentToUser(String content) {
        this.documents.add(new Document(content, this.userId));
        System.out.println("Successfully added document");
    }
}
