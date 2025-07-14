package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.magic.components.ManaComponent;
import net.IneiTsuki.regen.magic.components.ManaComponentImpl;
import net.IneiTsuki.regen.magic.core.scheduler.TickScheduler;

public class ManaRegenTask implements Runnable {
    private final ManaComponent manaComponent;
    private final int regenAmount;
    private final int delayTicks;


    public ManaRegenTask(ManaComponent manaComponent, int regenAmount, int delayTicks) {
        this.manaComponent = manaComponent;
        this.regenAmount = regenAmount;
        this.delayTicks = delayTicks;
    }

    @Override
    public void run() {
        if (manaComponent.getMana() < manaComponent.getMaxMana()) {
            manaComponent.addMana(regenAmount);

            if (manaComponent.getMana() < manaComponent.getMaxMana()) {
                TickScheduler.schedule(delayTicks, this);
            } else if (manaComponent instanceof ManaComponentImpl impl) {
                impl.setRegenerating(false);
            }
        } else if (manaComponent instanceof ManaComponentImpl impl) {
            impl.setRegenerating(false);
        }
    }
}