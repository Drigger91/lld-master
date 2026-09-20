package DocumentService;

import java.util.HashMap;
import java.util.Map;

public class Document {
    private final int documentId;
    private String content;
    private final Map<Integer, AccessLevel> accessLevelsMap;

    public Document(String content, int userId) {
        this.content = content;
        this.documentId = ++Environment.totalDocuments;
        this.accessLevelsMap = new HashMap<>();
        this.accessLevelsMap.put(userId, AccessLevel.OWNER);
    }

    private boolean canEditDocument(AccessLevel level) {
        return level.getPriority() >= AccessLevel.EDIT.getPriority();
    }

    private boolean canReadDocument(AccessLevel level) {
        return level.getPriority() >= AccessLevel.READ.getPriority();
    }

    public void editAccessForDocument(int userId, AccessLevel level) {
        this.accessLevelsMap.put(userId, level);
    }

    public void revokeAccessForDocument(int userId) {
        this.accessLevelsMap.remove(userId);
    }

    public AccessLevel getAccessLevel(int userId) {
        return accessLevelsMap.getOrDefault(userId, AccessLevel.NO_ACCESS);
    }

    public int getDocumentId() {
        return this.documentId;
    }

    public String getContent(int userId) {
        if (!canReadDocument(getAccessLevel(userId))) {
            return "You don't have access to read this document";
        }
        return this.content;
    }
    public void editDocument(int userId, String content) {
        if (canEditDocument(accessLevelsMap.getOrDefault(userId, AccessLevel.NO_ACCESS))) {
            this.content = content;
            System.out.println("Document edited successfully!");
        } else {
            System.out.println("You don't have access to edit this document");
        }
    }

    @Override
    public String toString() {
        return "Document#" + documentId;
    }
}
