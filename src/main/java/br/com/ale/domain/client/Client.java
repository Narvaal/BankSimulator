package br.com.ale.domain.client;

import java.util.Objects;

public class Client {

    private final long id;
    private final String name;
    private final String email;
    private final String password;
    private final Provider provider;
    private final String providerId;
    private final boolean emailVerified;
    private final String picture;

    public Client(
            long id,
            String name,
            String email,
            String password,
            Provider provider,
            String providerId,
            boolean emailVerified,
            String picture
    ) {
        this.id = id;
        this.name = validateName(name, provider);
        this.email = validateEmail(email);
        this.password = validatePassword(password, provider);
        this.provider = Objects.requireNonNull(provider, "Provider cannot be null");
        this.providerId = validateProviderId(providerId, provider);
        this.emailVerified = emailVerified;
        this.picture = picture;
    }

    private String validateName(String name, Provider provider) {
        if (provider == Provider.LOCAL) {
            if (name == null || name.isBlank() || name.length() < 3) {
                throw new IllegalArgumentException(
                        "Client name cannot be blank [name=" + name + "]"
                );
            }
        }
        return name;
    }

    private String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Client email cannot be blank [email=" + email + "]"
            );
        }
        return email;
    }

    private String validatePassword(String password, Provider provider) {
        if (provider == Provider.LOCAL) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException(
                        "Password is required for LOCAL provider"
                );
            }
        }
        return password;
    }

    private String validateProviderId(String providerId, Provider provider) {
        if (provider != Provider.LOCAL) {
            if (providerId == null || providerId.isBlank()) {
                throw new IllegalArgumentException(
                        "ProviderId is required for provider " + provider
                );
            }
        }
        return providerId;
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

    public String getPassword() {
        return password;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getPicture() {
        return picture;
    }
}
