package com.hiddenswitch.proto3.net;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hiddenswitch.proto3.net.amazon.AwsStack;
import com.hiddenswitch.proto3.net.amazon.AwsStackConfiguration;
import com.hiddenswitch.proto3.net.util.LocalMongo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.elasticmq.rest.sqs.SQSRestServer;
import org.elasticmq.rest.sqs.SQSRestServerBuilder;

import java.io.File;

public abstract class Service<T extends Service<T>> extends AbstractVerticle {
	private static Logger logger = LoggerFactory.getLogger(Service.class);
	private DynamoDBMapper dynamo;
	private AWSCredentials credentials;
	private AmazonSQSClient queue;
	private MongoClient mongo;
	private static AmazonDynamoDB dynamoDBEmbedded;
	private static SQSRestServer elasticMQ;
	private static boolean embeddedConfigured;
	private static LocalMongo localMongoServer;


	@SuppressWarnings("unchecked")
	public T withProductionConfiguration() {
		setCredentials(getAWSCredentials());
		setDynamo(new DynamoDBMapper(new AmazonDynamoDBClient(getCredentials())));
		setQueue(new AmazonSQSClient(getCredentials()));
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withEmbeddedConfiguration(File dbFile) {
		createdEmbeddedServices(dbFile);

		logger.info("Setting default services...");
		this.credentials = new BasicAWSCredentials("x", "y");
		this.dynamo = new DynamoDBMapper(dynamoDBEmbedded);
		this.queue = new AmazonSQSClient(credentials);
		this.queue.setEndpoint("http://localhost:9324");

		logger.info("Default services ready in embedded configuration.");
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withEmbeddedConfiguration() {
		return withEmbeddedConfiguration(null);
	}

	@Override
	public void start() {
		if (this.mongo == null
				&& embeddedConfigured) {
			this.mongo = MongoClient.createShared(vertx, localMongoServer.getConfig());
		}
	}

	private synchronized static void createdEmbeddedServices(File dbFile) {
		if (elasticMQ == null) {
			logger.info("Starting ElasticMQ...");
			elasticMQ = SQSRestServerBuilder.start();
			elasticMQ.waitUntilStarted();
			logger.info("Started ElasticMQ.");
		} else {
			logger.info("SQS already started.");
		}


		if (dynamoDBEmbedded == null) {
			logger.info("Starting DynamoDB embedded...");
			dynamoDBEmbedded = DynamoDBEmbedded.create(dbFile).amazonDynamoDB();
			logger.info("Started DynamoDB embedded.");
		} else {
			logger.info("DynamoDB already started.");
		}

		if (localMongoServer == null) {
			logger.info("Starting Mongod embedded...");
			localMongoServer = new LocalMongo();
			try {
				localMongoServer.start();
			} catch (Exception e) {
				logger.error("Mongo failed to start.", e);
				return;
			}
			logger.info("Started Mongod embedded.");
		} else {
			logger.info("Mongod already started.");
		}

		if (embeddedConfigured) {
			return;
		}

		logger.info("Configuring stack...");
		AwsStackConfiguration configuration = new AwsStackConfiguration();
		BasicAWSCredentials credentials = new BasicAWSCredentials("x", "y");
		configuration.credentials = credentials;
		configuration.dynamoDBClient = dynamoDBEmbedded;
		configuration.database = new DynamoDBMapper(dynamoDBEmbedded);
		configuration.queue = new AmazonSQSClient(credentials).withEndpoint("http://localhost:9324");
		AwsStack.initializeStack(configuration);
		logger.info("Stack initialized, embedding configured.");
		embeddedConfigured = true;
	}

	private AWSCredentials getAWSCredentials() {
		DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();
		return chain.getCredentials();
	}

	public AWSCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(AWSCredentials credentials) {
		this.credentials = credentials;
	}

	@SuppressWarnings("unchecked")
	public T withCredentials(AWSCredentials credentials) {
		setCredentials(credentials);
		return (T) this;
	}

	public AmazonSQSClient getQueue() {
		return queue;
	}

	public void setQueue(AmazonSQSClient queue) {
		this.queue = queue;
	}

	@SuppressWarnings("unchecked")
	public T withQueue(AmazonSQSClient queue) {
		setQueue(queue);
		return (T) this;
	}

	public DynamoDBMapper getDynamo() {
		return dynamo;
	}

	public void setDynamo(DynamoDBMapper dynamo) {
		this.dynamo = dynamo;
	}

	@SuppressWarnings("unchecked")
	public T withDatabase(DynamoDBMapper database) {
		setDynamo(database);
		return (T) this;
	}

	@Override
	public void stop() {
	}

	public MongoClient getMongo() {
		return mongo;
	}

	public void setMongo(MongoClient mongo) {
		this.mongo = mongo;
	}
}
