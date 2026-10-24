package fr.axephir.tnd.depths;
import fr.axephir.tnd.city.CityRegions;
import fr.axephir.tnd.sanctuary.SanctuaryContent;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootTable;
/** Authored volumetric architecture evaluated only inside the current chunk. Null means preserve the terrain. */
public final class DepthsArchitecture{
 private static BlockState s(String id){return SanctuaryContent.block(id).defaultBlockState();}
 private static BlockState stone(){return s("obsidian_metal");}private static BlockState light(){return s("luminous_script");}
 private static boolean corridor(int x,int z){
  if(Math.abs(z+96)<=5&&x>=-240&&x<=240)return true;
  if(Math.abs(z-32)<=5&&x>=-256&&x<=240)return true;
  if(Math.abs(x-192)<=5&&z>=-96&&z<=32)return true;
  if(Math.abs(x)<=5&&z>=64&&z<=112)return true;
  if(Math.abs(z-112)<=5&&x>=-256&&x<=0)return true;
  if(Math.abs(x+256)<=5&&z>=-96&&z<=112)return true;
  return Math.abs(z+96)<=5&&x>=-256&&x<=-240;
 }
 public static BlockState state(CityRegions.Region r,int wx,int y,int wz){int x=wx-r.x(),z=wz-r.z();
  // The sole shaft is located on the central east-west street, away from the incubation laboratory.
  if(Math.abs(x)<=6&&Math.abs(z-64)<=6&&y>=17&&y<=99){int d=Math.max(Math.abs(x),Math.abs(z-64));
   if(y>=89){if(y==89&&d==6&&(Math.abs(x)<=2||Math.abs(z-64)<=2))return SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Math.abs(x)==6?(x<0?Direction.EAST:Direction.WEST):(z<64?Direction.SOUTH:Direction.NORTH));if(y==89)return d<=2?(x==2&&Math.abs(z-64)<=1?Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST):Blocks.AIR.defaultBlockState()):s("obsidian_tiles");if(y==99&&d<=5)return s("obsidian_relief");if(d>=5&&Math.abs(x)>=4&&Math.abs(z-64)>=4)return (y%7==0?light():s("obsidian_pillar"));if(y==94&&d==5)return s("amber_seal");if(d<=4)return Blocks.AIR.defaultBlockState();return null;}
   if(y<=27&&z>=66&&Math.abs(x)<=2)return Blocks.AIR.defaultBlockState();
   if(d==3||d==4)return y%12==0?s("guiding_inlay"):stone();if(d<=2){if(x==2&&Math.abs(z-64)<=1)return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST);return Blocks.AIR.defaultBlockState();}if(y<=28)return null;return null;
  }
  if(y<14||y>62)return null;
  for(int i=0;i<8;i++){int dx=x-DepthsLayout.CENTRES[i][0],dz=z-DepthsLayout.CENTRES[i][1],ax=Math.abs(dx),az=Math.abs(dz),d=Math.max(ax,az);if(d>50)continue;
   if(y<=16){if(y<16)return stone();double radius=Math.hypot(dx,dz);if(Math.abs(radius-14)<.6||Math.abs(radius-26)<.6||Math.abs(radius-39)<.6)return s(i%2==0?"guiding_inlay":"amber_seal");if((ax%16==0||az%16==0)&&Math.max(ax,az)>26)return s("guiding_inlay");if(ax<=2||az<=2)return s("obsidian_ribbed");return (Math.floorDiv(dx,4)+Math.floorDiv(dz,4))%2==0?s("obsidian_tiles"):s("obsidian_relief");}
   if(y>=60)return (y==60&&(ax%16<2||az%16<2))?s("obsidian_ribbed"):stone();
   boolean door=(ax>=47&&az<=5||az>=47&&ax<=5)&&corridor(x,z)&&y<=26;
   var exit=DepthsLayout.exit(r,i);if(wx==exit.getX()&&Math.abs(wz-exit.getZ())<=5&&i!=3&&y<=26||wz==exit.getZ()&&Math.abs(wx-exit.getX())<=5&&i==3&&y<=26){return SanctuaryContent.GATE.get().defaultBlockState();}
   if(d>=47)return door?Blocks.AIR.defaultBlockState():(d==47&&y%12==4&&(ax%16<=2||az%16<=2))?light():d==47?s("obsidian_relief"):stone();
   if(d>=43&&(ax%16<=2||az%16<=2))return y%12==4?light():s("obsidian_pillar");
   if((y==28||y==45)&&d>=42)return s("obsidian_ribbed");
   // Raised side galleries, stairs and balustrades retain a large, clear fighting floor.
   if(az>=35&&az<=42){if(y==22&&!(dx>=-40&&dx<=-30))return s("obsidian_tiles");if(y==23&&az==35&&ax%8==0&&!(dx>=-40&&dx<=-28))return s("obsidian_pillar");if(y==24&&az==35&&!(dx>=-40&&dx<=-28))return s("obsidian_grate");if(dx>=-40&&dx<=-29&&y<=17+(dx+40)/2)return y==17+(dx+40)/2&&Math.floorMod(dx+40,2)==0?SanctuaryContent.STAIRS.get().defaultBlockState().setValue(StairBlock.FACING,Direction.EAST):s("obsidian_tiles");}
   // Repeating arch ribs rise towards the centre of the vault.
   int vault=58-ax/5;if((Math.floorMod(dz,24)<=1&&y>=vault&&y<=vault+1)||(Math.floorMod(dx,24)<=1&&y>=58-az/5&&y<=59-az/5))return s("obsidian_ribbed");
   if(y>=49&&(ax%24<=1&&az>=28||az%24<=1&&ax>=28))return s("silent_conduit");
   // Furnished wall alcoves: plinths, conduit stacks and softly lit mechanical racks.
   if(ax>=38&&az>=10&&az<=30&&Math.floorMod(dz,16)<5){if(y<=19)return s("obsidian_relief");if(y<=24&&ax>=41)return y%3==0?s(i%2==0?"amber_seal":"memory_lattice"):s("silent_conduit");}
   if(ax>=28&&ax<=34&&az>=27&&az<=33){
    if(y==17||y==19)return s("obsidian_ribbed");
    if((ax==28||ax==34)&&(az==27||az==33)&&y<=29)return s("obsidian_pillar");
    if(y>=20&&y<=27&&ax>=30&&ax<=32&&az>=29&&az<=31)return y%4==0?s("luminous_script"):s("silent_conduit");
    if((y==24||y==28)&&(ax==28||ax==34||az==27||az==33))return s("amber_seal");
   }
   // Segmented supports and service bays create real cover outside the central fighting circle.
   if(ax>=33&&ax<=35&&Math.abs(az-16)<=1&&y<=36)return y%8==4?s("luminous_script"):y%8==5?s("obsidian_relief"):s("obsidian_pillar");
   if(i%2==0&&ax>=39&&ax<=42&&az<=26&&Math.floorMod(dz+8,16)<=6){if(y==18||y==23)return s("obsidian_grate");if(y<=23&&(ax==42||Math.floorMod(dz+8,16)==0))return s("obsidian_ribbed");if(y==21&&ax==40)return s(i==2?"memory_lattice":"amber_seal");}
   // The landing's suspended crane is anchored by full-height pylons and steel ribs.
   if(i==0&&az<=1&&ax>=31&&ax<=33&&y<=42)return y%8==4?s("amber_seal"):s("obsidian_pillar");
   if(i==0&&az<=2&&ax<=33&&y>=40&&y<=42)return y==41?s("obsidian_relief"):s("obsidian_ribbed");
   if(i==0&&az==0&&ax==16&&y>=29&&y<=39)return s("silent_conduit");
   if(i==0&&az<=3&&ax>=13&&ax<=19&&y>=27&&y<=29)return y==27?s("amber_seal"):s("obsidian_ribbed");
   if(i==4&&ax>=29&&ax<=31&&az>=15&&az<=17&&y<=35)return y%6==0?s("amber_seal"):s("silent_conduit");
   if(i==7&&y>=39&&y<=42){double radius=Math.hypot(dx,dz);if(radius>=25&&radius<=27)return y==40?s("amber_seal"):s("obsidian_ribbed");}
   if(i==7&&ax>=25&&ax<=27&&az<=1&&y>=43&&y<58)return s("silent_conduit");
   // Different monumental silhouettes identify each pair of halls without a text waypoint.
   if(i==3&&y>=27&&y<=59){double rr=Math.hypot(dx,y-43);if(Math.abs(dz)<=1&&rr>=14&&rr<=16)return (dx+y)%8==0?light():s("obsidian_ribbed");}
   if(i==5&&y>=34&&ax>=16&&ax<=18&&az<=30)return (dz+y)%9==0?s("amber_seal"):s("obsidian_ribbed");
   if(i==7&&dz<=-30&&ax<=24&&y<=17+Math.min(5,(-dz-30)/2))return s("obsidian_tiles");
   if(i==7&&dz<=-41&&ax<=10&&y<=32)return (ax==8||y==31)?s("amber_seal"):s("obsidian_relief");
   if(ax==0&&dz==18&&y==17)return DepthsContent.NODE.get().defaultBlockState();
   if(DepthsLayout.boss(i)&&dx==4&&dz==18&&y==17)return DepthsContent.ORE.get().defaultBlockState();
   if((dx==-37||dx==37)&&dz==20&&y==17)return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,dx<0?Direction.EAST:Direction.WEST);
   // Lamps recessed in the floor and walls give legible combat silhouettes in the black stone.
   if(y==17&&ax>=44&&az%12==6)return s("observatory_brazier");
   return Blocks.AIR.defaultBlockState();
  }
  if(corridor(x,z)){if(y<=16||y>=28&&y<=30)return y==16?s("guiding_inlay"):stone();if(y>30)return null;boolean edge=!corridor(x+1,z)||!corridor(x-1,z)||!corridor(x,z+1)||!corridor(x,z-1);if(edge)return y==23?s("luminous_script"):s("obsidian_ribbed");return Blocks.AIR.defaultBlockState();}
  return null;
 }
 public static boolean relevant(CityRegions.Region r,int x0,int z0){for(int x=x0;x<x0+16;x+=15)for(int z=z0;z<z0+16;z+=15)if(Math.abs(x-r.x())<=270&&z-r.z()>=-150&&z-r.z()<=125)return true;return false;}
 public static int generate(LevelAccessor w,int x0,int z0){var r=CityRegions.nearest(x0+8,z0+8);if(!relevant(r,x0,z0))return 0;int count=0;
  for(int x=x0;x<x0+16;x++)for(int z=z0;z<z0+16;z++)for(int y=14;y<=99;y++){var b=state(r,x,y,z);if(b==null)continue;var p=new BlockPos(x,y,z);w.setBlock(p,b,2);count++;if(b.is(Blocks.CHEST)&&w.getBlockEntity(p) instanceof RandomizableContainerBlockEntity chest){chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("tnd","chests/nacre_depths")));chest.setLootTableSeed(CityRegions.mix(p.asLong()));}}
  return count;
 }
 private DepthsArchitecture(){}
}
