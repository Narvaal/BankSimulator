package br.com.ale.domain;

public class Client {

    private final long id;
    private final String name;
    private final String document;

    public Client(long id, String name, String document) {
        this.id = id;
        this.name = validateName(name);
        this.document = validateDocument(document);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank() || name.length() < 3) {
            throw new IllegalArgumentException(
                    "Client name cannot be blank" + "[name=" + name + "]"
            );
        }
        return name;
    }

    private String validateDocument(String document) {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException(
                    "Client document cannot be blank" + "[document=" + document + "]"
            );
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
