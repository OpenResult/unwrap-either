package se.openresult.unwrapeither;

import java.util.List;

public record UnwrapFunction(String left, String returnType, String functionName, List<UnwrapParameter> parameters) {
    
}
