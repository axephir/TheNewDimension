"""Twelve original synthesized sounds. Existing encoded releases remain byte-identical."""
from array import array
from pathlib import Path
import hashlib,json,math,random,subprocess,tempfile,wave
from city_resources import SOUNDS
RATE=22050

def generate(assets,write):
    sounds=json.loads((assets/'sounds.json').read_text());report=json.loads((assets/'sound_design_report.json').read_text())
    with tempfile.TemporaryDirectory(prefix='nacre-audio-') as tmp:
        for name in SOUNDS:
            loop=name=='nacre_ambience';seconds=24 if loop else 6 if name=='gate_charge' else 3.2 if name in ['gate_open','gate_transit','gate_hum'] else 2 if name in ['city_drone','city_alarm'] else 1.5 if name=='archive_read' else .7
            rng=random.Random(int.from_bytes(hashlib.sha256(name.encode()).digest()[:8],'big'));raw=array('f');low=mid=0.
            for i in range(round(seconds*RATE)):
                t=i/RATE;u=t/seconds;n=rng.uniform(-1,1);low=.985*low+.015*n;mid=.8*mid+.2*n
                sin=lambda f:math.sin(2*math.pi*f*t)
                if loop:
                    # All oscillators and modulators complete integral cycles: the PCM seam is quiet.
                    v=sin(43)*(.15+.04*math.cos(2*math.pi*u*2))+sin(66)*.06+sin(119)*.025
                    v+=sin(223)*.018*(1+math.sin(2*math.pi*u*3))
                    v+=sin(37)*.045*math.cos(2*math.pi*u*5)
                elif name=='gate_charge':v=(sin(45+u*18)*.25+sin(92+u*32)*.12+low*1.5+sin(185+u*260)*.05)*math.sin(math.pi*u)**.65
                elif name in ['gate_open','gate_transit','gate_hum']:
                    v=(sin(49)*.3+sin(99.1)*.1+sin(148.7)*.065+low*1.5)*math.sin(math.pi*u)**(1.7 if name=='gate_hum' else .7)
                    if name=='gate_transit':v+=sin(330-u*200)*.1*math.sin(math.pi*u)
                elif name=='city_alarm':v=(sin(250+80*math.sin(t*12))*.16+sin(500+160*math.sin(t*12))*.06)*math.sin(math.pi*u)
                elif name=='city_drone':v=(sin(110+u*70)*.2+sin(440+u*300)*.055+mid*.1)*math.sin(math.pi*u)
                elif name=='city_footstep':v=(sin(72)*.4+sin(201)*.1+mid*.65)*math.exp(-t*16)
                elif name=='city_laser':v=(sin(1200-u*950)*.13+sin(680-u*400)*.08+mid*.2)*math.exp(-t*5)
                elif name=='city_dash':v=(sin(175+u*200)*.14+low*2+mid*.5)*math.sin(math.pi*u)**1.2
                elif name=='city_impact':v=(sin(54)*.5+sin(108.2)*.14+mid*.6+low*2)*math.exp(-t*7)
                else:
                    v=0
                    for delay,f,g in [(0,294,.22),(.15,440,.15),(.3,587,.1),(.45,880,.05)]:
                        if t>delay:v+=math.sin(2*math.pi*f*(t-delay))*g*math.exp(-(t-delay)*3)
                if not loop:v*=max(0,min(1,t/.02,(seconds-t)/.12))
                raw.append(v)
            peak=max(abs(v) for v in raw);pcm=array('h',(round(v*.56/peak*32767) for v in raw));rms=math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm))
            report[name]={'duration':seconds,'loop':loop,'peak':max(abs(v) for v in pcm)/32768,'rms':rms,'splice_delta':abs(pcm[-1]-pcm[0])/32768}
            path=Path(tmp)/(name+'.wav')
            with wave.open(str(path),'wb') as f:f.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(path),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(assets/f'sounds/ambient/{name}.ogg')],check=True)
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[{'name':'tnd:ambient/'+name,'volume':.38 if loop else .65,'attenuation_distance':24}]}
    write(assets/'sounds.json',sounds);write(assets/'sound_design_report.json',report)
if __name__=='__main__':
    from generate_resources import ASSETS,write
    generate(ASSETS,write)
