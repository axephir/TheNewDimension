"""Original 16x16 creature atlases and native data. No vanilla mob model is reused."""
import random

def atlas(name):
    rng=random.Random(name);guard=name.startswith('rift');bonded=name.endswith('bonded');charged=name.endswith('charged')
    base=(94,95,86) if not guard else (69,76,72)
    p=[]
    for y in range(16):
        for x in range(16):
            n=rng.choice([-8,-4,0,0,4,7]);c=tuple(v+n for v in base)
            if y in [0,3,4,7,8,15] or x%4==0:c=tuple(max(0,v-22) for v in c)
            if x%4==1 and y in [1,5,9,14]:c=(151,131,79)
            if y>=8 and x>=8:
                glow=(211,111,67) if charged else (104,195,160) if bonded else (165,142,190)
                c=glow if (x+y)%4 else tuple(min(255,v+36) for v in glow)
            p.append((*c,255))
    # Inset face marks on the upper body island, never high-resolution noise.
    for x,y in [(5,5),(6,5),(13,5),(14,5)]:p[y*16+x]=(*( (235,146,88) if charged else (110,178,148) ),255)
    return p

def content(write,png,assets,data):
    for name in ['shardling','shardling_bonded','rift_sentinel','rift_sentinel_charged']:
        png(assets/f'textures/entity/{name}.png',atlas(name))
    def face(texture):return {d:{'texture':texture} for d in ['north','east','south','west','up','down']}
    for age in range(4):
        elements=[{'from':[2,0,2],'to':[14,2,14],'faces':face('#base')}]
        for x,z,h,angle in [(5,6,5+age*2,-22.5),(9,10,3+age*2,22.5),(10,5,4+age*2,0)]:
            elements.append({'from':[x-1,2,z-1],'to':[x+2,h,z+2],'faces':face('#crystal'),
                             'rotation':{'origin':[x,2,z],'axis':'z','angle':angle,'rescale':False}})
        write(assets/f'models/block/echo_bloom_{age}.json',{'textures':{'base':'tnd:block/ashstone','crystal':'tnd:block/echo_bloom','particle':'tnd:block/echo_bloom'},'elements':elements})
    write(assets/'blockstates/echo_bloom.json',{'variants':{f'charge={i}':{'model':f'tnd:block/echo_bloom_{i}'} for i in range(4)}})
    write(assets/'models/block/echo_bloom.json',{'parent':'tnd:block/echo_bloom_3'})
    write(data/'worldgen/configured_feature/echo_bloom.json',{'type':'tnd:echo_bloom','config':{}})
    write(data/'worldgen/placed_feature/echo_bloom.json',{'feature':'tnd:echo_bloom','placement':[]})
    write(data/'loot_table/entities/shardling.json',{'type':'minecraft:entity','pools':[]})
    write(data/'loot_table/entities/rift_sentinel.json',{'type':'minecraft:entity','pools':[
        {'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:echo_dust','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2}}]}],
         'conditions':[{'condition':'minecraft:killed_by_player'}]},
        {'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:resonant_shard'}],
         'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance','chance':.25}]}]})

def languages(fr,en):
    fr.update({'entity.tnd.shardling':'Éclat errant','entity.tnd.rift_sentinel':'Sentinelle de faille',
        'item.tnd.shardling_spawn_egg':'Œuf d’Éclat errant','item.tnd.rift_sentinel_spawn_egg':'Œuf de sentinelle de faille',
        'block.tnd.echo_bloom':'Agrégat d’écho','item.tnd.echo_dust':'Poussière d’écho','item.tnd.echo_lens':'Prisme d’écho',
        'message.tnd.companion_no_landing':'%s familier(s) attend(ent) au départ : aucune arrivée dégagée à proximité.',
        'message.tnd.shardling_hint':'Ses fragments semblent répondre à un cristal composite.',
        'message.tnd.shardling_healthy':'Votre Éclat errant est en pleine forme.',
        'message.tnd.shardling_tamed':'Un lien se forme. Main vide : suivre / attendre. Éclat résonant : soigner.',
        'message.tnd.shardling_wait':'L’Éclat errant attend ici.','message.tnd.shardling_follow':'L’Éclat errant vous accompagne.',
        'message.tnd.bloom_day':'L’agrégat reste scellé durant le jour de Vestige.',
        'message.tnd.bloom_charging':'L’agrégat accumule encore son écho. Revenez après sa croissance nocturne.',
        'message.tnd.lens_empty':'Aucune créature hostile dans un rayon de 16 blocs.',
        'message.tnd.lens_fuel':'Une poussière d’écho est nécessaire dans votre inventaire.',
        'message.tnd.lens_revealed':'%s présence(s) révélée(s) pendant 10 secondes.',
        'tooltip.tnd.echo_dust':'Récoltée la nuit sur un agrégat chargé. Alimente le prisme d’écho.',
        'tooltip.tnd.echo_lens':'Révèle les créatures hostiles à 16 blocs pendant 10 s. Coût : 1 poussière d’écho ; recharge : 20 s.',
        'tooltip.tnd.echo_bloom':'Se charge uniquement la nuit dans Vestige. Clic droit une fois mûr pour récolter sans le détruire.',
        'message.tnd.lore.fusion':'« Trois couleurs ouvrent le passage ; une quatrième réveille les machines. » — Réparez les consoles avec un éclat résonant. Un cristal composite peut aussi lier un petit Éclat errant.'})
    en.update({'entity.tnd.shardling':'Shardling','entity.tnd.rift_sentinel':'Rift Sentinel',
        'item.tnd.shardling_spawn_egg':'Shardling Spawn Egg','item.tnd.rift_sentinel_spawn_egg':'Rift Sentinel Spawn Egg',
        'block.tnd.echo_bloom':'Echo Aggregate','item.tnd.echo_dust':'Echo Dust','item.tnd.echo_lens':'Echo Prism',
        'message.tnd.companion_no_landing':'%s companion(s) stayed at departure: no safe arrival nearby.',
        'message.tnd.shardling_hint':'Its fragments seem to respond to a composite crystal.',
        'message.tnd.shardling_healthy':'Your Shardling is at full health.',
        'message.tnd.shardling_tamed':'A bond forms. Empty hand: follow / wait. Resonant shard: heal.',
        'message.tnd.shardling_wait':'The Shardling waits here.','message.tnd.shardling_follow':'The Shardling follows you.',
        'message.tnd.bloom_day':'The aggregate stays sealed during Vestige’s day.',
        'message.tnd.bloom_charging':'The aggregate is still gathering echoes. Return after its nocturnal growth.',
        'message.tnd.lens_empty':'No hostile creature within 16 blocks.',
        'message.tnd.lens_fuel':'You need one echo dust in your inventory.',
        'message.tnd.lens_revealed':'%s presence(s) revealed for 10 seconds.',
        'tooltip.tnd.echo_dust':'Harvested at night from a charged aggregate. Fuels the echo prism.',
        'tooltip.tnd.echo_lens':'Reveals hostile creatures within 16 blocks for 10 s. Cost: 1 echo dust; cooldown: 20 s.',
        'tooltip.tnd.echo_bloom':'Charges only at night in Vestige. Right-click when mature to harvest without destroying it.',
        'message.tnd.lore.fusion':'“Three colours open the passage; a fourth awakens the machines.” — Repair consoles with resonant shards. A composite crystal can also bond a little Shardling.'})
