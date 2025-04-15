package org.grobid.service;

import com.google.inject.Provides;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.grobid.core.engines.*;
import org.grobid.service.configuration.DatastetServiceConfiguration;
import org.grobid.service.controller.DatastetController;
import org.grobid.service.controller.DatastetProcessFile;
import org.grobid.service.controller.DatastetProcessString;
import org.grobid.service.controller.HealthCheck;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;


public class DatastetServiceModule extends DropwizardAwareModule<DatastetServiceConfiguration> {

    @Override
    public void configure() {
        // Generic modules
        bind(GrobidEngineInitialiser.class);
        bind(HealthCheck.class);

        // Core components
        bind(DatasetDisambiguator.class);
        bind(DatasetContextClassifier.class);
        bind(DataseerParser.class);
        bind(DataseerClassifier.class);
        bind(DatasetParser.class);
        bind(DatastetProcessFile.class);
        bind(DatastetProcessString.class);

        // REST
        bind(DatastetController.class);
    }

    @Provides
    Client provideClient() {
        return ClientBuilder.newClient();
    }

}