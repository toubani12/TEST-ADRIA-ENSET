package toubani.badr.accountservice.exception;

/** Violation d'une règle métier (HTTP 409). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
