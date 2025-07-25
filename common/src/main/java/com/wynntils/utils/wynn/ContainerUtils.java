/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public final class ContainerUtils {
    private static final short INVENTORY_SLOTS = 36;

    public static NonNullList<ItemStack> getItems(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            return containerScreen.getMenu().getItems();
        }

        // Defensive programming, should not really happen
        return NonNullList.create();
    }

    public static boolean openInventory(short slotNum) {
        int containerId = McUtils.containerMenu().containerId;
        if (containerId != 0) {
            // Another inventory is already open, cannot do this
            return false;
        }

        NonNullList<ItemStack> items = McUtils.containerMenu().getItems();
        // We need to offset the slot number so that it corresponds to the correct slot in the inventory
        clickOnSlot((short) (INVENTORY_SLOTS + slotNum), containerId, (byte) GLFW.GLFW_MOUSE_BUTTON_LEFT, items);

        return true;
    }

    /**
     * Clicks on a slot in the specified container. containerId and the list of items should correspond to the
     * same container!
     */
    public static void clickOnSlot(short clickedSlot, int containerId, byte mouseButton, List<ItemStack> items) {
        Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(
                clickedSlot,
                HashedStack.create(
                        new ItemStack(Items.AIR), McUtils.mc().getConnection().decoratedHashOpsGenenerator()));

        List<HashedStack> hashedItems = items.stream()
                .map(itemStack -> HashedStack.create(
                        itemStack, McUtils.mc().getConnection().decoratedHashOpsGenenerator()))
                .toList();

        // FIXME: To expand usage of this function, the following variables needs to
        // be properly handled
        int transactionId = 0;

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                mouseButton,
                ClickType.PICKUP,
                changedSlots,
                hashedItems.get(clickedSlot)));
    }

    public static void shiftClickOnSlot(short clickedSlot, int containerId, byte mouseButton, List<ItemStack> items) {
        Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(
                clickedSlot,
                HashedStack.create(
                        new ItemStack(Items.AIR), McUtils.mc().getConnection().decoratedHashOpsGenenerator()));

        List<HashedStack> hashedItems = items.stream()
                .map(itemStack -> HashedStack.create(
                        itemStack, McUtils.mc().getConnection().decoratedHashOpsGenenerator()))
                .toList();

        int transactionId = 0;

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                mouseButton,
                ClickType.QUICK_MOVE,
                changedSlots,
                hashedItems.get(clickedSlot)));
    }

    public static void pressKeyOnSlot(short clickedSlot, int containerId, byte buttonNum, List<ItemStack> items) {
        Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(
                clickedSlot,
                HashedStack.create(
                        new ItemStack(Items.AIR), McUtils.mc().getConnection().decoratedHashOpsGenenerator()));

        List<HashedStack> hashedItems = items.stream()
                .map(itemStack -> HashedStack.create(
                        itemStack, McUtils.mc().getConnection().decoratedHashOpsGenenerator()))
                .toList();

        int transactionId = 0;

        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                buttonNum,
                ClickType.SWAP,
                changedSlots,
                hashedItems.get(clickedSlot)));
    }

    public static void closeContainer(int containerId) {
        McUtils.sendPacket(new ServerboundContainerClosePacket(containerId));
    }

    /**
     * Closes invisible containers opened in the background, without closing the visible screen.
     */
    public static void closeBackgroundContainer() {
        McUtils.sendPacket(new ServerboundContainerClosePacket(McUtils.player().containerMenu.containerId));
        McUtils.player().containerMenu = McUtils.player().inventoryMenu;
    }
}
