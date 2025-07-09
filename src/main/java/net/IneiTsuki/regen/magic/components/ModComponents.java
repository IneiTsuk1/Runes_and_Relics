package net.IneiTsuki.regen.magic.components;

import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<ManaComponent> MANA = ComponentRegistry.getOrCreate(
            Identifier.of("regen", "mana"),
            ManaComponent.class
    );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MANA, ManaComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
