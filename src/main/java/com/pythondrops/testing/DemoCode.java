package com.pythondrops.testing;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

public class DemoCode {

    private DatabaseWrapper databaseWrapper;

    public DemoCode(DatabaseWrapper databaseWrapper) {
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

}
