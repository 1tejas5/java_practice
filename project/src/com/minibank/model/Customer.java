package com.minibank.model;

import java.util.Objects;

/**
 * CONCEPTS: immutable class, BUILDER pattern (static nested class), validation in build(),
 * equals/hashCode by identity field, private constructor.
 */
public final class Customer {

    private final String id;
    private final String name;
    private final String email;
    private final String phone;      // optional

    private Customer(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.email = b.email;
        this.phone = b.phone;
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public String id() { return id; }

    public String name() { return name; }

    public String email() { return email; }

    public String phone() { return phone; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Customer c && c.id.equals(id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    public static final class Builder {
        private final String id;     // required
        private final String name;   // required
        private String email = "";   // optional
        private String phone = "";   // optional

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Customer build() {
            if (id == null || id.isBlank()) throw new IllegalStateException("customer id is required");
            if (name == null || name.isBlank()) throw new IllegalStateException("customer name is required");
            if (!email.isEmpty() && !email.contains("@")) throw new IllegalStateException("invalid email: " + email);
            Objects.requireNonNull(phone);
            return new Customer(this);
        }
    }
}
