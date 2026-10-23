"""Five original restrained glass/stone/low resonant cues. Does not rewrite any older sound."""
from array import array
from pathlib import Path
import json,math,random,subprocess,tempfile,wave
from archive_resources import SOUNDS
RATE=22050
def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='tnd-archive-') as directory:
        for name,seconds in zip(SOUNDS,[1.1,4.6,9.5,8.2,2.8]):
            raw=array('f');rng=random.Random(name);low=mid=0
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;white=rng.uniform(-1,1);low=low*.997+white*.003;mid=mid*.93+white*.07
                def tone(f):return math.sin(math.tau*f*t)
                if name=='archive_turn':v=(tone(247)*.12*math.exp(-t*6)+tone(373.3)*.08*math.exp(-t*4)+mid*.15)*math.exp(-t*3)
                elif name=='archive_memory':v=(tone(164.8)*.12+tone(247.6)*.07+tone(330.1)*.045+low*1.2)*math.sin(math.pi*u)**.8
                elif name=='archive_atlas':v=(tone(31.2+u*.3)*.12+tone(63.8)*.05+tone(127.4)*.025+low*2.7)*math.sin(math.pi*u)**1.2*(.8+.2*math.sin(t*.8))
                elif name=='archive_awakening':v=(tone(35+u*6)*.16+tone(71.4)*.07+tone(142.3)*.035+low*2.2+mid*.09)*math.sin(math.pi*u)**.85
                else:v=(tone(293.66)*.13+tone(440.2)*.07+tone(587.7)*.035)*math.exp(-t*1.25)*(1-math.exp(-t*25))
                raw.append(v*min(1,t/.03,(seconds-t)/.3))
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.48/peak*32767) for v in raw));source=Path(directory)/(name+'.wav')
            with wave.open(str(source),'wb') as f:f.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(assets/f'sounds/ambient/{name}.ogg')],check=True)
            report[name]={'duration':seconds,'loop':False,'peak':max(abs(v) for v in pcm)/32768,'rms':math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm)),'splice_delta':0}
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'stream':False,'volume':.6,'attenuation_distance':48}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
    print('Five archive cues exported; existing sound files preserved.')
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
