package com.taczinspect.mixin;

import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.taczinspect.api.IFrameCached;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimationStateMachine.class)
public abstract class AnimationStateMachineMixin {

    /** tick 驱动失效：每个 state machine 每 tick 让它的 context 缓存失效一次。 */
    @Inject(method = {"update", "visualUpdate"}, at = @At("HEAD"))
    private void taczinspect$invalidateContextCache(CallbackInfo ci) {
        AnimationStateContext ctx = ((AnimationStateMachine<?>)(Object)this).getContext();
        if (ctx != null) {
            ((IFrameCached) ctx).taczinspect$beginFrame();
        }
    }
}