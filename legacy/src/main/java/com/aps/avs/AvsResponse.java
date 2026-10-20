package com.aps.avs;

public class AvsResponse {

    private final String decision;
    private final String reference;

    public AvsResponse(String decision, String reference) {
        this.decision = decision;
        this.reference = reference;
    }

    public String getDecision() {
        return decision;
    }

    public String getReference() {
        return reference;
    }
}