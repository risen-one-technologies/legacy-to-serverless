package com.aps.controller;

import com.aps.model.Application;
import com.aps.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(
            @RequestBody CreateApplicationRequest request) {

        Application application = applicationService.createApplication(
                request.getApplicantName(),
                request.getApplicationType(),
                request.getRequestedAmount()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(application);
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<Application> getApplication(
            @PathVariable String applicationId) {

        return applicationService
                .getApplication(applicationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(
                applicationService.getAllApplications()
        );
    }

    @PutMapping("/{applicationId}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable String applicationId,
            @RequestBody CreateApplicationRequest request) {

        try {
            Application application = applicationService.updateApplication(
                    applicationId,
                    request.getApplicantName(),
                    request.getApplicationType(),
                    request.getRequestedAmount()
            );

            return ResponseEntity.ok(application);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{applicationId}/validate")
    public ResponseEntity<Application> validateApplication(
            @PathVariable String applicationId) {

        try {
            Application application =
                    applicationService.validateApplication(applicationId);

            return ResponseEntity.ok(application);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{applicationId}/submit")
    public ResponseEntity<Application> submitApplication(
            @PathVariable String applicationId) {

        try {
            Application application =
                    applicationService.submitApplication(applicationId);

            return ResponseEntity.ok(application);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    public static class CreateApplicationRequest {

        private String applicantName;
        private String applicationType;
        private BigDecimal requestedAmount;

        public String getApplicantName() {
            return applicantName;
        }

        public void setApplicantName(String applicantName) {
            this.applicantName = applicantName;
        }

        public String getApplicationType() {
            return applicationType;
        }

        public void setApplicationType(String applicationType) {
            this.applicationType = applicationType;
        }

        public BigDecimal getRequestedAmount() {
            return requestedAmount;
        }

        public void setRequestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
        }
    }
}