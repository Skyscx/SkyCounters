package me.skyscx.skycounters;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class Datebase {
    private Connection connection;
    private final SkyCounters plugin;

    public Datebase(File dataFolder, SkyCounters plugin) throws SQLException {
        this.plugin = plugin;
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
                "deaths INTEGER," +
                "kills INTEGER," +
                "kills_mobs INTEGER," +
                "set_blocks INTEGER," +
                "break_blocks INTEGER," +
                "score_top_online INTEGER," +
                "titul_player TEXT," +
                "column_int_add1 INTEGER," +
                "column_int_add2 INTEGER," +
                "column_int_add3 INTEGER," +
                "column_text_add1 TEXT," +
                "column_text_add2 TEXT," +
                "column_text_add3 TEXT" +
                ");";
        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    /**Создание игрока в базе данных**/
    public void insertPlayer(String name, int messages) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO players_counters(name,messages) VALUES(?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, messages);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    public CompletableFuture<Boolean> checkPlayer(String name) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT * FROM players_counters WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);

                try (ResultSet rs = pstmt.executeQuery()) {
                    future.complete(rs.next());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                future.complete(false);
            }
        });
        return future;
    }
    public void deletePlayerLogyc(String name, CommandSender sender) {
        CompletableFuture<Boolean> future = checkPlayer(name);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            System.getLogger("Scheduler ENABLED");
            try {
                boolean result = future.join();
                if (!result) {
                    System.getLogger("!result");
                    sender.sendMessage("§3Такого игрока не существует в базе данных!");
                } else {
                    sender.sendMessage("§3Игрок §7" + name + "§3 удален из базы данных!");
                    deletePlayer(name);
                    System.getLogger("result");
                }
            } catch (CompletionException e) {
                e.printStackTrace();
            }
        });
    }
    public void checkPlayerTask(String name) {
        CompletableFuture<Boolean> future = checkPlayer(name);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean result = future.join();
                if (!result) {
                    insertPlayer(name, 0);
                }
            } catch (CompletionException e) {
                e.printStackTrace();
            }
        });
    }
    /**Счетчик сообщений и вывод топа по сообщениям**/
    public void updatePlayerCountMessages(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE players_counters SET messages = messages + 1 WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    LoadingCache<String, List<String>> topPlayersCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public List<String> load(String key) {
                    return (List<String>) getTopPlayersMessageTop();
                }
            });
    public CompletableFuture<List<String>> getTopPlayersMessageTop() {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> topPlayers = topPlayersCache.getIfPresent("topPlayers");
            if (topPlayers == null) {
                String sql = "SELECT * FROM players_counters ORDER BY messages DESC LIMIT 10";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        int i = 1;
                        topPlayers = new ArrayList<>();
                        while (rs.next()) {
                            int stage = i++;
                            String name = rs.getString("name");
                            int messages = rs.getInt("messages");
                            topPlayers.add(String.format("%d. §7%s§r: §7%d §rсообщений",stage, name, messages));
                        }
                        topPlayersCache.put("topPlayers", topPlayers);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            future.complete(topPlayers);
        });

        return future;
    }
    public int getPlayerPositionMessageTop(String playerName) {
        String sql = "SELECT * FROM players_counters ORDER BY messages DESC";
        int playerPosition = 1;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    if (name.equalsIgnoreCase(playerName)) {
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

    //TODO: Реализовать удаление пользователя по команде.
    public void deletePlayer(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            System.getLogger("Scheduler delete ENABLED");
            String sql = "DELETE FROM players_counters WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                System.getLogger("delete ENABLED");
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
