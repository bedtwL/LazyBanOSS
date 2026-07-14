package me.bedtwL.oss.utils;

import me.bedtwL.oss.LazyBanOSS;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtils {
    public static void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(LazyBanOSS.DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS bans (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "reason TEXT, " +
                    "ban_start INTEGER, " +
                    "ban_end INTEGER" +
                    ");";
            stmt.execute(sql);
        } catch (Exception ignored) {
        }
    }
    public static void saveBan(BanEntry entry) {
        String sql = "INSERT INTO bans (uuid, name, reason, ban_start, ban_end) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " +
                "name=excluded.name, reason=excluded.reason, " +
                "ban_start=excluded.ban_start, ban_end=excluded.ban_end;";
        try (Connection conn = DriverManager.getConnection(LazyBanOSS.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getUuid());
            pstmt.setString(2, entry.getName());
            pstmt.setString(3, entry.getReason());
            pstmt.setLong(4, entry.getBanStart());
            pstmt.setLong(5, entry.getBanEnd());
            pstmt.executeUpdate();

        } catch (Exception ignored) {
        }
    }
    public static BanEntry getBan(String uuid) {
        String sql = "SELECT * FROM bans WHERE uuid = ?;";
        try (Connection conn = DriverManager.getConnection(LazyBanOSS.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new BanEntry(
                            rs.getString("uuid"),
                            rs.getString("name"),
                            rs.getString("reason"),
                            rs.getLong("ban_start"),
                            rs.getLong("ban_end")
                    );
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static Boolean IsBanned(String uuid) {
        String sql = "SELECT ban_end FROM bans WHERE uuid = ?;";
        try (Connection conn = DriverManager.getConnection(LazyBanOSS.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long a=rs.getLong("ban_end");
                    if (a>System.currentTimeMillis()/1000)
                        return true;
                    if (a==-1)
                        return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    public static String getBannedReason(String uuid) {
        String sql = "SELECT reason FROM bans WHERE uuid = ?;";
        try (Connection conn = DriverManager.getConnection(LazyBanOSS.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                     return LazyBanOSS.config.node("ban-msg").getString().replace("{0}",rs.getString("reason"));
            }
        } catch (Exception ignored) {
        }
        return LazyBanOSS.config.node("ban-msg").getString();
    }
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])", Pattern.CASE_INSENSITIVE);

    public static long parseToSeconds(String input) {
        Matcher matcher = TIME_PATTERN.matcher(input);
        if (!matcher.matches())
            return -1;
        long duration = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        switch (unit) {
            case "s":
                return duration;
            case "m":
                return duration * 60;
            case "h":
                return duration * 60 * 60;
            case "d":
                return duration * 24 * 60 * 60;
            case "mo":
                return duration * 28 * 24 * 60 * 60;
            case "y":
                return duration * 365 * 24 * 60 * 60;
            default:
                return -1;
        }
    }
}
