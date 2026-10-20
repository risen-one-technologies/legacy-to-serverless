package com.aps.service;

import com.aps.avs.AvsClient;
import com.aps.avs.AvsResponse;
import com.aps.model.Application;
import com.aps.model.ApplicationStatus;
import com.aps.repository.ApplicationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AvsClient avsClient;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            AvsClient avsClient) {

        this.applicationRepository = applicationRepository;
        this.avsClient = avsClient;
    }

    public Application createApplication(
            String applicantName,
            String applicationType,
            BigDecimal requestedAmount) {

        Application application = new Application();

        application.setApplicationId(UUID.randomUUID().toString());
        application.setApplicantName(applicantName);
        application.setApplicationType(applicationType);
        application.setRequestedAmount(requestedAmount);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setValidationStatus("NOT_VALIDATED");

        LocalDateTime now = LocalDateTime.now();

        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        return applicationRepository.save(application);
    }

    public Optional<Application> getApplication(String applicationId) {
        return applicationRepository.findById(applicationId);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application updateApplication(
            String applicationId,
            String applicantName,
            String applicationType,
            BigDecimal requestedAmount) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Application not found: " + applicationId));

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT applications can be updated");
        }

        application.setApplicantName(applicantName);
        application.setApplicationType(applicationType);
        application.setRequestedAmount(requestedAmount);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public Application validateApplication(String applicationId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Application not found: " + applicationId));

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT applications can be validated");
        }

        if (application.getApplicantName() == null
                || application.getApplicantName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Applicant name is required");
        }

        if (application.getApplicationType() == null
                || application.getApplicationType().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Application type is required");
        }

        if (application.getRequestedAmount() == null
                || application.getRequestedAmount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Requested amount must be greater than zero");
        }

        application.setValidationStatus("VALIDATED");
        application.setStatus(ApplicationStatus.VALIDATED);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public Application submitApplication(String applicationId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Application not found: " + applicationId));

        if (application.getStatus() != ApplicationStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Only VALIDATED applications can be submitted");
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setUpdatedAt(LocalDateTime.now());

        applicationRepository.save(application);

        application.setStatus(ApplicationStatus.AVS_REVIEW);
        application.setUpdatedAt(LocalDateTime.now());

        AvsResponse avsResponse = avsClient.validate(application);

        application.setAvsReference(avsResponse.getReference());

        if ("APPROVE".equals(avsResponse.getDecision())) {
            application.setStatus(ApplicationStatus.APPROVED);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
        }

        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }
}