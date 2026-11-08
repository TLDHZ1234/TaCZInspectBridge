package com.taczinspect.mixin;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.taczinspect.api.IInspectAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
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

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    // display 缓存：key = (item, 静态 id)。开火/换弹/攻击时 id 不变，命中缓存。
    @Unique private Item taczinspect$lastItem = null;
    @Unique private ResourceLocation taczinspect$lastIdA = null; // gunId 或 melee displayId
    @Unique private ResourceLocation taczinspect$lastIdB = null; // gunDisplayId 或 melee id
    @Unique private Object taczinspect$lastDisplay = null;       // GunDisplayInstance 或 MeleeDisplayInstance
    @Unique private int taczinspect$lastDisplayType = 0;         // 0=无 1=gun 2=melee

    @Unique
    private boolean taczinspect$checkCurrentItemInspect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        ResourceLocation idA = null, idB = null;
        if (item instanceof IGun gun) {
            idA = gun.getGunId(stack);
            idB = gun.getGunDisplayId(stack);
        } else if (item instanceof IMeleeWeapon melee) {
            idA = melee.getDisplayId(stack);
            idB = melee.getId(stack);
        }

        // 缓存命中：item 相同且两个静态 id 相同（开火/换弹/攻击不失效）
        if (item != taczinspect$lastItem
                || !Objects.equals(idA, taczinspect$lastIdA)
                || !Objects.equals(idB, taczinspect$lastIdB)) {
            taczinspect$lastItem = item;
            taczinspect$lastIdA = idA;
            taczinspect$lastIdB = idB;

            var gunOpt = TimelessAPI.getGunDisplay(stack);
            if (gunOpt.isPresent()) {
                taczinspect$lastDisplay = gunOpt.get();
                taczinspect$lastDisplayType = 1;
            } else {
                var meleeOpt = LrTacticalAPI.getMeleeDisplay(stack);
                if (meleeOpt.isPresent()) {
                    taczinspect$lastDisplay = meleeOpt.get();
                    taczinspect$lastDisplayType = 2;
                } else {
                    taczinspect$lastDisplay = null;
                    taczinspect$lastDisplayType = 0;
                }
            }
        }

        if (taczinspect$lastDisplayType == 1) {
            LuaAnimationStateMachine<GunAnimationStateContext> sm =
                    ((GunDisplayInstance) taczinspect$lastDisplay).getAnimationStateMachine();
            if (sm == null) return false;
            GunAnimationStateContext ctx = sm.getContext();
            if (ctx == null) return false;
            return ((IInspectAccessor) ctx).taczinspect$isInspecting();
        } else if (taczinspect$lastDisplayType == 2) {
            LuaAnimationStateMachine<MeleeAnimationStateContext> sm =
                    ((MeleeDisplayInstance) taczinspect$lastDisplay).getStateMachine();
            if (sm == null) return false;
            MeleeAnimationStateContext ctx = sm.getContext();
            if (ctx == null) return false;
            return ((IInspectAccessor) ctx).taczinspect$isInspecting();
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