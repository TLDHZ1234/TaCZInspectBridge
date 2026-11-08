package com.taczinspect.mixin;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.taczinspect.api.IFrameCached;
import com.taczinspect.api.IInspectAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AnimationStateContext.class)
public abstract class AnimationStateContextMixin implements IInspectAccessor, IFrameCached {

    @Unique
    private static final int TACZINSPECT_MAX_TRACK = 64;

    @Unique private boolean taczinspect$cachedValid = false;
    @Unique private ObjectAnimationRunner taczinspect$cachedTarget = null;

    @Override
    public void taczinspect$beginFrame() {
        this.taczinspect$cachedValid = false;
    }

    @Unique
    private static boolean taczinspect$isInspectAnimation(String name) {
        if (name == null || name.isEmpty() || name.charAt(0) != 'i') return false;
        return name.equals("inspect")
                || name.equals("inspect_empty")
                || name.equals("inspect_xmag")
                || name.equals("inspect_empty_xmag")
                || name.startsWith("inspect_xmag_")
                || name.startsWith("inspect_empty_xmag_");
    }

    @Unique
    private ObjectAnimationRunner taczinspect$scanInspectTarget() {
        AnimationStateMachine<?> sm = ((AnimationStateContext)(Object)this).getStateMachine();
        if (sm == null) return null;
        AnimationController controller = sm.getAnimationController();
        if (controller == null) return null;

        for (int track = 0; track < TACZINSPECT_MAX_TRACK; track++) {
            ObjectAnimationRunner runner = controller.getAnimation(track);
            if (runner == null || runner.isStopped()) continue;

            ObjectAnimationRunner target = runner.getTransitionTo();
            if (target == null) target = runner;

            var anim = target.getAnimation();
            if (anim == null) continue;

            if (taczinspect$isInspectAnimation(anim.name)) {
                return target;
            }
        }
        return null;
    }

    @Unique
    private ObjectAnimationRunner taczinspect$getCachedOrScan() {
        if (taczinspect$cachedValid) {
            return taczinspect$cachedTarget;
        }
        ObjectAnimationRunner target = taczinspect$scanInspectTarget();
        taczinspect$cachedTarget = target;
        taczinspect$cachedValid = true;
        return target;
    }

    @Override
    public float taczinspect$getInspectProgressSeconds() {
        ObjectAnimationRunner target = taczinspect$getCachedOrScan();
        if (target == null) return -1f;
        return ((ObjectAnimationRunnerAccessor) target).taczinspect$getProgressNs() / 1.0E9F;
    }

    @Override
    public float taczinspect$getInspectTotalSeconds() {
        ObjectAnimationRunner target = taczinspect$getCachedOrScan();
        if (target == null) return -1f;
        var anim = target.getAnimation();
        return anim == null ? -1f : anim.getMaxEndTimeS();
    }

    @Override
    public boolean taczinspect$isInspecting() {
        return taczinspect$getCachedOrScan() != null;
    }
}