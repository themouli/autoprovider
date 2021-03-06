package in.workarounds.autoprovider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by madki on 08/10/15.
 */
@Retention(CLASS) @Target(FIELD)
public @interface NotNull {
}
