package com.pythondrops.testing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class DatabaseWrapperTest {

    @Test
    void testSuspendedUser() throws SQLException {
        System.out.println("Testing a suspende user");

        // Given:

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        PreparedStatement psUser = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT BIN_TO_UUID(U.ID) AS USER_ID, U.PII_CONTENT_LINK, U.SUSPENDED, BIN_TO_UUID(UC.CHANNEL_ID) AS CHANNEL_ID, UC.SUSPENDED as CHANNEL_SUSPENDED, C.HIDDEN FROM USER U INNER JOIN USER_CHANNEL UC ON U.ID = UC.USER_ID  INNER JOIN CHANNEL C ON UC.CHANNEL_ID = C.ID WHERE U.ID = ?"))
          .thenReturn(psUser);

        ResultSet rsUser = mock(ResultSet.class);
        when(rsUser.getBoolean("SUSPENDED")).thenReturn(true);  // User is suspended
        when(rsUser.getString( "USER_ID" )).thenReturn("162b27bf-4c0b-11ee-a0e1-0242ac110002");
        when(rsUser.getString( "pii_content_link" )).thenReturn("content-link");
        when(rsUser.getString( "CHANNEL_ID" )).thenReturn("347047f3-4bf4-11ee-a0e1-0242ac110002");
        when(rsUser.getBoolean( "CHANNEL_SUSPENDED" )).thenReturn(false);

        AtomicInteger first = new AtomicInteger();
        when(rsUser.next()).thenAnswer(x -> {
            if (first.getAndIncrement() > 0) {
                return false;
            }
            return true;
        });
        when(psUser.executeQuery()).thenReturn(rsUser);

        DatabaseWrapper dbWrapper = new DatabaseWrapper(dataSource);
        DatabaseWrapper spyDbWrapper = spy(dbWrapper);
        DemoCode dc = new DemoCode(spyDbWrapper);

        // When:

        Exception exception = assertThrows(UserNotAllowedException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "Message content");
        });

        // Then:

        String expectedMessage = "User does not exist or is suspended";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(spyDbWrapper).getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"));
    }

    @Test
    public void testingAhiddenChannel() throws SQLException {
        System.out.println("Testing a hidden channel");

        // Given:

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        PreparedStatement psUser = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT BIN_TO_UUID(U.ID) AS USER_ID, U.PII_CONTENT_LINK, U.SUSPENDED, BIN_TO_UUID(UC.CHANNEL_ID) AS CHANNEL_ID, UC.SUSPENDED as CHANNEL_SUSPENDED, C.HIDDEN FROM USER U INNER JOIN USER_CHANNEL UC ON U.ID = UC.USER_ID  INNER JOIN CHANNEL C ON UC.CHANNEL_ID = C.ID WHERE U.ID = ?"))
          .thenReturn(psUser);

        ResultSet rsUser = mock(ResultSet.class);
        when(rsUser.getBoolean("SUSPENDED")).thenReturn(false);
        when(rsUser.getString( "USER_ID" )).thenReturn("162b27bf-4c0b-11ee-a0e1-0242ac110002");
        when(rsUser.getString( "pii_content_link" )).thenReturn("content-link");
        AtomicInteger firstChannelId = new AtomicInteger();
        when(rsUser.getString( "CHANNEL_ID" )).thenAnswer(x -> {
            if (firstChannelId.getAndIncrement() > 0) {
                return "347047f3-4bf4-11ee-a0e1-e242ac110003";  // Second channel -> Should not be on the list
            }
            return "347047f3-4bf4-11ee-a0e1-0242ac110002";  // first channel
        });

        AtomicInteger firstChannelHidden = new AtomicInteger();
        when(rsUser.getBoolean( "HIDDEN" )).thenAnswer(x -> {
            if (firstChannelHidden.getAndIncrement() > 0) {
                return true;
            }
            return false;
        });

        when(rsUser.getBoolean( "CHANNEL_SUSPENDED" )).thenReturn(false);

        AtomicInteger first = new AtomicInteger();
        when(rsUser.next()).thenAnswer(x -> {
            if (first.getAndIncrement() > 1) {
                return false;
            }
            return true;
        });
        when(psUser.executeQuery()).thenReturn(rsUser);

        DatabaseWrapper dbWrapper = new DatabaseWrapper(dataSource);
        DatabaseWrapper spyDbWrapper = spy(dbWrapper);

        Message expectedMessage = new Message();
        expectedMessage.channelId = UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002");
        expectedMessage.title = "TITLE";
        expectedMessage.content = "Message content";
        expectedMessage.author = UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002");

        // When:

        User user = spyDbWrapper.getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"));

        // Then:

        assertTrue(user.channels.contains(UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002")));
        assertFalse(user.channels.contains(UUID.fromString("347047f3-4bf4-11ee-a0e1-e242ac110003")));
        verify(spyDbWrapper).getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"));
    }

    @Test
    public void testingUserIsSuspendedOnAchannel() throws SQLException {
        System.out.println("Testing user is suspended on a channel");

        // Given:

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        PreparedStatement psUser = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT BIN_TO_UUID(U.ID) AS USER_ID, U.PII_CONTENT_LINK, U.SUSPENDED, BIN_TO_UUID(UC.CHANNEL_ID) AS CHANNEL_ID, UC.SUSPENDED as CHANNEL_SUSPENDED, C.HIDDEN FROM USER U INNER JOIN USER_CHANNEL UC ON U.ID = UC.USER_ID  INNER JOIN CHANNEL C ON UC.CHANNEL_ID = C.ID WHERE U.ID = ?"))
          .thenReturn(psUser);

        ResultSet rsUser = mock(ResultSet.class);
        when(rsUser.getBoolean("SUSPENDED")).thenReturn(false);
        when(rsUser.getString( "USER_ID" )).thenReturn("162b27bf-4c0b-11ee-a0e1-0242ac110002");
        when(rsUser.getString( "pii_content_link" )).thenReturn("content-link");
        AtomicInteger firstChannelId = new AtomicInteger();
        when(rsUser.getString( "CHANNEL_ID" )).thenAnswer(x -> {
            if (firstChannelId.getAndIncrement() > 0) {
                return "347047f3-4bf4-11ee-a0e1-e242ac110003";  // Second channel -> Should not be on the list - user is suspended
            }
            return "347047f3-4bf4-11ee-a0e1-0242ac110002";  // first channel
        });

        AtomicInteger firstChannelSuspended = new AtomicInteger();
        when(rsUser.getBoolean( "CHANNEL_SUSPENDED" )).thenAnswer(x -> {
            if (firstChannelSuspended.getAndIncrement() > 0) {
                return true;
            }
            return false;
        });


        AtomicInteger first = new AtomicInteger();
        when(rsUser.next()).thenAnswer(x -> {
            if (first.getAndIncrement() > 1) {
                return false;
            }
            return true;
        });
        when(psUser.executeQuery()).thenReturn(rsUser);

        DatabaseWrapper dbWrapper = new DatabaseWrapper(dataSource);
        DatabaseWrapper spyDbWrapper = spy(dbWrapper);

        Message expectedMessage = new Message();
        expectedMessage.channelId = UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002");
        expectedMessage.title = "TITLE";
        expectedMessage.content = "Message content";
        expectedMessage.author = UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002");

        // When:

        User user = spyDbWrapper.getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"));

        // Then:

        assertTrue(user.channels.contains(UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002")));
        assertFalse(user.channels.contains(UUID.fromString("347047f3-4bf4-11ee-a0e1-e242ac110003")));
        verify(spyDbWrapper).getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"));
    }
}