package dev.mzcy.queue;

import dev.mzcy.RedisQ;
import dev.mzcy.queue.model.QueueModel;
import dev.mzcy.util.Config;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redisson.api.RQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QueueManager {

    RedisQ redisQ;
    Map<QueueModel, RQueue<String>> queues;
    Map<String, QueueModel> queueNames;

    public QueueManager(RedisQ redisQ) {
        this.redisQ = redisQ;
        this.queueNames = new HashMap<>();
        this.queues = new HashMap<>();
        Path queueFolder = Paths.get(redisQ.getDataFolder().getPath(), "queues");
        if (!queueFolder.toFile().exists()) {
            queueFolder.toFile().mkdir();
        }
        //Create template file
        File templateFile = new File(queueFolder.toString(), "_template.yml");
        if (!templateFile.exists()) {
            Config config = new Config(queueFolder.toString(), "_template.yml", (conf) -> {
                conf.getConfig().addDefault("name", "queue");
                conf.getConfig().setComments("name", List.of("Name of the queue"));
                conf.getConfig().addDefault("displayName", "Queue");
                conf.getConfig().setComments("displayName", List.of("Display name of the queue"));
                conf.getConfig().addDefault("npcId", "none");
                conf.getConfig().setComments("npcId", List.of("NPC ID to interact with to join the queue. Set to 'none' to disable.", "Currently only supports FancyNPCs"));
                conf.getConfig().addDefault("server", "lobby");
                conf.getConfig().setComments("server", List.of("Server to send players to"));
                conf.getConfig().addDefault("sendIntervalSeconds", 5);
                conf.getConfig().setComments("sendIntervalSeconds", List.of("Interval in seconds to send players to the server"));
                conf.getConfig().addDefault("maxPlayersInQueue", -1);
                conf.getConfig().setComments("maxPlayersInQueue", List.of("Maximum players in the queue.", "-1 for unlimited"));
                conf.saveConfig();
            });
        }

        try {
            Files.find(queueFolder, 1, (path, basicFileAttributes) -> path.toString().endsWith(".yml"))
                    .forEach(path -> {
                        if (path.toFile().isDirectory()) {
                            return;
                        }
                        if (!path.toFile().getName().equalsIgnoreCase("_template.yml")) {
                            return;
                        }
                        File file = path.toFile();
                        String name = file.getName().replace(".yml", "");
                        this.create(name, file);
                    });
        } catch (IOException e) {
            redisQ.getLogger().severe("Error while searching for queue files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void create(String queueName, File configFile) {
        Config config = new Config(configFile.getParent(), configFile.getName(), (conf) -> {
            conf.getConfig().addDefault("name", queueName);
            conf.getConfig().addDefault("displayName", queueName);
            conf.getConfig().addDefault("npcId", "none");
            conf.getConfig().addDefault("server", "lobby");
            conf.getConfig().addDefault("sendIntervalSeconds", 5);
            conf.getConfig().addDefault("maxPlayersInQueue", -1);
            conf.saveConfig();
        });

        QueueModel queueModel = new QueueModel();
        queueModel.setName(config.getConfig().getString("name"));
        queueModel.setDisplayName(config.getConfig().getString("displayName"));
        queueModel.setNpcId(config.getConfig().getString("npcId"));
        queueModel.setServer(config.getConfig().getString("server"));
        queueModel.setSendIntervalSeconds(config.getConfig().getLong("sendIntervalSeconds"));
        queueModel.setMaxPlayersInQueue(config.getConfig().getLong("maxPlayersInQueue"));
        RQueue<String> queue = redisQ.getRedissonClient().getQueue(queueModel.getName());
        queueNames.put(queueModel.getName(), queueModel);
        queues.put(queueModel, queue);
    }

    public void addPlayerToQueue(String queueName, UUID uniqueId) {
        QueueModel queueModel = queueNames.get(queueName);
        if (queueModel == null) {
            redisQ.getLogger().warning("Queue with the name " + queueName + " not found.");
            return;
        }
        RQueue<String> queue = queues.get(queueModel);
        queue.add(uniqueId.toString());
    }

    public void removePlayerFromQueue(String queueName, UUID uniqueId) {
        QueueModel queueModel = queueNames.get(queueName);
        if (queueModel == null) {
            redisQ.getLogger().warning("Queue with the name " + queueName + " not found.");
            return;
        }
        RQueue<String> queue = queues.get(queueModel);
        queue.remove(uniqueId.toString());
    }

    public void clearQueue(String queueName) {
        QueueModel queueModel = queueNames.get(queueName);
        if (queueModel == null) {
            redisQ.getLogger().warning("Queue with the name " + queueName + " not found.");
            return;
        }
        RQueue<String> queue = queues.get(queueModel);
        queue.clear();
    }

    public void deleteQueue(String queueName) {
        QueueModel queueModel = queueNames.get(queueName);
        if (queueModel == null) {
            redisQ.getLogger().warning("Queue with the name " + queueName + " not found.");
            return;
        }
        RQueue<String> queue = queues.get(queueModel);
        queue.delete();
        queueNames.remove(queueName);
        queues.remove(queueModel);
        Path queueFolder = Paths.get(redisQ.getDataFolder().getPath(), "queues");
        Path queueFile = Paths.get(queueFolder.toString(), queueName + ".yml");
        try {
            Files.delete(queueFile);
        } catch (IOException e) {
            redisQ.getLogger().severe("Error while deleting queue file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void pollQueue(String queueName) {
        QueueModel queueModel = queueNames.get(queueName);
        if (queueModel == null) {
            redisQ.getLogger().warning("Queue with the name " + queueName + " not found.");
            return;
        }
        RQueue<String> queue = queues.get(queueModel);
        String uuidString = queue.poll();
        assert uuidString != null;
        UUID uniqueId = UUID.fromString(uuidString);
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            player.sendMessage(RedisQ.SENDING_TO_SERVER.replace("%server%", queueModel.getServer()));
            //TODO: Send player to server
            return;
        }
        redisQ.getLogger().warning("Player with UUID " + uuidString + " not found. Could not send to server.");
    }

    public int getPositionInQueue(String queueName, UUID uniqueId) {
        QueueModel queueModel = queueNames.get(queueName);
        if (queueModel == null) {
            redisQ.getLogger().warning("Queue with the name " + queueName + " not found.");
            return -1;
        }
        RQueue<String> queue = queues.get(queueModel);
        int position = 0;
        for (String uuidString : queue) {
            if (uuidString.equals(uniqueId.toString())) {
                return position;
            }
            position++;
        }
        return -1;
    }

}
