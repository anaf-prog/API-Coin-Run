package com.anafXsamsul.notation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailDomainValidator.class)
@Documented
public @interface ValidEmailDomain {
    String message() default "Email tidak valid. Gunakan Email aktif seperti : Gmail, Outlook, iCloud, atau Yahoo";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
