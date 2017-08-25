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
< /Users/philippa.main/workspace_sandbox/kafka-streams-trial/src/test/resources/pm/kafkastreams/examples/WordCountDemoInput.txt
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

## To do:
* Try out WordCountDemo
* Write application to consume JSON from streams topic, read something into a GlobalKTable from another topic and join the stream with the table
    * Make application consume from input topic and produce to output topic
    * Make application consume from table topic and produce to output topic
    * Manually produce to table topic and see output change accordingly
    * See what happens when application is killed, topics are updated the application is restarted
    * Does table increase indefinitely or will it discard older entries if they are not updated?
 