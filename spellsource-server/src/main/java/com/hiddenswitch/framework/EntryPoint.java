package com.hiddenswitch.framework;

import com.google.devtools.common.options.OptionsParser;
import com.hiddenswitch.framework.impl.Options;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;

public class EntryPoint {
	public static void main(String[] args) {
		var optionsParser = OptionsParser.newOptionsParser(Options.class);
		optionsParser.parseAndExitUponError(args);
		var options = optionsParser.getOptions(Options.class);
		if (options.migrate) {
			try {
				Environment.migrate().toCompletionStage().toCompletableFuture().join();
			} catch (Throwable t) {
				t.printStackTrace();
				System.exit(1);
				return;
			}

			System.exit(0);
			return;
		}

		if (!options.writeDefaultConfig.trim().isEmpty()) {
			var configuration = Environment.defaultConfiguration();
			var path = options.writeDefaultConfig;
			write(configuration, path);
		}

		if (!options.writeCurrentConfig.trim().isEmpty()) {
			var configuration = Environment.getConfiguration();
			var path = options.writeCurrentConfig;
			write(configuration, path);
		}

		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();

		var application = new Application();
		application.deploy()
				.onFailure(t -> {
					System.exit(1);
				});
	}

	private static void write(Hiddenswitch.ServerConfiguration configuration, String path) {
		var vertx = Vertx.vertx();
		vertx.fileSystem().writeFile(path, Json.encodeToBuffer(configuration))
				.compose(v -> vertx.close())
				.toCompletionStage()
				.toCompletableFuture()
				.join();
	}
}