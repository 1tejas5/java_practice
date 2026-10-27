package com.minibank.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CONCEPTS: a custom annotation. RUNTIME retention so the TestRunner can find it with reflection
 * (exactly how JUnit's @Test works).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
    String value() default "";      // optional display name
}
