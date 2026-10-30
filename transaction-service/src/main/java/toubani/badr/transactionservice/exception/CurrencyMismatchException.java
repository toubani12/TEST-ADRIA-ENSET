package toubani.badr.transactionservice.exception;

public class CurrencyMismatchException extends BusinessRuleException {
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
