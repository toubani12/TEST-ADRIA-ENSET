package toubani.badr.transactionservice.exception;

public class AmountLimitExceededException extends BusinessRuleException {
    public AmountLimitExceededException(String message) {
        super(message);
    }
}
