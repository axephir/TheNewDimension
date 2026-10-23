package fr.axephir.tnd.domain;

/** Six small reversible mechanisms. All authored targets are reachable from the visible initial state. */
public final class SanctuaryPuzzles {
    public static int[] turn(int[] tones,int station,int dial){
        if(tones.length!=4||station<0||station>=48||dial<0||dial>=4)throw new IllegalArgumentException("Invalid dial mechanism");
        int[] next=tones.clone();for(int n=0;n<4;n++){if(tones[n]<0||tones[n]>3)throw new IllegalArgumentException("Invalid tone");
            int delta=switch(station/8){
                case 0->n==dial?1:0;
                case 1->n==dial||n==(dial+1)%4?1:0;
                case 2->n>=dial?1:0;
                case 3->n==dial||n==3-dial?1:0;
                case 4->n==dial||n==(dial+1)%4||n==(dial+3)%4?1:0;
                default->n==dial?1:n==(dial+2)%4?2:0;
            };next[n]=(tones[n]+delta)%4;
        }return next;
    }
    public static int[] target(int station){
        if(station<0||station>=48)throw new IllegalArgumentException("Invalid puzzle station");
        int[] desired=new int[4];int[] clicks={1+station/8%3,2,station/8%2,1};
        for(int i=0;i<4;i++)for(int n=0;n<clicks[i];n++)desired=turn(desired,station,i);return desired;
    }
    private SanctuaryPuzzles(){}
}
