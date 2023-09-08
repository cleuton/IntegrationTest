package com.pythondrops.testing;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import com.mysql.cj.jdbc.MysqlDataSource;

@Testcontainers
public class BaseIT {

    private DataSource dataSource;

    MySQLContainer<?> mysql;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        mysql = new MySQLContainer<>("mysql")
          .withUsername("root")
          .withPassword("my-secret-pw")
          .withExposedPorts(3306)
          .withCopyFileToContainer(MountableFile.forClasspathResource("database.sql"), "/docker-entrypoint-initdb.d/schema.sql");

        mysql.start();

        MysqlDataSource mds = new MysqlDataSource();
        mds.setUser("root");
        mds.setPassword("my-secret-pw");
        mds.setServerName("localhost");
        mds.setPort(mysql.getFirstMappedPort().intValue());
        mds.setDatabaseName("TESTDB");

        dataSource = mds;
    }

    @Test
    public void itPostMessageOK() throws SQLException, UserNotAllowedException, ChannelNotAvailableException, ParseException {
        System.out.println("IT post a message with no errors");

        // Given:

        DatabaseWrapper dbWrapper = new DatabaseWrapper(dataSource);
        DemoCode dc = new DemoCode(dbWrapper);

        // When:
        Date postDate = new Date();
        UUID messageId = dc.postMessageToChannel(UUID.fromString("162b27bf-4c0b-11ee-a0e1-0242ac110002"), UUID.fromString("347047f3-4bf4-11ee-a0e1-0242ac110002"), "TITLE", "Message content");

        // Then:

        Connection db = this.dataSource.getConnection();
        PreparedStatement query = db.prepareStatement("SELECT BIN_TO_UUID(ID) AS MESSAGE_ID, BIN_TO_UUID(AUTHOR) AS MESSAGE_AUTHOR, "
          + "BIN_TO_UUID(CHANNEL_ID) AS CHANNEL_ID, TITLE, CONTENT, CREATED_TIME FROM MESSAGE;");
        ResultSet rs = query.executeQuery();
        int count = 0;
        while (rs.next()) {
            count++;
            if (count > 1) {
                fail("There should be only one message");
            }
            String dbMessageId = rs.getString("MESSAGE_ID");
            String dbAuthorId = rs.getString("MESSAGE_AUTHOR");
            String dbChannelId = rs.getString("CHANNEL_ID");
            String dbTitle = rs.getString("TITLE");
            String dbContent = rs.getString("CONTENT");
            String dbDate = rs.getString("CREATED_TIME");
            System.out.println(dbDate);

            SimpleDateFormat inputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            inputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date myDate = inputSDF.parse(dbDate);
            SimpleDateFormat outputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dbConvertedDate = outputSDF.parse(outputSDF.format(myDate));
            System.out.println(dbConvertedDate);

            long timeDiff = abs(postDate.getTime() - dbConvertedDate.getTime());

            assertTrue(timeDiff < 5000);


        }
    }
}
