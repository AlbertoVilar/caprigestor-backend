package com.devmaster.goatfarm.goat.integration.abcc.adapter;

public class AbccMalformedResponseException extends AbccIntegrationException {

    public AbccMalformedResponseException(String message) {
        super(message);
    }

    public AbccMalformedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
