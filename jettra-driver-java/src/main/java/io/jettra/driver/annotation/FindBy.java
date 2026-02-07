package io.jettra.driver.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FindBy {
    String field() default "id";
}
