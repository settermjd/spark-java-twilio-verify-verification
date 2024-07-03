package org.settermjd.LoginVerification;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.glassfish.grizzly.http.util.HttpStatus;
import spark.ModelAndView;
import spark.Redirect;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class HelloWorld {
    private static final Logger logger = LogManager.getLogger(HelloWorld.class);

    private static String getPhoneNumberForUsername(String username) {
        String findMatchingUserQuery = "SELECT phone_number FROM user WHERE username = ?";
        String dsn = "jdbc:sqlite:src/main/resources/database/database.sqlite";
        try (Connection connection = DriverManager.getConnection(dsn);
             PreparedStatement statement = connection.prepareStatement(findMatchingUserQuery);) {
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

    public static void main(String[] args) {
        port(8080);

        Dotenv dotenv = null;
        dotenv = Dotenv.configure().load();

        String accountSID = dotenv.get("TWILIO_ACCOUNT_SID");
        String authToken = dotenv.get("TWILIO_AUTH_TOKEN");
        String verifySID = dotenv.get("VERIFY_SERVICE_SID");

        staticFiles.location("/public");
        staticFiles.expireTime(600);

        path("/login", () -> {
            before("/*", (q, a) -> logger.info("Received login request"));
            get("", (request, response) -> {
                Map<String, Object> model = new HashMap<>();
                String error = request.session().attribute("error");
                if (error != null && ! error.isEmpty()) {
                    model.put("error", error);
                }
                return new ModelAndView(model, "templates/login.vm");
            }, new VelocityTemplateEngine());
            post("", (request, response) -> {
                String username = request.queryParams("username");
                if (username == null || username.isEmpty()) {
                    String error = "Username not available";
                    request.session().attribute("error", error);
                    logger.error(error);
                    response.redirect("/login", Redirect.Status.SEE_OTHER.intValue());
                    return null;
                }

                String phoneNumber = getPhoneNumberForUsername(username);
                if (Objects.equals(phoneNumber, "")) {
                    request.session().attribute("error", "Could not find user's phone number");
                    response.redirect("/login", Redirect.Status.SEE_OTHER.intValue());
                    return null;
                }

                Twilio.init(accountSID, authToken);
                Verification
                        .creator(verifySID, phoneNumber, "sms")
                        .create();
                response.redirect("/verifyme", Redirect.Status.SEE_OTHER.intValue());
                return null;
            });
        });

        get("/verifyme", ((request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "templates/verifyme.vm");
        }), new VelocityTemplateEngine());

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
                            rs.getString("email_address"),
                            rs.getString("phone_number")
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