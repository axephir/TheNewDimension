"""Four original mechanical/resonant one-shots. Never regenerates previous OGG files."""
from array import array
from pathlib import Path
import json,math,random,subprocess,tempfile,wave
from witness_resources import SOUNDS
RATE=22050
def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='tnd-witness-') as directory:
        for name,seconds in zip(SOUNDS,[1.9,3.4,2.8,8.0]):
            raw=array('f');rng=random.Random(name);low=mid=0
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;white=rng.uniform(-1,1);low=low*.994+white*.006;mid=mid*.9+white*.1
                def tone(f):return math.sin(math.tau*f*t)
                if name=='witness_current':v=(tone(196)*.2+tone(294)*.12+tone(392)*.05+mid*.1)*math.exp(-t*1.2)*(1-math.exp(-t*15))
                elif name=='witness_bridge':v=(low*2.8+mid*.2+tone(46)*.17+tone(92.4)*.08)*(.4+.6*math.sin(t*11)**6)*math.sin(math.pi*u)**.65
                elif name=='witness_reliquary':v=(tone(261.6)*.15+tone(392.2)*.1+tone(523.8)*.06)*math.exp(-t*1.4)*(1-math.exp(-t*20))
                else:v=(tone(32+u*9)*.25+tone(65.3)*.13+tone(98.1)*.07+low*3+mid*.22)*math.sin(math.pi*u)**.8*(.7+.3*math.sin(t*3)**2)
                raw.append(v*min(1,t/.04,(seconds-t)/.3))
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.54/peak*32767) for v in raw));rms=math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm))
            source=Path(directory)/(name+'.wav')
            with wave.open(str(source),'wb') as f:f.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(assets/f'sounds/ambient/{name}.ogg')],check=True)
            report[name]={'duration':seconds,'loop':False,'peak':max(abs(v) for v in pcm)/32768,'rms':rms,'splice_delta':0}
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'stream':False,'volume':.65,'attenuation_distance':48}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
    print('Four new witness sounds exported; all earlier OGG files retained.')
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
