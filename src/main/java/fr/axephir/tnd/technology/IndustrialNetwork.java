package fr.axephir.tnd.technology;

import net.minecraft.core.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import java.util.*;

/** Bounded loaded-chunk traversal. Cables store nothing; all movement is atomic and conserves FE. */
final class IndustrialNetwork {
    static void distribute(IndustrialEntity source){
        var w=source.getLevel();var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(source.getBlockPos());seen.add(source.getBlockPos());
        var ports=new LinkedHashMap<BlockPos,Direction>();int visited=0;
        while(!queue.isEmpty()&&visited++<128){var p=queue.remove();for(var d:Direction.values()){
            var next=p.relative(d);if(!seen.add(next)||!w.hasChunkAt(next))continue;
            if(w.getBlockState(next).is(Industry.CABLE.get()))queue.add(next);
            else if(w.getCapability(Capabilities.Energy.BLOCK,next,d.getOpposite())!=null)ports.put(next,d.getOpposite());
        }}
        var entries=new ArrayList<>(ports.entrySet());if(entries.isEmpty())return;
        int budget=2560,start=Math.floorMod(source.networkRound++,entries.size());
        // Loads first, batteries absorb the surplus; accumulators never shuffle energy amongst themselves.
        for(int pass=0;pass<2;pass++)for(int i=0;i<entries.size()&&budget>0;i++){
            var e=entries.get((start+i)%entries.size());var other=w.getBlockEntity(e.getKey());
            boolean battery=other instanceof IndustrialEntity ie&&ie.kind==Industry.Kind.BATTERY;
            if(other instanceof IndustrialEntity ie&&ie.kind.source()&&!battery)continue;
            if(battery&&(source.kind==Industry.Kind.BATTERY||pass==0)||!battery&&pass==1)continue;
            budget-=EnergyHandlerUtil.move(source.energy,w.getCapability(Capabilities.Energy.BLOCK,e.getKey(),e.getValue()),budget,null);
        }
    }
    private IndustrialNetwork(){}
}
