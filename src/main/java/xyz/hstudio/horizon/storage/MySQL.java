package xyz.hstudio.horizon.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.cgoo.api.logger.Logger;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.Config;

import java.sql.*;

public class MySQL {

    private Connection connection;

    public void setup() {
        if (!Config.MYSQL_ENABLED) {
            Logger.info("MySQL was chosen to not be enabled!");
            return;
        }

        String host = Config.MYSQL_HOST;
        String database = Config.MYSQL_DATABASE;
        String user = Config.MYSQL_USER;
        String pass = Config.MYSQL_PASSWORD;
        int port = Config.MYSQL_PORT;

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
            config.setUsername(user);
            config.setPassword(pass);

            HikariDataSource source = new HikariDataSource(config);

            connection = source.getConnection();

            createTable();
            updateTable();
            Logger.info("Connected to MySQL!");
        } catch (SQLException e) {
            Logger.warn("Failed to connect to MySQL! Stacktrace:");
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet result = metaData.getTables(null, null, "playerdata", null);
        if (result.next()) {
            return;
        }
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE playerdata(uuid CHAR(36) NOT NULL, PRIMARY KEY(uuid))");
    }

    private void updateTable() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (Detection detection : Detection.values()) {
            ResultSet result = metaData.getColumns(null, null, "playerdata", detection.name());
            if (result.next()) {
                continue;
            }
            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE playerdata ADD " + detection + " INT DEFAULT 0");
        }
    }

    public void initData(HPlayer p) throws SQLException {
        if (!Config.MYSQL_ENABLED) {
            return;
        }
        String uuid = p.nms.getUniqueID().toString();
        PreparedStatement statement = connection.prepareStatement("SELECT * from playerdata WHERE uuid=?");
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return;
        }

        statement = connection.prepareStatement("INSERT IGNORE INTO playerdata(uuid) VALUES(?)");
        statement.setString(1, uuid);
        statement.execute();
    }

    public void syncData(HPlayer p, Detection detection, int vl) throws SQLException {
        String uuid = p.nms.getUniqueID().toString();

        PreparedStatement statement = connection.prepareStatement("SELECT * from playerdata WHERE uuid=?");
        statement.setString(1, uuid);
        ResultSet result = statement.executeQuery();
        result.updateInt(detection.name(), vl);
        result.updateRow();
    }

    public int loadData(HPlayer p, Detection detection) throws SQLException {
        String uuid = p.nms.getUniqueID().toString();
        PreparedStatement statement = connection.prepareStatement("SELECT * from playerdata WHERE uuid=?");
        statement.setString(1, uuid);
        ResultSet result = statement.executeQuery();
        return result.getInt(detection.name());
    }
}