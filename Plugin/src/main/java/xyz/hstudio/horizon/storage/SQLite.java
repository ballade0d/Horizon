package xyz.hstudio.horizon.storage;

import xyz.hstudio.horizon.configuration.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {

    private Connection connection;

    public void setupSql() {
        String host = Config.mysql_host;
        String database = Config.mysql_database;
        String user = Config.mysql_user;
        String pass = Config.mysql_password;
        int port = Config.mysql_port;

        if (Config.mysql_enabled) {
            try {
                synchronized (this) {
                    if (getConnection() != null && !getConnection().isClosed()) {
                        return;
                    }
                    setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, pass));
                    System.out.println(Config.PREFIX + "Connected to MySQL!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(Config.PREFIX + "Failed to connect to MySQL!");
            }

            createTables();
        } else {
            System.out.println(Config.PREFIX + "MySQL was chosen to not be enabled!");
        }
    }

    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    private void createTables() {
        try {
            String sqlCreate1 = "CREATE TABLE IF NOT EXISTS HORIZON(" +
                    "ID INTEGER AUTO_INCREMENT NOT NULL," +
                    "PLAYER TEXT," +
                    "IP TEXT," +
                    "PING TEXT," +
                    "TPS TEXT," +
                    "VL TEXT," +
                    "primary key(id))";
            Statement sql1 = getConnection().createStatement();
            sql1.execute(sqlCreate1);
        } catch (SQLException e) {
            System.out.println(Config.PREFIX + "Couldn't create MySQL Tables!");
        }
    }

    private Connection getConnection() {
        return this.connection;
    }
}