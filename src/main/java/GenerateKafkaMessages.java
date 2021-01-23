///usr/bin/env jbang "$0" "$@" ; exit $? # (1)

//DEPS org.apache.kafka:kafka_2.13:2.7.0


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class GenerateKafkaMessages
{

	public static void main(String[] args) throws InterruptedException
	{
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("client.id", "simple-producer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				  "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		var producer = new KafkaProducer<String, String>(props);
		var record = new ProducerRecord<String, String>("my-topic", "{\"eventName\":\"event\"}");

		while (true)
		{
			producer.send(record);
			Thread.sleep(500);
		}
	}
}
