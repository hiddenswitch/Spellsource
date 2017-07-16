package com.hiddenswitch.spellsource.impl.util;

import com.google.common.base.Objects;
import com.hiddenswitch.spellsource.client.models.Message;

import java.io.Serializable;

/**
 * Created by weller on 6/12/17.
 */
public class MessageRecord implements Serializable {
    private String authorId;
    private String authorDisplayName;
    private String text;
    private long timestamp;


    public MessageRecord() {
        super();
    }

    public MessageRecord(String authorId, String authorDisplayName, String text, long timestamp) {
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MessageRecord setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public MessageRecord setText(String text) {
        this.text = text;
        return this;
    }

    public MessageRecord setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageRecord setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
        return this;
    }

    public Message toMessageDto() {
        return new Message().authorId(this.authorId).text(this.text).timestamp(this.timestamp)
                .authorDisplayName(this.authorDisplayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageRecord that = (MessageRecord) o;
        return timestamp == that.timestamp &&
                Objects.equal(authorId, that.authorId) &&
                Objects.equal(authorDisplayName, that.authorDisplayName) &&
                Objects.equal(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(authorId, authorDisplayName, text, timestamp);
    }
}
