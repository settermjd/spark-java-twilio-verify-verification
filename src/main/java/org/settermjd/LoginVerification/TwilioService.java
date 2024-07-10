package org.settermjd.LoginVerification;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

public class TwilioService {
    String verifyServiceSID;

    public TwilioService(String accountSID, String authToken, String verifyServiceSID) {
        this.verifyServiceSID = verifyServiceSID;
        Twilio.init(accountSID, authToken);
    }

    /**
     * Checks if the supplied verification code is valid for the supplied phone number
     *
     * @param phoneNumber the phone number that the code was sent to
     * @param code the code sent to the specified phone number
     * @return a VerificationCheck object containing whether the supplied code is valid
     */
    public VerificationCheck verifyCode(String phoneNumber, String code) {
        return VerificationCheck.creator(this.verifyServiceSID)
                .setTo(phoneNumber)
                .setCode(code)
                .create();
    }

    /**
     * Generates and sends a verification code via SMS
     *
     * @param phoneNumber the phone number to send the code to
     */
    public void sendVerificationCode(String phoneNumber) {
        Verification
                .creator(this.verifyServiceSID, phoneNumber, "sms")
                .create();
    }
}
