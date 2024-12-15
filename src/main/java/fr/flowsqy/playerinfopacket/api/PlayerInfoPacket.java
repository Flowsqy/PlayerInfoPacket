package fr.flowsqy.playerinfopacket.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

public class PlayerInfoPacket {

    private static PlayerInfoPacket instance;
    private final Field ENTRIES_FIELD;

    private PlayerInfoPacket() {
        ENTRIES_FIELD = setupEntriesField();
    }

    public static PlayerInfoPacket getInstance() {
        if (instance == null) {
            instance = new PlayerInfoPacket();
        }
        return instance;
    }

    private Field setupEntriesField() {
        try {
            final var field = ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("b");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPackets(@NotNull Iterable<Packet<?>> packets, @NotNull Iterable<? extends Player> receivers) {
        for (var receiver : receivers) {
            final ServerGamePacketListenerImpl connection = ((CraftPlayer) receiver).getHandle().connection;
            for (var packet : packets) {
                connection.send(packet);
            }
        }
    }

    private void setEntries(@NotNull ClientboundPlayerInfoUpdatePacket packet, @NotNull List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        try {
            ENTRIES_FIELD.set(packet, entries);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNewPlayers(@NotNull Iterable<PlayerInfo> playerInfos, @NotNull Iterable<? extends Player> receivers) {
        final var actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
        final List<ClientboundPlayerInfoUpdatePacket.Entry> entries = new LinkedList<>();
        final var packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
        for (var playerInfo : playerInfos) {
            final var profile = playerInfo.profile();
            final var entry = new ClientboundPlayerInfoUpdatePacket.Entry(profile.id(), new GameProfile(profile.id(), profile.name()), playerInfo.listed(), 0, null, CraftChatMessage.fromStringOrNull(playerInfo.displayName()), null);
            entries.add(entry);
        }
        setEntries(packet, entries);
        sendPackets(Collections.singleton(packet), receivers);
    }

    public void sendUpdatePlayerNames(@NotNull Iterable<PlayerInfo> playerInfos, @NotNull Iterable<? extends Player> receivers) {
        final var actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
        final ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
        final List<ClientboundPlayerInfoUpdatePacket.Entry> entries = new LinkedList<>();
        for (var playerInfo : playerInfos) {
            var entry = new ClientboundPlayerInfoUpdatePacket.Entry(playerInfo.profile().id(), null, false, 0, null, CraftChatMessage.fromStringOrNull(playerInfo.displayName()), null);
            entries.add(entry);
        }
        setEntries(packet, entries);
        sendPackets(Collections.singleton(packet), receivers);
    }

    public void sendRemovePlayers(@NotNull List<UUID> profileIds, @NotNull Iterable<? extends Player> receivers) {
        final var packet = new ClientboundPlayerInfoRemovePacket(profileIds);
        sendPackets(Collections.singleton(packet), receivers);
    }

}
