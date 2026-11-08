package com.taczinspect.mixin;

import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ObjectAnimationRunner.class)
public interface ObjectAnimationRunnerAccessor {
    @Accessor("progressNs")
    long taczinspect$getProgressNs();
}