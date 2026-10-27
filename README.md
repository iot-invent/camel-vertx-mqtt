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

## Versions

| | |
|---|---|
| Java | 17 or later |
| Camel | 4.22 |
| Vert.x | 5.2 |

Camel up to 4.23 uses Vert.x 4.5, Camel 4.24 Vert.x 5. The component therefore does not depend on
`camel-vertx-common`, which it would on contribution.

## License

Apache License, Version 2.0, see [LICENSE](LICENSE).
