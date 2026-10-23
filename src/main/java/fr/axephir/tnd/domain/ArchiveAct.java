package fr.axephir.tnd.domain;

/** Local, chunk-order independent contract for the third act. */
public final class ArchiveAct {
    public record Point(int x,int y,int z){}
    public static boolean contains(int i){return i>=16&&i<24;}
    public static int height(int i){return i==20?84:i==23?78:58+(i%3)*6;}
    public static Point mirror(int i,int n){if(n<0||n>2)throw new IllegalArgumentException("Mirror outside circuit");return new Point(-24+n*24,1,-12);}
    public static int wanted(int i,int n){return (i==16?new int[]{1,2,3}:new int[]{1,3,2})[n];}
    public static Point relay(){return new Point(32,1,26);}
    public static Point resonance(int n){return switch(n){case 0->new Point(-38,13,-12);case 1->new Point(38,13,18);case 2->new Point(38,25,34);case 3->new Point(-38,25,-28);default->throw new IllegalArgumentException("Resonance outside atlas");};}
    public static int echo(int step){return new int[]{2,0,1}[Math.clamp(step,0,2)];}
    public static int phase(long mask){int n=0;for(int i=16;i<24;i++)if(SanctuaryRoute.done(mask,i))n++;return Math.min(3,(n+1)/3);}
    private ArchiveAct(){}
}
