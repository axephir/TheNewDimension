package fr.axephir.tnd.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.axephir.tnd.TheNewDimension;
import fr.axephir.tnd.domain.Profile;
import fr.axephir.tnd.domain.ActivityLedger;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PlayerProgression {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, TheNewDimension.ID);
    private static final Codec<Profile> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(Profile.decode(value)); }
        catch (IllegalArgumentException e) { return DataResult.error(e::getMessage); }
    }, Profile::encode);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Profile>> PROFILE = TYPES.register("profile",
            () -> AttachmentType.builder(Profile::empty)
                    .serialize(CODEC.fieldOf("profile"))
                    .copyOnDeath()
                    .sync((holder, receiver) -> holder == receiver,
                            ByteBufCodecs.STRING_UTF8.map(Profile::decode, Profile::encode))
                    .build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> SENSE_NEXT_TICK = TYPES.register("sense_next_tick",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf("next_tick")).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ActivityLedger>> ACTIVITIES = TYPES.register("activities",
            () -> AttachmentType.builder(ActivityLedger::empty).serialize(Codec.STRING.comapFlatMap(value -> {
                try { return DataResult.success(ActivityLedger.decode(value)); }
                catch(IllegalArgumentException e) { return DataResult.error(e::getMessage); }
            },ActivityLedger::encode).fieldOf("activities")).copyOnDeath().build());

    private PlayerProgression() {}
}
