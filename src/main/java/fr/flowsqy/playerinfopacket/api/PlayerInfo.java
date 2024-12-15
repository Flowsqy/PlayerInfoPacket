package fr.flowsqy.playerinfopacket.api;

import org.jetbrains.annotations.Nullable;

public record PlayerInfo(PlayerProfile profile, boolean listed, @Nullable String displayName) {
}
