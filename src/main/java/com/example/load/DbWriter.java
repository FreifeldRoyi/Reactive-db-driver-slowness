package com.example.load;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class DbWriter
{
	@ConfigProperty(name = "my.table-name") String tableName;
	@ConfigProperty(name = "quarkus.flyway.schemas") String schemasName;
	//	@Inject MySQLPool client;
	@Inject PgPool client;

	public Uni<RowSet<Row>> insert(Stream<Record> gwRecords)
	{
		// MYSQL statement
		// var sql = "INSERT INTO " + this.schemasName + "."+this.tableName + " (id, eventName) VALUES (?,?)";

		// POSTGRES statement
		var sql = "INSERT INTO " + this.schemasName + "." + this.tableName + " (id, eventName) VALUES ($1,$2)";


		var tuples = gwRecords.map(record -> {
			List<Object> fields = Arrays.asList(record.getId(), record.getEventName());
			return Tuple.wrap(fields);
		}).collect(Collectors.toList());
		return this.client.preparedQuery(sql).executeBatch(tuples);
	}
}
