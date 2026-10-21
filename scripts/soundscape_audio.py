"""Original procedural sound design. Standard library + ffmpeg; no sampled voices or downloaded audio.

Loops use a two-second overlap around their splice. Transients have slow attacks;
all PCM is measured and normalized before Vorbis encoding, with ample headroom.
"""
from pathlib import Path
from array import array
import hashlib,json,math,random,subprocess,tempfile,wave

RATE=22050
LOOPS=['vestige_wind','fracture_drone','hollow_resonance','glass_whispers','basin_lament','dune_rumble','night_pressure','enclosed_hum']
CUES=['low_signal','metal_echo','distant_pulse','glass_tinkle','stone_shift','submerged_groan','dust_scrape','reverse_breath','spatial_chime','mastery_ascend']

def synth(name,seconds,loop):
    rng=random.Random(int.from_bytes(hashlib.sha256(name.encode()).digest()[:8],'big'))
    phases=[rng.random()*math.tau for _ in range(8)]
    length=int(seconds*RATE);overlap=2*RATE if loop else 0;raw=array('f');low=mid=0.0
    tau=math.tau
    for i in range(length+overlap):
        t=i/RATE;white=rng.uniform(-1,1);low=low*.992+white*.008;mid=mid*.87+white*.13
        breath=.55+.25*math.sin(tau*t/11+phases[0])+.15*math.sin(tau*t/7.1+phases[1])
        s=lambda f,p=0:math.sin(tau*f*t+phases[p])
        if name=='vestige_wind':v=low*3.4*breath+mid*.16+s(73)*.08+s(109.3)*.04
        elif name=='fracture_drone':v=low*2.3*breath+s(61)*.12+s(91.7)*.08+s(183.3)*.04*(.6+.4*s(.13))
        elif name=='hollow_resonance':v=low*1.8*breath+s(58)*.15+s(87.2)*.12+s(174.6)*.05+s(349.8)*.025
        elif name=='glass_whispers':v=low*1.8*breath+mid*.2+s(164.2)*.055+s(246.8)*.04+s(493.9)*.025*(.5+.5*s(.11))
        elif name=='basin_lament':v=low*2.8*breath+mid*.25*(.5+.5*s(.4))+s(62)*.1+s(123.1)*.05+s(291+6*math.sin(t*.6))*.035
        elif name=='dune_rumble':v=low*3.6*breath+mid*.25+s(49)*.14+s(98.4)*.06
        elif name=='night_pressure':v=low*1.4*breath+s(55)*.17+s(55.7)*.14+s(82.6)*.1+s(220.3)*.025
        elif name=='enclosed_hum':v=low*1.5+s(79.8)*.12+s(120.4)*.11+s(241.3)*.05*(.5+.5*s(.09))
        elif name=='mastery_ascend':
            v=mid*.13*math.sin(math.pi*min(t/2.4,1))**2
            for delay,freq in [(0,392),(.17,493.88),(.34,587.33),(.52,783.99)]:
                dt=t-delay
                if dt>=0:v+=(1-math.exp(-dt*45))*math.exp(-dt*2.5)*(math.sin(tau*freq*dt)*.24+math.sin(tau*freq*2*dt)*.07)
        else:
            env=(1-math.exp(-t*3))*math.exp(-t*.65)
            if name=='low_signal':v=math.sin(math.pi*min(t/seconds,1))**2*(s(49)*.33+s(73.8)*.18+low*.9)
            elif name=='metal_echo':v=env*(s(173)*.32+s(269)*.22+s(419)*.14+s(631)*.07)
            elif name=='distant_pulse':v=env*(s(57)*.32+s(114.7)*.12+low*.8)*(.5+.5*math.cos(t*5))
            elif name=='glass_tinkle':v=env*(s(587)*.13+s(882.1)*.1+s(1181.4)*.045)*(.7+.3*s(.8))
            elif name=='stone_shift':v=env*(low*3+mid*.4+s(144)*.09)
            elif name=='submerged_groan':v=env*(s(79+9*math.sin(t*.7))*.25+s(158)*.07+low*1.5)
            elif name=='dust_scrape':v=env*(mid*.8+low*2)*(.6+.4*s(.8))
            elif name=='reverse_breath':v=math.sin(math.pi*min(t/seconds,1))**2*(mid*.6+low*2+s(313)*.025)
            else:v=env*(s(196)*.16+s(294.8)*.14+s(441.7)*.07+s(590.1)*.04)
        raw.append(v)
    if loop:
        for i in range(overlap):
            a=i/(overlap-1);raw[i]=raw[length+i]*(1-a)+raw[i]*a
        del raw[length:]
        # Match the endpoint over a six-millisecond bridge, without silencing the loop.
        bridge=128;target=raw[0]
        for j in range(bridge):
            mix=.5-.5*math.cos(math.pi*j/(bridge-1));at=length-bridge+j
            raw[at]=raw[at]*(1-mix)+target*mix
    else:
        # End with a smooth half-second release, including the tails of the chime.
        for i in range(length):raw[i]*=min(1,i/(RATE*.08),(length-1-i)/(RATE*.5))
    peak=max(abs(v) for v in raw);gain=min(1.5,.58/peak) if peak else 0
    pcm=array('h',(round(v*gain*32767) for v in raw))
    rms=math.sqrt(sum((s/32768)**2 for s in pcm)/len(pcm))
    return pcm,{'duration':seconds,'loop':loop,'peak':max(abs(s) for s in pcm)/32768,'rms':rms,'splice_delta':abs(pcm[-1]-pcm[0])/32768 if loop else 0}

def generate(assets,write):
    sounds={};report={}
    with tempfile.TemporaryDirectory(prefix='tnd-sound-design-') as tmp:
        for name in LOOPS+CUES:
            loop=name in LOOPS;duration=32 if loop else 2.5 if name=='mastery_ascend' else 7 if name in ['low_signal','reverse_breath','submerged_groan'] else 5
            pcm,stats=synth(name,duration,loop);report[name]=stats
            source=Path(tmp)/(name+'.wav')
            with wave.open(str(source),'wb') as out:out.setparams((1,2,RATE,len(pcm),'NONE','not compressed'));out.writeframes(pcm.tobytes())
            dest=assets/f'sounds/ambient/{name}.ogg';dest.parent.mkdir(parents=True,exist_ok=True)
            subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-map_metadata','-1','-c:a','libvorbis','-q:a','4',str(dest)],check=True)
            entry={'name':'tnd:ambient/'+name,'volume':.52 if loop else .8 if name=='mastery_ascend' else .65,'attenuation_distance':32}
            if loop:entry['stream']=True
            sounds[name]={'subtitle':'subtitles.tnd.'+name,'sounds':[entry]}
    write(assets/'sounds.json',sounds)
    write(assets/'sound_design_report.json',report)
    print('Synthesized',len(sounds),'original sounds; peak PCM',round(max(s['peak'] for s in report.values()),3))

if __name__=='__main__':
    root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/tnd'
    def write(p,data):p.write_text(json.dumps(data,indent=2)+'\n')
    generate(root,write)
