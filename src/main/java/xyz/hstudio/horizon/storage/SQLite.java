package xyz.hstudio.horizon.storage;

import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.configuration.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {

    private Connection connection;

    public void setup() {
        String host = Config.MYSQL_HOST;
        String database = Config.MYSQL_DATABASE;
        String user = Config.MYSQL_USER;
        String pass = Config.MYSQL_PASSWORD;
        int port = Config.MYSQL_PORT;

        if (Config.MYSQL_ENABLED) {
            try {
                synchronized (this) {
                    if (connection != null && !connection.isClosed()) {
                        return;
                    }
                    connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, pass);
                    Logger.msg("INFO", "Connected to MySQL!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Logger.msg("WARN", "Failed to connect to MySQL! Stacktrace:");
                e.printStackTrace();
            }

            createTables();
        } else {
            Logger.msg("INFO", "MySQL was chosen to not be enabled!");
        }
    }

    private void createTables() {
        try {
            String cmd = "CREATE TABLE IF NOT EXISTS HORIZON(" +
                    "ID INTEGER AUTO_INCREMENT NOT NULL," +
                    "PLAYER TEXT," +
                    "IP TEXT," +
                    "PING TEXT," +
                    "VL TEXT," +
                    "primary key(id))";
            Statement statement = connection.createStatement();
            statement.execute(cmd);
        } catch (SQLException e) {
            Logger.msg("WARN", "Failed to create MySQL tables! Stacktrace:");
            e.printStackTrace();
        }
    }
}