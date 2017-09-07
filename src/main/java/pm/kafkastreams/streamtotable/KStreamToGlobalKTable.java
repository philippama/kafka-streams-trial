package pm.kafkastreams.streamtotable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class KStreamToGlobalKTable {

    private static final String STREAM_INPUT_TOPIC = "streams-join-stream-input";
    private static final String TABLE_INPUT_TOPIC = "streams-join-table-input";
    private static final String OUTPUT_TOPIC = "streams-join-output";
    private static final String STORE_NAME = "table-store";

    public static void main(String[] args) throws Exception {

        final Serde<JsonNode> jsonSerde = createJsonNodeSerde();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-join");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
//        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, jsonSerde.getClass().getName());

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // TODO: how can I make this work?

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, JsonNode> source = builder.stream(Serdes.String(), jsonSerde, STREAM_INPUT_TOPIC);

        source
                .peek((key, value) -> printKeyValue(key, getValueAsString(value), "Input")) // Added this line to log input to console.
//                .peek((key, value) -> printKeyValue(key, value.toString(), "Output")) // Added these two lines to log results to console.
                .to(Serdes.String(), jsonSerde, "streams-join-output");

        final KafkaStreams streams = new KafkaStreams(builder, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        }
        catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static Serde<JsonNode> createJsonNodeSerde() {
        final Serializer<JsonNode> jsonSerialiser = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserialiser = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerialiser, jsonDeserialiser);

        // Test
        String json = "{\"time\":\"0722\",\"station\":\"Crewe\"}";
        JsonNode jsonNode = jsonDeserialiser.deserialize("topic", json.getBytes());
        ((ObjectNode) jsonNode).put("extra", "hi");
        System.out.println("Test: " + String.format("[time=%s, station=%s, extra=%s]", jsonNode.get("time").asText(), jsonNode.get("station").asText(), jsonNode.get("extra").asText()));
        System.out.println("Test: " + getValueAsString(jsonNode));
        return jsonSerde;
    }

    private static String getValueAsString(JsonNode jsonNode) {
        return String.format("[time=%s, station=%s]", jsonNode.get("time").asText(), jsonNode.get("station").asText());
    }

    private static void printKeyValue(String key, String value, String label) {
        System.out.println(LocalTime.now().toString() + " " + label + " key : value - " + key + " : " + value);
    }
}
