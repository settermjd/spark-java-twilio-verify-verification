package org.settermjd.LoginVerification;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
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
    private static final Dotenv dotenv = Dotenv.configure().load();

    public static void main(String[] args) {
        port(8080);

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

                sendVerificationCode(phoneNumber);
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
                VerificationCheck verificationCheck = verifyCode(phoneNumber, code);
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

    /**
     * Checks if the supplied verification code is valid for the supplied phone number
     *
     * @param phoneNumber the phone number that the code was sent to
     * @param code the code sent to the specified phone number
     * @return a VerificationCheck object containing whether the supplied code is valid
     */
    private static VerificationCheck verifyCode(String phoneNumber, String code) {
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));
        return VerificationCheck.creator(dotenv.get("VERIFY_SERVICE_SID"))
                        .setTo(phoneNumber)
                        .setCode(code)
                        .create();
    }

    /**
     * Generates and sends a verification code via SMS
     *
     * @param phoneNumber the phone number to send the code to
     */
    public static void sendVerificationCode(String phoneNumber) {
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));
        Verification
                .creator(dotenv.get("VERIFY_SERVICE_SID"), phoneNumber, "sms")
                .create();
    }
}