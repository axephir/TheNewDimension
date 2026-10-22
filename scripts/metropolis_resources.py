"""Whole-biome metropolis, transparent shield and one authored incubation laboratory per city."""
import gzip,json,math,random
from collections import defaultdict
from architecture import Building
from city_architecture import build
BLOCKS=['city_dome','city_dome_rib','incubation_computer','incubation_capsule','incubation_glass']
ITEMS=['incubator_nutrient','familiar_capsule']
TESTS=['symbiosis_'+n for n in ['trial','unique','injury','capsule','blocked','progression','skills','menu','computer','biome','city','shield','piston','dome']]

def save_template(grid,size,path,nbt_payload):
    palette=[];indices={};blocks=[]
    for (x,y,z),(block,props,loot) in sorted(grid.items(),key=lambda e:(e[0][1],e[0][2],e[0][0])):
        key=(block,props)
        if key not in indices:
            indices[key]=len(palette);state={'Name':(8,block)}
            if props:state['Properties']=(10,{k:(8,v) for k,v in props})
            palette.append(state)
        entry={'pos':(9,(3,[x,y,z])),'state':(3,indices[key])}
        if loot:entry['nbt']=(10,{'id':(8,'minecraft:chest'),'LootTable':(8,loot)})
        blocks.append(entry)
    root={'DataVersion':(3,4903),'size':(9,(3,list(size))),'palette':(9,(10,palette)),'blocks':(9,(10,blocks)),'entities':(9,(10,[]))}
    path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(gzip.compress(b'\x0a\x00\x00'+nbt_payload(10,root),mtime=0))

def laboratory():
    b=Building.__new__(Building);b.name='incubation_laboratory';b.size=(47,27,47);b.grid={};b.rooms=[];p=b.put;q=b.box
    q(0,0,0,46,26,46,'minecraft:air')
    q(0,0,0,46,0,46,'tnd:city_tiles')
    q(2,0,2,44,13,44,'tnd:reinforced_panel');q(3,1,3,43,12,43,'minecraft:air')
    q(5,13,5,41,16,41,'tnd:carbon_panel');q(6,14,6,40,15,40,'tnd:vent_panel')
    # Four generous doors open directly onto the city boulevards. Protected does not mean inaccessible.
    for x in (2,44):q(x,1,20,x,6,26,'minecraft:air');q(x,7,19,x,7,27,'tnd:neon_cyan')
    for z in (2,44):q(20,1,z,26,6,z,'minecraft:air');q(19,7,z,27,7,z,'tnd:neon_cyan')
    # Inset glazing, lit vertical buttresses, roof coils and a high central skylight.
    for t in range(6,42,6):
        for z in (2,44):q(t,3,z,t+3,8,z,'minecraft:cyan_stained_glass');q(t,9,z,t+3,9,z,'tnd:neon_cyan')
        for x in (2,44):q(x,3,t,x,8,t+3,'minecraft:cyan_stained_glass');q(x,9,t,x,9,t+3,'tnd:neon_cyan')
    for x in (8,38):
        for z in (8,38):
            q(x,0,z,x+1,15,z+1,'tnd:reinforced_panel');q(x,16,z,x+1,17,z+1,'tnd:relay_coil');p(x,18,z,'tnd:neon_amber')
    q(17,13,17,29,18,29,'tnd:reinforced_panel');q(18,14,18,28,18,28,'minecraft:cyan_stained_glass');q(19,13,19,27,13,27,'minecraft:air')
    # Capsule: a 3m chamber with a real front door and a computer six blocks in front.
    q(19,0,19,27,0,27,'tnd:carbon_panel');q(21,1,21,25,1,25,'tnd:incubation_capsule')
    for y in range(2,7):
        for x in range(21,26):
            for z in (21,25):p(x,y,z,'tnd:incubation_glass')
        for z in range(22,25):
            for x in (21,25):p(x,y,z,'tnd:incubation_glass')
    q(21,7,21,25,7,25,'tnd:reinforced_panel');q(22,7,22,24,7,24,'tnd:neon_cyan')
    # A dormant geometric heart is dissolved by the opening sequence; no hostile entity can escape before activation.
    p(23,3,23,'tnd:incubation_capsule');p(23,4,23,'tnd:relay_coil')
    p(23,2,17,'tnd:incubation_computer');p(23,1,17,'tnd:carbon_panel')
    # Research benches, analytical alcoves, server banks, a mezzanine and accessible stairs.
    for x in (8,13,32,37):
        for z in (10,32):
            q(x,1,z,x+2,1,z+3,'tnd:city_tiles');p(x+1,2,z+1,'tnd:circuit_panel');p(x+1,3,z+1,'tnd:relay_coil')
    for z in range(10,39,4):q(4,1,z,5,7,z+1,'tnd:vent_panel');p(5,5,z,'tnd:neon_amber')
    q(5,8,5,17,8,41,'tnd:city_tiles');q(6,9,6,16,11,40,'minecraft:air')
    for i in range(8):
        for x in (15,16):p(x,i,4+i,'tnd:cut_alloy_stairs',{'facing':'south','half':'bottom','shape':'straight','waterlogged':'false'})
    for z in range(16,40):p(17,9,z,'minecraft:glass_pane',{'north':'true','south':'true','east':'false','west':'false','waterlogged':'false'})
    # Hidden nourishment in a service recess: reachable by walking, never by breaking protected blocks.
    q(32,1,32,41,6,41,'tnd:carbon_panel');q(33,1,33,40,5,40,'minecraft:air')
    q(36,1,32,37,3,32,'minecraft:air')
    q(32,1,33,32,5,39,'tnd:vent_panel');p(36,4,39,'tnd:neon_amber');b.chest(36,38,'incubation_supplies',1)
    p(34,2,36,'tnd:vent_panel')
    for x in range(7,42,7):
        for z in range(7,42,7):p(x,12,z,'tnd:neon_cyan')
    p(10,9,36,'tnd:memory_terminal',{'entry':'6'})
    return b

def content(write,png,assets,data,nbt_payload):
    png(assets/"textures/block/gate_light.png",[(255,255,255,255)]*256)
    dim=json.loads((data/'dimension/vestige.json').read_text());outside=dim['generator']['biome_source']
    outside['biomes']=[e for e in outside['biomes'] if e['biome']!='tnd:neon_city']
    for e in outside['biomes']:e['parameters']['continentalness']=[-1,1]
    dim['generator']['biome_source']={'type':'tnd:metropolis','outside':outside,'city':'tnd:neon_city'};write(data/'dimension/vestige.json',dim)
    settings=json.loads((data/'worldgen/noise_settings/vestige.json').read_text());router=settings['noise_router']
    # Remove the old noise-city flattening layer, then use the exact city field as the biome source and shield.
    old=router['final_density']['argument1']['argument2'];blend={'type':'tnd:city_blend'}
    flat={'type':'minecraft:y_clamped_gradient','from_y':40,'to_y':138,'from_value':1.,'to_value':-1.}
    router['final_density']={'type':'minecraft:add','argument1':{'type':'minecraft:mul','argument1':{'type':'minecraft:add','argument1':1.,'argument2':{'type':'minecraft:mul','argument1':-1.,'argument2':blend}},'argument2':old},'argument2':{'type':'minecraft:mul','argument1':blend,'argument2':flat}}
    write(data/'worldgen/noise_settings/vestige.json',settings)
    # Biome features run per originating chunk, including border chunks; never one enormous structure reference.
    write(data/'worldgen/configured_feature/metropolis.json',{'type':'tnd:metropolis','config':{}})
    write(data/'worldgen/placed_feature/metropolis.json',{'feature':'tnd:metropolis','placement':[]})
    for path in (data/'worldgen/biome').glob('*.json'):
        if path.stem not in ['silent_expanse','fractured_fields','hollow_shelves','glass_woodland','ashen_basin','obsidian_dunes','neon_city']:continue
        b=json.loads(path.read_text());b['spawners']['creature']=[];b['features'][10].append('tnd:metropolis')
        if path.stem=='neon_city':
            b['attributes']['minecraft:visual/fog_start_distance']=110.;b['attributes']['minecraft:visual/fog_end_distance']=620.
        write(path,b)
    path=data/'worldgen/structure_set/vestige_sites.json';s=json.loads(path.read_text());s['structures']=[e for e in s['structures'] if e['structure']!='tnd:neon_district'];write(path,s)
    for prefix,floors,height in [('neon_midrise',8,79),('neon_lowrise',4,47)]:
        b=build(floors);tiles=defaultdict(dict)
        for (x,y,z),value in b.grid.items():tiles[x//32,z//32][x%32,y,z%32]=value
        for (x,z),grid in tiles.items():save_template(grid,(min(32,127-x*32),height,min(32,127-z*32)),data/f'structure/{prefix}/part_{x}_{z}.nbt',nbt_payload)
    b=laboratory();save_template(b.grid,b.size,data/'structure/incubation_laboratory.nbt',nbt_payload)
    for name in BLOCKS:
        pixels=[]
        for y in range(16):
            for x in range(16):
                if name in ['city_dome','city_dome_rib','incubation_glass']:
                    edge=x in (0,15) or y in (0,15);pixels.append((123,212,227,(50 if edge else 7) if name=='city_dome' else (120 if edge else 14) if name=='city_dome_rib' else (115 if edge else 24)))
                else:
                    c=(43,58,67,255)
                    if x in (0,15) or y in (0,15):c=(125,143,150,255)
                    if 3<x<12 and 3<y<12:c=(33,115,116,255) if y%4 else (141,237,205,255)
                    pixels.append(c)
        png(assets/f'textures/block/{name}.png',pixels)
        write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':'tnd:block/'+name}}})
        model={'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/'+name}}
        if name in ['city_dome','city_dome_rib','incubation_glass']:model['render_type']='minecraft:translucent'
        if name=='incubation_computer':
            base=json.loads((assets/'models/block/memory_terminal.json').read_text());base['textures']['screen']='tnd:block/'+name;model=base
        write(assets/f'models/block/{name}.json',model)
        write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
        (data/f'loot_table/blocks/{name}.json').unlink(missing_ok=True)
    for name in ITEMS:
        pixels=[]
        for y in range(16):
            for x in range(16):
                c=(0,0,0,0)
                if name=='incubator_nutrient' and 4<=x<=11 and 2<=y<=13:c=(69,90,94,255) if y in (2,3,12,13) or x in (4,11) else (101,210,151,255) if (x+y)%4 else (184,241,171,255)
                if name=='familiar_capsule' and ((x-7.5)/5)**2+((y-8)/6)**2<1:c=(114,137,147,255) if x<5 or x>10 or y<4 or y>12 else (85,205,211,255) if y%4 else (205,231,226,255)
                pixels.append(c)
        png(assets/f'textures/item/{name}.png',pixels)
    write(data/'recipe/incubator_nutrient.json',{'type':'minecraft:crafting_shaped','pattern':[' H ','SAS',' G '],'key':{'H':'minecraft:honey_bottle','S':'tnd:resonant_shard','A':'minecraft:golden_apple','G':'minecraft:glow_berries'},'result':{'id':'tnd:incubator_nutrient','count':3}})
    write(data/'loot_table/chests/incubation_supplies.json',{'type':'minecraft:chest','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:incubator_nutrient','functions':[{'function':'minecraft:set_count','count':6}]}]}]})
    write(data/'advancement/recipes/incubator_nutrient.json',{'criteria':{'food':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:incubator_nutrient'}]}}},'requirements':[['food']],'rewards':{'recipes':['tnd:incubator_nutrient']}})
    for test in TESTS:write(data/f'test_instance/{test}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+test,'structure':'tnd:test_habitat','max_ticks':500,'required':True})
    for tag in ['wither_immune','dragon_immune']:
        path=data.parent/f'minecraft/tags/block/{tag}.json';s=json.loads(path.read_text());s['values']+=['tnd:'+n for n in BLOCKS];write(path,s)
    write(data/'loot_table/entities/shardling.json',{'type':'minecraft:entity','pools':[]})

def languages(fr,en):
    for n,a,b in [('city_dome','Champ de Nacre','Nacre Force Field'),('city_dome_rib','Nervure du dôme','Dome Rib'),('incubation_computer',"Ordinateur d'incubation",'Incubation Computer'),('incubation_capsule',"Capsule d'incubation",'Incubation Capsule'),('incubation_glass',"Paroi d'incubation",'Incubation Wall')]:fr['block.tnd.'+n]=a;en['block.tnd.'+n]=b
    for n,a,b in [('incubator_nutrient','Nutriment de symbiose','Symbiosis Nutrient'),('familiar_capsule','Capsule de familier','Familiar Capsule')]:fr['item.tnd.'+n]=a;en['item.tnd.'+n]=b
    fr['biome.tnd.neon_city']='Métropole de Nacre-9';en['biome.tnd.neon_city']='Nacre-9 Metropolis'
    fr['city.tnd.protected']='Le dôme protège cette ville. Les portes, coffres et terminaux restent utilisables.';en['city.tnd.protected']='The dome protects this city. Doors, chests and terminals remain usable.'
    pairs={
      'title':('Familier','Familiar'),'tab.0':('Compagnon','Companion'),'tab.1':('Évolution','Evolution'),'tab.2':('Soins et guide','Care and guide'),
      'rename':('Renommer','Rename'),'follow':('Suivre','Follow'),'wait':('Attendre','Wait'),'pack':('Mettre en capsule','Store in capsule'),'heal':('Soigner','Heal'),'recover_capsule':('Récupérer la capsule','Recover capsule'),
      'capsule_name':('Capsule : %s','Capsule: %s'),'inventory_full':('Libérez une case dans votre inventaire pour transporter votre familier.','Free an inventory slot to carry your familiar.'),
      'deploy_blocked':('Posez votre capsule sur un sol solide, dégagé et sec. Seul son propriétaire peut la déployer.','Place your capsule on solid, clear, dry ground. Only its owner can deploy it.'),
      'one_only':('Un seul lien par joueur. Votre familier actuel garde ses niveaux et son identité.','One bond per player. Your existing familiar keeps its levels and identity.'),
      'defeat_first':('Le sujet doit être vaincu avant de pouvoir accepter un nutriment de symbiose.','The subject must be defeated before it can accept a symbiosis nutrient.'),
      'other_trial':('Ce sujet reconnaît la personne qui l’a vaincu.','This subject recognises the person who defeated it.'),
      'bonded':('Lien de symbiose établi. Votre compagnon grandira à vos côtés.','Symbiotic bond established. Your companion will grow alongside you.'),
      'revived':('Votre familier est soigné et peut vous suivre à nouveau.','Your familiar is healed and can follow you again.'),'healthy':('Aucun soin nécessaire. Le nutriment est conservé.','No healing needed. The nutrient is preserved.'),
      'foreign':('Ce familier est lié à un autre joueur.','This familiar belongs to another player.'),
      'trial_hint':('Sujet d’incubation : battez-le, puis soignez-le avec le nutriment caché dans le laboratoire.','Incubation subject: defeat it, then heal it with the nutrient hidden in the laboratory.'),
      'released':('Cette capsule a déjà libéré son unique sujet. Cherchez une autre métropole pour un autre joueur.','This capsule has already released its only subject. Another player must find another metropolis.'),
      'opening':('Décompression de la capsule en cours…','Capsule decompression in progress…'),
      'missing_pod':('La chambre d’incubation est incomplète. Cet ordinateur ne contient aucun sujet.','The incubation chamber is incomplete. This computer has no subject.'),
      'warning':('PROTOCOLE IRIS : ouverture dans 4 secondes. Le sujet se défendra. Cherchez ensuite le nutriment dans l’annexe de maintenance.','IRIS PROTOCOL: opening in 4 seconds. The subject will defend itself. Then search the maintenance annex for nourishment.'),
      'defend':('Sujet libéré. Battez-le sans crainte : il restera blessé au sol.','Subject released. Defeat it: it will remain injured on the ground.'),
      'evolved':('%s évolue au niveau %s ! Une nouvelle forme et de meilleures statistiques.','%s evolves at level %s! A new form and improved stats.'),
      'level_up':('%s atteint le niveau %s.','%s reaches level %s.'),
      'pack_tip':('À moins de 6 blocs. Shift + clic droit fonctionne aussi, même blessé.','Within 6 blocks. Shift + right-click also works, including when injured.'),
      'heal_tip':('Un nutriment de symbiose soigne complètement. En inventaire : %s.','One symbiosis nutrient fully heals. In inventory: %s.'),
      'stage.0':('Éclaireur · I','Scout · I'),'stage.1':('Gardien · II','Guardian · II'),'stage.2':('Ascendant · III','Ascendant · III'),
      'no_bond':('Un seul compagnon, une histoire commune','One companion, a shared story'),
      'guide.0':('Dans Vestige, repérez une immense ville sous son dôme. Un seul laboratoire IRIS occupe sa place centrale. Activez l’ordinateur devant la capsule. Le sujet vous attaquera : battez-le pour le mettre au sol, puis cherchez six nutriments cachés dans une annexe accessible du laboratoire. Donnez-lui un nutriment pour le soigner et créer votre lien.','In Vestige, find a vast city beneath its dome. A single IRIS laboratory occupies its central plaza. Activate the computer in front of the capsule. The subject will attack: defeat it, then find six nutrients hidden in an accessible laboratory annex. Feed it one nutrient to heal it and establish your bond.'),
      'city_hint':('Centre de métropole proche dans Vestige : X %s · Z %s','Nearby metropolis centre in Vestige: X %s · Z %s'),
      'level':('Niveau %s / 30','Level %s / 30'),
      'state.0':('Hors de portée · dernière position connue','Out of range · last known location'),'state.1':('Vous accompagne','Following you'),'state.2':('Attend sur place','Waiting here'),'state.3':('Blessé · nutriment requis','Injured · nutrient required'),'state.4':('En capsule · niveaux conservés','Stored · levels preserved'),'state.5':('Synchronisation du lien…','Bond synchronising…'),
      'health':('Vie : %s / %s','Health: %s / %s'),'xp':('Expérience : %s / %s','Experience: %s / %s'),'max_level':('Niveau maximal atteint','Maximum level reached'),
      'damage':('Attaque %s · Armure %s','Attack %s · Armour %s'),'position':('X %s · Z %s · %s','X %s · Z %s · %s'),
      'dimension.0':('Overworld','Overworld'),'dimension.1':('Vestige','Vestige'),'dimension.2':('Autre dimension','Other dimension'),
      'evolution_header':('Croissance et aptitudes · niveau %s','Growth and abilities · level %s'),
      'evolution_path':('I : niveau 1   →   II : niveau 8   →   III : niveau 20','I: level 1   →   II: level 8   →   III: level 20'),
      'skill.3':('Niv. 3 · Prospection','Lv. 3 · Survey'),'skill_desc.3':('Signale les minerais résonants proches, sans les miner.','Reveals nearby resonant ore without mining it.'),
      'skill.8':('Niv. 8 · Égide de symbiose','Lv. 8 · Symbiotic aegis'),'skill_desc.8':('Protège le propriétaire blessé, une fois par 30 s.','Shields the injured owner, once every 30 s.'),
      'skill.12':('Niv. 12 · Onde de résonance','Lv. 12 · Resonance pulse'),'skill_desc.12':('Onde annoncée contre les monstres, toutes les 8 s.','Telegraphed pulse against monsters, every 8 s.'),
      'skill.20':('Niv. 20 · Reconstruction','Lv. 20 · Reconstruction'),'skill_desc.20':('Régénère sa vie ; un familier au sol doit être soigné.','Regenerates health; a downed familiar needs food.'),
      'care':('Un compagnon ne disparaît pas à zéro point de vie','Your companion survives at zero health'),
      'guide.1':('Blessé, votre familier attend un nutriment pour se relever. Il gagne de l’XP sur les monstres qu’il tue et sur vos victoires à moins de 24 blocs lorsqu’il vous suit. Shift + clic droit le range en capsule, avec son nom, ses niveaux et ses blessures. Posez sa capsule sur un sol dégagé pour le libérer. Un joueur ne peut avoir qu’un seul lien.','An injured familiar needs a nutrient to get back up. It gains XP from monsters it kills and your victories within 24 blocks while following you. Shift + right-click stores it in a capsule, keeping its name, levels and injuries. Place the capsule on clear ground to release it. Each player can have only one bond.'),
      'food_recipe':('Miel + pomme dorée + 2 éclats + baies : 3 soins','Honey + golden apple + 2 shards + berries: 3 heals'),
      'footer':('Clic droit main vide : dossier · Shift + clic droit : capsule','Empty-hand right-click: dossier · Shift + right-click: capsule'),
      'scene.0':('Le sujet reconnaît votre résonance…','The subject recognises your resonance…'),'scene.1':('Le lien est établi. %s vous accompagne.','The bond is established. %s joins you.'),
    }
    for key,(a,b) in pairs.items():fr['familiar.tnd.'+key]=a;en['familiar.tnd.'+key]=b
    # Old guide entries are still reachable in saved murals and must describe the new survival rules.
    fr['message.tnd.lore.fusion']='« Trois couleurs ouvrent le passage ; une quatrième réveille les machines. » Réparez les consoles avec un éclat résonant. Un unique familier vous attend dans le laboratoire central d’une métropole de Nacre.'
    en['message.tnd.lore.fusion']='“Three colours open the passage; a fourth awakens the machines.” Repair consoles with resonant shards. A unique familiar awaits in a Nacre metropolis central laboratory.'


    for n,a,b in [('incubator_start','Capsule en décompression','Capsule decompresses'),('incubator_release','Capsule ouverte','Capsule opens'),('familiar_bond','Un lien se forme','A bond forms'),('familiar_evolve','Le familier évolue','Familiar evolves'),('familiar_down','Le familier est blessé','Familiar injured'),('familiar_pulse','Onde du familier','Familiar pulse')]:fr['subtitles.tnd.'+n]=a;en['subtitles.tnd.'+n]=b
