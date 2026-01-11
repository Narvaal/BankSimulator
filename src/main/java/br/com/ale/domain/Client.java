package br.com.ale.domain;

public class Client {

    private long id;
    private String name;
    private String document;

    public Client(String name, String document) {
        this(0, name, document);
    }

    public Client(long id, String name, String document) {
        this.id = id;
        this.name = validateName(name);
        this.document = validateDocument(document);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank() || name.length() < 3) {
            throw new IllegalArgumentException("Client name must have at least 3 characters");
        }
        return name;
    }

    private String validateDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("Document cannot be blank");
        }
        return document;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }
}
