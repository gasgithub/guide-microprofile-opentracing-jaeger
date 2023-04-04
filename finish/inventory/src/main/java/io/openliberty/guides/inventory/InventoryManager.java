// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.inventory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Collections;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Span;

@ApplicationScoped
// tag::InventoryManager[]
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "system.http.port", defaultValue = "9080")
    int SYSTEM_PORT;

    private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());

    @Inject
    @RestClient
    private SystemClient systemClient;

    // tag::customTracer[]
    @Inject Tracer tracer;
    // end::customTracer[]


    public Properties get(String hostname) {
        if( hostname == null) {
            // return default
            Properties properties = systemClient.getProperties();
            return properties;
        }
        else {
            String customURIString = "http://" + hostname + ":" + SYSTEM_PORT + "/system";
            URI customURI = null;
            try {
                customURI = URI.create(customURIString);
                SystemClient customRestClient = RestClientBuilder.newBuilder()
                                                    .baseUri(customURI)
                                                    .build(SystemClient.class);

                return customRestClient.getProperties();

            }
            catch (Exception e) {
               System.err.println("The given URI is unreachable.");
               return null;
            }
        }
    }

    public void add(String hostname, Properties systemProps) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        SystemData system = new SystemData(hostname, props);
        // tag::Add[]
        if (!systems.contains(system)) {
            // tag::addSpan[]
            Span span = tracer.buildSpan("add() Span").start();
            // end::addSpan[]
            // tag::Try[]
            try (Scope childScope = tracer.activateSpan(span)) {
                // tag::addToInvList[]
                systems.add(system);
                // end::addToInvList[]
            } finally {
                span.finish();
            }
            // end::Try[]
        }
        // end::Add[]
    }

    // tag::Traced[]
    @Traced(operationName = "InventoryManager.list")
    // end::Traced[]
    // tag::list[]
    public InventoryList list() {
        return new InventoryList(systems);
    }
    // end::list[]

    int clear() {
        int propertiesClearedCount = systems.size();
        systems.clear();
        return propertiesClearedCount;
    }
}
// end::InventoryManager[]
