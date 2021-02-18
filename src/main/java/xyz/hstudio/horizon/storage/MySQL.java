package xyz.hstudio.horizon.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.Config;
import xyz.hstudio.horizon.module.CheckBase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class MySQL {

    private Connection connection;

    public void setup() {
        if (!Config.MYSQL_ENABLED) {
            Logger.msg("INFO", "MySQL was chosen to not be enabled!");
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

            createTables();
            Logger.msg("INFO", "Connected to MySQL!");
        } catch (SQLException e) {
            Logger.msg("WARN", "Failed to connect to MySQL! Stacktrace:");
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS playerdata(" +
                "uuid char(36) NOT NULL, " +
                "AIM_ASSIST int, " +
                "ANTI_VELOCITY int, " +
                "BAD_PACKETS int, " +
                "FAST_BREAK int, " +
                "GROUND_SPOOF int, " +
                "HEALTH_TAG int, " +
                "HIT_BOX int, " +
                "KILL_AURA int, " +
                "KILL_AURA_BOT int, " +
                "NO_sWING int, " +
                "PHASE int, " +
                "VERTICAL_MOVEMENT int, " +
                "primary key(uuid))"
        );
    }

    private void save(HPlayer p) throws SQLException {
        Map<Detection, CheckBase> info = p.checks;
        String uuid = p.nms.getUniqueID().toString();

        Statement statement = connection.createStatement();

        ResultSet result = statement.executeQuery("SELECT * FROM playerdata WHERE uuid='" + uuid + "'");
        if (!result.next()) {
            statement.execute("INSERT INTO playerdata VALUES (" +
                    "'" + uuid + "', " +
                    info.get(Detection.AIM_ASSIST) + ", " +
                    info.get(Detection.ANTI_VELOCITY) + ", " +
                    info.get(Detection.BAD_PACKETS) + ", " +
                    info.get(Detection.FAST_BREAK) + ", " +
                    info.get(Detection.GROUND_SPOOF) + ", " +
                    info.get(Detection.HEALTH_TAG) + ", " +
                    info.get(Detection.HIT_BOX) + ", " +
                    info.get(Detection.KILL_AURA) + ", " +
                    info.get(Detection.KILL_AURA_BOT) + ", " +
                    info.get(Detection.NO_SWING) + ", " +
                    info.get(Detection.PHASE) + ", " +
                    info.get(Detection.VERTICAL_MOVEMENT) + ")"
            );
            return;
        }

        statement.execute("UPDATE playerdata SET " +
                "AIM_ASSIST=" + info.get(Detection.AIM_ASSIST) + ", " +
                "ANTI_VELOCITY=" + info.get(Detection.ANTI_VELOCITY) + ", " +
                "BAD_PACKETS=" + info.get(Detection.BAD_PACKETS) + ", " +
                "FAST_BREAK=" + info.get(Detection.FAST_BREAK) + ", " +
                "GROUND_SPOOF=" + info.get(Detection.GROUND_SPOOF) + ", " +
                "HEALTH_TAG=" + info.get(Detection.HEALTH_TAG) + ", " +
                "HIT_BOX=" + info.get(Detection.HIT_BOX) + ", " +
                "KILL_AURA=" + info.get(Detection.KILL_AURA) + ", " +
                "KILL_AURA_BOT=" + info.get(Detection.KILL_AURA_BOT) + ", " +
                "NO_SWING=" + info.get(Detection.NO_SWING) + ", " +
                "PHASE=" + info.get(Detection.PHASE) + ", " +
                "VERTICAL_MOVEMENT=" + info.get(Detection.VERTICAL_MOVEMENT) + " " +
                "WHERE uuid='" + uuid + "'"
        );
    }

    private void testLoad(HPlayer p) throws SQLException {
        String uuid = p.nms.getUniqueID().toString();

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM playerdata WHERE uuid='" + uuid + "'");

        if (!result.next()) {
            return;
        }
    }
}