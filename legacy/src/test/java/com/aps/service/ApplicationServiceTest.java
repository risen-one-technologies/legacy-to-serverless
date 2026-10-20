package com.aps.service;

import com.aps.avs.AvsClient;
import com.aps.avs.AvsResponse;
import com.aps.model.Application;
import com.aps.model.ApplicationStatus;
import com.aps.repository.ApplicationRepository;
import com.aps.repository.InMemoryApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    private ApplicationRepository applicationRepository;
    private AvsClient avsClient;
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationRepository = new InMemoryApplicationRepository();
        avsClient = mock(AvsClient.class);

        applicationService = new ApplicationService(
                applicationRepository,
                avsClient
        );
    }

    @Test
    void createApplicationShouldStartAsDraft() {
        Application application = applicationService.createApplication(
                "John Smith",
                "STANDARD",
                new BigDecimal("25000")
        );

        assertNotNull(application.getApplicationId());
        assertEquals("John Smith", application.getApplicantName());
        assertEquals("STANDARD", application.getApplicationType());
        assertEquals(new BigDecimal("25000"), application.getRequestedAmount());
        assertEquals(ApplicationStatus.DRAFT, application.getStatus());
        assertEquals("NOT_VALIDATED", application.getValidationStatus());
        assertNotNull(application.getCreatedAt());
        assertNotNull(application.getUpdatedAt());
    }

    @Test
    void validateApplicationShouldChangeStatusToValidated() {
        Application created = applicationService.createApplication(
                "Jane Smith",
                "STANDARD",
                new BigDecimal("15000")
        );

        Application validated = applicationService.validateApplication(
                created.getApplicationId()
        );

        assertEquals(ApplicationStatus.VALIDATED, validated.getStatus());
        assertEquals("VALIDATED", validated.getValidationStatus());
    }

    @Test
    void submitValidatedApplicationShouldApproveWhenAvsApproves() {
        Application created = applicationService.createApplication(
                "Alex Morgan",
                "STANDARD",
                new BigDecimal("20000")
        );

        applicationService.validateApplication(created.getApplicationId());

        AvsResponse avsResponse = mock(AvsResponse.class);
        when(avsResponse.getDecision()).thenReturn("APPROVE");
        when(avsResponse.getReference()).thenReturn("AVS-TEST-123");
        when(avsClient.validate(any(Application.class))).thenReturn(avsResponse);

        Application submitted = applicationService.submitApplication(
                created.getApplicationId()
        );

        assertEquals(ApplicationStatus.APPROVED, submitted.getStatus());
        assertEquals("AVS-TEST-123", submitted.getAvsReference());
        verify(avsClient).validate(any(Application.class));
    }

    @Test
    void submitDraftApplicationShouldBeRejected() {
        Application created = applicationService.createApplication(
                "Taylor Lee",
                "STANDARD",
                new BigDecimal("10000")
        );

        assertThrows(
                IllegalStateException.class,
                () -> applicationService.submitApplication(
                        created.getApplicationId()
                )
        );

        verifyNoInteractions(avsClient);
    }

    @Test
    void submitValidatedApplicationShouldRejectWhenAvsRejects() {
        Application created = applicationService.createApplication(
                "Morgan Taylor",
                "STANDARD",
                new BigDecimal("12000")
        );

        applicationService.validateApplication(created.getApplicationId());

        AvsResponse avsResponse = mock(AvsResponse.class);
        when(avsResponse.getDecision()).thenReturn("REJECT");
        when(avsResponse.getReference()).thenReturn("AVS-REJECT-123");
        when(avsClient.validate(any(Application.class))).thenReturn(avsResponse);

        Application submitted = applicationService.submitApplication(
                created.getApplicationId()
        );

        assertEquals(ApplicationStatus.REJECTED, submitted.getStatus());
        assertEquals("AVS-REJECT-123", submitted.getAvsReference());
        verify(avsClient).validate(any(Application.class));
    }
}