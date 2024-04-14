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
    /**Создание игрока в базе данных**/
    public void insertPlayer(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO players_counters(name,messages,deaths,kills,kills_mobs,set_blocks,break_blocks,score_top_online,titul_player,column_int_add1,column_int_add2,column_int_add3,column_text_add1,column_text_add2,column_text_add3) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, 0);
                pstmt.setInt(3, 0);
                pstmt.setInt(4, 0);
                pstmt.setInt(5, 0);
                pstmt.setInt(6, 0);
                pstmt.setInt(7, 0);
                pstmt.setInt(8, 0);
                pstmt.setString(9, "value");
                pstmt.setInt(10, 0);
                pstmt.setInt(11, 0);
                pstmt.setInt(12, 0);
                pstmt.setString(13, "value");
                pstmt.setString(14, "value");
                pstmt.setString(15, "value");
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
                    insertPlayer(name);
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
    LoadingCache<String, List<String>> topPlayersMessagesCache = CacheBuilder.newBuilder()
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
            List<String> topPlayersMessages = topPlayersMessagesCache.getIfPresent("topPlayersMessages");
            if (topPlayersMessages == null) {
                String sql = "SELECT * FROM players_counters ORDER BY messages DESC LIMIT 10";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        int i = 1;
                        topPlayersMessages = new ArrayList<>();
                        while (rs.next()) {
                            int stage = i++;
                            String name = rs.getString("name");
                            int messages = rs.getInt("messages");
                            topPlayersMessages.add(String.format("%d. §7%s§r: §7%d§r сообщения",stage, name, messages));
                        }
                        topPlayersMessagesCache.put("topPlayersMessages", topPlayersMessages);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            future.complete(topPlayersMessages);
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
    /**Счетчик смертей и вывод топа по смертям**/
    public void updatePlayerCountDeath(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE players_counters SET deaths = deaths + 1 WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    LoadingCache<String, List<String>> topPlayersDeathCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public List<String> load(String key) {
                    return (List<String>) getTopPlayersDeathTop();
                }
            });
    public CompletableFuture<List<String>> getTopPlayersDeathTop() {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> topPlayersDeaths = topPlayersDeathCache.getIfPresent("topPlayersDeaths");
            if (topPlayersDeaths == null) {
                String sql = "SELECT * FROM players_counters ORDER BY deaths DESC LIMIT 10";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        int i = 1;
                        topPlayersDeaths = new ArrayList<>();
                        while (rs.next()) {
                            int stage = i++;
                            String name = rs.getString("name");
                            int deaths = rs.getInt("deaths");
                            topPlayersDeaths.add(String.format("%d. §7%s§r: §7%d§r смерти",stage, name, deaths));
                        }
                        topPlayersDeathCache.put("topPlayersDeaths", topPlayersDeaths);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            future.complete(topPlayersDeaths);
        });

        return future;
    }
    public int getPlayerPositionDeathsTop(String playerName) {
        String sql = "SELECT * FROM players_counters ORDER BY deaths DESC";
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
    /**Счетчик убийств и вывод топа по убийствам**/
    public void updatePlayerCountKills(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE players_counters SET kills = kills + 1 WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    LoadingCache<String, List<String>> topPlayersKillsCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public List<String> load(String key) {
                    return (List<String>) getTopPlayersKillsTop();
                }
            });
    public CompletableFuture<List<String>> getTopPlayersKillsTop() {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> topPlayersKills = topPlayersKillsCache.getIfPresent("topPlayersKills");
            if (topPlayersKills == null) {
                String sql = "SELECT * FROM players_counters ORDER BY kills DESC LIMIT 10";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        int i = 1;
                        topPlayersKills = new ArrayList<>();
                        while (rs.next()) {
                            int stage = i++;
                            String name = rs.getString("name");
                            int kills = rs.getInt("kills");
                            topPlayersKills.add(String.format("%d. §7%s§r: §7%d§r убийства",stage, name, kills));
                        }
                        topPlayersKillsCache.put("topPlayersKills", topPlayersKills);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            future.complete(topPlayersKills);
        });

        return future;
    }
    public int getPlayerPositionKillsTop(String playerName) {
        String sql = "SELECT * FROM players_counters ORDER BY kills DESC";
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
    /**Счетчик убитых мобов и вывод топа по убитым мобам**/
    public void updatePlayerCountKillsMobs(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE players_counters SET kills_mobs = kills_mobs + 1 WHERE name = ?";
            System.out.println("Update...");
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    LoadingCache<String, List<String>> topPlayersKillsMobsCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public List<String> load(String key) {
                    return (List<String>) getTopPlayersKillsMobsTop();
                }
            });
    public CompletableFuture<List<String>> getTopPlayersKillsMobsTop() {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> topPlayersKillsMobs = topPlayersKillsMobsCache.getIfPresent("topPlayersKillsMobs");
            if (topPlayersKillsMobs == null) {
                String sql = "SELECT * FROM players_counters ORDER BY kills_mobs DESC LIMIT 10";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        int i = 1;
                        topPlayersKillsMobs = new ArrayList<>();
                        while (rs.next()) {
                            int stage = i++;
                            String name = rs.getString("name");
                            int kills_mobs = rs.getInt("kills_mobs");
                            topPlayersKillsMobs.add(String.format("%d. §7%s§r: §7%d§r убитых мобов",stage, name, kills_mobs));
                        }
                        topPlayersDeathCache.put("topPlayersKillsMobs", topPlayersKillsMobs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            future.complete(topPlayersKillsMobs);
        });

        return future;
    }
    public int getPlayerPositionKillsMobsTop(String playerName) {
        String sql = "SELECT * FROM players_counters ORDER BY kills_mobs DESC";
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
