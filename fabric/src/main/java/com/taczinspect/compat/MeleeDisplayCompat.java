package com.taczinspect.compat;

import me.xjqsh.lrtactical.client.api.LrTacticalClientAPI;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Access point for the LesRaisins Tactical Equipments display lookup.
 * <p>
 * The Forge / NeoForge builds of lrtactical expose {@code LrTacticalAPI.getMeleeDisplay}, while the
 * Fabric port only has it on {@code LrTacticalClientAPI}. Each loader module ships its own variant of
 * this class so that the shared sources in {@code common/} can stay loader independent.
 */
public final class MeleeDisplayCompat {

    private MeleeDisplayCompat() {
    }

    public static Optional<MeleeDisplayInstance> getMeleeDisplay(ItemStack stack) {
        return LrTacticalClientAPI.getMeleeDisplay(stack);
    }
}
