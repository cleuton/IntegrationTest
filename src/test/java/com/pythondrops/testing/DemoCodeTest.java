package com.pythondrops.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class DemoCodeTest {

    @Test
    void postMessageToChannelCheckAllNullArguments() {
        System.out.println("Testing all null arguments");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(null, null, null, null);
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageToChannelCheckAllZeroLengthArguments() {
        System.out.println("Testing all zero length arguments");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(UUID.fromString(""), UUID.fromString(""), "", "");
        });

        // Then:

        String expectedMessage = "Invalid UUID string";
        String actualMessage = exception.getMessage();

        System.out.println("MESSAGE: " + actualMessage);

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageToChannelCheckUserIdNullArgument() {
        System.out.println("Testing userId null argument");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(null, UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "CONTENT");
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void postMessageToChannelCheckChannelIdNullArgument() {
        System.out.println("Testing channelId null argument");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), null, "TITLE", "CONTENT");
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    void postMessageToChannelCheckTitledNullArgument() {
        System.out.println("Testing title null argument");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), null, "CONTENT");
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageToChannelCheckTitledZeroLengthArgument() {
        System.out.println("Testing title zero length argument");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "", "CONTENT");
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageToChannelCheckContentdNullArgument() {
        System.out.println("Testing content null argument");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", null);
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageToChannelCheckContentdZeroLengthArgument() {
        System.out.println("Testing content zero length argument");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "");
        });

        // Then:

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageWithNonExistentUser() throws SQLException {
        System.out.println("Testing non existent user");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        when(dbWrapper.getUser(any())).thenReturn(null);

        // When:

        Exception exception = assertThrows(UserNotAllowedException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "Message content");
        });

        // Then:

        String expectedMessage = "User does not exist or is suspended";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageWithUserNotInChannel() throws SQLException {
        System.out.println("Testing user not in channel");

        // Given:

        DatabaseWrapper dbWrapper = mock(DatabaseWrapper.class);
        DemoCode dc = new DemoCode(dbWrapper);

        User user = new User();
        user.id = UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002");
        user.suspended = false;
        user.piiContentLink = "blablabla";
        user.channels = new HashSet<>();
        user.channels.add(UUID.randomUUID());

        when(dbWrapper.getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"))).thenReturn(user);

        // When:

        Exception exception = assertThrows(ChannelNotAvailableException.class, () -> {
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "Message content");
        });

        // Then:

        String expectedMessage = "User is not in the channel";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void postMessageCheckResult() throws SQLException, UserNotAllowedException, ChannelNotAvailableException {
        System.out.println("Testing a complete post message");

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
        when(rsUser.getString( "CHANNEL_ID" )).thenReturn("347047f3-4bf4-11ee-a0e1-0242ac110002");
        when(rsUser.getBoolean( "CHANNEL_SUSPENDED" )).thenReturn(false);

        PreparedStatement psInsert = mock(PreparedStatement.class);
        when(connection.prepareStatement("INSERT INTO MESSAGE (AUTHOR, TITLE, CONTENT, CHANNEL_ID, CREATED_TIME) VALUES (?, ?, ?, ?, NOW());")).thenReturn(psInsert);
        PreparedStatement psQueryMessage = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT BIN_TO_UUID(ID) AS MESSAGE_ID, MAX(CREATED_TIME) AS CREATED FROM MESSAGE WHERE AUTHOR=? GROUP BY ID;")).thenReturn(psQueryMessage);
        ResultSet rsMessage = mock(ResultSet.class);
        when(psQueryMessage.executeQuery()).thenReturn(rsMessage);
        when(rsMessage.getString("MESSAGE_ID")).thenReturn("347047f3-4bf4-11ee-a0e1-0245ac110002");
        when(rsMessage.next()).thenReturn(true);
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

        Message expectedMessage = new Message();
        expectedMessage.channelId = UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002");
        expectedMessage.title = "TITLE";
        expectedMessage.content = "Message content";
        expectedMessage.author = UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002");

        // When:

        UUID messageId = dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "Message content");

        // Then:

        assertEquals(messageId, UUID.fromString("347047f3-4bf4-11ee-a0e1-0245ac110002"));
        verify(spyDbWrapper).getUser(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"));
        verify(spyDbWrapper).postMessage(expectedMessage);
    }

}