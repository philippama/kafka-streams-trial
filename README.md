# Trying out Kafka Streams

Developer Guide: https://kafka.apache.org/0110/documentation/streams/developer-guide

API documentation is part of the main Kafka javadoc: https://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/streams/KafkaStreams.html

## Useful Kafka commands

```bash
$KAFKA_HOME/bin/zookeeper-server-start.sh /usr/local/etc/kafka/zookeeper.properties
$KAFKA_HOME/bin/kafka-server-start.sh /usr/local/etc/kafka/server.properties

$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --list
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic $1
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic $1
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic $1 < input_file.txt
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --consumer-property group.id=philippa --topic $1
```
## WordcountDemo

WordcountDemo from https://github.com/apache/kafka/blob/0.11.0/streams/examples/src/main/java/org/apache/kafka/streams/examples/wordcount/WordCountDemo.java

(Instructions for running a slightly different word count demo: https://kafka.apache.org/0110/documentation/streams/quickstart)

### Running the demo

Create topics:
```bash
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-plaintext-input
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-wordcount-output
```
Produce to input topic:
```bash
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic streams-plaintext-input \
< /Users/philippa.main/workspace_sandbox/kafka-streams-trial/src/test/resources/pm/kafkastreams/examples/wordcount/WordCountDemoInput.txt
```
Consume from output topic:
```bash
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-wordcount-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

## Joining a KStream to a GlobalKTable

See
for joining a KStream to a GlobalKTable and for its comment about cleaning local state.
https://github.com/confluentinc/examples/blob/3.3.0-post/kafka-streams/src/main/java/io/confluent/examples/streams/GlobalKTablesExample.java

### Running it

Create topics:
```bash
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-join-stream-input
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-join-table-input
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-join-output
```

Produce to stream input topic:
```bash
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic streams-join-stream-input \
  --property "parse.key=true" --property "key.separator=:" \
  < /Users/philippa.main/workspace_sandbox/kafka-streams-trial/src/test/resources/pm/kafkastreams/streamtotable/StreamInput.txt

```
Produce to global table input topic:
```bash
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic streams-join-table-input \
  --property "parse.key=true" --property "key.separator=:" \
  < /Users/philippa.main/workspace_sandbox/kafka-streams-trial/src/test/resources/pm/kafkastreams/streamtotable/GlobalTableInput.txt

```
Consume from stream input topic:
```bash
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-join-stream-input \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    --from-beginning
```
Consume from output topic:
```bash
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-join-output \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --consumer-property group.id=philippa \
    --from-beginning
```

## To do:
* ~~Try out WordCountDemo~~
* Write application to consume JSON from streams topic, read something into a GlobalKTable from another topic and join the stream with the table
    * ~~See how to consume JSON -> key:value~~
        * ~~See uk.co.autotrader.forge.service.consumer.TopicConsumerRunnable [from subscribeAndKeepConsumingUntilShutdown()] and follow 
        uk.co.autotrader.forge.service.serialisation.ForgeSerialiser~~
    * ~~Make application consume from input topic and produce to output topic~~
    * Make application consume from table topic, join them and produce to output topic
    * Manually produce to table topic and see output change accordingly
    * See what happens when application is killed, topics are updated the application is restarted
    * Does table increase indefinitely or will it discard older entries if they are not updated?
    * What is that about cleaning local state in https://github.com/confluentinc/examples/blob/3.3.0-post/kafka-streams/src/main/java/io/confluent/examples/streams/GlobalKTablesExample.java?
    * Try EmbeddedKafka to test.
 