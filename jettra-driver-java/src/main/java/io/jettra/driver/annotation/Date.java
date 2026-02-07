package io.jettra.driver.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Date {
    String format() default "yyyy-MM-dd";
}
