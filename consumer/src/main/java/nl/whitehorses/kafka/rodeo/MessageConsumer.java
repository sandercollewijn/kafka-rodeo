package nl.whitehorses.kafka.rodeo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.List.*;

public class MessageConsumer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private static final String topicName = "test-topic-rf3";

    KafkaConsumer<String, String> kafkaConsumer;

    public  MessageConsumer(Map<String, Object> propsMap){
        kafkaConsumer = new KafkaConsumer<>(propsMap);
    }


    public void pollKafka(){
        kafkaConsumer.subscribe(List.of(topicName));
        Duration timeOutDuration = Duration.of(100, ChronoUnit.MILLIS);
        while(true){
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(timeOutDuration);
            consumerRecords.forEach((record)->{
                doWorkTaking(100);
                TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                OffsetAndMetadata metadata = new OffsetAndMetadata(record.offset()+1,null);

                kafkaConsumer.commitSync(Map.of(topicPartition, metadata));
                logger.info("JC [{},{},{}] : {} ", topicName, record.partition(), record.offset(), record.value());
            });
        }
    }

    private static void doWorkTaking(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (MessageConsumer messageConsumer = new MessageConsumer(buildConsumerProperties())){
            messageConsumer.pollKafka();
        } catch (Exception e){
            logger.error("Exception polling kafka",e);
        }
    }

    public static Map<String, Object> buildConsumerProperties() {

        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094");
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, "tcgc-rf3");

        //max.poll.interval.ms
        //propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "5000");
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "120000");

        //commit strategy
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return propsMap;
    }

    @Override
    public void close() {
        kafkaConsumer.close(); // always close the consumer for releasing the connections and sockets
    }
}
