package com.aps.avs;

import com.aps.model.Application;

public interface AvsClient {

    AvsResponse validate(Application application);
}