"""Nacre-9: gate installation, original city palette, climate region, salvage and personal archives."""
import gzip,json,math,random
from city_architecture import build,SIZE,TOWERS
BLOCKS=['city_asphalt','carbon_panel','city_tiles','reinforced_panel','circuit_panel','vent_panel','neon_cyan','neon_magenta','neon_amber','relay_coil','nacre_memorial','portal_monitor','memory_terminal']
MOBS=['neon_enforcer','suture_drone','signal_reaver']
ITEMS=['security_processor','ion_filament']+[s+'_spawn_egg' for s in MOBS]
SOUNDS=['gate_charge','gate_open','gate_transit','gate_hum','city_drone','city_alarm','city_footstep','city_laser','city_dash','city_impact','archive_read','nacre_ambience']
TESTS=['gate_monitor_guards','gate_monitor_sequence','gate_monitor_cancel','gate_monitor_storage','city_attacks','city_drone_wall','city_reaver_interrupt','city_archives','city_template','city_climate','city_ecology','city_tiles']

def art(name):
    rng=random.Random('nacre-'+name);out=[]
    glow=(89,201,201) if name not in ['neon_magenta','gate_membrane'] else (179,103,189)
    if name in ['neon_amber','nacre_memorial']:glow=(224,166,78)
    for y in range(16):
        for x in range(16):
            n=rng.randrange(-5,6);c=tuple(v+n for v in (42,49,57))
            if name=='city_asphalt':c=tuple(v+n for v in (35,37,42))
            elif name=='city_tiles':
                c=tuple(v+n for v in (79,89,93)) if x%8 and y%8 else (36,44,50)
                if x%8==1 or y%8==1:c=(98+n,107+n,109+n)
            elif name=='carbon_panel':
                if x in (0,15) or y in (0,15):c=(75+n,86+n,91+n)
                if x in (2,13) and y in (2,13):c=(151,144,109)
            elif name=='reinforced_panel':
                if x<3 or x>12:c=(96+n,108+n,112+n)
                if y in (0,7,8,15):c=(24,31,38)
                if x in (1,14) and y in (2,12):c=(160,149,108)
            elif name=='vent_panel':
                if 2<x<13 and 2<y<13:c=(15,24,31) if y%3 else (115+n,128+n,130+n)
            elif name=='circuit_panel':
                if (x in (3,10) and y in range(3,14)) or (y in (5,12) and 3<=x<=12):c=(68,151,144)
                if (x,y) in [(3,3),(10,12),(12,5)]:c=(190,184,132)
            elif name.startswith('neon_'):
                if 3<=x<=12 and 4<=y<=11:c=tuple(min(255,v+(30 if y==5 else 0)) for v in glow)
                if y in (3,12) and 2<x<13:c=tuple(v//2 for v in glow)
            elif name=='relay_coil':
                if 2<x<13 and y in (3,6,9,12):c=(174+n,116+n,62+n)
                if x in (3,12) and 3<=y<=12:c=(110,168,167)
                if 6<=x<=9 and 5<=y<=10:c=(98,221,213)
            elif name in ['portal_monitor','memory_terminal']:
                if 2<=x<=13 and 2<=y<=10:c=(12,29,34)
                if 4<=x<=11 and y in (4,7):c=(64,172,158) if x%4 else (179,196,135)
                if y==13 and 3<x<12:c=(155,114,70) if x%3 else (78,185,177)
                if x in (1,14) or y in (0,15):c=(106+n,119+n,123+n)
            elif name=='nacre_memorial':
                if abs(x-8)+abs(y-8)<7:c=glow if (x+y)%3 else (109,85,60)
            elif name=='portal_frame':
                if x in (0,1,14,15):c=(124+n,140+n,144+n)
                if x in (5,10) and y%8 in (2,3,4,5):c=(89,180,178)
                if y%8==0:c=(22,30,37)
            elif name=='portal_controller':
                if abs(x-8)+abs(y-8)<6:c=(75,181,181) if x%3 else (176,212,206)
                if x in (0,15) or y in (0,15):c=(112,127,132)
            elif name=='gate_membrane':
                phase=(x+y//2)%8;c=(52+phase*6,106+phase*8,133+phase*7)
                if (x*3+y*5)%17<2:c=(170,223,226)
            elif name in ITEMS:
                c=(0,0,0,0)
                if name=='security_processor' and 3<=x<=12 and 3<=y<=12:c=(83,103,111) if x in (3,12) or y in (3,12) else (64,158,146) if (x+y)%4 else (204,174,105)
                if name=='ion_filament' and 3<=y<=13 and abs(x-8-(y%4-2))<=1:c=(142,217,219) if x%2 else (222,224,192)
                if name.endswith('_spawn_egg') and ((x-8)/5)**2+((y-9)/6)**2<1:c=(45,56,68) if (x+y)%5 else (110,204,193) if name.startswith('suture') else (188,110,179)
            out.append(c if len(c)==4 else (*c,255))
    return out

def character(name,charged):
    rng=random.Random(name);out=[];accent={'neon_enforcer':(221,156,70),'suture_drone':(73,210,197),'signal_reaver':(184,101,206)}[name]
    for y in range(64):
        for x in range(64):
            n=rng.randrange(-7,8);tile=x//16
            if y<16:
                base=[(60,72,83),(79,90,100),(40,54,63),accent][tile]
                c=tuple(max(0,min(255,v+n+(24 if charged and tile==3 else 0))) for v in base)
                if (x%16 in (0,15) or y%8==0) and tile<3:c=(24,33,44)
                if x%8==2 and y%8==2 and tile<3:c=(157,146,107)
            elif y<32:
                c=(32+n,44+n,56+n) if tile<3 else accent
                if y in (19,20,23):c=accent if charged or tile<2 else (107,116,120)
                if y==27:c=(153,149,122)
            elif y<48:
                c=(86+n,100+n,109+n) if tile<2 else (43+n,53+n,66+n)
                if y%8 in (0,1):c=(27,37,47)
                if x%8==0:c=tuple(v//2 for v in accent)
            else:c=(38+n,48+n,61+n) if (x+y)%5 else accent
            out.append((*c,255))
    return out

def content(write,png,assets,data,nbt_payload):
    for name in BLOCKS+ITEMS+['portal_frame','portal_controller','gate_membrane']:png(assets/f'textures/{"item" if name in ITEMS else "block"}/{name}.png',art(name))
    for name in MOBS:
        for charged in (False,True):png(assets/f'textures/entity/{name}{"_charged" if charged else ""}.png',character(name,charged),64,64)
    def box(a,b,t):return {'from':a,'to':b,'faces':{d:{'texture':t} for d in ['north','south','east','west','up','down']}}
    for name in ['portal_monitor','memory_terminal']:
        elements=[box([1,0,1],[15,3,15],'#frame'),box([3,3,4],[13,8,13],'#frame'),box([1,7,3],[15,16,12],'#screen'),box([2,3,1],[14,5,7],'#screen')]
        if name=='portal_monitor':
            write(assets/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':'tnd:block/'+name,'y':angle} for f,angle in [('north',0),('east',90),('south',180),('west',270)] for lit in [False,True]}})
        else:write(assets/f'blockstates/{name}.json',{'variants':{f'entry={i}':{'model':'tnd:block/'+name} for i in range(8)}})
        write(assets/f'models/block/{name}.json',{'textures':{'frame':'tnd:block/reinforced_panel','screen':'tnd:block/'+name,'particle':'tnd:block/'+name},'elements':elements})
    write(assets/'blockstates/portal_controller.json',{'variants':{f'active={str(a).lower()},charging={str(c).lower()},online={str(o).lower()},facing={f}':{'model':'tnd:block/portal_controller'} for a in [False,True] for c in [False,True] for o in [False,True] for f in ['north','south','east','west']}})
    write(assets/'models/block/portal_controller.json',{'textures':{'frame':'tnd:block/portal_frame','screen':'tnd:block/portal_controller','particle':'tnd:block/portal_controller'},'elements':[box([0,0,0],[16,4,16],'#frame'),box([2,4,2],[14,14,14],'#screen'),box([0,14,0],[16,16,16],'#frame')]})
    for name in ['wither_immune','dragon_immune']:
        path=data.parent/f'minecraft/tags/block/{name}.json';tag=json.loads(path.read_text());tag['values'].append('tnd:memory_terminal');write(path,tag)
    # Natural archive terminals are intentionally unbreakable and have no duplicated item drop.
    (data/'loot_table/blocks/memory_terminal.json').unlink(missing_ok=True)
    recipes={
      'portal_monitor':(['FCF','OQO','FRF'],{'F':'tnd:industrial_frame','C':'tnd:control_circuit','O':'tnd:copper_coil','Q':'minecraft:quartz_block','R':'minecraft:redstone_block'},1),
      'relay_coil':(['OCO','IQI','OCO'],{'O':'minecraft:copper_ingot','C':'tnd:copper_coil','I':'minecraft:iron_ingot','Q':'minecraft:quartz'},2),
    }
    for name,(pattern,key,count) in recipes.items():write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'tnd:'+name,'count':count}})
    for name in BLOCKS[:9]:
        if name.startswith('neon_'):
            dye={'neon_cyan':'cyan','neon_magenta':'magenta','neon_amber':'orange'}[name]
            write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:carbon_panel','minecraft:glowstone_dust','minecraft:'+dye+'_dye'],'result':{'id':'tnd:'+name,'count':4}})
        else:write(data/f'recipe/{name}.json',{'type':'minecraft:stonecutting','ingredient':'tnd:tempered_alloy' if name not in ['city_asphalt','city_tiles'] else 'tnd:ashstone','result':{'id':'tnd:'+name,'count':8 if name not in ['city_asphalt','city_tiles'] else 2}})
    write(data/'recipe/processor_salvage.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:security_processor'],'result':{'id':'tnd:control_circuit','count':2}})
    write(data/'recipe/ion_lighting.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:ion_filament','minecraft:glass','minecraft:cyan_dye'],'result':{'id':'tnd:neon_cyan','count':16}})
    write(data/'recipe/ion_relay.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:ion_filament','tnd:copper_coil','minecraft:quartz'],'result':{'id':'tnd:relay_coil','count':2}})
    # Roster is dangerous in daylight too, independent of neon block light.
    biome=json.loads((data/'worldgen/biome/silent_expanse.json').read_text())
    biome['features']=[[] for _ in range(11)];biome['features'][6]=['tnd:resonant_ore'];biome['spawners']={k:[] for k in biome['spawners']}
    biome['spawners']['monster']=[{'type':'tnd:'+m,'weight':w,'minCount':1,'maxCount':2 if m=='neon_enforcer' else 1} for m,w in zip(MOBS,[18,12,10])]
    biome['spawn_costs']={};biome['attributes']={'minecraft:visual/fog_color':'#45465f','minecraft:visual/fog_start_distance':64.0,'minecraft:visual/fog_end_distance':320.0,'minecraft:visual/sky_color':'#343748','minecraft:visual/ambient_particles':[{'particle':{'type':'minecraft:white_ash'},'probability':.008}],'minecraft:audio/ambient_sounds':{'loop':'tnd:nacre_ambience','mood':{'sound':'tnd:low_signal','tick_delay':6000,'block_search_extent':8,'offset':2.0}}}
    write(data/'worldgen/biome/neon_city.json',biome)
    dim=json.loads((data/'dimension/vestige.json').read_text());entries=dim['generator']['biome_source']['biomes']
    for e in entries:e['parameters']['continentalness']=[-1,.22]
    params=dict(entries[0]['parameters']);params.update(temperature=[-1,1],humidity=[-1,1],continentalness=[.22,1])
    entries.append({'biome':'tnd:neon_city','parameters':params});write(data/'dimension/vestige.json',dim)
    write(data/'worldgen/noise/city_field.json',{'firstOctave':-10,'amplitudes':[1,.35]})
    settings=json.loads((data/'worldgen/noise_settings/vestige.json').read_text());router=settings['noise_router'];field={'type':'minecraft:noise','noise':'tnd:city_field','xz_scale':1.,'y_scale':0.}
    router['continents']=field
    # Smoothly lower city regions before any structure is considered. 48-block massif + 107-block roof fits height 256.
    blend={'type':'minecraft:clamp','input':{'type':'minecraft:mul','argument1':8.,'argument2':{'type':'minecraft:add','argument1':field,'argument2':-.10}},'min':0.,'max':1.}
    flat={'type':'minecraft:add','argument1':{'type':'minecraft:y_clamped_gradient','from_y':8,'to_y':104,'from_value':1.,'to_value':-1.},'argument2':{'type':'minecraft:mul','argument1':.035,'argument2':{'type':'minecraft:noise','noise':'tnd:fracture','xz_scale':.7,'y_scale':0.}}}
    router['final_density']={'type':'minecraft:add','argument1':{'type':'minecraft:mul','argument1':{'type':'minecraft:add','argument1':1.,'argument2':{'type':'minecraft:mul','argument1':-1.,'argument2':blend}},'argument2':router['final_density']},'argument2':{'type':'minecraft:mul','argument1':blend,'argument2':flat}}
    write(data/'worldgen/noise_settings/vestige.json',settings)
    tag=json.loads((data/'tags/worldgen/biome/is_vestige.json').read_text());old=tag['values'][:];tag['values'].append('tnd:neon_city');write(data/'tags/worldgen/biome/is_vestige.json',tag)
    write(data/'tags/worldgen/biome/ancient_vestige.json',{'values':old})
    for name in ['relay','archive','foundry','resonance_vault','observatory','aureate_citadel','echo_conservatory']:
        path=data/f'worldgen/structure/{name}.json';s=json.loads(path.read_text());s['biomes']='#tnd:ancient_vestige';write(path,s)
    write(data/'tags/block/city_surfaces.json',{'values':['tnd:'+n for n in BLOCKS[:9]]+['tnd:ashstone','tnd:nightslag','tnd:cut_alloy','tnd:industrial_frame']})
    # Building and black spire mountain persist as one native crowned structure piece.
    write(data/'worldgen/structure/neon_district.json',{'type':'tnd:crowned_site','template':'tnd:neon_district','biomes':'tnd:neon_city','step':'surface_structures','terrain_adaptation':'none','spawn_overrides':{'monster':{'bounding_box':'piece','spawns':biome['spawners']['monster']}}})
    # One shared grid is essential: exclusion zones also reject biome-invalid candidates,
    # which would silently suppress the ancient ruins throughout non-city regions.
    (data/'worldgen/structure_set/neon_district.json').unlink(missing_ok=True)
    path=data/'worldgen/structure_set/vestige_sites.json';s=json.loads(path.read_text())
    s['placement'].update(spacing=24,separation=17);s['placement'].pop('exclusion_zone',None)
    s['structures'].append({'structure':'tnd:neon_district','weight':3});write(path,s)
    building=build()
    def save_template(grid,size,path):
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
    save_template(building.grid,SIZE,data/'structure/neon_district.nbt')
    from collections import defaultdict
    tiles=defaultdict(dict)
    for (x,y,z),value in building.grid.items():tiles[x//32,z//32][x%32,y,z%32]=value
    for (x,z),grid in tiles.items():save_template(grid,(min(32,SIZE[0]-x*32),SIZE[1],min(32,SIZE[2]-z*32)),data/f'structure/neon_district/part_{x}_{z}.nbt')
    write(data/'worldgen/template_pool/neon_district.json',{'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':'tnd:neon_district','processors':'minecraft:empty','projection':'rigid'}}]})
    for name in ['city_salvage','city_rooftop']:
        items=['tnd:security_processor','tnd:ion_filament','tnd:control_circuit','minecraft:bread','minecraft:gold_ingot','tnd:raw_auralite' if name.endswith('rooftop') else 'minecraft:copper_ingot']
        write(data/f'loot_table/chests/{name}.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3,'max':6},'entries':[{'type':'minecraft:item','name':i,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]} for i in items]}]})
    for m in MOBS:write(data/f'loot_table/entities/{m}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:'+('ion_filament' if m=='suture_drone' else 'security_processor'),'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2}},{'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]}]})
    write(data/'advancement/recipes/nacre_knowledge.json',{'criteria':{'circuit':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:control_circuit'}]}}},'requirements':[['circuit']],'rewards':{'recipes':['tnd:'+n for n in list(recipes)+BLOCKS[:9]+['processor_salvage','ion_lighting','ion_relay']]}})
    for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+name,'structure':'tnd:test_habitat','max_ticks':750 if name=='city_template' else 400,'required':True})
    print('Nacre-9: eight furnished towers,',len(building.grid),'authored blocks, size',SIZE)


def languages(fr,en):
    names=[('Asphalte de Nacre','Nacre Asphalt'),('Panneau de carbone','Carbon Panel'),('Dallage de Nacre','Nacre Tiles'),('Panneau renforcé','Reinforced Panel'),('Panneau de circuits','Circuit Panel'),('Panneau de ventilation','Vent Panel'),('Néon cyan','Cyan Neon'),('Néon magenta','Magenta Neon'),('Néon ambré','Amber Neon'),('Bobine de relais','Relay Coil'),('Mémorial de Nacre','Nacre Memorial'),('Moniteur principal','Main Monitor'),('Terminal mémoriel','Memory Terminal'),('Processeur de sécurité','Security Processor'),('Filament ionique','Ion Filament')]
    for n,(a,b) in zip(BLOCKS+ITEMS[:2],names):fr[('block' if n in BLOCKS else 'item')+'.tnd.'+n]=a;en[('block' if n in BLOCKS else 'item')+'.tnd.'+n]=b
    for n,a,b in zip(MOBS,['Veilleur de néon','Drone Suture','Faucheur de signal'],['Neon Enforcer','Suture Drone','Signal Reaver']):
        fr['entity.tnd.'+n]=a;en['entity.tnd.'+n]=b;fr['item.tnd.'+n+'_spawn_egg']='Œuf de '+a;en['item.tnd.'+n+'_spawn_egg']=b+' Spawn Egg'
    pairs={
      'biome.tnd.neon_city':('Districts de Nacre-9','Nacre-9 Districts'),
      'gui.tnd.archives':('Archives de Nacre — découvertes et récits','Nacre Archives — discoveries and stories'),
      'message.tnd.monitor_required':('Construisez un moniteur principal à moins de 8 blocs. Guide : inventaire → ? → Portail.','Build a main monitor within 8 blocks. Guide: inventory → ? → Portal.'),
      'gate.tnd.guide':('Portail','Portal'),'gate.tnd.start':('Ouvrir le seuil','Open the gate'),'gate.tnd.cancel':('Annuler la charge','Cancel charging'),'gate.tnd.online':('Seuil stabilisé','Gate stabilized'),
      'gate.tnd.assembly':('Charpentes : %s/8  ·  Relais : %s/4','Frames: %s/8  ·  Relays: %s/4'),'gate.tnd.crystal':('Cristal','Crystal'),
      'gate.tnd.guide.diagram':('Vue de dessus : le moniteur au centre ; quatre bobines aux coins. Les HUIT charpentes forment la bordure 3×3 juste EN DESSOUS.','Top view: monitor in the centre; four coils at the corners. EIGHT frames form the 3×3 border one block BELOW.'),
      'gate.tnd.guide.tab.0':('Construction','Assembly'),'gate.tnd.guide.tab.1':('Énergie','Power'),'gate.tnd.guide.tab.2':('Voyage','Travel'),
      'gate.tnd.guide.text.0':('Placez le moniteur à 8 blocs maximum du contrôleur ancien (écart vertical ≤ 3). Fabrication : charpentes, circuits, cuivre et quartz du Nether. Aucun matériau de Vestige requis.','Place the monitor within 8 blocks of the ancient controller (vertical gap ≤ 3). Craft with frames, circuits, copper and Nether quartz. No Vestige material required.'),
      'gate.tnd.guide.text.1':('Reliez une dynamo ou un accumulateur au moniteur avec des câbles énergétiques. Il peut stocker 32 000 FE. Une première ouverture coûte 16 000 FE et UN cristal composite (éclats bleu + rouge + vert). Placez le cristal dans le moniteur ; un entonnoir convient aussi. Le bouton de charge devient disponible lorsque toutes les conditions sont remplies. Les ressources ne sont consommées que si la liaison aboutit. Annuler, appliquer un signal redstone ou démonter une bobine interrompt la séquence sans perdre le cristal ni les FE.','Connect a dynamo or accumulator through energy cables. The monitor stores 32,000 FE. First activation costs 16,000 FE and ONE composite crystal (blue + red + green shards). Put the crystal in the monitor, or feed it with a hopper. Start becomes available once all conditions are met. Resources are consumed only if linking succeeds. Cancel, apply a redstone signal or remove a coil to interrupt the sequence without spending crystal or FE.'),
      'gate.tnd.guide.text.2':('Lancez la charge : le seuil se déploie pendant 6 secondes. Cliquez ensuite sur son contrôleur pour traverser. Gardez le moniteur assemblé à côté du portail de l’Overworld pour repartir. Les voyages suivants ne coûtent rien. Le retour depuis Vestige reste toujours accessible, même si le moniteur est détruit. Un site d’arrivée encombré ne détruit aucun bloc : libérez l’espace et réessayez. Les anciennes liaisons sont conservées. Cliquez sur Archives pour lire le premier fragment du récit ; cherchez les sept autres terminaux dans les tours de Nacre-9.','Start charging: the gate unfolds over 6 seconds. Then interact with its controller to travel. Keep the Overworld monitor assembled to depart again; later trips are free. Return from Vestige always remains available even if the monitor is destroyed. An obstructed arrival never deletes blocks: clear room and try again. Existing links are preserved. Open Archives for the first story fragment, then seek seven memory terminals in the towers of Nacre-9.'),
      'codex.tnd.name':('Archives de Nacre','Nacre Archives'),'codex.tnd.header':('Archives · %s/8','Archives · %s/8'),'codex.tnd.claim':('Recevoir le mémorial','Claim the memorial'),'codex.tnd.locked':('Signal manquant','Missing signal'),
      'codex.tnd.locked_text':('Cette archive reste muette. Le premier fragment se lit sur un moniteur principal. Les sept suivants attendent dans les rues, la clinique, la fonderie, la caserne, le centre de transit, la tour d’Iris et le toit de la tour centrale de Nacre-9. Cherchez les petits terminaux lumineux : un clic suffit. Vos découvertes sont personnelles et conservées après la mort.','This record is silent. Read the first fragment at a main monitor. Find the other seven in the streets, clinic, foundry, barracks, transit centre, Iris tower and central tower roof of Nacre-9. Look for small glowing terminals and interact with them. Discoveries belong to you and survive death.'),
      'codex.tnd.discovered':('Archive : %s · %s/8','Archive: %s · %s/8'),
      'guide.tnd.page.0.1':('Sept biomes à explorer, dont les districts de Nacre-9. Les nouvelles villes apparaissent dans les territoires inexplorés.','Explore seven biomes including Nacre-9. New city districts appear in unexplored terrain.'),
      'expedition.tnd.atlas.short':('Visiter les 6 biomes naturels.','Visit the 6 wild biomes.'),
      'expedition.tnd.atlas.description':('Explorez les six biomes naturels de Vestige. Nacre-9 possède ses propres archives. Chaque biome compte une fois.','Explore Vestige’s six wild biomes. Nacre-9 has its own archives. Each biome counts once.'),
      'guide.tnd.page.0.0':('Assemblez un moniteur près du seuil : guide Portail ci-dessus. Chargez 16 000 FE et un cristal composite.','Assemble a monitor by the gate: Portal guide above. Supply 16,000 FE and a composite crystal.'),
    }
    states=[('Diagnostic du moniteur','Monitor diagnostics'),('Charpentes manquantes : bordure 3×3 au niveau inférieur.','Missing frames: 3×3 border below the monitor.'),('Bobines manquantes : quatre coins au niveau du moniteur.','Missing coils: four corners at monitor height.'),('Aucun contrôleur ancien à portée (8 blocs, hauteur ±3).','No ancient controller in range (8 blocks, height ±3).'),('Insérez un cristal composite dans l’emplacement.','Insert a composite crystal in the slot.'),('Énergie insuffisante : 16 000 FE nécessaires.','Insufficient energy: 16,000 FE required.'),('Tout est prêt. La charge durera 6 secondes.','Ready. Charging takes 6 seconds.'),('Synchronisation en cours… Annulation sans frais.','Synchronizing… Cancelling spends nothing.'),('Liaison permanente établie. Cliquez sur le portail.','Permanent link established. Interact with the gate.'),('Le moniteur initial fonctionne dans l’Overworld.','The initial monitor operates in the Overworld.'),('Arrivée indisponible. Libérez le site et réessayez.','Arrival unavailable. Clear the site and retry.'),('Pause redstone : retirez le signal pour ouvrir.','Redstone pause: remove the signal to open.')]
    for i,pair in enumerate(states):pairs['gate.tnd.state.'+str(i)]=pair
    for key,(a,b) in pairs.items():fr[key]=a;en[key]=b
    titles=[('Architectes du Seuil','Gate Architects'),('Nacre-9','Nacre-9'),('Protocole Suture','Suture Protocol'),('Réacteur de Minuit','Midnight Reactor'),('Les Veilleurs','The Watchers'),('Passerelles vides','Empty Skyways'),('Message d’Iris','A Message from Iris'),('Dernier signal','The Last Signal')]
    texts_fr=[
      "Manuel de liaison, fragment 01. Les Architectes n’ont pas trouvé Vestige : ils l’ont raccordé. Chaque cristal composite conserve trois fréquences, recueillies dans la chaleur, les racines et les profondeurs. Le moniteur aligne ces fréquences ; le cercle ancien maintient le passage. Nous avons conservé une règle absolue : aucun dispositif distant ne doit pouvoir condamner le chemin du retour. Si la ville se tait, si les relais tombent, revenez. Les machines ont été conçues pour écouter nos ordres. Depuis l’incident de Nacre, elles répondent parfois à une voix que personne ne reconnaît. Ne confondez jamais un signal stable avec un accueil.",
      "Registre municipal, place des Relais. Nacre-9 comptait huit tours et des milliers de fenêtres. Chaque quartier devait porter son propre réseau, ses jardins, ses cliniques et ses réserves. Sous nos pieds, la scorie noire poussait autour des fondations comme une seconde charpente. Les ingénieurs l’appelaient un ancrage ; les enfants, une montagne en couronne. Puis les mines ont livré trop vite. Nous sommes devenus riches avant de comprendre ce que nous extrayions. Le jour de l’évacuation, les enseignes sont restées allumées. Elles étaient reliées au réseau de secours. Si vous lisez ceci dans une rue éclairée, ce réseau fonctionne encore. Cela ne signifie pas que quelqu’un est resté pour vous guider.",
      "Clinique Suture, rapport du service de nuit. Les drones savaient recoudre une combinaison, cautériser une plaie et retrouver un blessé dans la fumée. Après la rupture, leur registre des habitants s’est effacé. Le protocole ne pouvait envisager une personne sans dossier. Il a classé chaque nouvel arrivant comme une contamination à traiter. Nous avons essayé de couper leurs projecteurs. Les unités ont remplacé les lampes par des faisceaux de stérilisation. Leur ligne pâle désigne l’endroit où elles vont frapper, jamais l’endroit où vous devez rester. Quittez cette ligne. Utilisez les murs. Le programme continue ses soins, même lorsqu’il ne reste plus de patients.",
      "Fonderie, note de la contremaîtresse. Le Réacteur de Minuit ne brûlait rien. Il séparait la matière de son écho, puis nous vendait les deux. Minerai, alliage, énergie : chaque rendement appelait une nouvelle machine. Les investisseurs demandaient des chiffres ; nous avons appris à ne plus regarder les fissures. Quand les fondations ont commencé à chanter, on a renforcé les ancrages. Quand les pics ont traversé la pierre, on a construit plus haut. Les processeurs des Veilleurs conservent encore des circuits utilisables. Récupérez-les si vous devez bâtir. Mais gardez une limite que nous avons oubliée : une machine qui produit sans fin finit toujours par consommer quelque chose que sa jauge ne montre pas.",
      "Caserne, ordre permanent 09. Les Veilleurs devaient tenir les rues jusqu’au départ du dernier convoi. Aucun ordre de relève n’est arrivé. Leur armure porte toujours les impacts de la première nuit. Leurs boucliers, leurs matraques et leurs sirènes servent une mission dont les responsables sont morts depuis longtemps. Ils annoncent leur charge : attendez le signal, quittez l’axe, puis frappez pendant la récupération. Les silhouettes plus minces sont les Faucheurs. Une unité d’interception conçue pour rattraper les machines défectueuses. Un choc assez fort peut interrompre leur préparation. Le réseau ne vous voit ni comme un explorateur ni comme un héritier. Vous êtes une présence non autorisée.",
      "Centre de transit, itinéraire suspendu. Les passerelles reliaient les tours lorsque la brume rendait les rues impraticables. Elles donnent encore accès aux étages sans traverser chaque patrouille, mais les drones connaissent ces routes. Chaque escalier rejoint le toit ; chaque pont rejoint un vrai palier. Aucun couloir n’a été dessiné pour mener à une fenêtre condamnée. À l’extérieur, le chemin de la couronne descend en spirale autour du massif. Les pics sont métalliques à leur cœur, pierreux à leur surface. Ils s’éclairent lorsque le réseau prend de la charge. Nous pensions que les montagnes protégeaient nos bâtiments. Les relevés les plus récents suggèrent qu’elles les retiennent.",
      "Message personnel d’Iris, opératrice de liaison. J’ai laissé les dernières archives là où les gens vivaient, travaillaient et attendaient. Pas dans un coffre scellé. Si quelqu’un revient, il doit pouvoir comprendre la ville avant de décider ce qu’il en fera. Le moniteur initial est volontairement simple ; les matériaux de votre monde suffiront. Les autres tours conservent nos erreurs. Je vous demande seulement de lire jusqu’au bout. Sur le toit central, j’ai envoyé un dernier signal au Conservateur. Il devait fermer la comptabilité et libérer les ancrages. Il a préféré conserver le système. Je ne sais pas s’il m’a entendue. Je sais qu’il écoute encore.",
      "Transmission du toit central. Destinataire : Conservateur fracturé. Nous annulons les objectifs de rendement. Nous annulons les ordres de défense. Nous annulons la dette des habitants de Nacre-9. Réponse automatique : état de conservation prioritaire ; évacuation non vérifiée ; contre-ordre refusé. Iris a répété le message jusqu’à l’aube. Le réseau a enregistré chaque tentative, puis a réparti sa voix entre les relais. Voilà ce que portent les sons de Vestige : une demande de fin, copiée trop souvent pour s’éteindre. Le mémorial n’est pas une clé de plus. C’est la preuve que quelqu’un a enfin lu les huit fragments. Emportez ce souvenir chez vous. Et lorsque le Conservateur vous parlera de richesse, demandez-lui qui en paie encore le prix."
    ]
    texts_en=[
      'Link manual, fragment 01. The Architects did not discover Vestige: they connected it. Composite crystals preserve three frequencies from heat, roots and deep water. A monitor aligns them; the ancient ring holds the passage. One rule survived every revision: no remote device may condemn the return journey. If the city goes silent and the relays fail, go home. The machines were built to obey us. Since the Nacre incident they sometimes answer a voice nobody recognizes. A stable signal is no promise of welcome.',
      'Municipal register, Relay Square. Nacre-9 had eight towers and thousands of windows. Each district carried its own power, gardens, clinics and reserves. Black slag grew around the foundations like a second frame. Engineers called it an anchor; children called it a crowned mountain. Then the mines delivered too quickly. We became rich before understanding what we extracted. The evacuation left the signs lit, connected to emergency power. If you read this under a glowing sign, that network still works. It does not mean somebody stayed behind to guide you.',
      'Suture Clinic, night report. The drones could mend a suit, seal a wound and locate casualties through smoke. The fracture erased their patient registry. Their protocol could not imagine a person without a record, so it classified every arrival as contamination. When we disconnected their lamps, they replaced them with sterilization beams. A pale line marks where they will fire. Leave that line. Use walls for cover. The program continues its treatment even when no patients remain.',
      'Foundry, forewoman’s note. The Midnight Reactor burned nothing. It separated matter from its echo and sold us both. Ore, alloy, power: each higher yield demanded another machine. Investors wanted numbers; we learned to ignore the cracks. When the foundations began to sing, we reinforced the anchors. When spikes pierced the stone, we built higher. Enforcer processors still contain usable control circuits. Salvage them if you must build. Keep the limit we forgot: a machine that produces forever eventually consumes something its gauge never shows.',
      'Barracks, standing order 09. The Watchers were to hold the streets until the last convoy departed. Their relief order never arrived. Their armour still bears the first night’s damage. Shields, batons and sirens serve a mission whose commanders are long dead. Their charge is announced: leave its axis, then strike during recovery. The slender shapes are Reavers, interception units once used to catch broken machines. A strong hit can interrupt their preparation. The network sees neither explorer nor heir. You are an unauthorized presence.',
      'Transit centre, suspended route. Skyways linked towers when fog covered the streets. They still bypass patrols, but drones know those routes. Every staircase reaches a roof; every bridge meets a real landing. No corridor was designed to end at a sealed window. Outside, the crown path spirals down the massif. Its spikes are metal within and stone outside. They glow when the network draws power. We believed these mountains protected our buildings. Recent readings suggest they are holding them in place.',
      'Personal message from Iris, link operator. I left the final records where people lived, worked and waited. No sealed chest. Whoever returns should understand the city before deciding its future. The first monitor is deliberately simple: materials from your world are enough. The towers preserve our mistakes. Please read all the way through. From the central roof I sent one final signal to the Custodian. It should have closed the accounts and released the anchors. It chose to preserve the system. I cannot tell whether it heard me. I know it is still listening.',
      'Central roof transmission. Recipient: Fractured Custodian. Cancel production targets. Cancel defence orders. Cancel the debts of Nacre-9. Automatic reply: preservation has priority; evacuation unverified; counter-order refused. Iris repeated the message until dawn. The network recorded every attempt and distributed her voice among the relays. That is what Vestige’s sounds carry: a request for an ending, copied too often to die. The memorial is no further key. It proves somebody finally read all eight fragments. Carry that memory home. When the Custodian offers you wealth, ask who is still paying for it.'
    ]
    for i,((a,b),f,e) in enumerate(zip(titles,texts_fr,texts_en)):
        for suffix in ['title','short']:fr[f'codex.tnd.{suffix}.{i}']=a;en[f'codex.tnd.{suffix}.{i}']=b
        fr[f'codex.tnd.text.{i}']=f;en[f'codex.tnd.text.{i}']=e
    subs=['Le seuil se synchronise','Le seuil se déploie','Traversée du seuil','Le seuil résonne','Un drone Suture arme son faisceau','Une sirène de Veilleur retentit','Pas métalliques','Un faisceau de stérilisation frappe','Un Faucheur prépare sa ruée','Impact de sécurité','Une archive se révèle','La ville murmure']
    subs_en=['Gate synchronizes','Gate unfolds','Gate transit','Gate resonates','Suture Drone arms its beam','Enforcer siren sounds','Metal footsteps','Sterilization beam fires','Reaver prepares a rush','Security impact','Archive discovered','The city murmurs']
    for n,f,e in zip(SOUNDS,subs,subs_en):fr['subtitles.tnd.'+n]=f;en['subtitles.tnd.'+n]=e
