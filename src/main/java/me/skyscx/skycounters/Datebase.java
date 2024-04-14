package me.skyscx.skycounters;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Datebase {
    private Connection connection;

    public Datebase(File dataFolder) throws SQLException {
        File datebaseFile = new File(dataFolder, "datebase.db");
        if (!datebaseFile.exists()) {
            try {
                datebaseFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String url = "jdbc:sqlite:" + datebaseFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        createTable();
    }

    void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players_counters (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "messages INTEGER NOT NULL," +
                "deaths INTEGER NOT NULL," +
                "kills INTEGER NOT NULL," +
                "kills_mobs INTEGER NOT NULL," +
                "set_blocks INTEGER NOT NULL," +
                "break_blocks INTEGER NOT NULL," +
                "score_top_online INTEGER NOT NULL," +
                "titul_player TEXT NOT NULL," +
                "column_int_add1 INTEGER NOT NULL," +
                "column_int_add2 INTEGER NOT NULL," +
                "column_int_add3 INTEGER NOT NULL," +
                "column_text_add1 TEXT NOT NULL," +
                "column_text_add2 TEXT NOT NULL," +
                "column_text_add3 TEXT NOT NULL" +
                ");";
        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertPlayer(String name, int messages) {
        String sql = "INSERT INTO players_counters(name,messages) VALUES(?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, messages);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerCountMessages(String name) {
        String sql = "UPDATE players_counters SET count = messages + 1 WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayers() {
        List<String> players = new ArrayList<>();
        String sql = "SELECT name, messages FROM players_counters";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                players.add(rs.getString("name") + " - " + rs.getInt("messages"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }
    public boolean checkPlayer(String name) {
        String sql = "SELECT * FROM players_counters WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deletePlayer(String name) {
        String sql = "DELETE FROM players_counters WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<String> getTopPlayers() {
        String sql = "SELECT * FROM players_counters ORDER BY messages DESC LIMIT 10";
        List<String> topPlayers = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                int i = 1;
                while (rs.next()) {
                    int stage = i++;
                    String name = rs.getString("name");
                    int messages = rs.getInt("count");
                    topPlayers.add(String.format("%d. §7%s§r: §7%d §rсообщений",stage, name, messages));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
    }
    public int getPlayerPosition(String name) {
        String sql = "SELECT * FROM players_counters ORDER BY messages DESC";
        int playerPosition = 1;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nameC = rs.getString("name");
                    if (nameC.equalsIgnoreCase(name)) {
                        return playerPosition;
                    }
                    playerPosition++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
