/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AccountBankContainer extends PersonalStorageContainer implements HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF0\uE00F\uDAFF\uDF68\uF000");
    private static final int FINAL_PAGE = 21;
    private static final List<Short> QUICK_JUMP_DESTINATIONS =
            Stream.of(1, 3, 5, 7, 9, 11, 13, 15, 17).map(Integer::shortValue).toList();

    public AccountBankContainer() {
        super(TITLE_PATTERN, PersonalStorageType.ACCOUNT_BANK, FINAL_PAGE, QUICK_JUMP_DESTINATIONS);
    }
}
