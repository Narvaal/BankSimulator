package br.com.ale.domain.client;

public class Client {

    private final long id;
    private final String name;
    private final String email;

    public Client(long id, String name, String email) {
        this.id = id;
        this.name = validateName(name);
        this.email = validateEmail(email);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank() || name.length() < 3) {
            throw new IllegalArgumentException(
                    "Client name cannot be blank" + "[name=" + name + "]"
            );
        }
        return name;
    }

    private String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Client email cannot be blank" + "[email=" + email + "]"
            );
        }
        return email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
