package com.taczinspect.mixin;

import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ObjectAnimationRunner.class)
public interface ObjectAnimationRunnerAccessor {
    // 必须加 remap = false
    @Accessor(value = "progressNs", remap = false)
    long taczinspect$getProgressNs();
}