package fr.axephir.tnd.depths;
import fr.axephir.tnd.city.CityRegions;
import net.minecraft.core.BlockPos;
/** Eight 97m halls, a single surface shaft and a continuous serpentine route. */
public final class DepthsLayout{
 public static final int FLOOR=16,ROOMS=8;public static final int[][] CENTRES={{-192,-96},{-64,-96},{64,-96},{192,-96},{192,32},{64,32},{-64,32},{-192,32}};
 public static BlockPos centre(CityRegions.Region r,int i){return new BlockPos(r.x()+CENTRES[i][0],FLOOR,r.z()+CENTRES[i][1]);}
 public static BlockPos node(CityRegions.Region r,int i){return centre(r,i).offset(0,1,18);}
 public static BlockPos entrance(CityRegions.Region r){return new BlockPos(r.x(),89,r.z()+64);}
 public static long key(CityRegions.Region r){return ((long)r.cellX()<<32)^(r.cellZ()&0xffffffffL);}
 public static boolean boss(int i){return (i&1)==1;}public static int target(int i){return boss(i)?25:48;}
 public static int atNode(CityRegions.Region r,BlockPos p){for(int i=0;i<8;i++)if(node(r,i).equals(p))return i;return -1;}
 public static BlockPos exit(CityRegions.Region r,int i){var c=centre(r,i);if(i==7)return c.offset(-47,1,0);var d=centre(r,i+1).subtract(c);return c.offset(Integer.signum(d.getX())*47,1,Integer.signum(d.getZ())*47);}
 private DepthsLayout(){}
}
