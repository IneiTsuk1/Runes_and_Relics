package net.IneiTsuki.regen.magic.effect.active;

import net.minecraft.world.World;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ActiveSpellTracker {
    private static final List<ActiveSpellEffect> activeSpells = new LinkedList<>();

    public static void tick(World world) {
        Iterator<ActiveSpellEffect> iterator = activeSpells.iterator();
        while (iterator.hasNext()) {
            ActiveSpellEffect spell = iterator.next();
            spell.tick(world);
            if (spell.isExpired()) {
                // cleanup logic
                iterator.remove();
            }
        }
    }

    public static void add(ActiveSpellEffect spell) {
        activeSpells.add(spell);
    }

    public static void clear() {
        activeSpells.clear();
    }

    public static int getCount() {
        return activeSpells.size();
    }
}
