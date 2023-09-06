package com.pythondrops.testing;

import java.util.Objects;
import java.util.UUID;

public class Channel {
    public UUID id;
    public String name;
    public int type;
    public boolean hidden;

    @Override
    public String toString() {
        return "Channel{" + "id='" + id.toString() + '\'' + ", name='" + name + '\'' + ", type=" + type + ", hidden=" + hidden + '}';
    }

}
