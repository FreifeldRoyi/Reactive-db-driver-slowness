package com.example.extract;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class KafkaProviders
{
	@Inject
	@Named("default-kafka-broker")
	Map<String, Object> config;

	@Inject Vertx vertx;


	@Produces
	public KafkaConsumer<String, MyKafkaMessage> kafkaConsumer()
	{
		var configMap = this.config.entrySet()
								   .stream()
								   .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));

		return KafkaConsumer.create(this.vertx, configMap);
	}
}
