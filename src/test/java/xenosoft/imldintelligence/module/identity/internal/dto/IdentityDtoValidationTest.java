package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityDtoValidationTest {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void tearDown() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void loginRequestShouldRejectBlankCredentials() {
        LoginRequest request = new LoginRequest("", "", "T".repeat(65));

        Set<ConstraintViolation<LoginRequest>> violations = VALIDATOR.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("username", "password", "tenantCode");
    }

    @Test
    void loginResponseShouldValidateNestedDtos() {
        LoginResponse response = new LoginResponse(
                new AuthToken("", "", 0L, ""),
                null
        );

        Set<ConstraintViolation<LoginResponse>> violations = VALIDATOR.validate(response);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("authToken.accessToken", "authToken.refreshToken", "authToken.expiresIn", "principal");
    }

    @Test
    void patientDtoShouldRejectFutureBirthDate() {
        PatientDto patient = new PatientDto(
                1L,
                2L,
                "PAT-001",
                "Patient A",
                "FEMALE",
                java.time.LocalDate.now().plusDays(1),
                "OUTPATIENT",
                "ACTIVE",
                "HOSPITAL",
                true,
                true,
                null,
                null
        );

        Set<ConstraintViolation<PatientDto>> violations = VALIDATOR.validate(patient);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("birthDate");
    }
}
