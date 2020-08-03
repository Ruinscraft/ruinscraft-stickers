package com.ruinscraft.stickers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StickerCodeStorage {

    CompletableFuture<String> queryCode(UUID mojangId);

    CompletableFuture<Void> insertCode(UUID mojangId, String code);

}
