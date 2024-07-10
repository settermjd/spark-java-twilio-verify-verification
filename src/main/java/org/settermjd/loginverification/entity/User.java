package org.settermjd.loginverification.entity;

public record User(
        int id,
        String username,
        String firstName,
        String lastName,
        String emailAddress,
        String phoneNumber
) {

}
