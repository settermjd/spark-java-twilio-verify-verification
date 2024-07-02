package org.settermjd.LoginVerification;

public class User {
    private final int id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String emailAddress;

    public User(int id, String username, String firstName, String lastName, String emailAddress) {
        this.username = username;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
