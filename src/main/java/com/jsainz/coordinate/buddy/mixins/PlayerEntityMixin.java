package com.jsainz.coordinate.buddy.mixins;

import com.jsainz.coordinate.buddy.utils.PlayerEntityExt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.Tag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityExt {

    private final String TAG_KEY = "cb-player-home";
    private String homeCoordinates;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setHomeCoordinates(String coordinates) {
        this.homeCoordinates = coordinates;
    }

    public String getHomeCoordinates() {
        return this.homeCoordinates;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);

        // save current home coordinates value
        tag.putString(TAG_KEY, this.homeCoordinates);

        return tag;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.homeCoordinates = tag.getString(TAG_KEY);
    }
}
