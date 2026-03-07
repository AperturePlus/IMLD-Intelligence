package xenosoft.imldintelligence.common;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CheckPermission
{
    String resource();

    String action();

    String message() default "Permission denied";

}
