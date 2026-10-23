package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import fr.axephir.tnd.progression.PlayerProgression;
import fr.axephir.tnd.world.DevelopmentCommands;

public final class SanctuaryRegions {
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.neoforged.neoforge.attachment.AttachmentType<?>,net.neoforged.neoforge.attachment.AttachmentType<Boolean>> GENERATED=PlayerProgression.TYPES.register("observatory_chunk",()->net.neoforged.neoforge.attachment.AttachmentType.builder(()->false).serialize(Codec.BOOL.fieldOf("observatory")).build());
    public static boolean contains(int x,int z){return SanctuaryRoute.nearest(x,z).contains(x,z);}
    public static boolean protectedAt(Level world,BlockPos pos){return world.dimension().equals(DevelopmentCommands.DIMENSION)&&contains(pos.getX(),pos.getZ())&&world.hasChunkAt(pos)&&world.getChunkAt(pos).getData(GENERATED);}
    public static BlockPos station(SanctuaryRoute.Site site,int index){return new BlockPos(site.x()+SanctuaryRoute.x(index),SanctuaryRoute.FLOOR+1,site.z()+SanctuaryRoute.z(index)+18);}
    private SanctuaryRegions(){}
}
