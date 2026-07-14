package me.bedtwL.oss.utils;

import lombok.Data;
import me.bedtwL.oss.LazyBanOSS;

@Data
public class BanEntry {
    private String uuid;
    private String name;
    private String reason;
    private long banStart;
    private long banEnd;
    public BanEntry() {}
    public BanEntry(String uuid, String name, long banEnd) {
        this.uuid = uuid;
        this.name = name;
        this.reason = LazyBanOSS.config.node("def-ban-reason").getString();
        this.banStart = System.currentTimeMillis()/1000;
        this.banEnd = banEnd;
    }
    public BanEntry(String uuid, String name, String reason, long banEnd) {
        this.uuid = uuid;
        this.name = name;
        this.reason = reason;
        this.banStart = System.currentTimeMillis()/1000;
        this.banEnd = banEnd;
    }
    public BanEntry(String uuid, String name, String reason, long banStart, long banEnd) {
        this.uuid = uuid;
        this.name = name;
        this.reason = reason;
        this.banStart = banStart;
        this.banEnd = banEnd;
    }
    public String getReasonCombined() {
        return LazyBanOSS.config.node("ban-msg").getString().replace("{0}",reason);
    }
}