package me.shershnyaga.bettercallfishing.events;


import lombok.AllArgsConstructor;
import me.shershnyaga.bettercallfishing.utils.AutoUpdate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class OnJoinEvent implements Listener {

    private final AutoUpdate autoUpdate;

    @EventHandler
    private void handleUpdateMessage(PlayerJoinEvent event) {

        if (autoUpdate == null) {
            return;
        }

        if (!event.getPlayer().isOp()) {
            return;
        }

        if (autoUpdate.isUpdateFound()) {
            Player player = event.getPlayer();

            for (String message: autoUpdate.getUpdateMessage()) {
                player.sendMessage(message);
            }
        }

    }

}
