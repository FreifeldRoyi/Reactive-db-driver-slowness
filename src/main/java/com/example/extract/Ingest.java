package com.example.extract;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecords;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class Ingest
{
	@ConfigProperty(name = "kafka.group.id") String consumerGroup;

	@Inject KafkaConsumer<String, MyKafkaMessage> consumer;

	public Multi<TopicPartition> partitions(String topic)
	{
		return this.consumer.partitionsFor(topic)
							.onItem()
							.transformToMulti(infos -> Multi.createFrom().iterable(infos))
							.map(info -> new TopicPartition(topic, info.getPartition()));
	}


	public Uni<Void> assignPartition(TopicPartition partition)
	{
		return this.consumer.assign(partition);
	}

	public Uni<Void> setOffset(TopicPartition partition)
	{
		return this.currentOffset(partition)
				   .onItem()
				   .transformToUni(position -> this.consumer.seek(partition, position));
	}

	private Uni<Long> currentOffset(TopicPartition partition)
	{
		var commitedUni = this.consumer.committed(partition);
		var positionUni = this.consumer.position(partition);

		return Uni.combine()
				  .all()
				  .unis(commitedUni, positionUni)
				  .combinedWith((offset, position) -> Optional.ofNullable(offset).map(unused -> position).orElse(0L));
	}

	public Uni<Tuple2<Integer, Tuple2<TopicPartition, Long>>> withEndOffset(TopicPartition partition)
	{
		var key = Uni.createFrom().item(partition.getPartition());
		var value = Uni.combine()
					   .all()
					   .unis(Uni.createFrom().item(partition), this.consumer.endOffsets(partition))
					   .asTuple();
		return Uni.combine().all().unis(key, value).asTuple();
	}

	public Uni<KafkaConsumerRecords<String, MyKafkaMessage>> fetchMessages()
	{
		return this.consumer.poll(Duration.ofSeconds(2));
	}

	public Uni<Void> commitOffsets()
	{
		return this.consumer.commit();
	}
}
