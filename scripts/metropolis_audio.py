"""Six original local interaction cues; existing 52 OGG files are not re-encoded."""
from array import array
from pathlib import Path
import hashlib,json,math,random,subprocess,tempfile,wave
RATE=22050
SOUNDS={'incubator_start':4.,'incubator_release':1.6,'familiar_bond':2.8,'familiar_evolve':2.8,'familiar_down':.9,'familiar_pulse':1.2}
def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='symbiosis-audio-') as tmp:
        for name,seconds in SOUNDS.items():
            rng=random.Random(name);raw=array('f');low=0
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;low=.96*low+.04*rng.uniform(-1,1);sin=lambda f:math.sin(2*math.pi*f*t)
                if name=='incubator_start':v=(sin(54+18*u)*.16+sin(147+100*u)*.06+low*.7)*math.sin(math.pi*u)**.6
                elif name=='incubator_release':v=(sin(73)*.22+sin(219)*.07+low*2)*math.exp(-t*2.2)
                elif name in ['familiar_bond','familiar_evolve']:
                    v=0;notes=[(0,294),(.20,440),(.40,587),(.65,740),(.9,880)] if name=='familiar_bond' else [(0,196),(.14,392),(.3,494),(.48,587),(.7,784)]
                    for delay,f in notes:
                        if t>=delay:v+=(math.sin(2*math.pi*f*(t-delay))*.16+math.sin(2*math.pi*f*2.003*(t-delay))*.028)*math.exp(-(t-delay)*2.4)
                elif name=='familiar_down':v=(sin(215-u*130)*.18+sin(86)*.22+low*.35)*math.exp(-t*5)
                else:v=(sin(70+u*90)*.26+sin(350-u*150)*.06+low*.4)*math.exp(-t*3)
                v*=max(0,min(1,t/.018,(seconds-t)/.15));raw.append(v)
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.48/peak*32767) for v in raw));rms=math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm))
            report[name]={'duration':seconds,'loop':False,'peak':max(abs(v) for v in pcm)/32768,'rms':rms,'splice_delta':abs(pcm[-1]-pcm[0])/32768}
            path=Path(tmp)/(name+'.wav')
            with wave.open(str(path),'wb') as f:f.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(path),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(assets/f'sounds/ambient/{name}.ogg')],check=True)
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'volume':.65,'attenuation_distance':24}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
