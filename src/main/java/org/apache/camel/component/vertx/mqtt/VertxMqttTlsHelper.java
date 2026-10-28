/*
* Copyright (c) 2020 - 2026 IoT Invent GmbH
* All rights reserved.
*/
package org.apache.camel.component.vertx.mqtt;

import java.util.LinkedHashSet;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.CamelContext;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.jsse.CipherSuitesParameters;
import org.apache.camel.support.jsse.JsseParameters;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.SecureSocketProtocolsParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;

import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.mqtt.MqttClientOptions;

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
	static void configure(final MqttClientOptions options, final SSLContextParameters parameters, final CamelContext camelContext) {
		own(parameters, camelContext);
		try {
			final KeyManagersParameters keyManagers = parameters.getKeyManagers();
			if (keyManagers != null) {
				own(keyManagers, camelContext);
				own(keyManagers.getKeyStore(), camelContext);
				options.setKeyCertOptions(KeyCertOptions.wrap(first(keyManagers.createKeyManagers())));
			}
			final TrustManagersParameters trustManagers = parameters.getTrustManagers();
			if (trustManagers != null) {
				own(trustManagers, camelContext);
				own(trustManagers.getKeyStore(), camelContext);
				options.setTrustOptions(TrustOptions.wrap(first(trustManagers.createTrustManagers())));
			}
		} catch (final Exception e) {
			throw new RuntimeCamelException("Cannot read the key or trust managers of the SSL context parameters", e);
		}
		final SecureSocketProtocolsParameters protocols = parameters.getSecureSocketProtocols();
		if (protocols != null && protocols.getSecureSocketProtocol() != null
				&& !protocols.getSecureSocketProtocol().isEmpty()) {
			options.setEnabledSecureTransportProtocols(new LinkedHashSet<>(protocols.getSecureSocketProtocol()));
		}
		final CipherSuitesParameters cipherSuites = parameters.getCipherSuites();
		if (cipherSuites != null && cipherSuites.getCipherSuite() != null) {
			cipherSuites.getCipherSuite().forEach(options::addEnabledCipherSuite);
		}
	}

	/**
	 * Key stores resolve their resource through the Camel context, which nested parameters do not inherit.
	 */
	private static void own(final JsseParameters parameters, final CamelContext camelContext) {
		if (parameters != null && parameters.getCamelContext() == null) {
			parameters.setCamelContext(camelContext);
		}
	}

	private static X509KeyManager first(final KeyManager[] managers) {
		for (final KeyManager manager : managers) {
			if (manager instanceof final X509KeyManager x509) {
				return x509;
			}
		}
		throw new IllegalArgumentException("The SSL context parameters hold no X.509 key manager");
	}

	private static X509TrustManager first(final TrustManager[] managers) {
		for (final TrustManager manager : managers) {
			if (manager instanceof final X509TrustManager x509) {
				return x509;
			}
		}
		throw new IllegalArgumentException("The SSL context parameters hold no X.509 trust manager");
	}
}
