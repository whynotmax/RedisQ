package dev.mzcy.listener;

import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import dev.mzcy.RedisQ;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public record NpcInteractListener(RedisQ redisQ) implements Listener {

    @EventHandler
    public void handleNpcInteractEvent(final NpcInteractEvent event) {
        Player player = event.getPlayer();
        String npcId = event.getNpc().getData().getId();

        redisQ.getQueueManager().getQueues().keySet().forEach(queue -> {
            if (queue.getNpcId().equals("none")) {
                return;
            }
            if (queue.getNpcId().equals(npcId)) {
                redisQ.getQueueManager().addPlayerToQueue(queue.getName(), player.getUniqueId());
                player.sendMessage(MiniMessage.miniMessage().deserialize(RedisQ.JOINED_QUEUE.replace("%name%", queue.getDisplayName()).replace("%position%", "" + (redisQ.getQueueManager().getPositionInQueue(queue.getName(), player.getUniqueId())))));
                return;
            }
        });
    }

}
