package toubani.badr.accountservice.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String reference) {
        super("Compte introuvable : " + reference);
    }
}
