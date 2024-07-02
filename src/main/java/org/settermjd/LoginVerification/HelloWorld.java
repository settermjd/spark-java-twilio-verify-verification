package org.settermjd.LoginVerification;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class HelloWorld {
    public static void main(String[] args) {
        port(8080);

        get("/profile", ((request, response) -> {
            Map<String, Object> model = new HashMap<>();

            String findMatchingUserQuery = "SELECT * FROM user WHERE username = ?";
            String dsn = "jdbc:sqlite:src/main/resources/database/database.sqlite";
            try (Connection connection = DriverManager.getConnection(dsn);
                 PreparedStatement statement = connection.prepareStatement(findMatchingUserQuery);) {
                String username = request.session().attribute("username");
                if (username == null || username.isEmpty()) {
                    halt(404, "User not available");
                }

                statement.setQueryTimeout(30);
                statement.setString(1, username);

                ResultSet rs = statement.executeQuery();
                while(rs.next())
                {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email_address")
                    );
                    model.put("user", user);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }

            return new ModelAndView(model, "templates/profile.vm");
        }), new VelocityTemplateEngine());
    }
}