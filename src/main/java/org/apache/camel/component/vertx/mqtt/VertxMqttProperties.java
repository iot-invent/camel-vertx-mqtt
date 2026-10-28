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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttProperties.BinaryProperty;
import io.netty.handler.codec.mqtt.MqttProperties.IntegerProperty;
import io.netty.handler.codec.mqtt.MqttProperties.MqttProperty;
import io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType;
import io.netty.handler.codec.mqtt.MqttProperties.StringPair;
import io.netty.handler.codec.mqtt.MqttProperties.StringProperty;
import io.netty.handler.codec.mqtt.MqttProperties.UserProperty;
import org.apache.camel.Message;

/**
 * The MQTT 5 properties of a PUBLISH packet and the headers of a Camel message that carry them.
 */
final class VertxMqttProperties {

    private VertxMqttProperties() {
    }

    /**
     * @return the properties the headers of a message set, empty if they set none
     */
    static MqttProperties fromHeaders(final Message message) {
        final MqttProperties properties = new MqttProperties();
        final Map<?, ?> user = message.getHeader(VertxMqttConstants.USER_PROPERTIES, Map.class);
        if (user != null) {
            user.forEach((key, value) -> {
                if (key != null && value != null) {
                    properties.add(new UserProperty(key.toString(), value.toString()));
                }
            });
        }
        final String contentType = message.getHeader(VertxMqttConstants.CONTENT_TYPE, String.class);
        if (contentType != null) {
            properties.add(new StringProperty(MqttPropertyType.CONTENT_TYPE.value(), contentType));
        }
        final String responseTopic = message.getHeader(VertxMqttConstants.RESPONSE_TOPIC, String.class);
        if (responseTopic != null) {
            properties.add(new StringProperty(MqttPropertyType.RESPONSE_TOPIC.value(), responseTopic));
        }
        final byte[] correlationData = message.getHeader(VertxMqttConstants.CORRELATION_DATA, byte[].class);
        if (correlationData != null) {
            properties.add(new BinaryProperty(MqttPropertyType.CORRELATION_DATA.value(), correlationData));
        }
        final Long expiry = message.getHeader(VertxMqttConstants.MESSAGE_EXPIRY_INTERVAL, Long.class);
        if (expiry != null) {
            if (expiry < 0 || expiry > 0xFFFF_FFFFL) {
                throw new IllegalArgumentException(
                        "The message expiry interval must be between 0 and 4294967295 seconds, not " + expiry);
            }
            // A four byte integer of the protocol, carried as the bits of an int
            properties.add(new IntegerProperty(
                    MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value(),
                    (int) expiry.longValue()));
        }
        return properties;
    }

    /**
     * Sets the headers of a message from the properties of a received PUBLISH packet.
     */
    static void toHeaders(final MqttProperties properties, final Message message) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        final Map<String, String> user = new LinkedHashMap<>();
        for (final MqttProperty<?> property : properties.getProperties(MqttPropertyType.USER_PROPERTY.value())) {
            final Object value = property.value();
            if (value instanceof final StringPair pair) {
                user.put(pair.key, pair.value);
            } else if (value instanceof final Collection<?> pairs) {
                for (final Object each : pairs) {
                    if (each instanceof final StringPair pair) {
                        user.put(pair.key, pair.value);
                    }
                }
            }
        }
        if (!user.isEmpty()) {
            message.setHeader(VertxMqttConstants.USER_PROPERTIES, user);
        }
        final MqttProperty<?> contentType = properties.getProperty(MqttPropertyType.CONTENT_TYPE.value());
        if (contentType != null) {
            message.setHeader(VertxMqttConstants.CONTENT_TYPE, contentType.value());
        }
        final MqttProperty<?> responseTopic = properties.getProperty(MqttPropertyType.RESPONSE_TOPIC.value());
        if (responseTopic != null) {
            message.setHeader(VertxMqttConstants.RESPONSE_TOPIC, responseTopic.value());
        }
        final MqttProperty<?> correlationData = properties.getProperty(MqttPropertyType.CORRELATION_DATA.value());
        if (correlationData != null) {
            message.setHeader(VertxMqttConstants.CORRELATION_DATA, correlationData.value());
        }
        final MqttProperty<?> expiry = properties.getProperty(MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value());
        if (expiry != null && expiry.value() instanceof final Integer seconds) {
            message.setHeader(VertxMqttConstants.MESSAGE_EXPIRY_INTERVAL, Integer.toUnsignedLong(seconds));
        }
    }
}
