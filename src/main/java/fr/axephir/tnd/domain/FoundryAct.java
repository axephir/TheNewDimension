package fr.axephir.tnd.domain;

/** Authored foundry coordinates. Old stations never opt into these objectives. */
public final class FoundryAct {
    public record Point(int x,int y,int z){}
    public static boolean contains(int index){return index>=8&&index<16;}
    public static Point valve(int index,int n){
        if(n<0||n>2)throw new IllegalArgumentException("Valve outside circuit");
        return switch(index){
            case 8->new Point(-26+n*26,1,-2);
            case 12->new Point(n==0?-34:34,n==2?21:11,n==0?-12:n==1?12:28);
            case 13->new Point(-20+n*20,1,-10);
            case 15->new Point(n==0?-30:30,1,-10);
            default->new Point(n==0?-28:28,1,8);
        };
    }
    public static int valves(int index){return index==8||index==12||index==13?3:index==15?2:0;}
    public static int wanted(int index,int n){return index==13?new int[]{1,3,2}[n]:2;}
    public static boolean ready(int index,int a,int b,int c){return a==wanted(index,0)&&b==wanted(index,1)&&c==wanted(index,2);}
    public static Point resonance(int sequence){return switch(sequence){
        case 0->new Point(-34,11,-18);case 1->new Point(34,11,18);
        case 2->new Point(34,21,34);case 3->new Point(-34,21,-28);
        default->throw new IllegalArgumentException("Resonance outside route");};}
    public static Point rotor(int n){return new Point(n==0?-26:26,14,-8);}
    public static Point press(int n){return new Point(n==0?-18:18,3,-26);}
    public static int heat(long mask){int finished=0;for(int i=8;i<16;i++)if(SanctuaryRoute.done(mask,i))finished++;return Math.min(3,(finished+2)/3);}
    /** The landing is deliberately outside the 56m-wide open shaft. */
    public static boolean shaft(int x,int z){return Math.abs(x)<28&&z>-36&&z<16;}
    public static int height(int index){return index==12?76:index==15?68:48+(index%3)*5;}
    private FoundryAct(){}
}
