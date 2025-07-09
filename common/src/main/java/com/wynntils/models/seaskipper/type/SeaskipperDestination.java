/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper.type;

import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.phys.Vec3;

public record SeaskipperDestination(SeaskipperDestinationProfile profile, SeaskipperDestinationItem item, short slot) {
    public boolean isAvailable() {
        return item != null && slot != -1;
    }

    public boolean isPlayerInside() {
        Vec3 position = McUtils.player().position();

        return position.x() >= profile.startX()
                && position.x() <= profile.endX()
                && position.z() >= profile.startZ()
                && position.z() <= profile.endZ();
    }
}
