package net.IneiTsuki.regen.magic;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class MagicScrollEffects {

    public static void fireSpell(World world, PlayerEntity user, List<MagicEnums.Clarification> clarifications, List<MagicEnums.MagicType> types) {
        int radius = 1;

        // Modifiers
        if (clarifications.contains(MagicEnums.Clarification.AREA)) radius = 3; //plus 3 to radius
        if (clarifications.contains(MagicEnums.Clarification.MANY)) radius += 2; // plus 2 to radius+area
        if (clarifications.contains(MagicEnums.Clarification.MOVE)) {
            user.setOnFireFor(5);
        }

        var center = user.getBlockPos();

        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (world.isAir(pos) && world.getBlockState(pos.down()).isBurnable()) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        user.sendMessage(Text.literal("You unleashed a fiery spell!"), false);
        world.playSound(null, center, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
