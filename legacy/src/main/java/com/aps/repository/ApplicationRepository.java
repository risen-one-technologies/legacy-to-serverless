package com.aps.repository;

import com.aps.model.Application;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository {

    Application save(Application application);

    Optional<Application> findById(String applicationId);

    List<Application> findAll();

    void deleteById(String applicationId);
}