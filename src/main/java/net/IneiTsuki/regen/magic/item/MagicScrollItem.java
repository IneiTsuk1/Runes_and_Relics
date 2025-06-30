package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.magic.MagicEnums;
import net.IneiTsuki.regen.magic.interfaces.MagicEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class MagicScrollItem extends Item {
    private final List<MagicEnums.Clarification> clarifications;
    private final List<MagicEnums.MagicType> types;
    private final MagicEffect effect;


    public MagicScrollItem(Settings settings, List<MagicEnums.Clarification> clarifications, List<MagicEnums.MagicType> types, MagicEffect effect) {
        super(settings);
        this.clarifications = clarifications;
        this.types = types;
        this.effect = effect;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient()) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0F, 1.0F);

            effect.apply(world, user, clarifications, types); // pass in both
            if (!user.getAbilities().creativeMode) itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        tooltip.add(Text.literal("Clarifications:").formatted(Formatting.GRAY));
        for (var clarification : clarifications) {
            tooltip.add(Text.literal("- " + clarification.getFormattedName())
                    .formatted(Formatting.BOLD)
                    .styled(style -> style.withColor(clarification.getColor())));
        }

        tooltip.add(Text.literal("Types:").formatted(Formatting.GRAY));
        for (var magicType : types) {
            tooltip.add(Text.literal("- " + magicType.getFormattedName())
                    .formatted(Formatting.BOLD)
                    .styled(style -> style.withColor(magicType.getColor())));
        }

        tooltip.add(Text.literal("Right-click to cast")
                .formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
    }

    public List<MagicEnums.Clarification> getClarifications() {
        return this.clarifications;
    }

    public List<MagicEnums.MagicType> getMagicTypes() {
        return this.types;
    }
}
