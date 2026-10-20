package com.aps.avs;

import com.aps.model.Application;

import java.util.UUID;

public class SimulatedAvsClient implements AvsClient {

    @Override
    public AvsResponse validate(Application application) {

        String reference = "AVS-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        return new AvsResponse("APPROVE", reference);
    }
}