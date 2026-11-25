package com.taczinspect.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.taczinspect.api.IInspectAccessor;
import com.taczinspect.compat.MeleeDisplayCompat;
import me.xjqsh.lrtactical.api.animation.MeleeAnimationStateContext;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla view bobbing while the held gun / melee weapon plays its inspect
 * animation, so the inspect animation is not disturbed by camera sway.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    private static boolean taczinspect$isHeldItemInspecting(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        if (item instanceof IGun) {
            var display = TimelessAPI.getGunDisplay(stack);
            if (display.isEmpty()) {
                return false;
            }
            LuaAnimationStateMachine<GunAnimationStateContext> machine =
                    display.get().getAnimationStateMachine();
            if (machine == null) {
                return false;
            }
            GunAnimationStateContext context = machine.getContext();
            return context != null && ((IInspectAccessor) context).taczinspect$isInspecting();
        }

        if (item instanceof IMeleeWeapon) {
            var display = MeleeDisplayCompat.getMeleeDisplay(stack);
            if (display.isEmpty()) {
                return false;
            }
            LuaAnimationStateMachine<MeleeAnimationStateContext> machine =
                    display.get().getStateMachine();
            if (machine == null) {
                return false;
            }
            MeleeAnimationStateContext context = machine.getContext();
            return context != null && ((IInspectAccessor) context).taczinspect$isInspecting();
        }

        return false;
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void taczinspect$cancelBobViewDuringInspect(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && taczinspect$isHeldItemInspecting(mc.player.getMainHandItem())) {
            ci.cancel();
        }
    }
}
