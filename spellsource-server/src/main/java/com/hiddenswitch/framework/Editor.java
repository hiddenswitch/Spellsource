package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.ServerGameContext;
import io.vertx.core.Closeable;
import io.vertx.core.Promise;
import net.demilich.metastone.game.GameContext;

public class Editor {
	public static boolean isEditable(GameContext target) {
		return false;
	}

	public static Closeable enableEditing(ServerGameContext serverGameContext) {
		return Promise::complete;
	}
}
