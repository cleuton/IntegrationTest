package com.pythondrops.testing;

import java.util.Set;
import java.util.UUID;

public class User {
    public UUID id;
    String piiContentLink;
    boolean suspended;

    public Set<UUID> channels;

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", piiContentLink='" + piiContentLink + '\'' + ", suspended=" + suspended + ", channels=" + channels + '}';
    }
}
