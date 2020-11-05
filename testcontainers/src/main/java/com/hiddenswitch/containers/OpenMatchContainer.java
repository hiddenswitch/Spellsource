package com.hiddenswitch.containers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.github.dockerjava.api.command.InspectContainerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;

public class OpenMatchContainer extends GenericContainer<OpenMatchContainer> {
	private static final int OPENMATCH_MINIMATCH_PORT = 50509;
	private static Logger LOGGER = LoggerFactory.getLogger(OpenMatchContainer.class);

	private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()
			.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final String OVERRIDE_PATH = "/app/config/override/matchmaker_config_override.yaml";

	protected String getOpenMatchServiceName() {
		return "minimatch";
	}

	protected boolean isAllInOne() {
		return true;
	}

	public OpenMatchContainer() {
		super();
		withExposedPorts(OPENMATCH_MINIMATCH_PORT);
		if (!isAllInOne()) {
			setDockerImageName("gcr.io/open-match-public-images/openmatch-" + getOpenMatchServiceName() + ":latest");
			return;
		}
		ImageFromDockerfile image = new ImageFromDockerfile();

		for (String path : new String[]{
				"Dockerfile", "app/config/default/matchmaker_config_default.yaml", "app/config/override/matchmaker_config_override.yaml"
		}) {
			image.withFileFromClasspath(path, "openmatch/" + path);
		}
		setImage(image);
		waitingFor(Wait.forLogMessage(".*Serving gRPC:.*", 1));
	}

	public OpenMatchOverrideConfig getConfig() {
		return copyFileFromContainer(OVERRIDE_PATH, t -> MAPPER.readValue(t, OpenMatchOverrideConfig.class));
	}

	public void setConfig(OpenMatchOverrideConfig value) {
		try {
			copyFileToContainer(Transferable.of(MAPPER.writeValueAsBytes(value)), OVERRIDE_PATH);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public int getPort() {
		return getMappedPort(OPENMATCH_MINIMATCH_PORT);
	}

	public static class OpenMatchOverrideConfig {

		private Api api;
		private Redis redis;

		public Api getApi() {
			return api;
		}

		public OpenMatchOverrideConfig setApi(Api api) {
			this.api = api;
			return this;
		}

		public Redis getRedis() {
			return redis;
		}

		public OpenMatchOverrideConfig setRedis(Redis redis) {
			this.redis = redis;
			return this;
		}

		public static class Api {
			private ApiTuple minimatch;
			private ApiTuple backend;
			private ApiTuple frontend;
			private ApiTuple query;
			private ApiTuple synchronizer;

			public ApiTuple getBackend() {
				return backend;
			}

			public Api setBackend(ApiTuple backend) {
				this.backend = backend;
				return this;
			}

			public ApiTuple getFrontend() {
				return frontend;
			}

			public Api setFrontend(ApiTuple frontend) {
				this.frontend = frontend;
				return this;
			}

			public ApiTuple getQuery() {
				return query;
			}

			public Api setQuery(ApiTuple query) {
				this.query = query;
				return this;
			}

			public ApiTuple getSynchronizer() {
				return synchronizer;
			}

			public Api setSynchronizer(ApiTuple synchronizer) {
				this.synchronizer = synchronizer;
				return this;
			}

			public ApiTuple getMinimatch() {
				return minimatch;
			}

			public Api setMinimatch(ApiTuple minimatch) {
				this.minimatch = minimatch;
				return this;
			}
		}

		public static class ApiTuple {
			private String hostname;
			private int grpcport;

			public String getHostname() {
				return hostname;
			}

			public ApiTuple setHostname(String hostname) {
				this.hostname = hostname;
				return this;
			}

			public int getGrpcport() {
				return grpcport;
			}

			public ApiTuple setGrpcport(int grpcport) {
				this.grpcport = grpcport;
				return this;
			}
		}

		public static class Redis {
			private String hostname;
			private int port;
			private String user;

			public String getHostname() {
				return hostname;
			}

			public Redis setHostname(String hostname) {
				this.hostname = hostname;
				return this;
			}

			public int getPort() {
				return port;
			}

			public Redis setPort(int port) {
				this.port = port;
				return this;
			}

			public String getUser() {
				return user;
			}

			public Redis setUser(String user) {
				this.user = user;
				return this;
			}
		}
	}

	@Override
	protected void containerIsStarted(InspectContainerResponse containerInfo) {
		super.containerIsStarted(containerInfo);
		Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
		followOutput(logConsumer);
	}

}