package com.taczinspect.mixin;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.taczinspect.api.IInspectAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds inspect-animation queries to every TaCZ animation state context.
 * <p>
 * Guns ({@code GunAnimationStateContext}), melee weapons, throwables and consumables all derive from
 * {@link AnimationStateContext}, so a single mixin on the base class covers every item type.
 */
@Mixin(AnimationStateContext.class)
public abstract class AnimationStateContextMixin implements IInspectAccessor {

    /** TaCZ never uses more than a handful of tracks; 64 is a safe upper bound. */
    @Unique
    private static final int TACZINSPECT_MAX_TRACKS = 64;

    @Unique
    private static boolean taczinspect$isInspectName(String name) {
        if (name == null || name.isEmpty() || name.charAt(0) != 'i') {
            return false;
        }
        return name.equals("inspect")
                || name.equals("inspect_empty")
                || name.equals("inspect_xmag")
                || name.equals("inspect_empty_xmag")
                || name.startsWith("inspect_xmag_")
                || name.startsWith("inspect_empty_xmag_");
    }

    /**
     * Walks the animation controller tracks and returns the runner that currently plays an inspect
     * animation, or {@code null} when no inspect animation is active.
     */
    @Unique
    private ObjectAnimationRunner taczinspect$findInspectRunner() {
        AnimationStateContext self = (AnimationStateContext) (Object) this;
        AnimationStateMachine<?> machine = self.getStateMachine();
        if (machine == null) {
            return null;
        }
        AnimationController controller = machine.getAnimationController();
        if (controller == null) {
            return null;
        }

        for (int track = 0; track < TACZINSPECT_MAX_TRACKS; track++) {
            ObjectAnimationRunner runner = controller.getAnimation(track);
            if (runner == null) {
                continue;
            }
            // A new animation is blended in as the transition target of the current runner.
            ObjectAnimationRunner target = runner.getTransitionTo();
            if (target == null) {
                target = runner;
            }
            if (target.isStopped()) {
                continue;
            }
            var animation = target.getAnimation();
            if (animation != null && taczinspect$isInspectName(animation.name)) {
                return target;
            }
        }
        return null;
    }

    @Override
    public boolean taczinspect$isInspecting() {
        return taczinspect$findInspectRunner() != null;
    }

    @Override
    public float taczinspect$getInspectProgressSeconds() {
        ObjectAnimationRunner runner = taczinspect$findInspectRunner();
        return runner == null ? -1.0F : runner.getProgressNs() / 1.0E9F;
    }

    @Override
    public float taczinspect$getInspectTotalSeconds() {
        ObjectAnimationRunner runner = taczinspect$findInspectRunner();
        return runner == null ? -1.0F : runner.getAnimation().getMaxEndTimeS();
    }
}
