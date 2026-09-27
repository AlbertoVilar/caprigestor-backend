package com.devmaster.goatfarm.goat.business.abcc;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

class AbccImportEligibilityPolicyTest {

    private static final String MISSING_FARM_TOD =
            "A fazenda não possui TOD configurado. Configure o TOD da fazenda para usar a importação ABCC.";
    private static final String ABCC_TOD_MISMATCH =
            "O animal selecionado possui TOD diferente do TOD da fazenda. Importação ABCC permitida apenas para animais do mesmo TOD.";
    private static final String REQUEST_TOD_MISMATCH =
            "Para importar pela ABCC, o TOD informado deve ser igual ao TOD da fazenda.";

    private final AbccImportEligibilityPolicy policy = new AbccImportEligibilityPolicy();

    @Test
    void nonAdminWithMatchingTodCanImport() {
        String farmTod = policy.requireFarmTodForImport(farm("12345"), false);

        assertThatCode(() -> policy.validatePreviewTod(false, farmTod, "12345"))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.validateRequestTod(false, farmTod, "12345"))
                .doesNotThrowAnyException();
    }

    @Test
    void nonAdminFarmTodMustNotBeNullOrBlank() {
        assertTodError(() -> policy.requireFarmTodForImport(farm(null), false), MISSING_FARM_TOD);
        assertTodError(() -> policy.requireFarmTodForImport(farm("  \t "), false), MISSING_FARM_TOD);
    }

    @Test
    void nonAdminAbccTodMismatchAndMissingValuesUseExistingContract() {
        assertTodError(() -> policy.validatePreviewTod(false, "12345", "99999"), ABCC_TOD_MISMATCH);
        assertTodError(() -> policy.validatePreviewTod(false, "12345", null), ABCC_TOD_MISMATCH);
        assertTodError(() -> policy.validatePreviewTod(false, "12345", "  "), ABCC_TOD_MISMATCH);
    }

    @Test
    void nonAdminSubmittedRequestTodMismatchAndMissingValuesUseExistingContract() {
        assertTodError(() -> policy.validateRequestTod(false, "12345", "99999"), REQUEST_TOD_MISMATCH);
        assertTodError(() -> policy.validateRequestTod(false, "12345", null), REQUEST_TOD_MISMATCH);
        assertTodError(() -> policy.validateRequestTod(false, "12345", "  "), REQUEST_TOD_MISMATCH);
    }

    @Test
    void comparisonTrimsWhitespaceAndIgnoresCase() {
        String farmTod = policy.requireFarmTodForImport(farm("  AbC  "), false);

        assertThat(farmTod).isEqualTo("AbC");
        assertThatCode(() -> policy.validatePreviewTod(false, farmTod, " abc "))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.validateRequestTod(false, farmTod, " ABC "))
                .doesNotThrowAnyException();
    }

    @Test
    void adminMayImportWithoutFarmTodAndWithDivergentAbccAndRequestTod() {
        String farmTod = policy.requireFarmTodForImport(farm(null), true);

        assertThat(farmTod).isNull();
        assertThatCode(() -> policy.validatePreviewTod(true, farmTod, "99999"))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.validateRequestTod(true, farmTod, "88888"))
                .doesNotThrowAnyException();
    }

    @Test
    void onlyPolicyGeneratedAbccTodMismatchIsClassifiedForBatchSkipping() {
        BusinessRuleException mismatch = captureTodError(
                () -> policy.validatePreviewTod(false, "12345", "99999"), ABCC_TOD_MISMATCH);
        BusinessRuleException requestMismatch = captureTodError(
                () -> policy.validateRequestTod(false, "12345", "99999"), REQUEST_TOD_MISMATCH);
        BusinessRuleException unrelated = new BusinessRuleException("registrationNumber", "Registro inválido.");

        assertThat(policy.isAbccTodMismatch(mismatch)).isTrue();
        assertThat(policy.isAbccTodMismatch(requestMismatch)).isFalse();
        assertThat(policy.isAbccTodMismatch(unrelated)).isFalse();
        assertThat(policy.isAbccTodMismatch(null)).isFalse();
    }

    private void assertTodError(Runnable action, String expectedMessage) {
        captureTodError(action, expectedMessage);
    }

    private BusinessRuleException captureTodError(Runnable action, String expectedMessage) {
        Throwable thrown = catchThrowable(action::run);
        assertThat(thrown).isInstanceOf(BusinessRuleException.class).hasMessage(expectedMessage);
        BusinessRuleException exception = (BusinessRuleException) thrown;
        assertThat(exception.getFieldName()).isEqualTo("tod");
        return exception;
    }

    private FarmRecord farm(String tod) {
        return new FarmRecord(1L, "Capril Vilar", tod, null, null, null, List.of(), null, null, null);
    }
}
