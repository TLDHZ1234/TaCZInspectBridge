package com.taczinspect.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.taczinspect.api.IInspectAccessor;
import com.taczinspect.compat.MeleeDisplayCompat;
import me.xjqsh.lrtactical.api.animation.MeleeAnimationStateContext;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * Suppresses the vanilla view bobbing while the held gun / melee weapon plays its inspect
 * animation, so the inspect animation is not disturbed by camera sway.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    // Display cache: key = (item, static ids). Firing / reloading / attacking does not change those
    // ids, so the cache keeps hitting while the animation state changes every tick.
    @Unique private Item taczinspect$lastItem = null;
    @Unique private ResourceLocation taczinspect$lastIdA = null; // gunId or melee displayId
    @Unique private ResourceLocation taczinspect$lastIdB = null; // gunDisplayId or melee id
    @Unique private Object taczinspect$lastDisplay = null;       // GunDisplayInstance or MeleeDisplayInstance
    @Unique private int taczinspect$lastDisplayType = 0;         // 0 = none, 1 = gun, 2 = melee

    @Unique
    private boolean taczinspect$checkCurrentItemInspect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        ResourceLocation idA = null;
        ResourceLocation idB = null;
        if (item instanceof IGun gun) {
            idA = gun.getGunId(stack);
            idB = gun.getGunDisplayId(stack);
        } else if (item instanceof IMeleeWeapon melee) {
            idA = melee.getDisplayId(stack);
            idB = melee.getId(stack);
        }

        if (item != taczinspect$lastItem
                || !Objects.equals(idA, taczinspect$lastIdA)
                || !Objects.equals(idB, taczinspect$lastIdB)) {
            taczinspect$lastItem = item;
            taczinspect$lastIdA = idA;
            taczinspect$lastIdB = idB;

            var gunDisplay = TimelessAPI.getGunDisplay(stack);
            if (gunDisplay.isPresent()) {
                taczinspect$lastDisplay = gunDisplay.get();
                taczinspect$lastDisplayType = 1;
            } else {
                var meleeDisplay = MeleeDisplayCompat.getMeleeDisplay(stack);
                if (meleeDisplay.isPresent()) {
                    taczinspect$lastDisplay = meleeDisplay.get();
                    taczinspect$lastDisplayType = 2;
                } else {
                    taczinspect$lastDisplay = null;
                    taczinspect$lastDisplayType = 0;
                }
            }
        }

        if (taczinspect$lastDisplayType == 1) {
            LuaAnimationStateMachine<GunAnimationStateContext> machine =
                    ((GunDisplayInstance) taczinspect$lastDisplay).getAnimationStateMachine();
            if (machine == null) {
                return false;
            }
            GunAnimationStateContext context = machine.getContext();
            return context != null && ((IInspectAccessor) context).taczinspect$isInspecting();
        } else if (taczinspect$lastDisplayType == 2) {
            LuaAnimationStateMachine<MeleeAnimationStateContext> machine =
                    ((MeleeDisplayInstance) taczinspect$lastDisplay).getStateMachine();
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
        if (taczinspect$checkCurrentItemInspect()) {
            ci.cancel();
        }
    }
}
