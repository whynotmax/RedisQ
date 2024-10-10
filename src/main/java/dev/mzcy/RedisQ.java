package dev.mzcy;

import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.File;

public final class RedisQ extends JavaPlugin {
    private RedissonClient redissonClient;
    private File configFile;
    private dev.mzcy.util.Config config;

    public static String PREFIX;
    public static String NO_PERMISSION;
    public static String NO_CONSOLE;
    public static String NO_ARGS;
    public static String NO_QUEUE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            config = new dev.mzcy.util.Config(getDataFolder().getPath(), "config.yml", () -> {
                getConfig().addDefault("redis.url", "redis://127.0.0.1:6379");
                getConfig().addDefault("redis.password", "");
                getConfig().addDefault("general.prefix", "<dark_gray>[<red>RedisQ<dark_gray>]<gray>");
                getConfig().addDefault("general.no-permission", "%prefix% <red>You do not have permission to do that. (%permission%)");
                getConfig().addDefault("general.no-console", "%prefix% <red>This command can only be executed by players.");
                getConfig().addDefault("general.no-args", "%prefix% <red>Usage: %usage%");
                getConfig().addDefault("general.no-queue", "%prefix% <red>Queue with the name %name% not found.");
                getConfig().options().copyDefaults(true);
            });
        }

        PREFIX = this.config.getConfig().getString("general.prefix");
        NO_PERMISSION = this.config.getConfig().getString("general.no-permission");
        NO_CONSOLE = this.config.getConfig().getString("general.no-console");
        NO_ARGS = this.config.getConfig().getString("general.no-args");
        NO_QUEUE = this.config.getConfig().getString("general.no-queue");

        Config config = new Config();
        String redisUrl = this.config.getConfig().getString("redis.url");
        String redisPassword = this.config.getConfig().getString("redis.password");
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setAddress(redisUrl).setPassword(redisPassword);
        } else {
            config.useSingleServer().setAddress(redisUrl);
        }
        redissonClient = Redisson.create(config);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }
}