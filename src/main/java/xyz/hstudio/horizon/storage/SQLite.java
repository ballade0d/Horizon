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
        String host = Config.mysql_host;
        String database = Config.mysql_database;
        String user = Config.mysql_user;
        String pass = Config.mysql_password;
        int port = Config.mysql_port;

        if (Config.mysql_enabled) {
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