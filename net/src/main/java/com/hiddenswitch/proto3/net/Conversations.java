package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.util.ConversationRecord;
import com.hiddenswitch.proto3.net.impl.util.MessageRecord;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import static com.hiddenswitch.proto3.net.util.QuickJson.fromJson;
import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static com.hiddenswitch.proto3.net.util.QuickJson.toJson;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by weller on 6/13/17.
 */
public class Conversations {

    private static final String CONVERSATIONS = "conversations";
    private final static char ID_SEPARATOR = '$';

    public static String getId(String id1, String id2) {
        return id1.compareTo(id2)>0 ? id1 + ID_SEPARATOR + id2 : id2 + ID_SEPARATOR + id1;
    }

    public static MessageRecord insertMessage(MongoClient mongo, String originId, String authorDisplayName,
                                              String destId, String text)
            throws SuspendExecution, InterruptedException{

        //get conversation
        ConversationRecord conversation = getCreateConversation(mongo, originId, destId);

        //create message record
        MessageRecord messageRecord = new MessageRecord(originId, authorDisplayName, text, System.currentTimeMillis());

        //insert message record
        MongoClientUpdateResult result = awaitResult(h -> mongo.updateCollection(CONVERSATIONS,
                json("_id", conversation.getId()),
                json("$push", json("messages", json(messageRecord))), h));

        return messageRecord;
    }

    public static ConversationRecord getCreateConversation(MongoClient mongo, String player1, String player2)
            throws SuspendExecution, InterruptedException{
        String conversationId = getId(player1, player2);
        JsonObject result = awaitResult(h -> mongo.findOne(CONVERSATIONS, json("_id", conversationId), json(), h));

        ConversationRecord conversation;
        if (result != null) {
            conversation = fromJson(result, ConversationRecord.class);
        } else {
            final ConversationRecord newConversation = new ConversationRecord(conversationId);
            String _ = awaitResult(h-> mongo.insert(CONVERSATIONS, toJson(newConversation), h));
            conversation = newConversation;
        }
        return conversation;
    }
}
