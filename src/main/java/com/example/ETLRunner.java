package com.example;

import com.example.extract.MyKafkaMessage;
import com.example.extract.Ingest;
import com.example.load.DbWriter;
import com.example.transform.Mapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.kafka.client.common.TopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class ETLRunner
{
	@ConfigProperty(name = "kafka.topic") String topic;
	@Inject Ingest extract;
	@Inject Mapper transform;
	@Inject DbWriter load;

	public Uni<Void> run()
	{
		return this.extract.partitions(this.topic)
						   .call(this.extract::assignPartition)
						   .call(this.extract::setOffset)
						   .onItem()
						   .transformToUniAndMerge(this.extract::withEndOffset)
						   .collectItems()
						   .asMap(Tuple2::getItem1, Tuple2::getItem2)
						   .onItem()
						   .transformToUni(this::poll);

	}

	private Uni<Void> poll(Map<Integer, Tuple2<TopicPartition, Long>> partitions)
	{
		var messagesUni = this.extract.fetchMessages();
		return messagesUni.onItem().transformToUni(records -> {
			ConsumerRecords<String, MyKafkaMessage> nativeRecords = records.getDelegate().records(); // https://github.com/smallrye/smallrye-reactive-utils/issues/236
			var completeRun = this.isLastBatch(partitions, nativeRecords);
			var messagesStream = StreamSupport.stream(nativeRecords.records(this.topic).spliterator(), false);
			var recordStream = this.transform.transform(messagesStream);
			var rowsUni = this.load.insert(recordStream).onItem().call(unused -> this.extract.commitOffsets());

			if (!completeRun)
			{
				return rowsUni.onItem().transformToUni(unused -> this.poll(partitions));
			}
			else
			{
				return rowsUni.onItem().ignore().andContinueWithNull();
			}
		});
	}

	private Boolean isLastBatch(
			Map<Integer, Tuple2<TopicPartition, Long>> partitionsWithOffset,
			ConsumerRecords<String, MyKafkaMessage> consumerRecords
	)
	{
		return partitionsWithOffset.entrySet().stream().allMatch(partition -> {
			var vertxTopicPartition = partition.getValue().getItem1();
			var topicPartition = new org.apache.kafka.common.TopicPartition(vertxTopicPartition.getTopic(),
																			vertxTopicPartition.getPartition());
			return Optional.of(consumerRecords.records(topicPartition))
						   .filter(Predicate.not(List::isEmpty))
						   .map(partitionMessages -> {
							   var lastMessage = partitionMessages.get(partitionMessages.size() - 1);
							   var endOffset = partitionsWithOffset.get(lastMessage.partition()).getItem2();
							   return lastMessage.offset() >= endOffset;
						   })
						   .orElse(true);
		});
	}
}
