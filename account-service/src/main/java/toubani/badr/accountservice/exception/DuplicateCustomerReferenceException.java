package toubani.badr.accountservice.exception;

public class DuplicateCustomerReferenceException extends BusinessRuleException {
    public DuplicateCustomerReferenceException(String message) {
        super(message);
    }
}
