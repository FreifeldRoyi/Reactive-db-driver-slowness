package com.example.transform;

import com.example.extract.MyKafkaMessage;
import com.example.load.Record;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class Mapper
{
	public Stream<Record> transform(Stream<ConsumerRecord<String, MyKafkaMessage>> messages)
	{
		return messages.map(message -> new Record(UUID.randomUUID().toString(), message.value().eventName));
	}
}
