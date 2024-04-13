package me.skyscx.skycounters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Datebase {
    private Connection connection;

    public Datebase(String database) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS players_count_messages " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name TEXT NOT NULL," +
                    " count INTEGER NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPlayer(String name, int count) {
        String sql = "INSERT INTO players_count_messages(name,count) VALUES(?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, count);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerCountMessages(String name) {
        String sql = "UPDATE players_count_messages SET count = count + 1 WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayers() {
        List<String> players = new ArrayList<>();
        String sql = "SELECT name, count FROM players_count_messages";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                players.add(rs.getString("name") + " - " + rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }
    public boolean checkPlayer(String name) {
        String sql = "SELECT * FROM players WHERE name = ?";

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
        String sql = "DELETE FROM players_count_messages WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTopPlayers() {
        String sql = "SELECT * FROM players_count_messages ORDER BY count DESC LIMIT 10";
        List<String> topPlayers = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    int count = rs.getInt("count");
                    topPlayers.add(String.format("%s: %d messages", name, count));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
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
