package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.models.MatchmakingRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.Serializable;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class MatchmakingQueueEntry implements Serializable, ClusterSerializable {
	public enum Command {
		ENQUEUE,
		CANCEL
	}

	private Command command;
	private MatchmakingRequest request;
	private String userId;


	@Override
	public void writeToBuffer(Buffer buffer) {
		json(this).writeToBuffer(buffer);
	}

	@Override
	public int readFromBuffer(int pos, Buffer buffer) {
		JsonObject obj = new JsonObject();
		int newPos = obj.readFromBuffer(pos, buffer);
		MatchmakingQueueEntry inst = obj.mapTo(MatchmakingQueueEntry.class);
		this.setCommand(inst.getCommand());
		this.setRequest(inst.getRequest());
		this.setUserId(inst.getUserId());
		return newPos;
	}


	public Command getCommand() {
		return command;
	}

	public MatchmakingQueueEntry setCommand(Command command) {
		this.command = command;
		return this;
	}

	public MatchmakingRequest getRequest() {
		return request;
	}

	public MatchmakingQueueEntry setRequest(MatchmakingRequest request) {
		this.request = request;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public MatchmakingQueueEntry setUserId(String userId) {
		this.userId = userId;
		return this;
	}
}
