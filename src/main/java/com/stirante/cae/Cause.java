package com.stirante.cae;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Cause {

    private static final Cause DUMMY = new Cause() {
        @Override
        public boolean to(Entity e) {
            return false;
        }

        @Override
        public boolean to(Block b) {
            return false;
        }
    };

    private static HashMap<Location, Cause> blockCause = new HashMap<>();
    private static HashMap<Entity, Cause> entityCause = new HashMap<>();

    private UUID player;
    private ArrayList<CauseEvent> events = new ArrayList<>();

    private Cause(UUID player, CauseEvent event) {
        this.player = player;
        events.add(event);
    }

    private Cause() {

    }

    private Cause(Cause previous, CauseEvent event) {
        this.player = previous.player;
        events.addAll(previous.events);
        if (getLastEvent() != event)
            events.add(event);
    }

    public CauseEvent getLastEvent() {
        return events.get(events.size() - 1);
    }

    public static Cause startFrom(Player player, CauseEvent event) {
        return new Cause(player.getUniqueId(), event);
    }

    public static Cause from(Cause cause, CauseEvent event) {
        return new Cause(cause, event);
    }

    public static Cause from(Block block, CauseEvent event) {
        Cause c = getCause(block);
        if (c == null)
            return DUMMY;
        if (event == CauseEvent.REDSTONE && !(c.getLastEvent() == CauseEvent.REDSTONE || c.getLastEvent() == CauseEvent.POWER_SOURCE))
            return DUMMY;
        if (event == CauseEvent.DISPENSE && !(c.getLastEvent() == CauseEvent.REDSTONE || c.getLastEvent() == CauseEvent.POWER_SOURCE || c.getLastEvent() == CauseEvent.DISPENSE))
            return DUMMY;
        else return from(c, event);
    }

    public static Cause from(Entity e, CauseEvent event) {
        Cause c = getCause(e);
        if (c == null && e instanceof Player)
            return startFrom((Player) e, event);
        if (c == null)
            return DUMMY;
        else return from(c, event);
    }

    public static void addEvent(Player pl, CauseEvent event) {
        Cause c = getCause(pl);
        if (c == null)
            return;
        if (c.getLastEvent() != event)
            c.events.add(event);
    }

    public static Cause getCause(Block b) {
        return blockCause.get(b.getLocation());
    }

    public static Cause getCause(Entity e) {
        return entityCause.get(e);
    }

    public static boolean to(Block block, Cause cause) {
        if (cause == DUMMY) return false;
        blockCause.put(block.getLocation(), cause);
        return true;
    }

    public static boolean to(Entity e, Cause cause) {
        if (cause == DUMMY) return false;
        if (e instanceof TNTPrimed && cause.getLastEvent() == CauseEvent.EXPLOSION && getCause(e) != null) return false;
        entityCause.put(e, cause);
        return true;
    }

    public boolean to(Entity e) {
        return to(e, this);
    }

    public boolean to(Block b) {
        return to(b, this);
    }

    @Override
    public String toString() {
        return "Cause{" +
                "player=" + Bukkit.getOfflinePlayer(player).getName() +
                ", events=" + events +
                '}';
    }

    public enum CauseEvent {
        REDSTONE, FIRE, FALL, EXPLOSION, PROJECTILE, ATTACK, POWER_SOURCE, PISTON, DISPENSE, LAVA, POISON, POTION, DROP, WATER
    }

}
