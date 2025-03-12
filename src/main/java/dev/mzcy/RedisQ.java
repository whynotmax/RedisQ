package dev.mzcy;

import dev.mzcy.queue.QueueManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.File;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RedisQ extends JavaPlugin {
    RedissonClient redissonClient;
    File configFile;
    dev.mzcy.util.Config config;

    QueueManager queueManager;

    public static String PREFIX;
    public static String NO_PERMISSION;
    public static String NO_CONSOLE;
    public static String NO_ARGS;
    public static String NO_QUEUE;
    public static String JOINED_QUEUE;
    public static String SENDING_TO_SERVER;
    public static String QUEUE_LEFT;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            config = new dev.mzcy.util.Config(getDataFolder().getPath(), "config.yml", (cfg) -> {
                cfg.getConfig().addDefault("redis.url", "redis://127.0.0.1:6379");
                cfg.getConfig().addDefault("redis.password", "password");
                cfg.getConfig().addDefault("general.prefix", "<dark_gray>[<red>RedisQ<dark_gray>]<gray>");
                cfg.getConfig().addDefault("general.no-permission", "%prefix% <red>You do not have permission to do that. (%permission%)");
                cfg.getConfig().addDefault("general.no-console", "%prefix% <red>This command can only be executed by players.");
                cfg.getConfig().addDefault("general.no-args", "%prefix% <red>Usage: %usage%");
                cfg.getConfig().addDefault("general.no-queue", "%prefix% <red>Queue with the name %name% not found.");
                cfg.getConfig().addDefault("general.queue-joined", "%prefix% <gray>You have joined the queue %name%. You are in position %position%.");
                cfg.getConfig().addDefault("general.sending-to-server", "%prefix% <gray>Sending you to %server%...");
                cfg.getConfig().addDefault("general.queue-left", "%prefix% <gray>You have left the queue.");
            });
        }

        PREFIX = this.config.getConfig().getString("general.prefix");
        NO_PERMISSION = this.config.getConfig().getString("general.no-permission").replace("%prefix%", PREFIX);
        NO_CONSOLE = this.config.getConfig().getString("general.no-console").replace("%prefix%", PREFIX);
        NO_ARGS = this.config.getConfig().getString("general.no-args").replace("%prefix%", PREFIX);
        NO_QUEUE = this.config.getConfig().getString("general.no-queue").replace("%prefix%", PREFIX);
        JOINED_QUEUE = this.config.getConfig().getString("general.queue-joined").replace("%prefix%", PREFIX);
        SENDING_TO_SERVER = this.config.getConfig().getString("general.sending-to-server").replace("%prefix%", PREFIX);
        QUEUE_LEFT = this.config.getConfig().getString("general.queue-left").replace("%prefix%", PREFIX);


        Config config = new Config();
        String redisUrl = this.config.getConfig().getString("redis.url");
        String redisPassword = this.config.getConfig().getString("redis.password");
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setAddress(redisUrl).setPassword(redisPassword);
        } else {
            config.useSingleServer().setAddress(redisUrl);
        }
        redissonClient = Redisson.create(config);

        queueManager = new QueueManager(this);
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