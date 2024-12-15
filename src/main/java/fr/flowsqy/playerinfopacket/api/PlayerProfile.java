package fr.flowsqy.playerinfopacket.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PlayerProfile(@NotNull UUID id, @NotNull String name) {
}
