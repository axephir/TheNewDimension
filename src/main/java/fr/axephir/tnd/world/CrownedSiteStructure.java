package fr.axephir.tnd.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.axephir.tnd.TheNewDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.*;
import java.util.Optional;

/** A building and its mountain are a single, saved, chunk-clipped structure piece. */
public final class CrownedSiteStructure extends Structure {
    public static final DeferredRegister<StructureType<?>> TYPES=DeferredRegister.create(Registries.STRUCTURE_TYPE,TheNewDimension.ID);
    public static final DeferredRegister<StructurePieceType> PIECES=DeferredRegister.create(Registries.STRUCTURE_PIECE,TheNewDimension.ID);
    public static final MapCodec<CrownedSiteStructure> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            settingsCodec(i),Identifier.CODEC.fieldOf("template").forGetter(s->s.template)
    ).apply(i,CrownedSiteStructure::new));
    public static final DeferredHolder<StructureType<?>,StructureType<CrownedSiteStructure>> TYPE=TYPES.register("crowned_site",()->()->CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=PIECES.register("crowned_site",()->CrownedSitePiece::new);
    private final Identifier template;
    public CrownedSiteStructure(StructureSettings settings,Identifier template){super(settings);this.template=template;}
    @Override public StructureType<?> type(){return TYPE.get();}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        var building=c.structureTemplateManager().get(template);
        if(building.isEmpty())return Optional.empty();
        var size=building.get().getSize();
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
        int radius=CrownedSitePiece.radius(size.getX(),size.getZ());
        int low=Integer.MAX_VALUE,high=Integer.MIN_VALUE;
        // Sample the whole footprint, not only the entrance chunk. Reject terrain
        // that would bury a wing, cut the crown at the build limit or leave a cliff.
        for(int ix=-2;ix<=2;ix++)for(int iz=-2;iz<=2;iz++){
            int y=c.chunkGenerator().getFirstOccupiedHeight(x+ix*radius/2,z+iz*radius/2,
                    Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState());
            low=Math.min(low,y);high=Math.max(high,y);
        }
        var rotation=Rotation.getRandom(c.random());
        var entrance=new BlockPos((int)Math.round(Math.cos(-Math.PI/2-1.8)*(radius-5)),0,
                (int)Math.round(Math.sin(-Math.PI/2-1.8)*(radius-5))).rotate(rotation);
        int base=c.chunkGenerator().getFirstOccupiedHeight(x+entrance.getX(),z+entrance.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState());
        int summit=Math.max(base+48,high+26);
        if(high-low>72||summit+Math.max(size.getY(),36)>=c.heightAccessor().getMaxY()-2)return Optional.empty();
        var center=new BlockPos(x,summit,z);
        return Optional.of(new GenerationStub(center,b->b.addPiece(new CrownedSitePiece(
                c.structureTemplateManager(),template,center,base,c.heightAccessor().getMinY()+1,rotation))));
    }
}
