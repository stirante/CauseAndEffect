package com.stirante.cae.listener;

import com.stirante.cae.Cause;
import com.stirante.cae.CauseAndEffect;
import com.stirante.cae.util.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.PistonBaseMaterial;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class BlockListener implements Listener {
    private static BlockFace[] SIDES =
            {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH};

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRedstone(BlockRedstoneEvent e) {
        // Propagate cause through redstone
        for (BlockFace side : SIDES) {
            if (Cause.from(e.getBlock().getRelative(side), Cause.CauseEvent.REDSTONE).to(e.getBlock())) {
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerIgnite(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        // Propagate cause from player to placed fire
        if (e.getBlockPlaced().getType() == Material.FIRE) {
            Cause.startFrom(e.getPlayer(), Cause.CauseEvent.FIRE).to(e.getBlockPlaced());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (e.isCancelled()) {
            return;
        }
        // Propagate cause from source of fire (lava or another fire) to new fire
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LAVA || e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            Cause.from(e.getIgnitingBlock(), Cause.CauseEvent.FIRE).to(e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireSpread(BlockSpreadEvent e) {
        if (e.isCancelled()) {
            return;
        }
        // Propagate cause from source of fire (lava or another fire) to new fire
        if (e.getSource().getType() == Material.FIRE || e.getSource().getType() == Material.LAVA) {
            Cause.from(e.getSource(), Cause.CauseEvent.FIRE).to(e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDispense(BlockDispenseEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getItem().getType() == Material.LAVA_BUCKET || e.getItem().getType() == Material.WATER_BUCKET) {
            // After dispensing lava of water, we need to cancel the event and place water/fire ourselves, so we can add cause to it
            e.setCancelled(true);
            BlockFace facing = ((Dispenser) e.getBlock().getState().getData()).getFacing();
            Block b = e.getBlock().getRelative(facing);
            if (b.getType() == Material.AIR) {
                if (e.getItem().getType() == Material.LAVA_BUCKET) {
                    b.setType(Material.STATIONARY_LAVA);
                }
                else {
                    b.setType(Material.STATIONARY_WATER);
                }
                org.bukkit.block.Dispenser state = (org.bukkit.block.Dispenser) e.getBlock().getState();
                state.getInventory().remove(e.getItem());
                state.getInventory().addItem(new ItemStack(Material.BUCKET));
                state.update();
                forcePhysicsUpdate(b);
                checkBlockPowered(e.getBlock(), Cause.CauseEvent.DISPENSE);
                Cause.from(e.getBlock(), Cause.CauseEvent.DISPENSE).to(b);
            }
        }
        else if (e.getItem().getType() == Material.TNT) {
            // After dispensing tnt, we need to cancel the event and place tnt entity ourselves, so we can add cause to it
            e.setCancelled(true);
            BlockFace facing = ((Dispenser) e.getBlock().getState().getData()).getFacing();
            Block b = e.getBlock().getRelative(facing);
            if (!b.getType().isSolid()) {
                TNTPrimed spawn = b.getWorld().spawn(b.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
                org.bukkit.block.Dispenser state = (org.bukkit.block.Dispenser) e.getBlock().getState();
                state.getInventory().remove(e.getItem());
                state.update();
                checkBlockPowered(e.getBlock(), Cause.CauseEvent.DISPENSE);
                Cause.from(e.getBlock(), Cause.CauseEvent.DISPENSE).to(spawn);
            }
        }
    }

    private void forcePhysicsUpdate(Block b) {
        try {
//          net.minecraft.server.v1_8_R3.Block block = CraftMagicNumbers.getBlock(b);
//          World world = ((CraftChunk) b.getChunk()).getHandle().getWorld();
//          world.d(new BlockPosition(b.getX(), b.getY(), b.getZ()), block);
            // Code below is equivalent of code above, but with reflection
            Method getBlock =
                    ReflectionUtils.getMethod("CraftMagicNumbers", ReflectionUtils.PackageType.CRAFTBUKKIT_UTIL, "getBlock", Block.class);
            Object block = getBlock.invoke(null, b);
            Method getHandle =
                    ReflectionUtils.getMethod("CraftChunk", ReflectionUtils.PackageType.CRAFTBUKKIT, "getHandle");
            Method getWorld =
                    ReflectionUtils.getMethod("Chunk", ReflectionUtils.PackageType.MINECRAFT_SERVER, "getWorld");
            Object world = getWorld.invoke(getHandle.invoke(b.getChunk()));
            Class<?> blockPositionClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("BlockPosition");
            Class<?> blockClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("Block");
            Constructor<?> blockPositionConstructor =
                    ReflectionUtils.getConstructor("BlockPosition", ReflectionUtils.PackageType.MINECRAFT_SERVER, int.class, int.class, int.class);
            Object blockPosition = blockPositionConstructor.newInstance(b.getX(), b.getY(), b.getZ());
            Method d =
                    ReflectionUtils.getMethod("World", ReflectionUtils.PackageType.MINECRAFT_SERVER, "d", blockPositionClass, blockClass);
            d.invoke(world, blockPosition, block);
        } catch (Exception e1) {
            CauseAndEffect.LOGGER.log(Level.WARNING, "Failed to update physics from dispenser. Check if server version is correct", e1);
        }
    }

    private void checkBlockPowered(Block b, Cause.CauseEvent event) {
        for (BlockFace side : SIDES) {
            Block relative = b.getRelative(side);
            if (Cause.from(relative, event).to(b)) {
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLiquidFlow(BlockFromToEvent e) {
        if (e.isCancelled()) {
            return;
        }
        // Propagate cause from lava/water along the flow
        if (e.getBlock().getType() == Material.LAVA || e.getBlock().getType() == Material.STATIONARY_LAVA) {
            Cause.from(e.getBlock(), Cause.CauseEvent.LAVA).to(e.getToBlock());
        }
        else if (e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.STATIONARY_WATER) {
            Cause.from(e.getBlock(), Cause.CauseEvent.WATER).to(e.getToBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPiston(BlockPistonRetractEvent e) {
        if (e.isCancelled()) {
            return;
        }
        // Propagate cause from powered block to piston
        BlockFace facing = ((PistonBaseMaterial) e.getBlock().getState().getData()).getFacing();
        Block b = e.getBlock().getRelative(facing);
        checkBlockPowered(e.getBlock(), Cause.CauseEvent.PISTON);
        Cause.from(e.getBlock(), Cause.CauseEvent.PISTON).to(b);
    }
}
