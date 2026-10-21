#!/usr/bin/env python3
"""Concatenate the six actual 0.7 gameplay cues, with short silences for listening."""
from pathlib import Path
from array import array
import argparse,subprocess,tempfile,wave

parser=argparse.ArgumentParser()
parser.add_argument('--output',type=Path,default=Path('/workspace/shared/Vestige-0.7-gameplay-preview.ogg'))
args=parser.parse_args();args.output.parent.mkdir(parents=True,exist_ok=True)
assets=Path(__file__).resolve().parents[1]/'src/main/resources/assets/tnd/sounds/ambient'
pcm=array('h')
for name in ['custodian_awaken','custodian_fracture','custodian_crown','custodian_impact','custodian_fall','forge_complete']:
    part=array('h');part.frombytes(subprocess.check_output(['ffmpeg','-v','error','-i',str(assets/(name+'.ogg')),'-f','s16le','-ac','1','-ar','22050','-']))
    pcm.extend(part);pcm.extend(array('h',[0])*5512)
with tempfile.TemporaryDirectory(prefix='tnd07-audio-') as tmp:
    wav=Path(tmp)/'preview.wav'
    with wave.open(str(wav),'wb') as f:f.setparams((1,2,22050,len(pcm),'NONE','not compressed'));f.writeframes(pcm.tobytes())
    subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-map_metadata','-1','-c:a','libvorbis','-q:a','5',str(args.output)],check=True)
print(args.output,round(len(pcm)/22050,2),'seconds')
