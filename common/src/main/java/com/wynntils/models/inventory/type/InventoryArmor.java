/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.inventory.type;

import java.util.Locale;
import net.minecraft.world.entity.EquipmentSlot;

public enum InventoryArmor {
    HELMET("Helmet", 3, EquipmentSlot.HEAD),
    CHESTPLATE("Chestplate", 2, EquipmentSlot.BODY),
    LEGGINGS("Leggings", 1, EquipmentSlot.LEGS),
    BOOTS("Boots", 0, EquipmentSlot.BODY);

    private final String armorName;
    private final int armorSlot;
    private final EquipmentSlot equipmentSlot;

    InventoryArmor(String armorName, int armorSlot, EquipmentSlot equipmentSlot) {
        this.armorName = armorName;
        this.armorSlot = armorSlot;
        this.equipmentSlot = equipmentSlot;
    }

    public int getSlot() {
        return armorSlot;
    }

    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public static InventoryArmor fromString(String type) {
        try {
            return valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int[] getSlots() {
        int[] slots = new int[values().length];

        for (int i = 0; i < values().length; i++) {
            slots[i] = values()[i].getSlot();
        }

        return slots;
    }
}
