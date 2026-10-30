package toubani.badr.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank String customerReference,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "doit être un code ISO 4217 (3 lettres majuscules)") String currency,
        @PositiveOrZero double initialBalance) {
}
