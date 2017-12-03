package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.impl.util.ConversationRecord;
import com.hiddenswitch.spellsource.impl.util.MessageRecord;
import com.hiddenswitch.spellsource.util.QuickJson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;

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
                QuickJson.json("_id", conversation.getId()),
                QuickJson.json("$push", QuickJson.json("messages", QuickJson.json(messageRecord))), h));

        return messageRecord;
    }

    public static ConversationRecord getCreateConversation(MongoClient mongo, String player1, String player2)
            throws SuspendExecution, InterruptedException{
        String conversationId = getId(player1, player2);
        JsonObject result = awaitResult(h -> mongo.findOne(CONVERSATIONS, QuickJson.json("_id", conversationId), QuickJson.json(), h));

        ConversationRecord conversation;
        if (result != null) {
            conversation = QuickJson.fromJson(result, ConversationRecord.class);
        } else {
            final ConversationRecord newConversation = new ConversationRecord(conversationId);
            String ignored = awaitResult(h-> mongo.insert(CONVERSATIONS, QuickJson.toJson(newConversation), h));
            conversation = newConversation;
        }
        return conversation;
    }
}
