package com.devmaster.goatfarm.goat.business.abcc;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import org.springframework.stereotype.Component;

/** Applies farm TOD compatibility rules to ABCC imports. */
@Component
public class AbccImportEligibilityPolicy {

    private static final String FIELD_TOD = "tod";
    private static final String MSG_MISSING_FARM_TOD =
            "A fazenda não possui TOD configurado. Configure o TOD da fazenda para usar a importação ABCC.";
    private static final String MSG_TOD_MISMATCH =
            "O animal selecionado possui TOD diferente do TOD da fazenda. Importação ABCC permitida apenas para animais do mesmo TOD.";
    private static final String MSG_REQUEST_TOD_MISMATCH =
            "Para importar pela ABCC, o TOD informado deve ser igual ao TOD da fazenda.";

    public String requireFarmTodForImport(FarmRecord farm, boolean isAdmin) {
        if (isAdmin) {
            return null;
        }

        String farmTod = trimOrNull(farm.tod());
        if (farmTod == null) {
            throw new BusinessRuleException(FIELD_TOD, MSG_MISSING_FARM_TOD);
        }
        return farmTod;
    }

    public void validatePreviewTod(boolean isAdmin, String farmTod, String abccTod) {
        if (isAdmin) {
            return;
        }
        if (!isSameTod(abccTod, farmTod)) {
            throw new BusinessRuleException(FIELD_TOD, MSG_TOD_MISMATCH);
        }
    }

    public void validateRequestTod(boolean isAdmin, String farmTod, String requestTod) {
        if (!isAdmin && !isSameTod(requestTod, farmTod)) {
            throw new BusinessRuleException(FIELD_TOD, MSG_REQUEST_TOD_MISMATCH);
        }
    }

    public boolean isAbccTodMismatch(BusinessRuleException exception) {
        return exception != null
                && FIELD_TOD.equals(exception.getFieldName())
                && MSG_TOD_MISMATCH.equals(exception.getMessage());
    }

    private boolean isSameTod(String left, String right) {
        String normalizedLeft = trimOrNull(left);
        String normalizedRight = trimOrNull(right);
        if (normalizedLeft == null || normalizedRight == null) {
            return false;
        }
        return normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
