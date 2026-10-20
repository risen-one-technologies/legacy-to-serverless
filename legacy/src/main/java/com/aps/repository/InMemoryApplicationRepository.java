package com.aps.repository;

import com.aps.model.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryApplicationRepository implements ApplicationRepository {

    private final ConcurrentMap<String, Application> applications =
            new ConcurrentHashMap<>();

    @Override
    public Application save(Application application) {
        applications.put(application.getApplicationId(), application);
        return application;
    }

    @Override
    public Optional<Application> findById(String applicationId) {
        return Optional.ofNullable(applications.get(applicationId));
    }

    @Override
    public List<Application> findAll() {
        return new ArrayList<>(applications.values());
    }

    @Override
    public void deleteById(String applicationId) {
        applications.remove(applicationId);
    }
}