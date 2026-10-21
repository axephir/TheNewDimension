#!/usr/bin/env python3
"""Create a short illustrative montage from the actual distributed audio assets."""
from array import array
from pathlib import Path
import subprocess,tempfile,wave

root=Path(__file__).resolve().parents[1];sounds=root/'src/main/resources/assets/tnd/sounds/ambient';rate=22050
def decode(name):
    samples=array('h');samples.frombytes(subprocess.check_output(['ffmpeg','-v','error','-i',str(sounds/(name+'.ogg')),'-f','s16le','-ac','1','-ar',str(rate),'-']));return samples
montage=array('f')
for name,cue in [('vestige_wind',None),('fracture_drone','distant_pulse'),('hollow_resonance','stone_shift'),('glass_whispers','glass_tinkle'),('basin_lament','submerged_groan'),('dune_rumble','dust_scrape'),('night_pressure','low_signal'),('enclosed_hum','reverse_breath'),('mastery_ascend',None)]:
    pcm=decode(name);part=array('f',(v/32768*.85 for v in pcm[:8*rate]))
    if cue:
        for i,v in enumerate(decode(cue)):
            at=i+rate
            if at<len(part):part[at]+=v/32768*.3
    overlap=min(rate,len(montage),len(part))
    for i in range(overlap):
        a=i/overlap;montage[len(montage)-overlap+i]=montage[len(montage)-overlap+i]*(1-a)+part[i]*a
    montage.extend(part[overlap:])
peak=max(abs(v) for v in montage);gain=min(1,.8/peak)
pcm=array('h',(round(v*gain*32767) for v in montage));out=Path('/workspace/shared/Vestige-0.6-audio-preview.ogg');out.parent.mkdir(parents=True,exist_ok=True)
with tempfile.TemporaryDirectory(prefix='tnd-audio-preview-') as tmp:
    wav=Path(tmp)/'preview.wav'
    with wave.open(str(wav),'wb') as f:f.setparams((1,2,rate,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
    subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-map_metadata','-1','-c:a','libvorbis','-q:a','5',str(out)],check=True)
print(out,round(len(pcm)/rate,2),'seconds')
