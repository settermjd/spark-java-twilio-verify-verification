package org.settermjd.LoginVerification;

import com.twilio.rest.verify.v2.service.VerificationCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import spark.ModelAndView;
import spark.Redirect;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class AuthenticateUser {
    private static final Logger logger = LogManager.getLogger(AuthenticateUser.class);
    private static final UserService userService = new UserService();


    public static void main(String[] args) {
        port(8080);

        Dotenv dotenv = Dotenv.configure().load();
        TwilioService twilioService = new TwilioService(
                dotenv.get("TWILIO_ACCOUNT_SID"),
                dotenv.get("TWILIO_AUTH_TOKEN"),
                dotenv.get("VERIFY_SERVICE_SID")
        );

        // Make static files available in the application
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

                String phoneNumber = userService.getPhoneNumberForUsername(username);
                if (Objects.equals(phoneNumber, "")) {
                    request.session().attribute("error", "Could not find user's phone number");
                    response.redirect("/login", Redirect.Status.SEE_OTHER.intValue());
                    return null;
                }

                twilioService.sendVerificationCode(phoneNumber);
                request.session().attribute("username", username);
                response.redirect("/verifyme", Redirect.Status.SEE_OTHER.intValue());
                return null;
            });
        });

        path("/verifyme", () -> {
            get("", ((request, response) -> {
                Map<String, Object> model = new HashMap<>();
                return new ModelAndView(model, "templates/verifyme.vm");
            }), new VelocityTemplateEngine());
            post("", ((request, response) -> {
                String code = request.queryParams("code");
                if (code == null || code.isEmpty()) {
                    String error = "Verification code not available";
                    request.session().attribute("error", error);
                    logger.error(error);
                    response.redirect("/verifyme", Redirect.Status.SEE_OTHER.intValue());
                    return null;
                }
                String username = request.session().attribute("username");
                String phoneNumber = userService.getPhoneNumberForUsername(username);
                if (Objects.equals(phoneNumber, "")) {
                    request.session().attribute("error", "Could not find user's phone number");
                    response.redirect("/verifyme", Redirect.Status.SEE_OTHER.intValue());
                    return null;
                }
                
                VerificationCheck verificationCheck = twilioService.verifyCode(phoneNumber, code);
                final String path = (Objects.equals(verificationCheck.getStatus(), "approved"))
                        ? "/profile"
                        : "/verifyme";
                response.redirect(path, Redirect.Status.SEE_OTHER.intValue());

                return null;
            }), new VelocityTemplateEngine());
        });

        get("/profile", ((request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String username = request.session().attribute("username");
            if (username == null || username.isEmpty()) {
                response.status(404);
                return new ModelAndView(model, "templates/404.vm");
            }

            User user = userService.getUserByUsername(username);
            model.put("user", user);

            return new ModelAndView(model, "templates/profile.vm");
        }), new VelocityTemplateEngine());
    }
}