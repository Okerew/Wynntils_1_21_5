/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.models.worlds.actionbar.matchers.CharacterCreationSegmentMatcher;
import com.wynntils.models.worlds.actionbar.matchers.CharacterSelectionSegmentMatcher;
import com.wynntils.models.worlds.actionbar.matchers.WynncraftVersionSegmentMatcher;
import com.wynntils.models.worlds.actionbar.segments.CharacterCreationSegment;
import com.wynntils.models.worlds.actionbar.segments.CharacterSelectionSegment;
import com.wynntils.models.worlds.actionbar.segments.WynncraftVersionSegment;
import com.wynntils.models.worlds.bossbars.SkipCutsceneBar;
import com.wynntils.models.worlds.event.CutsceneStartedEvent;
import com.wynntils.models.worlds.event.StreamModeEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.CutsceneState;
import com.wynntils.models.worlds.type.ServerRegion;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.models.worlds.type.WynncraftVersion;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public final class WorldStateModel extends Model {
    private static final UUID WORLD_NAME_UUID = UUID.fromString("16ff7452-714f-2752-b3cd-c3cb2068f6af");
    private static final Pattern WORLD_NAME = Pattern.compile("^§f {2}§lGlobal \\[(.*)\\]$");
    private static final Pattern HOUSING_NAME = Pattern.compile("^§f  §l([^§\"\\\\]{1,35})$");
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com \n$");
    private static final Pattern STREAMER_MESSAGE =
            Pattern.compile("§a(?:\uE008\uE002|\uE001) Streamer mode (disabled|was enabled)\\..*", Pattern.DOTALL);
    private static final Position CHARACTER_SELECTION_POSITION = new Vec3(-1337.5, 16.2, -1120.5);
    private static final String WYNNCRAFT_BETA_NAME = "beta";
    private static final String UNKNOWN_WORLD = "WC??";

    private static final SkipCutsceneBar skipCutsceneBar = new SkipCutsceneBar();
    private CutsceneState cutsceneState = CutsceneState.NOT_IN_CUTSCENE;

    private StyledText currentTabListFooter = StyledText.EMPTY;
    private String currentWorldName = "";
    private String currentHousingName = "";
    private ServerRegion currentRegion = ServerRegion.WC;
    private long serverJoinTimestamp = 0;
    private boolean onBetaServer;
    private boolean hasJoinedAnyWorld = false;
    private boolean inStream = false;
    private boolean onHousing = false;
    private WynncraftVersion worldVersion = null;

    public WorldStateModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new CharacterCreationSegmentMatcher());
        Handlers.ActionBar.registerSegment(new CharacterSelectionSegmentMatcher());
        Handlers.ActionBar.registerSegment(new WynncraftVersionSegmentMatcher());
        Handlers.BossBar.registerBar(skipCutsceneBar);
    }

    private WorldState currentState = WorldState.NOT_CONNECTED;

    public boolean onWorld() {
        return currentState == WorldState.WORLD;
    }

    public boolean onHousing() {
        return onHousing;
    }

    public boolean isInStream() {
        return inStream;
    }

    public boolean isOnBetaServer() {
        return onBetaServer;
    }

    public WynncraftVersion getWorldVersion() {
        return worldVersion;
    }

    public WorldState getCurrentState() {
        return currentState;
    }

    private void setState(WorldState newState, String newWorldName, boolean isFirstJoinWorld) {
        if (newState == currentState && newWorldName.equals(currentWorldName)) return;

        // Streamer mode is always disabled upon changing world state
        inStream = false;
        WynntilsMod.postEvent(new StreamModeEvent(inStream));

        cutsceneEnded();
        WorldState oldState = currentState;
        // Switch state before sending event
        currentState = newState;
        currentWorldName = newWorldName;
        if (newState == WorldState.WORLD) {
            serverJoinTimestamp = System.currentTimeMillis();
        }

        if (currentWorldName.length() >= 2) {
            String region = currentWorldName.substring(0, 2);
            currentRegion = ServerRegion.fromString(region);
        }

        WynntilsMod.postEvent(new WorldStateEvent(newState, oldState, newWorldName, isFirstJoinWorld));
    }

    private void setState(WorldState newState) {
        setState(newState, "", false);
    }

    @SubscribeEvent
    public void disconnected(WynncraftConnectionEvent.Disconnected e) {
        setState(WorldState.NOT_CONNECTED);
    }

    @SubscribeEvent
    public void connecting(WynncraftConnectionEvent.Connected e) {
        if (currentState != WorldState.NOT_CONNECTED) {
            WynntilsMod.error("Got connected event while already connected to server: " + e.getHost());
            currentState = WorldState.NOT_CONNECTED;
            currentWorldName = "";
        }

        String host = e.getHost();
        onBetaServer = host.equals(WYNNCRAFT_BETA_NAME);
        setState(WorldState.CONNECTING);
        currentTabListFooter = StyledText.EMPTY;
    }

    @SubscribeEvent
    public void remove(PlayerLogOutEvent e) {
        if (e.getId().equals(WORLD_NAME_UUID) && !currentWorldName.isEmpty()) {
            setState(WorldState.INTERIM);
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent e) {
        StyledText styledText =
                StyledTextUtils.unwrap(e.getOriginalStyledText()).stripAlignment();
        Matcher matcher = styledText.getMatcher(STREAMER_MESSAGE);

        if (matcher.matches()) {
            inStream = matcher.group(1).equals("was enabled");
            WynntilsMod.postEvent(new StreamModeEvent(inStream));
        }
    }

    @SubscribeEvent
    public void onTeleport(PlayerTeleportEvent e) {
        if (PosUtils.isSame(e.getNewPosition(), CHARACTER_SELECTION_POSITION)) {
            // We get here even if the character selection menu will not show up because of autojoin
            if (getCurrentState() != WorldState.CHARACTER_SELECTION) {
                // Sometimes the TP comes after the character selection menu, instead of before
                // Don't lose the CHARACTER_SELECTION state if that is the case
                setState(WorldState.INTERIM);
            }
        }
    }

    @SubscribeEvent
    public void onTabListFooter(PlayerInfoFooterChangedEvent e) {
        StyledText footer = e.getFooter();
        if (footer.equals(currentTabListFooter)) return;

        currentTabListFooter = footer;

        if (!footer.isEmpty()) {
            if (footer.getMatcher(HUB_NAME).find()) {
                setState(WorldState.HUB);
            }
        }
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(CharacterCreationSegment.class, this::onCharacterCreation);
        event.runIfPresent(CharacterSelectionSegment.class, this::onCharacterSelection);
        event.runIfPresent(WynncraftVersionSegment.class, this::setWorldVersion);
    }

    private void onCharacterCreation(CharacterCreationSegment segment) {
        setState(WorldState.CHARACTER_SELECTION);
    }

    private void onCharacterSelection(CharacterSelectionSegment segment) {
        setState(WorldState.CHARACTER_SELECTION);
    }

    private void setWorldVersion(WynncraftVersionSegment segment) {
        worldVersion = segment.getWynncraftVersion();
    }

    @SubscribeEvent
    public void update(PlayerDisplayNameChangeEvent e) {
        if (!e.getId().equals(WORLD_NAME_UUID)) return;
        if (inStream) return;

        Component displayName = e.getDisplayName();
        StyledText name = StyledText.fromComponent(displayName);
        Matcher m = name.getMatcher(WORLD_NAME);
        if (setWorldIfMatched(m, false)) return;
        // must check in this order as housing name regex matches anything that WORLD_NAME would match, housing names
        // need to exclude world names.
        Matcher housingNameMatcher = name.getMatcher(HOUSING_NAME);
        setWorldIfMatched(housingNameMatcher, true);
    }

    private boolean setWorldIfMatched(Matcher m, boolean housing) {
        if (m.find()) {
            String worldName = housing ? currentWorldName : m.group(1);
            if (worldName.isEmpty() && housing) {
                worldName = UNKNOWN_WORLD;
                WynntilsMod.warn("Changed world via housing join, current world name is unknown");
            }
            setState(WorldState.WORLD, worldName, !hasJoinedAnyWorld);
            hasJoinedAnyWorld = true;
            onHousing = housing;
            currentHousingName = onHousing ? m.group(1) : "";
            return true;
        }
        return false;
    }

    public void cutsceneStarted(boolean groupCutscene) {
        if (cutsceneState == CutsceneState.NOT_IN_CUTSCENE) {
            cutsceneState = CutsceneState.IN_CUTSCENE;

            CutsceneStartedEvent event = new CutsceneStartedEvent(groupCutscene);
            WynntilsMod.postEvent(event);

            if (event.isCanceled()) {
                cutsceneState = CutsceneState.SKIPPED_CUTSCENE;
            }
        }
    }

    public void cutsceneEnded() {
        cutsceneState = CutsceneState.NOT_IN_CUTSCENE;
    }

    /**
     * @return Full name of the current world, such as "NA32"
     */
    public String getCurrentWorldName() {
        return currentWorldName;
    }

    public ServerRegion getCurrentServerRegion() {
        return currentRegion;
    }

    public String getCurrentHousingName() {
        return currentHousingName;
    }

    public long getServerJoinTimestamp() {
        return serverJoinTimestamp;
    }
}
