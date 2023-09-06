package com.pythondrops.testing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

import javax.sql.DataSource;
import javax.xml.crypto.Data;

import com.mysql.cj.jdbc.MysqlDataSource;

public class DatabaseWrapper {

    private DataSource dataSource;

    public DatabaseWrapper() {
        this.dataSource = getDataSource();
    }

    public DatabaseWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private DataSource getDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser("root");
        dataSource.setPassword("my-secret-pw");
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("TESTDB");
        return dataSource;
    }

    private Connection getConnection(DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        return conn;
    }

    /**
     * Get a channel.
     * @param id String Channel UUID
     * @return Channel a channel instance or null if not found
     */
    public Channel getChannel(UUID id) throws SQLException {
        Channel channel = new Channel();
        Connection conn = this.dataSource.getConnection();
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(id.getMostSignificantBits())
          .putLong(id.getLeastSignificantBits());
        PreparedStatement query = conn.prepareStatement("SELECT * FROM CHANNEL WHERE ID = ?");
        query.setBytes(1, uuidBytes);
        ResultSet rs = query.executeQuery();
        if (rs.next()) {
            byte[] channelIdBytes = rs.getBytes( "id" );
            channel.id = UUID.nameUUIDFromBytes( channelIdBytes );
            channel.hidden = rs.getBoolean("hidden");
            channel.name = rs.getString("name");
            return channel;
        } else {
            return null;
        }
    }

    /**
     * Return user data with all channels that he/she subscribe.
     * Note:
     * 1) Suspended users will not be returned;
     * 2) Hidden channels will not be returned;
     * 3) If an user is suspended on a channel, this channel will not be returned;
     * @param id
     * @return
     * @throws SQLException
     */
    public User getUser(UUID id) throws SQLException {
        User user = null;
        Connection conn = this.dataSource.getConnection();
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(id.getMostSignificantBits())
          .putLong(id.getLeastSignificantBits());
        PreparedStatement query = conn.prepareStatement("SELECT BIN_TO_UUID(U.ID) AS USER_ID, U.PII_CONTENT_LINK, U.SUSPENDED, BIN_TO_UUID(UC.CHANNEL_ID) AS CHANNEL_ID, UC.SUSPENDED as CHANNEL_SUSPENDED, C.HIDDEN FROM USER U INNER JOIN USER_CHANNEL UC ON U.ID = UC.USER_ID  INNER JOIN CHANNEL C ON UC.CHANNEL_ID = C.ID WHERE U.ID = ?");
        query.setBytes(1, uuidBytes);
        ResultSet rs = query.executeQuery();

        while (rs.next()) {
            if (user == null) {
                boolean userSuspended = rs.getBoolean("SUSPENDED");
                if (userSuspended) {
                    break;
                }
                user = new User();
                user.id = UUID.fromString(rs.getString( "USER_ID" ));
                user.suspended = false; // User is not suspended
                user.piiContentLink = rs.getString("pii_content_link");
                user.channels = new HashSet<UUID>();
            }
            boolean channelIsHidden = rs.getBoolean("HIDDEN");
            if (channelIsHidden) {
                continue;
            }
            UUID channelId = UUID.fromString(rs.getString("CHANNEL_ID"));
            boolean channelSuspended = rs.getBoolean("CHANNEL_SUSPENDED");
            if (!channelSuspended) {
                user.channels.add(channelId);
            }
        }

        return user;
    }

    public UUID postMessage(Message message) throws SQLException {
        Connection conn = this.dataSource.getConnection();
        byte[] authorUuidBytes = new byte[16];
        ByteBuffer.wrap(authorUuidBytes)
          .order(ByteOrder.BIG_ENDIAN)
          .putLong(message.author.getMostSignificantBits())
          .putLong(message.author.getLeastSignificantBits());
        PreparedStatement query = conn.prepareStatement("INSERT INTO MESSAGE (AUTHOR, TITLE, CONTENT, CHANNEL_ID, CREATED_TIME) VALUES (?, ?, ?, ?, NOW());");
        query.setBytes(1, authorUuidBytes);
        query.setString(2, message.title);
        query.setString(3, message.content);
        byte[] channelUuidBytes = new byte[16];
        ByteBuffer.wrap(channelUuidBytes)
          .order(ByteOrder.BIG_ENDIAN)
          .putLong(message.channelId.getMostSignificantBits())
          .putLong(message.channelId.getLeastSignificantBits());
        query.setBytes(4, channelUuidBytes);
        query.execute();

        PreparedStatement query2 = conn.prepareStatement("SELECT BIN_TO_UUID(ID) AS MESSAGE_ID, MAX(CREATED_TIME) AS CREATED FROM MESSAGE WHERE AUTHOR=? GROUP BY ID;");
        query2.setBytes(1, authorUuidBytes);
        ResultSet rs = query2.executeQuery();
        UUID messageId = null;
        if (rs.next()) {
            messageId = UUID.fromString(rs.getString( "MESSAGE_ID" ));
        }
        return messageId;
    }


}
