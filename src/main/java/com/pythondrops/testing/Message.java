package com.pythondrops.testing;

import java.util.Date;
import java.util.UUID;

public class Message {
    public UUID id;
    public UUID author;
    public String title;
    public String content;
    public UUID channelId;
    public Date createdTime;

    @Override
    public String toString() {
        return "Message{" + "id=" + id + ", author=" + author + ", title='" + title + '\'' + ", content='" + content + '\'' + ", channelId=" + channelId + ", createdTime=" + createdTime + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            Message m = (Message) obj;
            if (m.id != null) {
                return this.id.equals(m.id);
            } else {
                return
                  this.title.equals(m.title) &&
                    this.author.equals(m.author) &&
                    this.content.equals(m.content) &&
                    this.channelId.equals(m.channelId);
            }
        } else {
            return false;
        }
    }
}
