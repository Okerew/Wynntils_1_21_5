/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ItemTooltipFlagsEvent;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class TooltipVanillaHideFeature extends Feature {
    private static final List<DataComponentType<?>> HIDDEN_DATA_COMPONENTS =
            List.of(DataComponents.DYED_COLOR, DataComponents.ENCHANTMENTS);

    @Persisted
    public final Config<Boolean> hideAdvanced = new Config<>(true);

    @Persisted
    public final Config<Boolean> hideAdditionalInfo = new Config<>(true);

    @SubscribeEvent
    public void onTooltipFlagsAdvanced(ItemTooltipFlagsEvent.Advanced event) {
        if (!hideAdvanced.get()) return;

        event.setFlags(TooltipFlag.NORMAL);
    }

    @SubscribeEvent
    public void onTooltipFlagsMask(ItemTooltipFlagsEvent.HideAdditionalTooltip event) {
        if (!hideAdditionalInfo.get()) return;

        if (HIDDEN_DATA_COMPONENTS.contains(event.getDataComponent())) {
            event.setCanceled(true);
        }
    }
}
