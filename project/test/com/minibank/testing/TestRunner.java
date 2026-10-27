package com.minibank.testing;

import com.minibank.BankServiceTest;
import com.minibank.ModelAndParserTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A tiny JUnit clone.
 * CONCEPTS: REFLECTION (getDeclaredMethods, isAnnotationPresent, getDeclaredConstructor().newInstance(),
 * Method.invoke), InvocationTargetException unwrapping, a fresh instance per test (isolation).
 */
public final class TestRunner {

    private static final List<Class<?>> TEST_CLASSES = List.of(BankServiceTest.class, ModelAndParserTest.class);

    public static void main(String[] args) throws Exception {
        int passed = 0, failed = 0;
        long start = System.currentTimeMillis();
        for (Class<?> testClass : TEST_CLASSES) {
            System.out.println("\n" + testClass.getSimpleName());
            Method[] methods = testClass.getDeclaredMethods();
            Arrays.sort(methods, Comparator.comparing(Method::getName));       // deterministic order
            for (Method m : methods) {
                if (!m.isAnnotationPresent(Test.class)) continue;
                String name = m.getAnnotation(Test.class).value();
                if (name.isEmpty()) name = m.getName();
                Object instance = testClass.getDeclaredConstructor().newInstance();   // fresh state per test
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                    passed++;
                    System.out.println("  PASS  " + name);
                } catch (InvocationTargetException e) {                         // the test itself threw
                    failed++;
                    Throwable cause = e.getCause();
                    System.out.println("  FAIL  " + name + "\n        " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                }
            }
        }
        System.out.printf("%n%d passed, %d failed (%d ms)%n", passed, failed, System.currentTimeMillis() - start);
        if (failed > 0) System.exit(1);
    }
}
