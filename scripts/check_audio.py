#!/usr/bin/env python3
"""Decode the distributed Vorbis files and check duration, audibility and headroom (ffmpeg required)."""
from pathlib import Path
from array import array
import json,math,subprocess

assets=Path(__file__).resolve().parents[1]/'src/main/resources/assets/tnd'
report=json.loads((assets/'sound_design_report.json').read_text())
for name,expected in report.items():
    path=assets/f'sounds/ambient/{name}.ogg'
    pcm=array('h');pcm.frombytes(subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-f','s16le','-ac','1','-ar','22050','-']))
    peak=max(abs(v) for v in pcm)/32768;rms=math.sqrt(sum((v/32768)**2 for v in pcm)/len(pcm));duration=len(pcm)/22050
    assert abs(duration-expected['duration'])<.05,(name,duration)
    assert peak<.7 and rms>.015,(name,peak,rms)
    if expected['loop']:assert abs(pcm[0]-pcm[-1])/32768<.05,(name,'loop boundary')
    print(f'{name}: {duration:.2f}s, peak={peak:.3f}, RMS={rms:.3f}')
print(f'PASS: {len(report)} distributed sounds decode, with audible signal and no clipping.')
