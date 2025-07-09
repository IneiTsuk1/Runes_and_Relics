package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.magic.components.ManaComponent;
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
        }
        // Reschedule itself to run again after delayTicks
        TickScheduler.schedule(delayTicks, this);
    }
}
