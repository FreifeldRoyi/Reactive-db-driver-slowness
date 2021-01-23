package com.example.extract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyKafkaMessage
{
	public String eventName;

	public MyKafkaMessage()
	{ }

	public MyKafkaMessage(
			String eventName
	)
	{
		this.eventName = eventName;
	}
}
