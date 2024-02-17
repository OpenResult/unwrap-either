package se.openresult.unwrapeither.example;

import se.openresult.unwrapeither.Unwrapped;

import io.jbock.util.Either;

@Unwrapped(ServiceOneError.class)
public class ServiceOne {
    public Either<ServiceOneError, Integer> findUserId(String cookie) {
        if (cookie.startsWith("valid")) {
            return Either.right(cookie.length());
        } else {
            return Either.left(ServiceOneError.UserIdNotFound);
        }
    }
    public Either<ServiceOneError, String> findUserName(Integer id) {
        if (id > 12) {
            return Either.right("" + id);
        } else {
            return Either.left(ServiceOneError.UserNameNotFound);
        }
    }
}
