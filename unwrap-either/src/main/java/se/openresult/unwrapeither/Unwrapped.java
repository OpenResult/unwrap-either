package se.openresult.unwrapeither;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Unwrapped {
    Class<?> value() default String.class;
}
