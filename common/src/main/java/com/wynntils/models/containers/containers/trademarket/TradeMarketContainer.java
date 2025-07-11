/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.trademarket;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import java.util.regex.Pattern;

public class TradeMarketContainer extends Container
        implements ScrollableContainerProperty, HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFE8\uE011");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    public TradeMarketContainer() {
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
        return 53;
    }

    @Override
    public short getPreviousItemSlot() {
        return 51;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 0, 5, 6);
    }
}
