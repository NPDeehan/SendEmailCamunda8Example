package io.camunda.getstarted.sendemail;

import org.apache.commons.validator.routines.EmailValidator;

public class ValidateEmail {

    public static boolean isValidEmail(String email) {
        // create the EmailValidator instance
        EmailValidator validator = EmailValidator.getInstance();

        // check for valid email addresses using isValid method
        return validator.isValid(email);
    }

}
