package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import java.util.*;

/** Server-authored first act. Every block mutation is whitelisted and requires an already loaded chunk.
 * No client supplied mask, elapsed wall clock, missing mob or distant interaction awards progress. */
public final class WitnessScene {
    private static Component tr(String key,Object...args){return Component.translatable("observatory.tnd.witness."+key,args);}
    static BlockPos point(SanctuaryNodeEntity n,WitnessAct.Point p){return n.local(p.x(),p.y(),p.z());}
    public static int circuit(SanctuaryNodeEntity n){
        if(!n.enhanced()||n.index()!=5)return 0;int[] tones=new int[3];
        for(int i=0;i<3;i++){var at=point(n,WitnessAct.coupler(i));if(!n.getLevel().hasChunkAt(at))return -1;var s=n.getLevel().getBlockState(at);if(!s.is(SanctuaryContent.COUPLER.get()))return -1;tones[i]=s.getValue(WitnessDeviceBlock.TONE);}
        return WitnessAct.circuit(tones[0],tones[1],tones[2]);
    }
    public static boolean use(SanctuaryNodeEntity n,ServerPlayer p,BlockPos at,WitnessDeviceBlock.Kind kind){
        if(!n.enhanced()||!n.natural()||n.getLevel()!=p.level()||!p.isAlive()||p.isSpectator()||p.distanceToSqr(Vec3.atCenterOf(at))>36||!p.level().hasChunkAt(at)||(!n.available()&&!SanctuaryRoute.done(n.mask(),n.index())))return false;
        var hit=p.level().clip(new ClipContext(p.getEyePosition(),Vec3.atCenterOf(at),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK||!hit.getBlockPos().equals(at))return false;
        if(kind==WitnessDeviceBlock.Kind.RELIQUARY){
            int secret=WitnessAct.secret(n.index());if(secret<0||!point(n,WitnessAct.reliquary(n.index())).equals(at)||!p.level().getBlockState(at).is(SanctuaryContent.RELIQUARY.get()))return false;
            boolean unlocked=switch(n.index()){case 0->SanctuaryRoute.done(n.mask(),0);case 3->n.relays()==15;case 5->n.circuitLatched;default->false;};
            if(!unlocked){p.sendOverlayMessage(tr("sealed."+secret));return false;}
            if(!SanctuaryLedger.get(p.level().getServer()).discover(n.siteKey(),p.getUUID(),secret)){p.sendOverlayMessage(tr("remembered"));return false;}
            n.relicStage=Math.max(1,n.relicStage);n.setChanged();
            give(p,new ItemStack(SanctuaryContent.SHARD.get(),4+secret*2));
            if(secret==1)give(p,new ItemStack(SanctuaryContent.DUST.get(),6));
            if(secret==2)give(p,new ItemStack(SanctuaryContent.CIRCUIT.get()));
            p.sendOverlayMessage(tr("lore."+secret));sound(p.level(),at,"witness_reliquary",.7f,1);return true;
        }
        if(!p.level().getBlockState(at).is(SanctuaryContent.COUPLER.get()))return false;
        if(n.index()==4&&(at.equals(n.local(-42,11,0))||at.equals(n.local(42,11,0)))){
            if(!n.bridgeWanted){n.bridgeWanted=true;n.setChanged();sound(p.level(),at,"witness_bridge",.8f,1);}
            p.sendOverlayMessage(tr(n.bridgeStage==10?"bridge_ready":"bridge_building"));return true;
        }
        if(n.index()!=5)return false;
        for(int i=0;i<3;i++)if(at.equals(point(n,WitnessAct.coupler(i)))){
            if(n.circuitLatched){p.sendOverlayMessage(tr("current_stable"));return false;}
            var s=p.level().getBlockState(at);p.level().setBlock(at,s.setValue(WitnessDeviceBlock.TONE,(s.getValue(WitnessDeviceBlock.TONE)+1)&3),3);
            sound(p.level(),at,"observatory_relay",.35f,.75f+i*.15f);n.setChanged();
            if(circuit(n)==7){n.circuitLatched=true;n.setChanged();sound(p.level(),at,"witness_current",.7f,1);p.sendOverlayMessage(tr("current_stable"));if(n.traces()==7)n.witnessFinish();}
            else p.sendOverlayMessage(tr("coupler_turn"));return true;
        }
        return false;
    }
    private static void give(ServerPlayer p,ItemStack item){if(!p.getInventory().add(item))p.drop(item,false);}
    static void sound(net.minecraft.world.level.Level w,BlockPos at,String cue,float volume,float pitch){w.playSound(null,at,ModContent.atmosphereSound(cue),SoundSource.BLOCKS,volume,pitch);}
    private static boolean near(SanctuaryNodeEntity n,Player p){return p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(n.local(0,1,0)))<58*58;}
    public static void tick(SanctuaryNodeEntity n,ServerLevel w){
        if(w.players().stream().noneMatch(p->near(n,p)))return;
        long now=w.getGameTime();boolean slow=now%20==0;
        if(n.index()==2&&n.available()){
            boolean aligned=true;int[] wanted=n.tones();for(int i=0;i<4;i++)if(n.dialTone(i)!=wanted[i])aligned=false;
            n.alignTicks=aligned?n.alignTicks+5:0;if(n.alignTicks>=20)n.witnessFinish();
        }
        if(n.index()==4&&n.bridgeWanted&&now%10==0)bridge(n,w);
        if(n.index()==3&&n.relays()==15&&n.shutterStage<3&&now%10==0){
            boolean loaded=true;for(int z=6;z<=8;z++)if(!w.hasChunkAt(n.local(43,1+n.shutterStage,z)))loaded=false;
            if(loaded){for(int z=6;z<=8;z++){var at=n.local(43,1+n.shutterStage,z);if(w.getBlockState(at).is(SanctuaryContent.GATE.get()))w.setBlock(at,Blocks.AIR.defaultBlockState(),3);}n.shutterStage++;n.setChanged();if(n.shutterStage==1)sound(w,n.local(43,1,7),"witness_bridge",.5f,.85f);}
        }
        int secret=WitnessAct.secret(n.index());
        if(secret>=0){
            var at=point(n,WitnessAct.reliquary(n.index()));
            if(n.relicStage>0&&now%10==0){if(n.relicStage<3){n.relicStage++;n.setChanged();}if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.RELIQUARY.get()))set(w,at,w.getBlockState(at).setValue(WitnessDeviceBlock.TONE,n.relicStage).setValue(WitnessDeviceBlock.LIT,true));}
            if(now%60==0&&w.players().stream().anyMatch(p->p.distanceToSqr(Vec3.atCenterOf(at))<81))w.sendParticles(ParticleTypes.END_ROD,at.getX()+.5,at.getY()+1.1,at.getZ()+.5,2,.12,.1,.12,.002);
        }
        if(slow){
            if(n.index()==5&&n.circuitLatched)for(int i=0;i<3;i++){var at=point(n,WitnessAct.coupler(i));if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.COUPLER.get()))set(w,at,w.getBlockState(at).setValue(WitnessDeviceBlock.TONE,WitnessAct.facing(i)));}
            illumination(n,w);
        }
        if(now%10==0){
            if(n.index()==2)dialBeams(n,w);
            if(n.index()==5)circuitBeams(n,w);
            if(n.scenePulse>0)ring(w,Vec3.atBottomCenterOf(n.local(0,1,0)),(24-n.scenePulse)*1.6,24);
        }
        if(n.scenePulse>0){n.scenePulse--;n.setChanged();}
    }
    private static void bridge(SanctuaryNodeEntity n,ServerLevel w){
        // Require the span to be resident before advancing its persistent animation stage.
        for(int x=-37;x<=37;x++)for(int z:new int[]{-2,2})if(!w.hasChunkAt(n.local(x,10,z)))return;
        int next=Math.min(10,n.bridgeStage+1);
        for(int x=-37;x<=37;x++)if(WitnessAct.bridgeColumn(next,x))for(int z=-2;z<=2;z++)for(int y=9-Math.abs(x)/12;y<=11;y++)if(!bridgeMaterial(w.getBlockState(n.local(x,y,z))))return;
        for(int x=-37;x<=37;x++)if(WitnessAct.bridgeColumn(next,x))for(int z=-2;z<=2;z++){
            var floor=n.local(x,10,z);if(bridgeMaterial(w.getBlockState(floor)))set(w,floor,SanctuaryContent.block("obsidian_grate").defaultBlockState());
            for(int y=9-Math.abs(x)/12;y<10;y++){var support=n.local(x,y,z);if(bridgeMaterial(w.getBlockState(support)))set(w,support,SanctuaryContent.block("obsidian_ribbed").defaultBlockState());}
            if(Math.abs(z)==2){var rail=n.local(x,11,z);if(bridgeMaterial(w.getBlockState(rail)))set(w,rail,SanctuaryContent.SLAB.get().defaultBlockState());}
        }
        if(n.bridgeStage!=next){n.bridgeStage=next;n.setChanged();if(next==10)sound(w,n.local(0,10,0),"witness_current",.6f,1.2f);}
        for(int side:new int[]{-1,1}){var at=n.local(side*42,11,0);if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.COUPLER.get()))set(w,at,w.getBlockState(at).setValue(WitnessDeviceBlock.LIT,next==10));}
    }
    private static boolean bridgeMaterial(BlockState s){return s.isAir()||s.is(SanctuaryContent.block("obsidian_grate"))||s.is(SanctuaryContent.block("obsidian_ribbed"))||s.is(SanctuaryContent.SLAB.get());}
    private static void set(ServerLevel w,BlockPos at,BlockState state){if(w.hasChunkAt(at)&&!w.getBlockState(at).equals(state))w.setBlock(at,state,3);}
    private static void illumination(SanctuaryNodeEntity n,ServerLevel w){
        boolean done=SanctuaryRoute.done(n.mask(),n.index());
        int progress=done?19:n.index()==0||n.index()==5?Integer.bitCount(n.traces())*6:n.index()==2?(int)java.util.stream.IntStream.range(0,4).filter(i->n.dialTone(i)==n.tones()[i]).count()*4:n.index()==4?n.sequence()*4:n.active()?Math.max(1,n.killed()*19/Math.max(1,n.target())):0;
        if(n.index()==7&&n.active())progress=19;
        int power=n.index()==7?Math.max(1,n.seenPhase+1):1;
        for(int side:new int[]{-1,1})for(int i=0;i<19;i++)light(n,w,n.local(side*8,0,-36+i*4),i<progress?power:0);
        int height=n.index()==7?62:32+n.index()%3*7;
        for(int x=-27;x<=27;x+=3)for(int z:new int[]{-30,-28,28,30})light(n,w,n.local(x,height-7-x*x/96,z),progress>0?power:0);
    }
    private static void light(SanctuaryNodeEntity n,ServerLevel w,BlockPos at,int power){if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.FILAMENT.get()))set(w,at,w.getBlockState(at).setValue(WitnessFilamentBlock.POWER,power));}
    private static void line(ServerLevel w,Vec3 a,Vec3 b){int count=Math.min(12,Math.max(1,(int)(a.distanceTo(b)/2)));for(int i=0;i<=count;i++){var p=a.lerp(b,i/(double)count);w.sendParticles(ParticleTypes.END_ROD,p.x,p.y,p.z,1,0,0,0,0);}}
    private static void dialBeams(SanctuaryNodeEntity n,ServerLevel w){
        var origin=Vec3.atBottomCenterOf(n.local(-24,3,-14));line(w,origin,Vec3.atBottomCenterOf(n.local(-12,3,-14)));
        int[] wanted=n.tones();for(int i=0;i<4;i++){
            int tone=n.dialTone(i);if(tone<0)return;
            var start=Vec3.atBottomCenterOf(n.local(-12+i*8,3,-14));
            var end=tone==wanted[i]?Vec3.atBottomCenterOf(n.local(i==3?24:-4+i*8,3,-14)):start.add(Vec3.atLowerCornerOf(WitnessArchitecture.direction(tone).getUnitVec3i()).scale(4));
            line(w,start,end);
        }
    }
    private static void circuitBeams(SanctuaryNodeEntity n,ServerLevel w){
        int mask=circuit(n);if(mask<0)return;int prefix=WitnessAct.litPrefix(mask);
        Vec3 start=Vec3.atBottomCenterOf(n.local(-30,2,-6));
        for(int i=0;i<3;i++){var at=point(n,WitnessAct.coupler(i));if(!w.hasChunkAt(at))return;var state=w.getBlockState(at);if(!state.is(SanctuaryContent.COUPLER.get()))return;
            var end=Vec3.atCenterOf(at).add(0,.5,0);if(i<=prefix)line(w,start,end);set(w,at,state.setValue(WitnessDeviceBlock.LIT,i<prefix));start=end;
        }
        if(prefix==3)line(w,start,Vec3.atBottomCenterOf(n.local(30,2,-6)));
    }
    public static void bossSpawn(SanctuaryNodeEntity n,SanctuaryConstruct mob,List<ServerPlayer> players){
        n.seenPhase=0;n.setChanged();sound((ServerLevel)n.getLevel(),mob.blockPosition(),"witness_awaken",1,.85f);
        for(var p:players)p.sendOverlayMessage(tr("awakening"));
    }
    public static void phase(SanctuaryNodeEntity n,SanctuaryConstruct mob,int phase){
        if(!n.enhanced()||n.index()!=7||phase<=n.seenPhase)return;n.seenPhase=phase;n.scenePulse=18;n.setChanged();
        var w=(ServerLevel)n.getLevel();sound(w,mob.blockPosition(),"witness_current",.85f,.7f+phase*.2f);for(var p:w.players())if(near(n,p))p.sendOverlayMessage(tr("phase."+phase));
    }
    private static void ring(ServerLevel w,Vec3 at,double r,int count){for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(ParticleTypes.END_ROD,at.x+Math.cos(a)*r,at.y+.15,at.z+Math.sin(a)*r,1,0,0,0,0);}}
    private WitnessScene(){}
}
