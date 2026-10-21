"""Original 16px pixel art: restrained vanilla palettes, clustered stone, hand-placed motifs."""
import hashlib, random

EXTRA_BLOCKS = ['ashstone_bricks', 'cracked_ashstone_bricks', 'mossy_ashstone_bricks',
    'polished_ashstone', 'ashstone_tiles', 'chiseled_ashstone', 'ancient_pillar',
    'riveted_alloy', 'cut_alloy', 'oxidized_alloy', 'ancient_grate', 'dormant_conduit',
    'resonant_lamp', 'blue_lamp', 'red_lamp', 'green_lamp', 'archive_shelf',
    'ancient_console', 'alloy_vent']
SHAPES = {f'{base}_{shape}': (base,shape) for base in ['ashstone_bricks','polished_ashstone','cut_alloy'] for shape in ['stairs','slab']}
ACCENTS = {'blue': (48,126,179), 'red': (179,63,45), 'green': (66,132,64),
           'resonant': (150,121,179), 'composite': (84,182,160)}

def shade(c,n):return tuple(max(0,min(255,v+n)) for v in c[:3])
def pixels(name, item=False):
    rng=random.Random(int.from_bytes(hashlib.sha256(name.encode()).digest()[:8],'big'))
    accent=next((c for k,c in ACCENTS.items() if k in name),(72,137,128))
    p=[(0,0,0,0)]*256
    def dot(x,y,c):
        if 0<=x<16 and 0<=y<16:p[y*16+x]=(*c[:3],c[3] if len(c)>3 else 255)
    def rect(x0,y0,x1,y1,c):
        for y in range(y0,y1+1):
            for x in range(x0,x1+1):dot(x,y,c)
    def line(x0,y0,x1,y1,c):
        steps=max(abs(x1-x0),abs(y1-y0),1)
        for i in range(steps+1):dot(round(x0+(x1-x0)*i/steps),round(y0+(y1-y0)*i/steps),c)
    if item:
        outline=(34,39,39)
        if 'shard' in name or 'crystal' in name:
            for y in range(2,15):
                for x in range(2,14):
                    if abs(x-8)*1.6+abs(y-8)<9:
                        dot(x,y,outline)
                        if abs(x-8)*1.6+abs(y-8)<7:dot(x,y,shade(accent,28 if x<8 else -15))
            line(7,5,5,9,shade(accent,90));dot(7,4,(219,238,211));line(9,9,8,12,shade(accent,45))
            if 'composite' in name:
                for x,y,c in [(5,9,ACCENTS['blue']),(8,4,ACCENTS['red']),(10,9,ACCENTS['green'])]:dot(x,y,shade(c,55))
        elif name.endswith('_spawn_egg'):
            for y in range(2,15):
                for x in range(3,13):
                    if ((x-7.5)/4.6)**2+((y-8)/6.5)**2<1:
                        c=(89,103,89) if 'shardling' in name else (62,71,67)
                        if (x*3+y*7)%11<3:c=(105,180,145) if 'shardling' in name else (177,125,84)
                        dot(x,y,c)
        elif name=='echo_dust':
            for x,y in [(4,9),(8,5),(11,10),(7,12),(4,13),(11,4)]:
                dot(x,y,(46,62,58));dot(x+1,y,(110,199,180));dot(x,y-1,(189,229,202))
        elif name=='echo_lens':
            for y in range(2,13):
                for x in range(2,14):
                    d=abs(x-7.5)+abs(y-7)
                    if d<8:dot(x,y,(128,115,74) if d>5 else (62,148,142) if d>2 else (188,225,194))
            rect(7,12,8,14,(124,103,62));dot(5,5,(205,229,190))
        elif name=='relic_fragment':
            for y in range(3,13):
                for x in range(3,13):
                    if abs(x-8)+abs(y-8)<7:dot(x,y,outline if abs(x-8)+abs(y-8)>5 else (120,107,73))
            line(6,5,9,5,(191,176,110));line(6,5,6,10,(97,173,144));line(6,10,10,8,(97,173,144));dot(9,7,(220,222,156))
        elif name=='stabilizer_module':
            rect(2,3,13,12,outline);rect(3,4,12,11,(103,109,100));rect(4,5,11,10,(42,60,57))
            for x,y in [(4,5),(11,5),(4,10),(11,10)]:dot(x,y,(188,161,88))
            rect(6,6,9,9,(67,154,173));dot(6,6,(187,231,217));dot(8,7,(115,208,189))
        elif name=='field_journal':
            rect(3,2,12,14,outline);rect(4,3,11,12,(116,72,43));rect(6,3,10,11,(155,106,63))
            rect(5,13,11,13,(209,196,157));line(5,5,5,10,(191,156,92));rect(8,6,10,8,(86,149,128))
        elif name=='detector':
            rect(6,10,9,14,outline);rect(7,10,8,13,(125,80,45))
            for y in range(1,12):
                for x in range(2,14):
                    d=(x-7.5)**2+(y-6)**2
                    if d<30:dot(x,y,(49,58,56) if d<19 else (137,115,68))
            line(8,6,10,3,(194,224,187));line(7,7,5,9,(61,128,126));dot(8,6,(234,225,168))
        elif name=='locator_core':
            for y in range(2,14):
                for x in range(2,14):
                    d=abs(x-7.5)+abs(y-7.5)
                    if d<8:dot(x,y,outline if d>6 else (141,110,62))
                    if d<4:dot(x,y,shade((67,162,148),35 if x<8 else -20))
            dot(7,5,(218,238,197));dot(6,6,(154,215,187))
        else:
            rect(2,3,13,12,outline);rect(3,4,12,11,(119,102,66));rect(4,5,11,10,(57,67,60))
            for x in [4,7,10]:rect(x,6,x+1,8,(94,164,139));dot(x,10,(169,154,95))
        return p
    metal=any(k in name for k in ['alloy','conduit','console','extractor','vent','grate','controller','frame'])
    base=(112,111,106) if not metal else (85,88,79)
    if name=='oxidized_alloy':base=(68,113,99)
    if name in ['portal_frame','portal_controller']:base=(53,58,55)
    # Low contrast clusters, not per-pixel RGB snow.
    cluster={}
    for y in range(16):
        for x in range(16):
            k=(x//2,(y+(x//5))//2)
            if k not in cluster:cluster[k]=rng.choice([-12,-6,-3,0,3,7,11])
            dot(x,y,shade(base,cluster[k]+rng.choice([-2,0,0,2])))
    if name=='echo_bloom':
        rect(0,0,15,15,(48,93,91));
        for x in [2,6,10,14]:line(x,0,x,15,(119,199,173));line(x+1,0,x+1,15,(76,145,137))
        for x,y in [(3,4),(9,11),(13,2),(7,7)]:dot(x,y,(203,230,192))
    elif name.endswith('_ore'):
        for cx,cy in [(3,4),(11,3),(8,9),(3,12),(13,12)]:
            for dx,dy in [(0,-1),(-1,0),(0,0),(1,0),(0,1),(1,1)]:dot(cx+dx,cy+dy,shade(accent,-15 if dx==1 or dy==1 else 30))
            dot(cx-1,cy-1,shade(base,-25));dot(cx,cy-1,shade(accent,75))
    elif 'bricks' in name or name=='ashstone_tiles':
        h=4 if name=='ashstone_tiles' else 8
        for y in range(16):
            for x in range(16):
                offset=4 if (y//h)%2 else 0
                if y%h==0 or (x+offset)%8==0:dot(x,y,shade(base,-35))
                elif y%h==1:dot(x,y,shade(base,15))
        if 'cracked' in name:
            for a in [(4,2,5,5),(5,5,3,7),(11,9,10,12),(10,12,12,15)]:line(*a,shade(base,-43))
        if 'mossy' in name:
            for x,y in [(1,0),(2,0),(3,1),(12,8),(13,8),(14,9),(13,10),(0,14),(1,15)]:dot(x,y,(71,97,49))
    elif name in ['polished_ashstone','cut_alloy','riveted_alloy','ancient_alloy','oxidized_alloy']:
        for y in range(16):
            for x in range(16):
                if x==0 or y==0:dot(x,y,shade(base,20))
                if x==15 or y==15:dot(x,y,shade(base,-28))
        if name=='cut_alloy':
            line(0,7,15,7,shade(base,-28));line(7,0,7,15,shade(base,-28));line(1,8,14,8,shade(base,14))
        if name in ['ancient_alloy','riveted_alloy']:
            for x,y in [(2,2),(13,2),(2,13),(13,13)]:dot(x,y,(165,144,96));dot(x+1,y+1,(48,50,44))
    elif name=='ancient_pillar':
        for x in [1,5,10,14]:line(x,0,x,15,shade(base,-24));line(x+1,0,x+1,15,shade(base,14))
        for y in [0,2,13,15]:line(0,y,15,y,(74,83,74))
    elif name.startswith('mural_') or name=='chiseled_ashstone':
        rect(1,1,14,14,(73,79,71));rect(2,2,13,13,(135,133,117));rect(3,3,12,12,(91,94,82))
        if 'ocean' in name:
            for y in [5,8,11]:
                for x in range(4,12):dot(x,y+(x//2)%2,shade(ACCENTS['blue'],30))
        elif 'lava' in name:
            for y in range(4,12):
                for x in range(4,12):
                    if abs(x-8)<(y-3)//2:dot(x,y,shade(ACCENTS['red'],30 if x<8 else 0))
        elif 'forest' in name:
            line(8,6,8,12,(161,128,74));rect(5,5,10,7,(79,128,57));rect(6,4,9,5,(98,151,68))
        else:
            for x,y,c in [(5,5,ACCENTS['blue']),(10,5,ACCENTS['red']),(8,10,ACCENTS['green'])]:rect(x-1,y-1,x+1,y+1,shade(c,35))
            line(6,7,8,9,(181,177,123));line(9,7,8,9,(181,177,123))
    elif name in ['conduit','dormant_conduit','portal_frame','portal_controller']:
        dark=(34,44,40); glow=(94,167,143) if name!='dormant_conduit' else (80,96,77)
        for x in [4,11]:line(x,0,x,15,dark);line(x+1,0,x+1,15,glow)
        line(5,7,11,7,glow);rect(7,6,9,9,(177,196,143) if name=='conduit' else (109,132,100))
        if name=='portal_controller':rect(2,2,13,13,(91,94,76));rect(4,4,11,11,(35,49,43));rect(6,6,9,9,(91,178,145))
    elif 'lamp' in name:
        rect(1,1,14,14,(55,65,53));rect(3,3,12,12,shade(accent,5));rect(4,4,11,11,shade(accent,55));rect(6,6,9,9,(222,227,175))
        for x,y in [(2,2),(13,2),(2,13),(13,13)]:dot(x,y,(160,142,95))
        line(7,2,7,13,(91,102,76));line(2,7,13,7,(91,102,76))
    elif name=='archive_shelf':
        rect(0,0,15,15,(63,58,43))
        for x in [1,4,6,9,12]:
            c=rng.choice([(125,70,51),(83,111,93),(155,133,78),(100,95,124)])
            rect(x,2,x+1,6,c);rect(x,10,x+1,14,shade(c,-9));dot(x,3,(185,170,113));dot(x,11,(185,170,113))
        for y in [0,7,8,15]:line(0,y,15,y,(114,98,66))
    elif name in ['ancient_grate','alloy_vent']:
        for y in range(2,15,3):rect(2,y,13,y+1,(35,43,37));line(2,y-1,13,y-1,(131,130,103))
        for x in [0,15]:line(x,0,x,15,(149,134,95))
    elif name in ['extractor','ancient_console','ancient_console_active']:
        rect(1,1,14,14,(53,61,52));rect(3,3,12,8,(24,40,33))
        for y in [4,6]:line(5,y,10,y,(151,222,152) if name.endswith('_active') else (60,83,64) if name=='ancient_console' else (87,151,104))
        rect(3,11,5,12,(146,112,64));rect(8,11,10,12,(98,136,109));dot(12,11,(178,83,47))
        for x,y in [(1,1),(14,1),(1,14),(14,14)]:dot(x,y,(163,146,100))
    return p
