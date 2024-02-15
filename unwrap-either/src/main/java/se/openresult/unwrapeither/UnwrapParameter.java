package se.openresult.unwrapeither;

public record UnwrapParameter(String type, String name) {
    String toCode() {
        return name + " " + type;
    }
}
