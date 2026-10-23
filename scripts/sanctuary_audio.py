"""Seven original measured cues; no external recording and no replacement of earlier sounds."""
from pathlib import Path
from array import array
import math,random,json,wave,tempfile,subprocess
from sanctuary_resources import SOUNDS
RATE=22050
def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='tnd-observatory-audio-') as tmp:
        for index,name in enumerate(SOUNDS):
            loop=name=='observatory_drone';seconds=24 if loop else 3.4 if name in ['observatory_threshold','observatory_core'] else 2.8 if name=='observatory_victory' else 2.4 if name=='observatory_warning' else 1.8
            raw=array('f');rng=random.Random(name);low=mid=0
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;noise=rng.uniform(-1,1);low=low*.993+noise*.007;mid=mid*.84+noise*.16;s=lambda f:math.sin(2*math.pi*f*t)
                if loop:v=(s(43)*.2+s(64.7)*.13+s(86.3)*.08+low*1.8)*(.7+.15*s(1/12)+.12*s(1/8))+s(172.1)*.025
                elif name=='observatory_warning':v=(s(82+u*30)*.2+s(165+u*18)*.1+mid*.25)*math.sin(math.pi*u)**.7
                elif name=='observatory_impact':v=(s(39)*.5+s(78.6)*.18+low*2+mid*.45)*(1-math.exp(-t*50))*math.exp(-t*3)
                elif name=='observatory_relay':v=(s(392)*.2+s(588.4)*.08+s(785.2)*.04)*math.exp(-t*2.8)
                elif name=='observatory_victory':
                    v=0
                    for delay,f,g in [(0,147,.22),(.14,220,.17),(.28,294,.14),(.42,440,.08)]:
                        dt=t-delay
                        if dt>0:v+=math.sin(2*math.pi*f*dt)*math.exp(-dt*1.7)*g
                else:v=(s(48+u*8)*.24+s(96.4)*.12+s(143.7)*.07+low*1.5)*math.sin(math.pi*u)**1.1
                if not loop:v*=min(1,t/.04,(seconds-t)/.3)
                raw.append(v)
            if loop:
                target=raw[0]
                for n in range(256):
                    a=.5-.5*math.cos(math.pi*n/255);at=len(raw)-256+n;raw[at]=raw[at]*(1-a)+target*a
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.56/peak*32767) for v in raw));rms=math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm))
            report[name]={'duration':seconds,'loop':loop,'peak':max(abs(v) for v in pcm)/32768,'rms':rms,'splice_delta':abs(pcm[-1]-pcm[0])/32768 if loop else 0}
            source=Path(tmp)/(name+'.wav')
            with wave.open(str(source),'wb') as w:w.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));w.writeframes(pcm.tobytes())
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(assets/f'sounds/ambient/{name}.ogg')],check=True)
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'stream':loop,'volume':.65,'attenuation_distance':48 if 'impact' in name else 32}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
    print('Seven original observatory sounds generated; previous recordings retained.')
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
