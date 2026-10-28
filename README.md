# Camel Vert.x MQTT

An Apache Camel component for MQTT 3.1.1 and 5 clients on the [Vert.x MQTT client](https://github.com/vert-x3/vertx-mqtt):
a consumer subscribes, a producer publishes.

```
vertx-mqtt:sensors/+/temp?brokerUrl=tcp://broker.example.com:1883&qos=1&mqttVersion=MQTT_5_0
```

The component is built like a module of `components/camel-vertx` in Apache Camel, so that it can be
contributed there: package `org.apache.camel.component.vertx.mqtt`, generated code in `src/generated`.
It is modelled on `camel-paho-mqtt5` and `camel-hivemq`.

## Build

```
mvn clean install
```

The configurers, the URI factory, the catalog JSON and the service descriptors are generated from the
annotations by `camel-component-maven-plugin` into `src/generated` and committed. The plugin runs after
compilation, so after a change of an option a second build compiles what the first one generated.

## Code style

The style of Apache Camel: `etc/eclipse/camel-eclipse-formatter-config.xml` is the formatter of Camel, taken
unchanged from `org.apache.camel:camel-buildtools:4.22.2` (4 spaces, lines up to 128 characters). Imports in the
groups `java`, `jakarta`, `javax`, `org.w3c`, `org.xml`, `junit`, then all others, static imports last, no
wildcards. No `@author` tags. Every Java file starts with the Apache License header of `LICENSE`, copyright IoT Invent
GmbH, until the contribution replaces it with the header of the ASF.

In Eclipse: project properties, Java Code Style, Formatter, enable project specific settings and import the file,
the same for Organize Imports and Save Actions, so that the settings of the workspace do not apply here.

## Release lines

| Branch | Versions | Camel | Vert.x | Java |
|---|---|---|---|---|
| `4.22` | `4.22.N` | 4.22 | 5.2 | 17 or later |
| `main` | `0.2.N` | 4.22, 4.24 once it is released | 5.2 | 17 or later |

Each line has a version of its own (`revision` in `pom.xml`), so that its tags `v<revision>.N` never collide
with those of another line. `main` is the line towards the contribution to Apache Camel and moves to Camel 4.24
once it is released. A fix for `4.22` is made there and merged into `main`.

Camel up to 4.23 uses Vert.x 4.5, Camel 4.24 Vert.x 5. The component therefore does not depend on
`camel-vertx-common`, which it would on contribution.

## License

Apache License, Version 2.0, see [LICENSE](LICENSE).
