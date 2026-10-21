"""Sixteen original short interaction/creature cues; preserves the previous 24 encoded sounds."""
from pathlib import Path
from array import array
import math,random,hashlib,json,wave,subprocess,tempfile
from awakening_resources import SOUNDS
RATE=22050
def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='tnd-awakening-audio-') as tmp:
        for index,name in enumerate(SOUNDS):
            seconds=2.6 if name in ['lithoguard_wake','lithoguard_fall','ruin_chime'] else 1.8 if name.endswith('charge') or name=='anchor_depart' else .6 if name.endswith(('step','hurt','shot')) else 1.2
            rng=random.Random(int.from_bytes(hashlib.sha256(name.encode()).digest()[:8],'big'));raw=array('f');low=mid=0.
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;noise=rng.uniform(-1,1);low=low*.985+noise*.015;mid=mid*.8+noise*.2
                sin=lambda f:math.sin(2*math.pi*f*t)
                if name.startswith('lithoguard'):
                    if name.endswith('charge'):v=(sin(58+u*15)*.28+sin(117.3)*.12+low*2.2)*math.sin(math.pi*u)**.7
                    elif name.endswith('wake'):v=(sin(39+u*12)*.35+sin(82.7)*.15+mid*.22+low*1.8)*math.sin(math.pi*u)**1.2
                    elif name.endswith('fall'):v=(sin(78-u*30)*.32+low*3+mid*.35)*math.sin(math.pi*u)*math.exp(-u*1.4)
                    else:v=(sin(52)*.6+sin(103.1)*.2+low*2+mid*.7)*math.exp(-t*(11 if name.endswith('step') else 5))
                elif name.startswith('stalker'):
                    frequency=430+u*190 if name.endswith('charge') else 610-u*220
                    v=(sin(frequency)*.13+sin(frequency*1.51)*.08+mid*.6)*math.sin(math.pi*u)**.8
                elif name=='hammer_charge':v=(sin(88+u*70)*.3+sin(177)*.1+mid*.35)*math.sin(math.pi*u)
                elif name=='hammer_impact':v=(sin(47)*.6+sin(141.2)*.2+mid*.8+low*2.5)*math.exp(-t*4)
                elif name=='anchor_depart':v=(sin(176+u*110)*.16+sin(352+u*80)*.12+low*.7)*math.sin(math.pi*u)**1.2
                else:
                    roots={'separator_complete':220,'reaper_harvest':294,'ruin_chime':196,'expedition_complete':440};root=roots.get(name,330)
                    v=0
                    for delay,mult,gain in [(0,1,.3),(.09,1.5,.18),(.18,2,.1),(.27,3.01,.06)]:
                        dt=t-delay
                        if dt>0:v+=math.sin(2*math.pi*root*mult*dt)*math.exp(-dt*(2.1 if name=='ruin_chime' else 5))*gain
                    v+=mid*.07*math.exp(-t*4)
                raw.append(v*min(1,t/.025,(seconds-t)/.15))
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.56/peak*32767) for v in raw));rms=math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm))
            report[name]={'duration':seconds,'loop':False,'peak':max(abs(v) for v in pcm)/32768,'rms':rms,'splice_delta':0}
            source=Path(tmp)/(name+'.wav')
            with wave.open(str(source),'wb') as out:out.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));out.writeframes(pcm.tobytes())
            dest=assets/f'sounds/ambient/{name}.ogg'
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(dest)],check=True)
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'volume':.65,'attenuation_distance':24}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
    print('Generated 16 new original cues; 24 previous encoded sounds retained.')
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
