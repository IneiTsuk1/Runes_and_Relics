package net.IneiTsuki.regen.magic.interfaces;

import net.IneiTsuki.regen.magic.MagicEnums;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.List;

@FunctionalInterface
public interface MagicEffect {
    void apply(World world, PlayerEntity user, List<MagicEnums.Clarification> clarifications, List<MagicEnums.MagicType> types);
}
