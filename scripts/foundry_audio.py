"""Five original mono mechanical cues; prior sound files are never touched."""
from array import array
from pathlib import Path
import json,math,random,subprocess,tempfile,wave
from foundry_resources import SOUNDS
RATE=22050
def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='tnd-foundry-') as directory:
        for name,seconds in zip(SOUNDS,[1.3,7.5,5.6,3.1,3.8]):
            raw=array('f');rng=random.Random(name);lo=mid=0
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;white=rng.uniform(-1,1);lo=lo*.996+white*.004;mid=mid*.94+white*.06
                def tone(f):return math.sin(math.tau*f*t)
                if name=='foundry_valve':v=(mid*.25+tone(84)*.07+lo*1.8)*math.sin(math.pi*u)**.7*(.35+.65*math.sin(t*29)**4)
                elif name=='foundry_ignite':v=(tone(28+u*10)*.22+tone(57.3)*.13+lo*3.2+mid*.12)*math.sin(math.pi*u)**.75*(.8+.2*math.sin(t*2.1))
                elif name=='foundry_work':
                    beat=t%1.4;impact=math.exp(-beat*15);v=(tone(43)*.18+tone(86.7)*.08+lo*2.5+mid*.15)*(.25+.75*impact)+impact*(tone(151)*.12+mid*.4)
                elif name=='foundry_release':v=(mid*.7+lo*2+tone(74)*.05)*math.sin(math.pi*u)**.8*math.exp(-t*.6)
                else:v=(tone(196)*.14+tone(293.6)*.10+tone(392.4)*.06+mid*.14*math.exp(-t*5))*math.exp(-t*.75)*(1-math.exp(-t*20))
                raw.append(v*min(1,t/.035,(seconds-t)/.3))
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.52/peak*32767) for v in raw));source=Path(directory)/(name+'.wav')
            with wave.open(str(source),'wb') as f:f.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(assets/f'sounds/ambient/{name}.ogg')],check=True)
            report[name]={'duration':seconds,'loop':False,'peak':max(abs(v) for v in pcm)/32768,'rms':math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm)),'splice_delta':0}
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'stream':False,'volume':.65,'attenuation_distance':48}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
    print('Five foundry sounds exported; all 69 previous OGG files retained.')
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
