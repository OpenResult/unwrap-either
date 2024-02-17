package se.openresult.unwrapeither.example;

import org.junit.jupiter.api.Test;

import io.jbock.util.Either;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceOneFlatmapTest {
    ServiceOne serviceOne;

    private Either<ServiceOneError, String> execute(String cookie) {
        return serviceOne.findUserId(cookie).flatMap(id -> serviceOne.findUserName(id));
    }

    @Test
    public void test() {

        serviceOne = new ServiceOne();

        // Success
        assertEquals("17", execute("valid-cookie-long").getRight().get());
        // UserIdNotFound
        assertEquals(ServiceOneError.UserIdNotFound, execute("invalid").getLeft().get());
        // UserNameNotFound
        assertEquals(ServiceOneError.UserNameNotFound, execute("valid-short").getLeft().get());
    }
}
