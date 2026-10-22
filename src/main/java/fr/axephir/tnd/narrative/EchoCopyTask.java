package fr.axephir.tnd.narrative;

import fr.axephir.tnd.city.CityContent;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Source chunks are read only. Bulk section construction and relighting happen solely in the isolated echo dimension. */
public final class EchoCopyTask {
    private final ServerLevel source,echo;private final BlockPos machine,focus;private final int radius,offsetX,offsetZ;private final boolean personalBase;
    private int cursor;private final List<CompletableFuture<ChunkAccess>> lighting=new ArrayList<>();
    private final List<BlockPos> impacts=new ArrayList<>();
    public EchoCopyTask(ServerLevel source,ServerLevel echo,BlockPos machine,int slot,int radius){
        this(source,echo,machine,slot,radius,null);
    }
    public EchoCopyTask(ServerLevel source,ServerLevel echo,BlockPos machine,int slot,int radius,PlayerBase.Site site){
        if(source==echo||!echo.dimension().equals(Convergence.ECHO)||source.dimension().equals(Convergence.ECHO))throw new IllegalArgumentException("A story can only write to an isolated echo");
        if(radius<0||radius>12||slot<0||slot>100000)throw new IllegalArgumentException("Bounded story footprint required");
        this.source=source;this.echo=echo;this.machine=(site==null?machine:site.spawn()).immutable();this.focus=(site==null?machine:site.focus()).immutable();this.radius=radius;personalBase=site!=null;
        machine=this.machine;
        offsetX=plotX(slot)-(machine.getX()&~15);offsetZ=plotZ(slot)-(machine.getZ()&~15);
        if(personalBase){
            for(var target:site.targets()){
                if(Math.abs((target.getX()>>4)-(machine.getX()>>4))>radius||Math.abs((target.getZ()>>4)-(machine.getZ()>>4))>radius)throw new IllegalArgumentException("Base impact outside the bounded copy");
                impacts.add(target.immutable());
            }
            if(impacts.isEmpty()||impacts.size()>8)throw new IllegalArgumentException("A personal base needs one to eight observed impact points");
        }else{
        for(int[] d:new int[][]{{12,0},{-17,8},{7,22},{-24,-14},{28,-21}})impacts.add(machine.offset(d[0],0,d[1]));
        // Breach upper floors and roofs too, rather than leaving an intact building
        // floating over a few floor craters. The restorer's protected core is retained.
        impacts.addAll(List.of(impacts.get(0).above(14),impacts.get(2).above(22),impacts.get(4).above(30)));
        }
    }
    public static int plotX(int slot){return 1024+(slot%128)*2048;}public static int plotZ(int slot){return 1024+(slot/128)*2048;}
    public BlockPos mapped(BlockPos p){return p.offset(offsetX,0,offsetZ);}public int offsetX(){return offsetX;}public int offsetZ(){return offsetZ;}public int radius(){return radius;}
    public BlockPos core(){return mapped(machine);}public BlockPos focus(){return mapped(focus);}
    public int progress(){return Math.min(999,cursor*1000/((radius*2+1)*(radius*2+1)));}
    public boolean finished(){return cursor>=(radius*2+1)*(radius*2+1)&&lighting.stream().allMatch(CompletableFuture::isDone);}
    public void step(){
        if(cursor>=(radius*2+1)*(radius*2+1)){for(var f:lighting)if(f.isCompletedExceptionally())f.join();return;}
        int side=radius*2+1,cx=(machine.getX()>>4)+cursor%side-radius,cz=(machine.getZ()>>4)+cursor/side-radius;cursor++;
        LevelChunk from=source.getChunk(cx,cz),to=echo.getChunk(cx+(offsetX>>4),cz+(offsetZ>>4));
        var localImpacts=new ArrayList<BlockPos>();if(!personalBase)for(var p:impacts)if(p.getX()>=cx*16-10&&p.getX()<=cx*16+25&&p.getZ()>=cz*16-10&&p.getZ()<=cz*16+25)localImpacts.add(p);
        // Ruined technology scatters craters around the infrastructure the player actually built.
        if(!personalBase)for(var be:from.getBlockEntities().values())if(BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).getNamespace().equals("tnd")&&be.getBlockPos().distSqr(machine)>36){if(localImpacts.size()<12)localImpacts.add(be.getBlockPos());}
        var entities=new ArrayList<Map.Entry<BlockPos,BlockState>>();var pos=new BlockPos.MutableBlockPos();
        for(int sectionIndex=0;sectionIndex<from.getSectionsCount();sectionIndex++){
            var input=from.getSection(sectionIndex);if(input.hasOnlyAir())continue;int minY=from.getSectionYFromSectionIndex(sectionIndex)*16;if(minY<echo.getMinY()||minY+15>echo.getMaxY())continue;
            var output=to.getSection(to.getSectionIndex(minY));
            for(int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++){
                var block=input.getBlockState(x,y,z);if(block.isAir())continue;int sx=cx*16+x,sy=minY+y,sz=cz*16+z;var changed=ruin(block,sx,sy,sz,localImpacts);
                if(changed.isAir())continue;output.setBlockState(x,y,z,changed);if(changed.hasBlockEntity())entities.add(Map.entry(new BlockPos(sx+offsetX,sy,sz+offsetZ),changed));
            }
        }
        // Only empty native block entities are constructed. Source container data, energy, commands and inventories are never copied.
        for(var entry:entities){to.setBlockState(entry.getKey(),Blocks.AIR.defaultBlockState(),Block.UPDATE_SKIP_ALL_SIDEEFFECTS);to.setBlockState(entry.getKey(),entry.getValue(),Block.UPDATE_SKIP_ALL_SIDEEFFECTS);}
        Heightmap.primeHeightmaps(to,EnumSet.of(Heightmap.Types.MOTION_BLOCKING,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,Heightmap.Types.OCEAN_FLOOR,Heightmap.Types.WORLD_SURFACE));to.initializeLightSources();to.markUnsaved();
        var engine=echo.getChunkSource().getLightEngine();lighting.add(engine.initializeLight(to,false).thenCompose(chunk->engine.lightChunk(chunk,false)));engine.tryScheduleUpdate();
    }
    /** A bounded visible collapse during the camera shot; only already copied, loaded echo blocks are touched. */
    public BlockPos cinematicImpact(int step){
        if(!finished())throw new IllegalStateException("A cinematic collapse requires a completed isolated copy");
        int index=Math.floorMod(step,personalBase?impacts.size():5);var center=mapped(personalBase?impacts.get(index):impacts.get(index).offset(0,14,(index&1)==0?8:-8));
        int reach=personalBase?5:4;
        for(var p:BlockPos.betweenClosed(center.offset(-reach,-reach,-reach),center.offset(reach,reach,reach))){
            if(p.distSqr(center)>reach*reach||p.getY()<echo.getMinY()||p.getY()>echo.getMaxY()||!inside(p)||!echo.hasChunkAt(p)||protectedCore(p))continue;
            var state=echo.getBlockState(p);if(!state.isAir()&&!state.is(Blocks.BEDROCK))echo.setBlock(p,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        }
        // Remove a narrow column of unsupported upper debris during the same shot, rather than leave an intact roof over the blast.
        if(personalBase)for(var p:BlockPos.betweenClosed(center.offset(-2,6,-2),center.offset(2,24,2))){
            if(p.getY()>echo.getMaxY()||!inside(p)||!echo.hasChunkAt(p)||protectedCore(p))continue;
            var state=echo.getBlockState(p);if(!state.isAir()&&!state.is(Blocks.BEDROCK))echo.setBlock(p,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        }
        return center;
    }
    private boolean inside(BlockPos p){var core=core();return Math.abs((p.getX()>>4)-(core.getX()>>4))<=radius&&Math.abs((p.getZ()>>4)-(core.getZ()>>4))<=radius;}
    private boolean protectedCore(BlockPos p){var core=core();return personalBase?p.getX()==core.getX()&&p.getZ()==core.getZ()&&p.getY()<=core.getY()&&p.getY()>=core.getY()-2:Math.abs(p.getX()-core.getX())<5&&Math.abs(p.getZ()-core.getZ())<5;}
    private BlockState ruin(BlockState block,int x,int y,int z,List<BlockPos> impacts){
        if(x==machine.getX()&&y==machine.getY()&&z==machine.getZ())return ConvergenceContent.RESTORER.get().defaultBlockState();
        if(block.hasBlockEntity()&&!BuiltInRegistries.BLOCK.getKey(block.getBlock()).getNamespace().equals("minecraft"))return CityContent.DECOR.get("vent_panel").get().defaultBlockState();
        if(block.is(Blocks.BEDROCK))return block;
        if(!personalBase&&Math.abs(x-machine.getX())<5&&Math.abs(z-machine.getZ())<5)return block;
        for(var impact:impacts){double dx=x-impact.getX(),dz=z-impact.getZ(),dy=(y-impact.getY())*.65,d=dx*dx+dz*dz+dy*dy;
            if(d<55)return Blocks.AIR.defaultBlockState();if(d<75&&Math.floorMod(x*31+y*17+z*13,5)==0)return CityContent.DECOR.get("carbon_panel").get().defaultBlockState();}
        return block;
    }
}
