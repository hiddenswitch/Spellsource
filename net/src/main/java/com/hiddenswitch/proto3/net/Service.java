package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hiddenswitch.proto3.net.util.LocalMongo;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sync.SyncVerticle;

import java.io.File;

public abstract class Service<T extends Service<T>> extends SyncVerticle {
	private static Logger logger = LoggerFactory.getLogger(Service.class);
	private MongoClient mongo;
	private static boolean embeddedConfigured;
	private static LocalMongo localMongoServer;


	@SuppressWarnings("unchecked")
	public T withProductionConfiguration() {
		// TODO: Set production credentials for Mongo
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withEmbeddedConfiguration(File dbFile) {
		createdEmbeddedServices(dbFile);

		logger.info("Setting default services...");

		logger.info("Default services ready in embedded configuration.");
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withEmbeddedConfiguration() {
		return withEmbeddedConfiguration(null);
	}

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		if (this.mongo == null
				&& embeddedConfigured) {
			this.mongo = MongoClient.createShared(vertx, localMongoServer.getConfig());
		}
	}

	private synchronized static void createdEmbeddedServices(File dbFile) {
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
		logger.info("Stack initialized, embedding configured.");
		embeddedConfigured = true;
	}


	public MongoClient getMongo() {
		return mongo;
	}

	public void setMongo(MongoClient mongo) {
		this.mongo = mongo;
	}
}
