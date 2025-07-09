/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BookshelfContainer extends PersonalStorageContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF0\uE00F\uDAFF\uDF68\uF005");
    private static final int FINAL_PAGE = 10;
    private static final List<Short> QUICK_JUMP_DESTINATIONS =
            Stream.of(1, 3, 5, 7, 9).map(Integer::shortValue).toList();

    public BookshelfContainer() {
        super(TITLE_PATTERN, PersonalStorageType.BOOKSHELF, FINAL_PAGE, QUICK_JUMP_DESTINATIONS);
    }
}
