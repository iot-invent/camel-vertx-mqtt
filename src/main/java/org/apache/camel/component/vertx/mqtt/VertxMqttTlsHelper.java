/*
 * Copyright (c) 2026 IoT Invent GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.vertx.mqtt;

import java.util.LinkedHashSet;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.mqtt.MqttClientOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.jsse.CipherSuitesParameters;
import org.apache.camel.support.jsse.JsseParameters;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.SecureSocketProtocolsParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;

/**
 * Maps SSL context parameters to the TLS options of the Vert.x client. On contribution to Camel this is what
 * camel-vertx-common does, which up to Camel 4.23 is built on Vert.x 4.
 */
final class VertxMqttTlsHelper {

    private VertxMqttTlsHelper() {
    }

    /**
     * Sets the key managers, trust managers, protocols and cipher suites of the parameters on the options. Host name
     * verification is set by the caller and not touched here.
     */
    static void configure(MqttClientOptions options, SSLContextParameters parameters, CamelContext camelContext) {
        own(parameters, camelContext);
        try {
            KeyManagersParameters keyManagers = parameters.getKeyManagers();
            if (keyManagers != null) {
                own(keyManagers, camelContext);
                own(keyManagers.getKeyStore(), camelContext);
                options.setKeyCertOptions(KeyCertOptions.wrap(first(keyManagers.createKeyManagers())));
            }
            TrustManagersParameters trustManagers = parameters.getTrustManagers();
            if (trustManagers != null) {
                own(trustManagers, camelContext);
                own(trustManagers.getKeyStore(), camelContext);
                options.setTrustOptions(TrustOptions.wrap(first(trustManagers.createTrustManagers())));
            }
        } catch (Exception e) {
            throw new RuntimeCamelException("Cannot read the key or trust managers of the SSL context parameters", e);
        }
        SecureSocketProtocolsParameters protocols = parameters.getSecureSocketProtocols();
        if (protocols != null && protocols.getSecureSocketProtocol() != null
                && !protocols.getSecureSocketProtocol().isEmpty()) {
            options.setEnabledSecureTransportProtocols(new LinkedHashSet<>(protocols.getSecureSocketProtocol()));
        }
        CipherSuitesParameters cipherSuites = parameters.getCipherSuites();
        if (cipherSuites != null && cipherSuites.getCipherSuite() != null) {
            cipherSuites.getCipherSuite().forEach(options::addEnabledCipherSuite);
        }
    }

    /**
     * Key stores resolve their resource through the Camel context, which nested parameters do not inherit.
     */
    private static void own(JsseParameters parameters, CamelContext camelContext) {
        if (parameters != null && parameters.getCamelContext() == null) {
            parameters.setCamelContext(camelContext);
        }
    }

    private static X509KeyManager first(KeyManager[] managers) {
        for (KeyManager manager : managers) {
            if (manager instanceof X509KeyManager x509) {
                return x509;
            }
        }
        throw new IllegalArgumentException("The SSL context parameters hold no X.509 key manager");
    }

    private static X509TrustManager first(TrustManager[] managers) {
        for (TrustManager manager : managers) {
            if (manager instanceof X509TrustManager x509) {
                return x509;
            }
        }
        throw new IllegalArgumentException("The SSL context parameters hold no X.509 trust manager");
    }
}
