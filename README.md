# Background

This is a simple code I created to show the difference between MySQL reactive client and Postgres reactive client
Postgres shows much better performance, and I'm not sure if it's a db/db-config issue, code tweeking issue, or client issue.

In order to change between dbs, just switch between comment and code in:
- application.properties
- DbWriter (Don't forget to switch the sql statements)

# Preparations

- Start dockers with `docker-compose up -d`
- Generate records with `jbang src/main/java/GenerateKafkaMessages.java`. It will loop forever so make sure to stop it after a while



You can use IDE or a simple `mvn clean package`
