package fr.axephir.tnd.domain;

/** Coordinate and progression contract; no world access and no dependency on chunk order. */
public final class SanctuaryRoute {
    public static final int ACTS=6,PER_ACT=8,COUNT=48,STEP=128,FLOOR=128,HALF_X=640,HALF_Z=512,CELL=6144;
    public static final long FULL=(1L<<COUNT)-1;
    public record Site(int gridX,int gridZ,int x,int z){
        public long key(){return ((long)gridX<<32)^(gridZ&0xffffffffL);}
        public boolean contains(int px,int pz){return Math.abs((long)px-x)<=HALF_X&&Math.abs((long)pz-z)<=HALF_Z;}
        public int localX(int px){return px-x;}public int localZ(int pz){return pz-z;}
    }
    public static Site nearest(int x,int z){int gx=Math.floorDiv(x,CELL),gz=Math.floorDiv(z,CELL);return new Site(gx,gz,gx*CELL+CELL/2,gz*CELL+CELL/2);}
    public static int x(int index){check(index);int n=index%PER_ACT;return -448+(index/PER_ACT%2==0?n:7-n)*STEP;}
    public static int z(int index){check(index);return -320+index/PER_ACT*STEP;}
    public static int act(int index){check(index);return index/PER_ACT;}
    public static boolean boss(int index){return index>=0&&index<COUNT&&index%PER_ACT==7;}
    public static boolean done(long mask,int index){return index>=0&&index<COUNT&&(mask&(1L<<index))!=0;}
    public static boolean available(long mask,int index){return index>=0&&index<COUNT&&!done(mask,index)&&((mask&((1L<<index)-1))==((1L<<index)-1));}
    public static long complete(long mask,int index){return available(mask,index)?(mask|1L<<index)&FULL:mask&FULL;}
    public static int reached(long mask){int n=0;while(n<COUNT&&done(mask,n))n++;return n;}
    public static int indexAt(int localX,int localZ){int row=Math.floorDiv(localZ+384,STEP),col=Math.floorDiv(localX+512,STEP);return row<0||row>=6||col<0||col>=8?-1:row*8+(row%2==0?col:7-col);}
    public static boolean inChamber(int lx,int lz,int index){return index>=0&&Math.abs(lx-x(index))<=50&&Math.abs(lz-z(index))<=50;}
    private static void check(int index){if(index<0||index>=COUNT)throw new IllegalArgumentException("Station outside route");}
    private SanctuaryRoute(){}
}
