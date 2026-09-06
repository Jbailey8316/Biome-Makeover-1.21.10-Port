package party.lemons.biomemakeover.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import party.lemons.biomemakeover.BiomeMakeover;

public record CompleteWitchQuestPayload(int index) implements CustomPacketPayload {
    public static final Type<CompleteWitchQuestPayload> TYPE = new Type<>(BiomeMakeover.id("complete_witch_quest"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompleteWitchQuestPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, CompleteWitchQuestPayload::index, CompleteWitchQuestPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
