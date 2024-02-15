package se.openresult.unwrapeither.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceOneTest {

    @Test
    public void test() {

        var serviceOneWrapped = new ServiceOneUnwrapped<String, String>(new ServiceOne()) {

            @Override
            public String apply(String t) {
                var userId = findUserId(t);
                var userName = findUserName(userId);
                return userName;
            }

        };

        // Success
        assertEquals("17", serviceOneWrapped.execute("valid-cookie-long").getRight().get());
        // UserIdNotFound
        assertEquals(ServiceOneError.UserIdNotFound, serviceOneWrapped.execute("invalid").getLeft().get());
        // UserNameNotFound
        assertEquals(ServiceOneError.UserNameNotFound, serviceOneWrapped.execute("valid-short").getLeft().get());
    }
}
