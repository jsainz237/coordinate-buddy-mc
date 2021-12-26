package com.jsainz.coordinate.buddy;

import com.jsainz.coordinate.buddy.components.SyncedWorldComponent;
import com.jsainz.coordinate.buddy.utils.WorldComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class MyComponents implements WorldComponentInitializer {
    public static final ComponentKey<WorldComponent> CBWORLD =
            ComponentRegistry.getOrCreate(new Identifier("cb", "world"), WorldComponent.class);

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(CBWORLD, SyncedWorldComponent::new);
    }

    public static SyncedWorldComponent useWorld(World provider) {
        return (SyncedWorldComponent) CBWORLD.get(provider);
    }
}
