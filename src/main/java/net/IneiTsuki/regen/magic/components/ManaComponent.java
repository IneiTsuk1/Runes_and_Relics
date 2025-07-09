package net.IneiTsuki.regen.magic.components;

import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface ManaComponent extends AutoSyncedComponent {
    int getMana();
    void setMana(int mana);
    int getMaxMana();
    void setMaxMana(int maxMana);
    void addMana(int amount);
    void consumeMana(int amount);
    boolean hasEnoughMana(int amount);
    void syncToClient();
}