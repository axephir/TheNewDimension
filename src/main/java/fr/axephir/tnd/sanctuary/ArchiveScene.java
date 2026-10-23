package fr.axephir.tnd.sanctuary;

import fr.axephir.tnd.domain.*;
import fr.axephir.tnd.entities.Shardling;
import fr.axephir.tnd.familiar.FamiliarLedger;
import fr.axephir.tnd.core.ModContent;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Bounded, nearby-only scene updates. No chunk tickets and no writing through player obstructions. */
public final class ArchiveScene {
    public static Component tr(String s,Object...args){return Component.translatable("observatory.tnd.archive."+s,args);}
    public static BlockPos point(SanctuaryNodeEntity n,ArchiveAct.Point p){return n.local(p.x(),p.y(),p.z());}
    public static int circuit(SanctuaryNodeEntity n){if(!n.archive()||!n.natural()||n.index()!=16&&n.index()!=18)return -1;int mask=0;for(int i=0;i<3;i++){var p=point(n,ArchiveAct.mirror(n.index(),i));if(!n.getLevel().hasChunkAt(p))return -1;var s=n.getLevel().getBlockState(p);if(!s.is(ArchiveContent.MIRROR.get()))return -1;if(s.getValue(ArchiveDeviceBlock.STEP)==ArchiveAct.wanted(n.index(),i))mask|=1<<i;}return mask;}
    public static boolean use(SanctuaryNodeEntity n,ServerPlayer p,BlockPos at){
        if(!n.archive()||!n.natural()||p.level()!=n.getLevel()||!p.isAlive()||p.isSpectator()||p.distanceToSqr(Vec3.atCenterOf(at))>36||!p.level().hasChunkAt(at)||!n.available()&&!SanctuaryRoute.done(n.mask(),n.index()))return false;
        var hit=p.level().clip(new ClipContext(p.getEyePosition(),Vec3.atCenterOf(at),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.BLOCK||!hit.getBlockPos().equals(at))return false;
        if(n.index()==16||n.index()==18){
            if(n.archiveLatched||!p.level().getBlockState(at).is(ArchiveContent.MIRROR.get()))return false;
            boolean authored=false;for(int j=0;j<3;j++)if(point(n,ArchiveAct.mirror(n.index(),j)).equals(at))authored=true;if(!authored)return false;
            var s=p.level().getBlockState(at);int step=(s.getValue(ArchiveDeviceBlock.STEP)+1)&3;p.level().setBlock(at,s.setValue(ArchiveDeviceBlock.STEP,step),3);n.participant(p);sound(p.level(),at,"archive_turn",.5f,.9f+step*.08f);
            p.sendOverlayMessage(tr("mirror_setting",step));if(circuit(n)==7){n.archiveLatched=true;n.setChanged();sound(p.level(),at,"archive_memory",.65f,1);if(n.index()==16&&n.traces()==7)n.archiveFinish();}return true;
        }
        if(!p.level().getBlockState(at).is(ArchiveContent.RELAY.get()))return false;
        if(n.index()==20&&at.equals(point(n,ArchiveAct.relay()))&&!SanctuaryRoute.done(n.mask(),20)){
            n.archiveHold=3600;n.participant(p);var pet=FamiliarLedger.get(p.level().getServer()).loaded(p.level().getServer(),p.getUUID());
            if(pet!=null&&pet.level()==p.level()&&pet.available()&&!pet.isOrderedToSit()&&pet.getTarget()==null&&pet.distanceToSqr(Vec3.atCenterOf(at))<12*12){pet.holdArchive(at.south(2),3600);n.archiveKeeper=pet.getUUID().toString();p.sendOverlayMessage(tr("pet_holding"));}
            else p.sendOverlayMessage(tr("manual_holding"));n.setChanged();sound(p.level(),at,"archive_atlas",.65f,1);return true;
        }
        if(n.index()==23&&at.getZ()==n.local(0,1,24).getZ()&&(at.getX()==n.local(-22,1,24).getX()||at.getX()==n.local(0,1,24).getX()||at.getX()==n.local(22,1,24).getX())&&n.active()){
            if(n.archiveCooling>0){p.sendOverlayMessage(tr("seal_wait",(n.archiveCooling+19)/20));return false;}
            var boss=p.level().getEntitiesOfClass(SanctuaryConstruct.class,new AABB(n.getBlockPos()).inflate(65),e->e.kind()==SanctuaryConstruct.Kind.CONSERVATOR&&e.isAlive()&&e.token().equals(n.encounterToken()));if(boss.isEmpty()||boss.stream().anyMatch(e->e.awakening()>0))return false;
            boss.forEach(e->e.vent(120));n.archiveCooling=440;n.setChanged();p.sendOverlayMessage(tr("seal_open"));sound(p.level(),at,"archive_memory",.8f,.7f);return true;
        }return false;
    }
    public static boolean echo(SanctuaryNodeEntity n,ServerPlayer p,int trace){
        if(n.index()!=21)return true;
        if(trace!=ArchiveAct.echo(n.archiveEcho)){n.archiveEcho=0;n.setChanged();p.sendOverlayMessage(tr("echo_discord"));sound(p.level(),p.blockPosition(),"archive_turn",.35f,.6f);return false;}
        n.archiveEcho++;n.setChanged();p.sendOverlayMessage(tr("echo_progress",n.archiveEcho));if(n.archiveEcho==3)n.archiveLatched=true;return true;
    }
    public static void tick(SanctuaryNodeEntity n,ServerLevel w){
        if(w.players().stream().noneMatch(p->p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(Vec3.atCenterOf(n.getBlockPos()))<80*80))return;
        long now=w.getGameTime();if(n.archiveCooling>0){n.archiveCooling=Math.max(0,n.archiveCooling-5);n.setChanged();}
        if(n.archiveHold>0){n.archiveHold=Math.max(0,n.archiveHold-5);n.setChanged();}
        if(n.index()==20&&now%20==0){
            Shardling pet=null;try{if(!n.archiveKeeper.isEmpty()&&w.getEntity(java.util.UUID.fromString(n.archiveKeeper)) instanceof Shardling s)pet=s;}catch(IllegalArgumentException ignored){}
            if(pet!=null&&pet.available()&&pet.archiveHolding()&&pet.distanceToSqr(Vec3.atCenterOf(point(n,ArchiveAct.relay())))<6*6){n.archiveHold=3600;w.sendParticles(ParticleTypes.ENCHANT,pet.getX(),pet.getY()+1,pet.getZ(),3,.2,.2,.2,.01);}
            if(SanctuaryRoute.done(n.mask(),20)){if(pet!=null)pet.releaseArchive();n.archiveKeeper="";shortcut(n,w);}
            var at=point(n,ArchiveAct.relay());light(w,at,ArchiveContent.RELAY.get(),n.archiveHold>0||SanctuaryRoute.done(n.mask(),20));
        }
        if((n.index()==16||n.index()==18)&&now%10==0)for(int i=0;i<3;i++){
            var at=point(n,ArchiveAct.mirror(n.index(),i));if(!w.hasChunkAt(at)||!w.getBlockState(at).is(ArchiveContent.MIRROR.get()))continue;
            var s=w.getBlockState(at);boolean lit=n.archiveLatched||s.getValue(ArchiveDeviceBlock.STEP)==ArchiveAct.wanted(n.index(),i);light(w,at,ArchiveContent.MIRROR.get(),lit);
            // An actual collision-clipped beam grows from the small prism toward its matching receptor.
            var start=Vec3.atCenterOf(at).add(0,.3,0);var end=start.add(switch(s.getValue(ArchiveDeviceBlock.STEP)){case 0->new Vec3(0,0,-10);case 1->new Vec3(10,0,0);case 2->new Vec3(0,0,10);default->new Vec3(-10,0,0);});
            if(!w.hasChunkAt(BlockPos.containing(end)))continue;
            var ray=w.clip(new ClipContext(start.add(end.subtract(start).normalize().scale(.65)),end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));beam(w,start,ray.getType()==HitResult.Type.MISS?end:ray.getLocation(),lit?ParticleTypes.END_ROD:ParticleTypes.ENCHANT);
        }
        if(now%20==0){int phase=ArchiveAct.phase(n.mask());
            for(int side:new int[]{-1,1})for(int j=0;j<4;j++){
                var at=n.local(side*10,3,-24+j*16);if(w.hasChunkAt(at)&&w.getBlockState(at).is(SanctuaryContent.block("observatory_brazier"))&&phase>0)w.sendParticles(ParticleTypes.END_ROD,at.getX()+.5,at.getY()+1.2,at.getZ()+.5,1,0,.2,0,0);
            }
            if(n.index()==23)for(int x:new int[]{-22,0,22})light(w,n.local(x,1,24),ArchiveContent.RELAY.get(),n.active()&&n.archiveCooling==0);
            // Permanent earned illumination, only replace the authored socket material.
            for(int side:new int[]{-1,1})for(int j=0;j<4;j++){var at=n.local(side*33,0,-24+j*16);if(!w.hasChunkAt(at))continue;var s=w.getBlockState(at);var desired=SanctuaryContent.block(j<=phase?"luminous_script":"guiding_inlay").defaultBlockState();if((s.is(SanctuaryContent.block("guiding_inlay"))||s.is(SanctuaryContent.block("luminous_script")))&&!s.equals(desired))w.setBlock(at,desired,3);}
        }
        if(now%240==0&&(n.active()||ArchiveAct.phase(n.mask())>0))sound(w,n.local(0,12,0),"archive_atlas",.28f,.7f);
        if(n.index()==20&&n.active()&&n.sequence()<4&&now%10==0){var at=n.resonance();w.sendParticles(ParticleTypes.END_ROD,at.getX()+.5,at.getY()+.3,at.getZ()+.5,6,.5,.15,.5,.01);}
    }
    private static void light(ServerLevel w,BlockPos at,net.minecraft.world.level.block.Block block,boolean lit){if(w.hasChunkAt(at)){var s=w.getBlockState(at);if(s.is(block)&&s.getValue(ArchiveDeviceBlock.LIT)!=lit)w.setBlock(at,s.setValue(ArchiveDeviceBlock.LIT,lit),3);}}
    private static void shortcut(SanctuaryNodeEntity n,ServerLevel w){
        if(n.archiveSpan>=10)return;int next=n.archiveSpan+1;
        for(int x=-33;x<=33;x++)for(int z=-26;z<=-22;z++)if(!w.hasChunkAt(n.local(x,24,z)))return;
        for(int x=-31;x<=31;x++)if(Math.abs(x)>=32-next*4)for(int z=-26;z<=-22;z++)for(int y=24;y<=25;y++){var s=w.getBlockState(n.local(x,y,z));if(!s.isAir()&&!s.is(SanctuaryContent.block("obsidian_grate"))&&!s.is(SanctuaryContent.SLAB.get()))return;}
        for(int x=-31;x<=31;x++)if(Math.abs(x)>=32-next*4)for(int z=-26;z<=-22;z++){w.setBlock(n.local(x,24,z),SanctuaryContent.block("obsidian_grate").defaultBlockState(),3);if(z==-26||z==-22)w.setBlock(n.local(x,25,z),SanctuaryContent.SLAB.get().defaultBlockState(),3);}
        n.archiveSpan=next;n.setChanged();if(next==1||next==10)sound(w,n.local(0,24,-24),"archive_memory",.6f,next==10?1.2f:.7f);
        if(next==10)for(int x:new int[]{-33,33})for(int z=-26;z<=-22;z++)for(int y=25;y<=27;y++)if(w.getBlockState(n.local(x,y,z)).is(SanctuaryContent.GATE.get()))w.setBlock(n.local(x,y,z),Blocks.AIR.defaultBlockState(),3);
    }
    public static void beam(ServerLevel w,Vec3 a,Vec3 b,net.minecraft.core.particles.SimpleParticleType type){int count=Math.clamp((int)(a.distanceTo(b)*1.2),2,28);for(int i=0;i<=count;i++){var p=a.lerp(b,i/(double)count);w.sendParticles(type,p.x,p.y,p.z,1,0,0,0,0);}}
    public static void sound(ServerLevel w,BlockPos at,String s,float volume,float pitch){w.playSound(null,at,ModContent.atmosphereSound(s),SoundSource.BLOCKS,volume,pitch);}
    private ArchiveScene(){}
}
