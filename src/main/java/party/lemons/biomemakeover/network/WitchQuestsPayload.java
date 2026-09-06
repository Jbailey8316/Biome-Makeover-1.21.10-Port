package party.lemons.biomemakeover.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import party.lemons.biomemakeover.BiomeMakeover;

public record WitchQuestsPayload(int menuId, CompoundTag quests) implements CustomPacketPayload {
    public static final Type<WitchQuestsPayload> TYPE = new Type<>(BiomeMakeover.id("witch_quests"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WitchQuestsPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> { buf.writeVarInt(payload.menuId); buf.writeNbt(payload.quests); },
        buf -> new WitchQuestsPayload(buf.readVarInt(), buf.readNbt()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
