package com.att.det;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = { })
public @interface ValidStuff {
    String message() default "Stuff not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
