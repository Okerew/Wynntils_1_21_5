/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public final class InventoryUtils {
    public static final short COMPASS_SLOT_NUM = 7;
    public static final short CONTENT_BOOK_SLOT_NUM = 8;
    public static final short INGREDIENT_POUCH_SLOT_NUM = 13;

    private static final int RING_1_SLOT_NUM = 9;
    private static final int RING_2_SLOT_NUM = 10;
    private static final int BRACELET_SLOT_NUM = 11;
    private static final int NECKLACE_SLOT_NUM = 12;
    private static final List<Integer> ACCESSORY_SLOTS =
            List.of(RING_1_SLOT_NUM, RING_2_SLOT_NUM, BRACELET_SLOT_NUM, NECKLACE_SLOT_NUM);

    public static void sendInventorySlotMouseClick(short slotNumber, MouseClickType mouseButton) {
        Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap<>();
        ItemStack itemStack = McUtils.inventory().getItem(slotNumber);
        changedSlots.put(
                slotNumber,
                HashedStack.create(itemStack, McUtils.mc().getConnection().decoratedHashOpsGenenerator()));

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                McUtils.inventoryMenu().containerId,
                McUtils.inventoryMenu().getStateId(),
                slotNumber,
                mouseButton.getButtonNum(),
                ClickType.PICKUP,
                changedSlots,
                HashedStack.EMPTY));
    }

    public static List<ItemStack> getAccessories(Player player) {
        List<ItemStack> accessories = new ArrayList<>();
        ACCESSORY_SLOTS.forEach(
                slot -> accessories.add(player.getInventory().items.get(slot)));
        return accessories;
    }

    public static ItemStack getItemInHand() {
        return McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
    }

    public enum MouseClickType {
        LEFT_CLICK((byte) 0),
        RIGHT_CLICK((byte) 1);

        private final byte buttonNum;

        MouseClickType(byte buttonNum) {
            this.buttonNum = buttonNum;
        }

        public byte getButtonNum() {
            return buttonNum;
        }
    }
}
