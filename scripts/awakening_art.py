"""Authored 16px palettes and silhouettes for 0.9. No scaled texture-pack assets."""
import hashlib,random
DECOR=['citadel_bricks','carved_citadel_bricks','cracked_citadel_bricks','gilded_pillar','gilded_trim','auralite_block','umbra_block','umbra_tiles','etched_umbra','crystal_mosaic','conservatory_tiles','verdigris_panel','latticed_alloy','runic_lamp','amber_lamp','violet_lamp','lithic_trophy','nightslag','veined_nightslag']
BLOCKS=['auralite_ore','noctilite_ore']+DECOR+['harmonic_chime','ore_separator','echo_reaper']
ITEMS=['raw_auralite','raw_noctilite','auralite_grit','noctilite_grit','auralite_ingot','noctilite_crystal','umbra_alloy','lithic_heart','glass_filament','auralite_pickaxe','auralite_blade','umbra_pickaxe','umbra_blade','gravitas_hammer','umbra_helmet','umbra_chestplate','umbra_leggings','umbra_boots','lithoguard_spawn_egg','glass_stalker_spawn_egg']
ENTITIES=['lithoguard','lithoguard_charged','glass_stalker','glass_stalker_charged']
def paint(name,item=False):
    from armor_icons import ART, pixels
    if name in ART:return pixels(name)
    if name not in BLOCKS+ITEMS+ENTITIES+['umbra_armor','umbra_leggings_armor']:return None
    rng=random.Random(int.from_bytes(hashlib.sha256(name.encode()).digest()[:8],'big'))
    p=[(0,0,0,0)]*256
    dark=(31,31,42);gold=(185,140,66);light=(232,204,128);violet=(146,115,192);pale=(203,185,228);teal=(73,126,119)
    accent=violet if any(t in name for t in ['umbra','noctilite','stalker','violet']) else gold
    high=pale if accent==violet else light
    def shade(c,n):return tuple(max(0,min(255,v+n)) for v in c)
    def dot(x,y,c):
        if 0<=x<16 and 0<=y<16:p[y*16+x]=(*c[:3],255)
    def rect(x0,y0,x1,y1,c):
        for y in range(y0,y1+1):
            for x in range(x0,x1+1):dot(x,y,c)
    def line(x0,y0,x1,y1,c):
        count=max(abs(x1-x0),abs(y1-y0),1)
        for t in range(count+1):dot(round(x0+(x1-x0)*t/count),round(y0+(y1-y0)*t/count),c)
    if item:
        if 'spawn_egg' in name:
            for y in range(2,15):
                for x in range(2,14):
                    if ((x-7.5)/5.5)**2+((y-8)/6.2)**2<1:dot(x,y,shade((76,72,68),rng.choice([-12,0,10])))
            for x,y in [(5,5),(9,4),(11,9),(5,11),(7,8)]:rect(x,y,x+1,y+1,accent)
        elif name.endswith(('_pickaxe','_blade')) or name=='gravitas_hammer':
            line(3,14,11,6,dark);line(4,14,12,6,dark);line(4,13,11,6,(141,105,64));line(5,12,11,6,(192,156,94))
            if name.endswith('pickaxe'):
                for x,y in [(3,3),(4,2),(5,2),(6,2),(7,3),(8,3),(9,4),(10,5),(11,6),(12,7),(13,8),(13,9)]:dot(x,y,dark);dot(x,y+1,accent);dot(x,y-1,high)
            elif name=='gravitas_hammer':
                for x,y in [(5,2),(6,1),(7,2),(8,3),(9,4),(10,5),(11,6),(12,7)]:rect(x,y,x+2,y+3,dark);dot(x,y+1,accent)
                line(6,2,12,8,high);line(5,5,10,10,accent);dot(9,5,(222,224,177))
            else:
                for i in range(8):rect(5+i,10-i,7+i,12-i,dark);dot(6+i,10-i,high);dot(7+i,10-i,accent)
                line(3,9,7,13,gold);dot(13,2,high)
        elif name.startswith('umbra_') and name.endswith(('helmet','chestplate','leggings','boots')):
            if name.endswith('helmet'):
                rect(3,3,12,10,dark);rect(4,3,11,4,accent);rect(3,5,5,11,accent);rect(10,5,12,11,accent);line(6,6,9,6,high)
            elif name.endswith('chestplate'):
                rect(2,3,13,7,dark);rect(4,6,11,13,dark);rect(3,4,5,6,accent);rect(10,4,12,6,accent);rect(5,8,10,12,accent);line(7,7,8,11,gold);dot(8,8,pale)
            elif name.endswith('leggings'):
                rect(4,2,11,6,dark);rect(4,6,6,13,dark);rect(9,6,11,13,dark);rect(5,3,10,4,accent);line(5,6,5,12,accent);line(10,6,10,12,accent)
            else:
                rect(3,6,6,12,dark);rect(9,6,12,12,dark);rect(2,12,6,14,accent);rect(9,12,13,14,accent);line(4,7,5,10,accent);line(10,7,11,10,accent)
        elif name=='glass_filament':
            for y in range(3,13):x=5+(y%4);line(x,y,x+3,y+1,accent);dot(x,y,high)
        elif name.endswith(('ingot','alloy')):
            rect(3,7,12,11,dark);rect(4,5,11,10,accent);line(4,5,10,5,high);line(3,7,11,7,high);line(5,11,12,11,shade(accent,-35));line(11,6,12,9,shade(accent,-20))
        elif name.endswith('grit'):
            for x,y in [(3,10),(6,8),(9,6),(11,10),(7,12)]:rect(x,y,x+2,y+1,dark);dot(x+1,y,accent);dot(x+2,y,high)
        else:
            for y in range(2,14):
                for x in range(2,14):
                    if abs(x-7.5)*.8+abs(y-7.5)<6:dot(x,y,dark if x in [3,12] or y in [2,13] else shade(accent,rng.choice([-25,-10,0,12])))
            line(5,4,7,3,high);line(4,6,5,4,high);dot(8,7,high)
        return p
    if name in ['umbra_armor','umbra_leggings_armor']:
        # Native armor UVs use 64x32 logical coordinates. Map those islands deliberately
        # into the requested 16x16 atlas, including an open visor over the player's face.
        for y in range(16):
            for x in range(16):dot(x,y,shade((73,61,84),rng.choice([-5,0,5])))
        rect(0,0,7,7,(79,65,94));rect(2,4,3,5,(156,130,177))
        for x in [2,3]:
            for y in [6,7]:p[y*16+x]=(0,0,0,0)
        line(1,4,4,4,gold);line(0,7,1,7,gold);line(4,7,5,7,gold)
        for x0 in [5,8]:
            rect(x0,10,x0+2,15,(99,78,120));line(x0,10,x0+2,10,gold)
            dot(x0+1,12,pale);dot(x0+1,13,gold);line(x0,15,x0+2,15,gold)
        for x0 in [1,3,11,13]:
            rect(x0,10,x0+1,15,(101,80,121));line(x0,10,x0+1,10,gold);line(x0,14,x0+1,14,(165,139,187))
        return p
    base=(60,61,67) if 'umbra' in name else (102,107,103) if 'citadel' in name else (61,85,82) if any(t in name for t in ['conservatory','verdigris']) else (69,75,78)
    for y in range(16):
        for x in range(16):dot(x,y,shade(base,rng.choice([-8,-3,0,2,5])))
    if name in ['nightslag','veined_nightslag']:
        for y in range(16):
            for x in range(16):
                c=(24,28,34) if (x+y*2)%9<6 else (39,44,50)
                dot(x,y,shade(c,rng.choice([-4,-1,0,2,4])))
        for x0,y0 in [(1,3),(6,0),(10,8),(2,12)]:
            line(x0,y0,min(15,x0+4),min(15,y0+3),(12,15,21))
            line(x0+1,y0,min(15,x0+5),min(15,y0+3),(59,65,73))
        if name=='veined_nightslag':
            line(2,0,4,5,(84,92,109));line(4,5,9,8,(109,114,139));line(9,8,10,15,(65,83,111))
            for x,y in [(3,3),(6,6),(10,12)]:dot(x,y,(162,164,183))
    elif name.endswith('_ore'):

        for x,y in [(3,4),(10,2),(12,9),(5,12),(7,7)]:
            rect(x-1,y,x+1,y+2,shade(base,-24));dot(x,y,high);dot(x+1,y+1,accent);dot(x,y+2,shade(accent,-20));dot(x-1,y+1,accent)
    elif 'armor' in name or name in ENTITIES:
        for y in [0,7,15]:line(0,y,15,y,dark);line(1,y+1,14,y+1,shade(accent,-25))
        for x in [0,7,15]:line(x,1,x,14,dark)
        for x,y in [(2,3),(9,3),(3,10),(10,10)]:rect(x,y,x+2,y+2,accent);dot(x,y,high)
        if 'charged' in name:rect(8,8,13,12,high);line(9,9,12,11,(239,231,192))
    elif name.endswith('lamp'):
        rect(1,1,14,14,dark);rect(3,3,12,12,gold);rect(4,4,11,11,high);rect(6,5,9,10,accent);line(4,11,11,11,shade(accent,-20))
    elif name in ['ore_separator','echo_reaper','harmonic_chime','lithic_trophy']:
        rect(0,0,15,2,dark);rect(0,13,15,15,dark);rect(1,3,14,12,shade(base,-15));rect(3,4,12,11,accent)
        for y in range(5,11,2):line(4,y,11,y,dark)
        for x,y in [(1,1),(14,1),(1,14),(14,14)]:dot(x,y,high)
        if name=='echo_reaper':line(6,4,9,10,pale)
    elif name in ['gilded_pillar','gilded_trim']:
        for x in [1,4,11,14]:line(x,0,x,15,gold);line(x+1,0,x+1,15,light if x==4 else shade(gold,-20))
        for y in [0,1,14,15]:line(0,y,15,y,dark if y in [0,15] else gold)
    elif name=='latticed_alloy':
        for t in range(-12,16,4):line(max(0,t),max(0,-t),min(15,t+15),min(15,15-t),gold)
    elif name in ['auralite_block','umbra_block','etched_umbra','crystal_mosaic']:
        rect(1,1,14,14,dark);rect(2,2,13,13,shade(accent,-30));line(2,2,13,2,high);line(2,2,2,13,accent)
        for x,y in [(7,4),(5,6),(9,6),(7,8),(7,11)]:dot(x,y,high);dot(x+1,y,accent)
    else:
        # Staggered masonry with hand-set highlights and restrained mortar.
        for y in [3,11]:
            line(0,y,15,y,shade(base,-25));line(0,y+1,15,y+1,shade(base,10))
        for x,y in [(4,0),(12,0),(0,4),(8,4),(4,12),(12,12)]:line(x,y,x,min(15,y+6),shade(base,-24))
        if name in ['carved_citadel_bricks','conservatory_tiles','verdigris_panel']:
            line(5,5,10,5,gold);line(5,5,5,9,gold);line(10,5,10,9,gold);line(5,10,10,10,shade(gold,-25))
        if name=='cracked_citadel_bricks':line(7,1,9,7,dark);line(9,7,6,14,dark)
    return p
