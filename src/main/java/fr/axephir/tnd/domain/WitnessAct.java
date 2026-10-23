package fr.axephir.tnd.domain;

/** The first act's authored mechanisms. Coordinates are local to a chamber. */
public final class WitnessAct {
    public record Point(int x,int y,int z){}
    public static final int BRIDGE_STAGES=10;
    public static Point coupler(int n){return switch(n){case 0->new Point(-20,1,-6);case 1->new Point(0,1,-10);case 2->new Point(20,1,-6);default->throw new IllegalArgumentException("Unknown coupler");};}
    public static int facing(int n){return switch(n){case 0->1;case 1->3;case 2->2;default->throw new IllegalArgumentException("Unknown coupler");};}
    public static int secret(int room){return switch(room){case 0->0;case 3->1;case 5->2;default->-1;};}
    public static Point reliquary(int room){return switch(room){case 0->new Point(25,-3,-26);case 3->new Point(45,1,7);case 5->new Point(-24,3,33);default->throw new IllegalArgumentException("No reliquary in this room");};}
    public static boolean bridgeColumn(int stage,int x){return stage>0&&Math.abs(x)<=37&&Math.abs(x)>=Math.max(0,38-Math.clamp(stage,0,10)*4);}
    public static int circuit(int a,int b,int c){return (a==1?1:0)|(b==3?2:0)|(c==2?4:0);}
    public static int litPrefix(int mask){int n=0;while(n<3&&(mask&(1<<n))!=0)n++;return n;}
    private WitnessAct(){}
}
