/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.containers.personal.PersonalStorageContainer;
import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class BankModel extends Model {
    @Persisted
    private final Storage<Short> finalAccountBankPage = new Storage<>((short) 21);

    @Persisted
    private final Storage<Short> finalBlockBankPage = new Storage<>((short) 12);

    @Persisted
    private final Storage<Short> finalBookshelfPage = new Storage<>((short) 10);

    @Persisted
    private final Storage<Short> finalMiscBucketPage = new Storage<>((short) 10);

    @Persisted
    private final Storage<Map<String, Short>> finalCharacterBankPages = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Short, String>> customAccountBankPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Short, String>> customBlockBankPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Short, String>> customBookshelfPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Short, String>> customMiscBucketPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Map<Short, String>>> customCharacterBankPagesNames =
            new Storage<>(new TreeMap<>());

    public static final short QUICK_JUMP_SLOT = 7;
    public static final String FINAL_PAGE_NAME = "\uDB3F\uDFFF";

    private static final short MAX_CHARACTER_BANK_PAGES = 10;
    private static final StyledText LAST_BANK_PAGE_STRING = StyledText.fromString(">§4>§c>§4>§c>");

    private boolean editingName;
    private boolean updatedPage;
    private short currentPage = 1;
    private PersonalStorageContainer personalStorageContainer = null;
    private PersonalStorageType storageContainerType = null;

    public BankModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (!(Models.Container.getCurrentContainer() instanceof PersonalStorageContainer container)) {
            storageContainerType = null;
            return;
        }

        personalStorageContainer = container;

        storageContainerType = personalStorageContainer.getPersonalStorageType();

        editingName = false;
        updatedPage = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenClose(ScreenClosedEvent e) {
        storageContainerType = null;
        currentPage = 1;
        editingName = false;
        updatedPage = false;
    }

    // Swapping between account/character bank or personal/island storage does not
    // send the set slot packets for the slots we need to check so we have to use
    // the set content packet
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (storageContainerType == null) return;

        ItemStack previousPageItem = event.getItems().get(personalStorageContainer.getPreviousItemSlot());
        ItemStack nextPageItem = event.getItems().get(personalStorageContainer.getNextItemSlot());

        updateState(previousPageItem, nextPageItem);

        updatedPage = true;
    }

    // Right clicking the next/previous buttons or using quick jumps with a full inventory
    // does not send the set content packet, so we have to check the set slot packets
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (storageContainerType == null) return;
        if (!updatedPage) return;

        if (event.getSlot() == personalStorageContainer.getPreviousItemSlot()) {
            updateState(event.getItemStack(), ItemStack.EMPTY);
        }

        if (event.getSlot() == personalStorageContainer.getNextItemSlot()) {
            updateState(ItemStack.EMPTY, event.getItemStack());
        }
    }

    public String getPageName(short page) {
        Map<Short, String> pageNamesMap = getCurrentNameMap();

        if (pageNamesMap == null) return I18n.get("feature.wynntils.personalStorageUtilities.page", page);

        return pageNamesMap.getOrDefault(page, I18n.get("feature.wynntils.personalStorageUtilities.page", page));
    }

    public void saveCurrentPageName(String nameToSet) {
        switch (storageContainerType) {
            case ACCOUNT_BANK -> {
                customAccountBankPageNames.get().put(currentPage, nameToSet);
                customAccountBankPageNames.touched();
            }
            case BLOCK_BANK -> {
                customBlockBankPageNames.get().put(currentPage, nameToSet);
                customBlockBankPageNames.touched();
            }
            case BOOKSHELF -> {
                customBookshelfPageNames.get().put(currentPage, nameToSet);
                customBookshelfPageNames.touched();
            }
            case CHARACTER_BANK -> {
                customCharacterBankPagesNames.get().putIfAbsent(Models.Character.getId(), new TreeMap<>());

                Map<Short, String> nameMap = customCharacterBankPagesNames.get().get(Models.Character.getId());

                nameMap.put(currentPage, nameToSet);

                customCharacterBankPagesNames.get().put(Models.Character.getId(), nameMap);
                customCharacterBankPagesNames.touched();
            }
            case MISC_BUCKET -> {
                customMiscBucketPageNames.get().put(currentPage, nameToSet);
                customMiscBucketPageNames.touched();
            }
        }

        editingName = false;
    }

    public void resetCurrentPageName() {
        switch (storageContainerType) {
            case ACCOUNT_BANK -> {
                customAccountBankPageNames.get().remove(currentPage);
                customAccountBankPageNames.touched();
            }
            case BLOCK_BANK -> {
                customBlockBankPageNames.get().remove(currentPage);
                customBlockBankPageNames.touched();
            }
            case BOOKSHELF -> {
                customBookshelfPageNames.get().remove(currentPage);
                customBookshelfPageNames.touched();
            }
            case CHARACTER_BANK -> {
                customCharacterBankPagesNames
                        .get()
                        .getOrDefault(Models.Character.getId(), new TreeMap<>())
                        .remove(currentPage);
                customCharacterBankPagesNames.touched();
            }
            case MISC_BUCKET -> {
                customMiscBucketPageNames.get().remove(currentPage);
                customMiscBucketPageNames.touched();
            }
        }

        editingName = false;
    }

    public short getFinalPage() {
        return switch (storageContainerType) {
            case ACCOUNT_BANK -> finalAccountBankPage.get();
            case BLOCK_BANK -> finalBlockBankPage.get();
            case BOOKSHELF -> finalBookshelfPage.get();
            case CHARACTER_BANK ->
                finalCharacterBankPages.get().getOrDefault(Models.Character.getId(), MAX_CHARACTER_BANK_PAGES);
            case MISC_BUCKET -> finalMiscBucketPage.get();
        };
    }

    public PersonalStorageType getStorageContainerType() {
        return storageContainerType;
    }

    public short getCurrentPage() {
        return currentPage;
    }

    public boolean isEditingName() {
        return editingName;
    }

    public void toggleEditingName(boolean editingName) {
        this.editingName = editingName;
    }

    private void updateState(ItemStack previousPageItem, ItemStack nextPageItem) {
        Matcher previousPageMatcher = StyledText.fromComponent(previousPageItem.getHoverName())
                .getMatcher(personalStorageContainer.getPreviousItemPattern());

        if (previousPageMatcher.matches()) {
            currentPage = (short) (Short.parseShort(previousPageMatcher.group(1)) + 1);
        }

        Matcher nextPageMatcher = StyledText.fromComponent(nextPageItem.getHoverName())
                .getMatcher(personalStorageContainer.getNextItemPattern());

        if (nextPageMatcher.matches()) {
            currentPage = (short) (Short.parseShort(nextPageMatcher.group(1)) - 1);
        }

        if (isItemIndicatingLastBankPage(nextPageItem)) {
            updateFinalPage();
        }
    }

    private boolean isItemIndicatingLastBankPage(ItemStack item) {
        return StyledText.fromComponent(item.getHoverName()).endsWith(LAST_BANK_PAGE_STRING)
                || item.getHoverName().getString().equals(FINAL_PAGE_NAME);
    }

    private void updateFinalPage() {
        switch (storageContainerType) {
            case ACCOUNT_BANK -> {
                finalAccountBankPage.store(currentPage);
            }
            case BLOCK_BANK -> {
                if (currentPage > finalBlockBankPage.get()) {
                    finalBlockBankPage.store(currentPage);
                }
            }
            case BOOKSHELF -> {
                finalBookshelfPage.store(currentPage);
            }
            case CHARACTER_BANK -> {
                finalCharacterBankPages.get().put(Models.Character.getId(), currentPage);
                finalCharacterBankPages.touched();
            }
            case MISC_BUCKET -> {
                finalMiscBucketPage.store(currentPage);
            }
        }
    }

    private Map<Short, String> getCurrentNameMap() {
        return switch (storageContainerType) {
            case ACCOUNT_BANK -> customAccountBankPageNames.get();
            case BLOCK_BANK -> customBlockBankPageNames.get();
            case BOOKSHELF -> customBookshelfPageNames.get();
            case CHARACTER_BANK ->
                customCharacterBankPagesNames.get().getOrDefault(Models.Character.getId(), new TreeMap<>());
            case MISC_BUCKET -> customMiscBucketPageNames.get();
        };
    }
}
