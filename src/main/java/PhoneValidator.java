import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

class PhoneValidator implements ConstraintValidator<Phone, String>{

    @Override
    public void initialize(Phone constraintAnnotation) {
        // initialize annotation do something
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) return true;
        return isIncorrectUsername(phone);
    }

    private boolean isIncorrectUsername(String phone) {
        Pattern PHONE_REGX =
                Pattern.compile("(09|03|08|07|05|\\+84[9|3|8|7])+([0-9]{8})", Pattern.CASE_INSENSITIVE);
        return PHONE_REGX.matcher(phone).matches();
    }
}
