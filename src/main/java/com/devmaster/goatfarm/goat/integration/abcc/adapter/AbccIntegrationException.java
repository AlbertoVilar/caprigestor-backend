package com.devmaster.goatfarm.goat.integration.abcc.adapter;

import com.devmaster.goatfarm.config.exceptions.custom.ExternalServiceUnavailableException;

/** Base exception for failures while calling or interpreting the public ABCC service. */
public class AbccIntegrationException extends ExternalServiceUnavailableException {

    public AbccIntegrationException(String message) {
        super(message, null);
    }

    public AbccIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
