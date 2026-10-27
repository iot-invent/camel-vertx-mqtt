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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

class HiveMqIT extends AbstractBrokerIT {

    @Override
    GenericContainer<?> createBroker() {
        // The default configuration of the image listens for MQTT on 1883
        return new GenericContainer<>(DockerImageName.parse("hivemq/hivemq-ce:2026.5"))
                .withExposedPorts(1883)
                .waitingFor(new WaitAllStrategy().withStrategy(Wait.forLogMessage(".*Started HiveMQ in.*", 1))
                        .withStrategy(Wait.forListeningPort()));
    }
}
