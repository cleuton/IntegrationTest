package com.pythondrops.testing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        String expectedMessage = "Missing argument(s)";
        String actualMessage = exception.getMessage();

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
            dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "");
        });

        // Then:

        String expectedMessage = "User does not exist or is suspended";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}