"""Original 64px character atlases. Blocks and inventory icons remain vanilla-scale 16px.

UV islands follow the native cube unfolding: shell (0,0), face (0,16),
joints (32,0), luminous cores (48,0), relief plates (0,32), metal (32,32).
Armor keeps the vanilla 64x32 layout in the upper half, with relief in the lower half.
"""
import random

MOBS=['shardling','shardling_bonded','rift_sentinel','rift_sentinel_charged',
      'fractured_custodian','fractured_custodian_open','fractured_custodian_enraged',
      'lithoguard','lithoguard_charged','glass_stalker','glass_stalker_charged']

def mob(name):
    rng=random.Random('character-v09-'+name)
    glass='stalker' in name;heavy='lithoguard' in name
    charged=any(s in name for s in ['charged','enraged','open'])
    bonded='bonded' in name
    stone=(38,47,54) if glass else (66,64,60) if heavy else (58,65,64)
    rim=(138,122,86) if heavy else (109,121,125) if glass else (120,116,98)
    glow=(243,191,111) if heavy else (183,157,227) if glass else (112,206,181) if bonded else (181,157,215)
    if charged:glow=(250,222,151) if heavy else (235,199,245)
    pixels=[]
    def shade(c,n):return tuple(max(0,min(255,v+n)) for v in c)
    for y in range(64):
        for x in range(64):
            base=stone
            if x>=48 and y<32:
                c=shade(glow,(-18 if (x+y)%7==0 else 12 if (x-y)%5==0 else 0))
            else:
                if x>=32:base=tuple(v-13 for v in stone)
                if y>=32:base=tuple(v+12 for v in base)
                c=shade(base,rng.choice([-5,-2,0,0,2,4]))
                # Intentional bevels and interlocking panels, not a scaled 16px bitmap.
                if y%8==0 or x%8==0:c=shade(base,-20)
                elif y%8==1 or x%8==1:c=shade(rim,-23)
                elif (x+2*y)%31==0:c=shade(base,21)
                if x%16 in [3,12] and y%16 in [3,12]:c=rim
                if (x+y//3)%19==0 and y%8>2:c=shade(base,-25)
            pixels.append((*c,255))
    # Face marks are asymmetric on the broken custodian, paired on the other shells.
    for x in [4,7,12]:
        for y in [20,21]:pixels[y*64+x]=(*glow,255)
    return pixels

def armor(leggings=False):
    rng=random.Random('umbra-relief-'+str(leggings));p=[(0,0,0,0)]*4096
    def dot(x,y,c):
        if 0<=x<64 and 0<=y<64:p[y*64+x]=(*c,255)
    def box(x0,y0,x1,y1,c):
        for y in range(y0,y1+1):
            for x in range(x0,x1+1):dot(x,y,c)
    dark=(32,32,44);plate=(73,65,91);edge=(139,119,156);gold=(181,147,87);glow=(199,180,229)
    for y in range(64):
        for x in range(64):
            c=tuple(v+rng.choice([-3,0,0,2]) for v in plate)
            if y%8 in [0,7]:c=dark
            elif y%8==1:c=edge
            if x%8 in [0,7]:c=dark
            dot(x,y,c)
    # Native helmet front: upper brow, cheek protection and an open face.
    box(8,8,15,9,gold);box(9,10,14,10,edge)
    for y in range(11,16):
        for x in range(9,15):p[y*64+x]=(0,0,0,0)
    # Breastplate front, side seams and central inlaid sigil.
    box(20,20,27,29,dark);box(21,21,26,27,plate)
    for y in range(21,28):dot(23,y,gold);dot(24,y,gold)
    for x,y in [(23,22),(24,22),(22,23),(25,23),(23,24),(24,24)]:dot(x,y,glow)
    box(20,29,27,30,gold)
    # Native limbs, gauntlet bands and articulated knee plates.
    for x0 in [4,44]:
        box(x0,20,x0+3,21,edge);box(x0,26,x0+3,27,gold)
        for y in range(22,26):dot(x0,y,dark);dot(x0+3,y,dark)
        dot(x0+1,24,glow);dot(x0+2,24,gold)
    # Lower half: separate islands for the new shoulder plates, crest, bracers and greaves.
    for y0 in [32,44,54]:
        for x in range(0,64):dot(x,y0,gold)
    for y in range(34,43):
        for x in range(0,64):
            if (x+y)%13==0:dot(x,y,edge)
    for x,y in [(6,38),(12,48),(46,38),(52,49),(4,58)]:
        box(x,y,x+1,y+2,glow)
    if leggings:
        box(20,20,27,22,gold);box(4,24,7,25,edge)
    return p

def content(png,assets):
    for name in MOBS:png(assets/f'textures/entity/{name}.png',mob(name),64,64)
    png(assets/'textures/entity/equipment/humanoid/umbra_armor.png',armor(),64,64)
    png(assets/'textures/entity/equipment/humanoid_leggings/umbra_leggings_armor.png',armor(True),64,64)
