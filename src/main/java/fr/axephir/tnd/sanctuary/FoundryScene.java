package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.core.ModContent;
import fr.axephir.tnd.domain.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

/** Server-owned foundry circuit, moving machinery, staged illumination and vent openings. */
public final class FoundryScene {
    private static Component tr(String s,Object...args){return Component.translatable("observatory.tnd.foundry."+s,args);}
    public static BlockPos point(SanctuaryNodeEntity n,FoundryAct.Point p){return n.local(p.x(),p.y(),p.z());}
    public static int circuit(SanctuaryNodeEntity n){
        if(!n.foundry()||!n.natural()||FoundryAct.valves(n.index())!=3)return -1;
        int mask=0;for(int i=0;i<3;i++){
            var at=point(n,FoundryAct.valve(n.index(),i));if(!n.getLevel().hasChunkAt(at))return -1;
            var state=n.getLevel().getBlockState(at);if(!state.is(SanctuaryContent.FOUNDRY_VALVE.get()))return -1;
            if(state.getValue(FoundryDeviceBlock.STEP)==FoundryAct.wanted(n.index(),i))mask|=1<<i;
        }return mask;
    }
    public static boolean use(SanctuaryNodeEntity n,ServerPlayer p,BlockPos at){
        if(!n.foundry()||!n.natural()||n.getLevel()!=p.level()||!p.isAlive()||p.isSpectator()||p.distanceToSqr(Vec3.atCenterOf(at))>36||!p.level().hasChunkAt(at)||!p.level().getBlockState(at).is(SanctuaryContent.FOUNDRY_VALVE.get())||!n.available()&&!SanctuaryRoute.done(n.mask(),n.index()))return false;
        var hit=p.level().clip(new ClipContext(p.getEyePosition(),Vec3.atCenterOf(at),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.BLOCK||!hit.getBlockPos().equals(at))return false;
        boolean authored=false;for(int i=0;i<FoundryAct.valves(n.index());i++)if(point(n,FoundryAct.valve(n.index(),i)).equals(at))authored=true;if(!authored)return false;
        if(n.index()==15){
            if(!n.active()){p.sendOverlayMessage(tr("vent_idle"));return false;}
            if(n.foundryCooling>0){p.sendOverlayMessage(tr("vent_wait",(n.foundryCooling+19)/20));return false;}
            var bosses=p.level().getEntitiesOfClass(SanctuaryConstruct.class,new AABB(Vec3.atLowerCornerOf(n.local(-45,-2,-45)),Vec3.atLowerCornerOf(n.local(45,30,45))),e->e.isAlive()&&e.kind().foundryPair()&&e.token().equals(n.encounterToken()));
            if(bosses.isEmpty()||bosses.stream().anyMatch(e->e.awakening()>0))return false;
            bosses.forEach(e->e.vent(100));n.foundryCooling=360;n.setChanged();sound(p.level(),at,"foundry_release",.8f,.85f);
            p.sendOverlayMessage(tr("vent_open"));p.level().sendParticles(ParticleTypes.CLOUD,at.getX()+.5,at.getY()+1,at.getZ()+.5,25,.7,.4,.7,.04);return true;
        }
        if(n.foundryLatched){p.sendOverlayMessage(tr("stable"));return false;}
        var state=p.level().getBlockState(at);p.level().setBlock(at,state.setValue(FoundryDeviceBlock.STEP,(state.getValue(FoundryDeviceBlock.STEP)+1)&3),3);
        n.participant(p);sound(p.level(),at,"foundry_valve",.5f,.9f);n.setChanged();
        if(circuit(n)==7){n.foundryLatched=true;n.foundryPulse=20;n.setChanged();sound(p.level(),at,"foundry_ignite",.75f,1);p.sendOverlayMessage(tr("stable"));if((n.index()==8||n.index()==13)&&n.traces()==7)n.authoredFinish();}
        else p.sendOverlayMessage(tr("setting",(state.getValue(FoundryDeviceBlock.STEP)+1)&3));return true;
    }
    public static void tick(SanctuaryNodeEntity n,ServerLevel w){
        if(w.players().stream().noneMatch(p->p.isAlive()&&!p.isSpectator()&&p.position().distanceToSqr(Vec3.atBottomCenterOf(n.local(0,1,0)))<70*70))return;
        long now=w.getGameTime();
        if(n.foundryCooling>0){n.foundryCooling=Math.max(0,n.foundryCooling-5);n.setChanged();}
        if(n.index()==12&&SanctuaryRoute.done(n.mask(),12)&&now%10==0)shortcut(n,w);
        int heat=FoundryAct.heat(n.mask());boolean awake=heat>0||n.active()||n.foundryLatched;
        if(now%10==0){
            int frame=(int)(now/10)&3;
            for(int i=0;i<2;i++){
                animate(w,point(n,FoundryAct.rotor(i)),SanctuaryContent.FOUNDRY_ROTOR.get(),awake?frame:0,awake);
                animate(w,point(n,FoundryAct.press(i)),SanctuaryContent.FOUNDRY_PRESS.get(),awake?frame:0,awake);
            }
        }
        if(now%20==0){
            for(int i=0;i<FoundryAct.valves(n.index());i++){
                var at=point(n,FoundryAct.valve(n.index(),i));if(!w.hasChunkAt(at)||!w.getBlockState(at).is(SanctuaryContent.FOUNDRY_VALVE.get()))continue;
                var state=w.getBlockState(at);boolean lit=n.index()==15?n.foundryCooling==0&&n.active():n.foundryLatched||state.getValue(FoundryDeviceBlock.STEP)==FoundryAct.wanted(n.index(),i);
                if(n.foundryLatched&&n.index()!=15)state=state.setValue(FoundryDeviceBlock.STEP,FoundryAct.wanted(n.index(),i));
                set(w,at,state.setValue(FoundryDeviceBlock.LIT,lit));
            }
            int progress=SanctuaryRoute.done(n.mask(),n.index())?17:n.index()==8||n.index()==13?Integer.bitCount(n.traces())*4+(n.foundryLatched?5:0):n.active()?Math.max(1,n.killed()*17/Math.max(1,n.target())):heat*4;
            for(int side:new int[]{-1,1})for(int k=0;k<17;k++){
                var at=n.local(side*8,0,-32+k*4);if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.FILAMENT.get()))set(w,at,w.getBlockState(at).setValue(WitnessFilamentBlock.POWER,k<progress?Math.max(1,heat):0));
            }
        }
        if(now%160==0&&awake)sound(w,n.local(0,n.index()==12?4:14,-8),"foundry_work",.38f,.75f+heat*.08f);
        if(now%40==0&&awake){
            for(int side:new int[]{-1,1})w.sendParticles(ParticleTypes.WHITE_ASH,n.local(side*26,14,-8).getX()+.5,n.local(side*26,14,-8).getY(),n.local(side*26,14,-8).getZ()+.5,5,.5,1,.5,.01);
        }
        if(n.index()==12&&n.active()){
            var p=n.resonance();if(now%10==0)w.sendParticles(ParticleTypes.END_ROD,p.getX()+.5,p.getY()+.35,p.getZ()+.5,7,.6,.15,.6,.01);
        }
        if(n.foundryPulse>0){n.foundryPulse--;n.setChanged();if(now%10==0)for(int i=0;i<24;i++){double a=i*Math.PI/12,r=(20-n.foundryPulse)*1.4;w.sendParticles(ParticleTypes.END_ROD,n.local(0,1,0).getX()+.5+Math.cos(a)*r,n.local(0,1,0).getY()+.15,n.local(0,1,0).getZ()+.5+Math.sin(a)*r,1,0,0,0,0);}}
    }
    private static boolean bridgeMaterial(BlockState s){return s.isAir()||s.is(SanctuaryContent.block("obsidian_grate"))||s.is(SanctuaryContent.block("obsidian_ribbed"))||s.is(SanctuaryContent.SLAB.get());}
    private static void shortcut(SanctuaryNodeEntity n,ServerLevel w){
        for(int x=-29;x<=29;x++)if(!w.hasChunkAt(n.local(x,20,-23)))return;
        int next=Math.min(10,n.foundrySpan+1);
        for(int x=-27;x<=27;x++)if(Math.abs(x)>=28-next*3)for(int z=-25;z<=-21;z++)for(int y=19-Math.abs(x)/4;y<=21;y++)if(!bridgeMaterial(w.getBlockState(n.local(x,y,z))))return;
        for(int x=-27;x<=27;x++)if(Math.abs(x)>=28-next*3)for(int z=-25;z<=-21;z++){
            set(w,n.local(x,20,z),SanctuaryContent.block("obsidian_grate").defaultBlockState());
            for(int y=19-Math.abs(x)/4;y<20;y++)set(w,n.local(x,y,z),SanctuaryContent.block("obsidian_ribbed").defaultBlockState());
            if(z==-25||z==-21)set(w,n.local(x,21,z),SanctuaryContent.SLAB.get().defaultBlockState());
        }
        if(n.foundrySpan!=next){n.foundrySpan=next;n.setChanged();if(next==1)sound(w,n.local(0,20,-23),"witness_bridge",.7f,.7f);if(next==10)sound(w,n.local(0,20,-23),"foundry_ignite",.5f,1.2f);}
        if(next==10)for(int x:new int[]{-29,29})for(int z=-25;z<=-21;z++)for(int y=21;y<=23;y++){var at=n.local(x,y,z);if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.GATE.get()))set(w,at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
    }
    private static void animate(ServerLevel w,BlockPos at,net.minecraft.world.level.block.Block block,int frame,boolean lit){if(w.hasChunkAt(at)&&w.getBlockState(at).is(block))set(w,at,w.getBlockState(at).setValue(FoundryDeviceBlock.STEP,frame).setValue(FoundryDeviceBlock.LIT,lit));}
    private static void set(ServerLevel w,BlockPos at,BlockState state){if(w.hasChunkAt(at)&&!w.getBlockState(at).equals(state))w.setBlock(at,state,3);}
    static void sound(ServerLevel w,BlockPos at,String cue,float volume,float pitch){w.playSound(null,at,ModContent.atmosphereSound(cue),SoundSource.BLOCKS,volume,pitch);}
    public static void bossSpawn(SanctuaryNodeEntity n,SanctuaryConstruct mob){sound((ServerLevel)n.getLevel(),mob.blockPosition(),"foundry_ignite",.9f,.7f);}
    private FoundryScene(){}
}
