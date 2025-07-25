/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public class GuildMemberListContainer extends Container implements SearchableContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile(".+: Members");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§a§lNext Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§a§lPrevious Page");

    public GuildMemberListContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public Pattern getNextItemPattern() {
        return NEXT_PAGE_PATTERN;
    }

    @Override
    public Pattern getPreviousItemPattern() {
        return PREVIOUS_PAGE_PATTERN;
    }

    @Override
    public short getNextItemSlot() {
        return 28;
    }

    @Override
    public short getPreviousItemSlot() {
        return 10;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 2, 4, 8);
    }

    @Override
    public List<ItemProviderType> supportedProviderTypes() {
        return List.of();
    }
}
