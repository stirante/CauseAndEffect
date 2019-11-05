package com.stirante.cae.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public class CuboidIterator implements Iterator<Block> {

    private Iterator<Block> it;

    public CuboidIterator(Location center, int size) {
        ArrayList<Block> blocks = new ArrayList<>();
        for (int x = center.getBlockX() - size; x < center.getBlockX() + size; x++) {
            for (int y = center.getBlockY() - size; y < center.getBlockY() + size; y++) {
                for (int z = center.getBlockZ() - size; z < center.getBlockZ() + size; z++) {
                    blocks.add(center.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        it = blocks.iterator();
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public void forEachRemaining(Consumer<? super Block> action) {
        it.forEachRemaining(action);
    }

    @Override
    public Block next() {
        return it.next();
    }
}
