package com.aps.config;

import com.aps.avs.AvsClient;
import com.aps.avs.SimulatedAvsClient;
import com.aps.repository.ApplicationRepository;
import com.aps.repository.InMemoryApplicationRepository;
import com.aps.service.ApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ApplicationRepository applicationRepository() {
        return new InMemoryApplicationRepository();
    }

    @Bean
    public AvsClient avsClient() {
        return new SimulatedAvsClient();
    }

    @Bean
    public ApplicationService applicationService(
            ApplicationRepository applicationRepository,
            AvsClient avsClient) {

        return new ApplicationService(
                applicationRepository,
                avsClient
        );
    }
}