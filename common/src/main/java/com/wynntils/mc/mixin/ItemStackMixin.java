/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.event.ItemTooltipFlagsEvent;
import com.wynntils.mc.extension.ItemStackExtension;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtension {
    @Unique
    private ItemAnnotation wynntilsAnnotation;

    @Unique
    private StyledText wynntilsOriginalName;

    @Inject(
            method =
                    "addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onAddTooltipInfo(
            DataComponentType<?> component,
            Item.TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag tooltipFlag,
            CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemTooltipFlagsEvent.HideAdditionalTooltip event =
                new ItemTooltipFlagsEvent.HideAdditionalTooltip(itemStack, component);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @ModifyVariable(
            method =
                    "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private TooltipFlag onGetTooltipLines(TooltipFlag flags) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemTooltipFlagsEvent.Advanced event = new ItemTooltipFlagsEvent.Advanced(itemStack, flags);
        MixinHelper.post(event);

        return event.getFlags();
    }

    @Override
    @Unique
    public ItemAnnotation getAnnotation() {
        return wynntilsAnnotation;
    }

    @Override
    @Unique
    public void setAnnotation(ItemAnnotation annotation) {
        this.wynntilsAnnotation = annotation;
    }

    @Override
    @Unique
    public StyledText getOriginalName() {
        return this.wynntilsOriginalName;
    }

    @Override
    @Unique
    public void setOriginalName(StyledText name) {
        this.wynntilsOriginalName = name;
    }
}
