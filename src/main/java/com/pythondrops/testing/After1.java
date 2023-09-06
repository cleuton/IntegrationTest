package com.pythondrops.testing;

import java.sql.SQLException;
import java.util.UUID;

public class After1 {

    private DatabaseWrapper databaseWrapper;

    public After1(DatabaseWrapper databaseWrapper) {
        this.databaseWrapper = databaseWrapper;
    }

    /**
     * Store a message into MESSAGE table.
     * User must be a member of the channel (USER_CHANNEL) and not be suspended.
     * Channel cannot be suspendend.
     *
     * All parameters are mandatory:
     *
     * @param userId String UUID of the message's author
     * @param channelId String Channel UUID
     * @param title String Message title
     * @param content String Message content
     *
     * @return Message UUID String - The uuid of the new message
     *
     * @throws SQLException - In case of database problems
     * @throws UserNotAllowedException - In case of user not being a member or is suspended
     * @throws ChannelNotAvailableException - In case of a suspended channel
     */
    public UUID postMessageToChannel(UUID userId, UUID channelId, String title, String content)
                throws SQLException, UserNotAllowedException, ChannelNotAvailableException {

        checkArgs(userId, channelId, title, content);

        User user = databaseWrapper.getUser(userId);

        if (user == null) {
            throw new UserNotAllowedException("User does not exist or is suspended");
        } else if (!user.channels.contains(channelId)) {
            throw new ChannelNotAvailableException("User is not in the channel");
        }

        Message message = new Message();
        message.channelId = channelId;
        message.author = userId;
        message.title = title;
        message.content = content;

        return databaseWrapper.postMessage(message);
    }

    private void checkArgs(Object... args) {
        for(Object arg : args) {
            if (arg instanceof String) {
                String argString = (String) arg;
                if (argString == null || argString.isEmpty()) {
                    throw new IllegalArgumentException("Missing argument(s)");
                }
            } else if (arg == null) {
                throw new IllegalArgumentException("Missing argument(s)");
            }
        }
    }
}
