package se.openresult.unwrapeither.example;

public class ServiceOneUnwrapped extends ServiceOneUnwrappedGen<String, String> {

    public ServiceOneUnwrapped(ServiceOne service) {
        super(service);
    }

    @Override
    public String apply(String cookie) {
        var userId = findUserId(cookie);
        var userName = findUserName(userId);
        return userName;
    }
    
}
