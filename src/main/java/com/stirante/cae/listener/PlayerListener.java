package com.stirante.cae.listener;

import com.stirante.cae.Cause;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            e.getPlayer().sendMessage(String.valueOf(Cause.getCause(e.getClickedBlock())));
            e.setCancelled(true);
        }
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK && isClickable(e.getClickedBlock().getType())) ||
                (e.getAction() == Action.PHYSICAL && isPhysical(e.getClickedBlock().getType()))) {
            Cause.startFrom(e.getPlayer(), Cause.CauseEvent.POWER_SOURCE).to(e.getClickedBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDrop(PlayerDropItemEvent e) {
        Cause.startFrom(e.getPlayer(), Cause.CauseEvent.DROP).to(e.getItemDrop());
    }

    private boolean isClickable(Material m) {
        switch (m) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
                return true;
            default:
                return false;
        }
    }

    private boolean isPhysical(Material m) {
        switch (m) {
            case WOOD_PLATE:
            case STONE_PLATE:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
                return true;
            default:
                return false;
        }
    }
}
