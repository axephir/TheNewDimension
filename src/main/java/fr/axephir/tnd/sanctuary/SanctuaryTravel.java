package fr.axephir.tnd.sanctuary;
import fr.axephir.tnd.domain.SanctuaryRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
public final class SanctuaryTravel {
    public static boolean go(ServerPlayer p,SanctuaryRoute.Site site,int index){
        if(!p.level().dimension().equals(fr.axephir.tnd.world.DevelopmentCommands.DIMENSION))return false;
        BlockPos at=index<0?new BlockPos(site.x()-620,SanctuaryRoute.FLOOR+1,site.z()-320):SanctuaryRegions.station(site,index).offset(0,0,4);
        // Deliberate player travel may load its destination chunk; generation never loads neighbours.
        p.level().getChunkAt(at);
        for(int n=0;n<8;n++){var feet=at.offset(n%3-1,0,n/3);if(p.level().getBlockState(feet.below()).isFaceSturdy(p.level(),feet.below(),net.minecraft.core.Direction.UP)&&p.level().noCollision(p,p.getBoundingBox().move(Vec3.atBottomCenterOf(feet).subtract(p.position())))){
            p.teleportTo(p.level(),feet.getX()+.5,feet.getY(),feet.getZ()+.5,java.util.Set.of(),p.getYRot(),p.getXRot(),false);p.fallDistance=0;p.closeContainer();return true;
        }}return false;
    }
    private SanctuaryTravel(){}
}
