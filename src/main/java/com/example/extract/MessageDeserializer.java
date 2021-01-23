package com.example.extract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MessageDeserializer extends ObjectMapperDeserializer<MyKafkaMessage>
{
	public MessageDeserializer()
	{
		super(MyKafkaMessage.class);
	}
}
