package se.openresult.unwrapeither.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceOneUnwrappedTest {
    
    @Test
    public void testExecuteToEither() {

        var serviceOneWrapped = new ServiceOneUnwrapped(new ServiceOne());

        // Success
        assertEquals("17", serviceOneWrapped.execute("valid-cookie-long").getRight().get());
        // UserIdNotFound
        assertEquals(ServiceOneError.UserIdNotFound, serviceOneWrapped.execute("invalid").getLeft().get());
        // UserNameNotFound
        assertEquals(ServiceOneError.UserNameNotFound, serviceOneWrapped.execute("valid-short").getLeft().get());

    }    
    @Test
    public void testExecuteWithThrows() {

        var serviceOneWrapped = new ServiceOneUnwrapped(new ServiceOne());

        // Success
        assertEquals("17", serviceOneWrapped.executeThrows("valid-cookie-long"));
        // UserIdNotFound
        assertThrows(ServiceOneUnwrappedGenException.class, () -> serviceOneWrapped.executeThrows("invalid"));
        // UserNameNotFound
        assertThrows(ServiceOneUnwrappedGenException.class, () -> serviceOneWrapped.executeThrows("valid-short"));

    }
}
