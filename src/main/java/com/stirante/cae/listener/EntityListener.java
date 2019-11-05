package com.stirante.cae.listener;

import com.stirante.cae.Cause;
import com.stirante.cae.util.CuboidIterator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.List;

public class EntityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e instanceof EntityDamageByBlockEvent && e.getEntity() instanceof Player) {
            CuboidIterator it = new CuboidIterator(e.getEntity().getLocation(), 1);
            Cause c = null;
            while (it.hasNext()) {
                Block next = it.next();
                Cause cause = Cause.getCause(next);
                if (cause != null && (next.getType() == Material.LAVA || next.getType() == Material.STATIONARY_LAVA || next.getType() == Material.FIRE)) {
                    c = cause;
                    break;
                }
            }
            if (c != null) {
                Cause.from(c, c.getLastEvent()).to(e.getEntity());
            }
        } else if (e instanceof EntityDamageByEntityEvent && e.getEntity() instanceof Player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                Cause.from(((EntityDamageByEntityEvent) e).getDamager(), Cause.CauseEvent.EXPLOSION).to(e.getEntity());
            } else if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                Cause.from(((EntityDamageByEntityEvent) e).getDamager(), Cause.CauseEvent.PROJECTILE).to(e.getEntity());
            } else if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                Cause.from(((EntityDamageByEntityEvent) e).getDamager(), Cause.CauseEvent.ATTACK).to(e.getEntity());
            } else if (e.getCause() == EntityDamageEvent.DamageCause.THORNS) {
                Cause.from(((EntityDamageByEntityEvent) e).getDamager(), Cause.CauseEvent.ATTACK).to(e.getEntity());
            } else if (e.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                Cause.from(((EntityDamageByEntityEvent) e).getDamager(), Cause.CauseEvent.POTION).to(e.getEntity());
            }
        } else if (e.getEntity() instanceof Player) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                Cause.addEvent((Player) e.getEntity(), Cause.CauseEvent.FALL);
            } else if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                Cause.addEvent((Player) e.getEntity(), Cause.CauseEvent.FIRE);
            } else if (e.getCause() == EntityDamageEvent.DamageCause.POISON) {
                Cause.addEvent((Player) e.getEntity(), Cause.CauseEvent.POISON);
            }
        }
        Cause cause = Cause.getCause(e.getEntity());
        if (cause != null) System.out.println(cause);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() != null && e.getEntity().getShooter() instanceof Player && e.getEntity() instanceof Arrow && e.getEntity().getFireTicks() > 0) {
            List<Entity> entities = e.getEntity().getNearbyEntities(1, 1, 1);
            for (Entity entity : entities) {
                if (entity instanceof TNTPrimed) {
                    Cause.from(e.getEntity(), Cause.CauseEvent.PROJECTILE).to(entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Cause.from(((Player) e.getEntity().getShooter()), Cause.CauseEvent.PROJECTILE).to(e.getEntity());
        } else if (e.getEntity().getShooter() instanceof BlockProjectileSource) {
            Cause.from(((BlockProjectileSource) e.getEntity().getShooter()).getBlock(), Cause.CauseEvent.PROJECTILE).to(e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        for (LivingEntity entity : e.getAffectedEntities()) {
            if (entity instanceof Player) {
                Cause.from(e.getEntity(), Cause.CauseEvent.POTION).to(entity);
            }
        }
    }

}
