package com.hiddenswitch.spellsource.impl.util;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by weller on 6/12/17.
 */
public class ConversationRecord extends MongoRecord implements Serializable{

    private List<MessageRecord> messages;

    public ConversationRecord() {
        super();
    }

    public ConversationRecord(String id) {
        super(id);
        this.messages = new ArrayList<>();
    }

    public List<MessageRecord> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConversationRecord that = (ConversationRecord) o;
        return Objects.equal(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), messages);
    }

    public Conversation toConversationDto() {
        return null;
    }
}
