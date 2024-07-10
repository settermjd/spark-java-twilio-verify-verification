package org.settermjd.LoginVerification;

import java.sql.*;

import static spark.Spark.halt;

public class UserService {
    public static final String findPhoneNumberFromUsernameQuery = "SELECT phone_number FROM user WHERE username = ?";
    public static final String findMatchingUserQuery = "SELECT * FROM user WHERE username = ?";

    public String getPhoneNumberForUsername(String username) {
        String dsn = "jdbc:sqlite:src/main/resources/database/database.sqlite";
        try (Connection connection = DriverManager.getConnection(dsn);
             PreparedStatement statement = connection.prepareStatement(findPhoneNumberFromUsernameQuery);) {
            statement.setQueryTimeout(30);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("phone_number");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        return "";
    }

    public User getUserByUsername(String username) {
        String dsn = "jdbc:sqlite:src/main/resources/database/database.sqlite";
        try (Connection connection = DriverManager.getConnection(dsn);
             PreparedStatement statement = connection.prepareStatement(findMatchingUserQuery);) {
            statement.setQueryTimeout(30);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email_address"),
                        rs.getString("phone_number")
                );
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        return null;
    }
}
