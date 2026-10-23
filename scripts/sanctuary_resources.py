"""Original sanctuary architecture resources, explicit industry recipes and complete practical bilingual UI."""
import json,math,random
from sanctuary_art import ART,pixels
from sanctuary_item_art import ITEM_ART,pixels as item_pixels
BLOCKS=list(ART)
MACHINES=['mnemonic_refinery','nocturne_crucible','axiomatic_assembler','mnemonic_sorter']
ITEMS=['observatory_compass','mnemonic_shard','axiomatic_core','nocturne_ingot','mnemonic_circuit','purified_auralite','observatory_memory','nocturne_blade','nocturne_pickaxe']
ENTITIES=['obsidian_watcher','prismatic_lancer','mnemonic_cantor','threshold_colossus','keeper_of_lenses','silent_smelter','petrified_hortulan','axiom_guardian','antiphon_guardian','first_silence_archon']
SOUNDS=['observatory_drone','observatory_threshold','observatory_relay','observatory_warning','observatory_impact','observatory_victory','observatory_core']
TESTS=['observatory_route','observatory_ledger','observatory_palette','observatory_codec','observatory_mechanics','observatory_industry','observatory_industry_guards','observatory_construct','observatory_attacks','observatory_familiar','observatory_generation','observatory_protection','observatory_core_layers','observatory_sorter','observatory_pads','observatory_relays','observatory_trace_gate','observatory_duo_rewards','observatory_linked_dials','observatory_live_ai']

def item_art(name):
    if name in ITEM_ART:return item_pixels(name)
    # Original compact silhouettes; all foreground pixels align to the same native 16px grid.
    p=[(0,0,0,0)]*256
    def dot(x,y,c):
        if 0<=x<16 and 0<=y<16:p[y*16+x]=(*c,255)
    dark=(24,30,38);edge=(77,93,105);light=(213,215,179);gold=(181,143,67);cyan=(118,197,196)
    if name.endswith(('blade','pickaxe')):
        for i in range(2,14):dot(i,15-i,dark);dot(i+1,15-i,edge);dot(i,14-i,light if i>6 else gold)
        if name.endswith('pickaxe'):
            for x in range(3,13):dot(x,3,dark);dot(x,4,cyan);dot(x,5,edge)
        else:
            for i in range(4):dot(3+i,9+i,gold)
    else:
        for y in range(2,14):
            for x in range(2,14):
                dx=x-7.5;dy=y-7.5;r=dx*dx+dy*dy
                keep=(r<35 if 'core' in name or 'compass' in name or 'memory' in name else abs(dx)+abs(dy)<7 if 'shard' in name else 4<=y<=11 and 2<=x<=13)
                if keep:dot(x,y,dark if r>26 else edge if (x+y)%4 else gold)
        for i in range(4,12):dot(7,i,light);dot(i,7,cyan if 'circuit' in name else gold)
        if 'ingot' in name:
            for x in range(4,12):dot(x,5,light);dot(x,10,gold)
        if 'purified' in name:
            for x,y in [(3,11),(6,4),(11,9),(9,3),(5,12)]:dot(x,y,light)
    return p

def content(write,png,assets,data):
    for name in BLOCKS:
        png(assets/f'textures/block/{name}.png',pixels(name))
        write(assets/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/'+name}})
        if name=='observatory_dial':
            write(assets/f'blockstates/{name}.json',{'variants':{f'tone={i}':{'model':'tnd:block/'+name,'y':i*90} for i in range(4)}})
        else:write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':'tnd:block/'+name}}})
        write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
        if not name.startswith('observatory_'):
            write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
            write(data/f'recipe/{name}.json',{'type':'minecraft:stonecutting','ingredient':'tnd:nightslag','result':{'id':'tnd:'+name,'count':1}})
    for active in [False,True]:
        write(assets/f'models/block/observatory_relay{"_active" if active else ""}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/'+('amber_seal' if active else 'silent_conduit')}})
    write(assets/'blockstates/observatory_relay.json',{'variants':{'lit=false':{'model':'tnd:block/observatory_relay'},'lit=true':{'model':'tnd:block/observatory_relay_active'}}})
    write(assets/'models/item/observatory_relay.json',{'parent':'tnd:block/observatory_relay_active'});write(assets/'items/observatory_relay.json',{'model':{'type':'minecraft:model','model':'tnd:item/observatory_relay'}})
    for name in ['obsidian_stairs','obsidian_slab']:
        tex={'side':'tnd:block/obsidian_tiles','bottom':'tnd:block/obsidian_tiles','top':'tnd:block/obsidian_tiles'}
        if name.endswith('slab'):
            write(assets/f'models/block/{name}.json',{'parent':'minecraft:block/slab','textures':tex});write(assets/f'models/block/{name}_top.json',{'parent':'minecraft:block/slab_top','textures':tex})
            variants={'type=bottom':{'model':'tnd:block/'+name},'type=top':{'model':'tnd:block/'+name+'_top'},'type=double':{'model':'tnd:block/obsidian_tiles'}}
        else:
            for suffix,parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:write(assets/f'models/block/{name}{suffix}.json',{'parent':'minecraft:block/'+parent,'textures':tex})
            variants={}
            for facing,rotation in [('east',0),('south',90),('west',180),('north',270)]:
                for half in ['bottom','top']:
                    for form in ['straight','inner_left','inner_right','outer_left','outer_right']:
                        suffix='' if form=='straight' else '_'+form.split('_')[0];angle=(rotation+(-90 if form.endswith('left') else 0)+(90 if half=='top' and form!='straight' else 0))%360
                        variants[f'facing={facing},half={half},shape={form}']={'model':f'tnd:block/{name}{suffix}','x':180 if half=='top' else 0,'y':angle,'uvlock':True}
        write(assets/f'blockstates/{name}.json',{'variants':variants});write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
        write(data/f'recipe/{name}.json',{'type':'minecraft:stonecutting','ingredient':'tnd:obsidian_tiles','result':{'id':'tnd:'+name,'count':2 if name.endswith('slab') else 1}})
        entry={'type':'minecraft:item','name':'tnd:'+name}
        if name.endswith('slab'):entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'tnd:'+name,'properties':{'type':'double'}}]}]
        write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    def box(a,b,texture):return {'from':a,'to':b,'faces':{side:{'texture':texture} for side in ['north','south','east','west','up','down']}}
    for name in MACHINES:
        body='observatory_station' if 'assembler' in name else 'silent_conduit' if 'crucible' in name else 'memory_lattice'
        png(assets/f'textures/block/{name}.png',pixels(body))
        write(assets/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':'tnd:block/'+name+('_active' if lit else ''),'y':angle} for f,angle in [('north',0),('east',90),('south',180),('west',270)] for lit in [False,True]}})
        for active in [False,True]:
            elements=[box([0,0,0],[16,2,16],'#frame'),box([1,2,1],[15,14,15],'#body'),box([0,14,0],[16,16,16],'#frame'),box([4,4,0],[12,11,1],'#body')]
            if name=='mnemonic_refinery':elements+=[box([3,16,3],[7,20,7],'#frame'),box([9,16,9],[13,20,13],'#frame')]
            if name=='nocturne_crucible':elements+=[box([3,16,3],[13,19,13],'#frame'),box([5,19,5],[11,20,11],'#light')]
            if name=='axiomatic_assembler':elements+=[box([1,16,5],[3,21,11],'#frame'),box([13,16,5],[15,21,11],'#frame'),box([3,20,6],[13,22,10],'#body')]
            if active:elements+=[box([6,5,-.05],[10,7,.05],'#light')]
            write(assets/f'models/block/{name}{"_active" if active else ""}.json',{'textures':{'body':'tnd:block/'+name,'frame':'tnd:block/obsidian_ribbed','light':'tnd:block/luminous_script','particle':'tnd:block/'+name},'elements':elements})
        write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
        write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    panel_tex={'body':'tnd:block/obsidian_relief','screen':'tnd:block/observatory_station','light':'tnd:block/luminous_script','particle':'tnd:block/obsidian_metal'}
    elements=[box([1,0,1],[15,3,15],'#body'),box([4,3,4],[12,11,12],'#body'),box([1,10,1],[15,12,15],'#body'),box([2,12,3],[14,15,13],'#screen')]
    write(assets/'models/block/observatory_station.json',{'textures':panel_tex,'elements':elements})
    for active in [False,True]:
        relay_tex={'body':'tnd:block/obsidian_pillar','light':'tnd:block/'+('amber_seal' if active else 'silent_conduit'),'particle':'tnd:block/obsidian_pillar'}
        pieces=[box([2,0,2],[14,3,14],'#body'),box([6,3,6],[10,12,10],'#body'),box([3,10,3],[13,13,13],'#body'),box([5,12,5],[11,16,11],'#light')]
        write(assets/f'models/block/observatory_relay{"_active" if active else ""}.json',{'textures':relay_tex,'elements':pieces})
    for name in ITEMS+[n+'_spawn_egg'  for n in ENTITIES]:
        png(assets/f'textures/item/{name}.png',item_art(name));write(assets/f'models/item/{name}.json',{'parent':'minecraft:item/handheld' if name in ['nocturne_blade','nocturne_pickaxe'] else 'minecraft:item/generated','textures':{'layer0':'tnd:item/'+name}});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
    for index,name in enumerate(ENTITIES):
        for opened in [False,True]:
            rng=random.Random(name);atlas=[]
            for y in range(64):
                for x in range(64):
                    n=rng.randint(-3,3);c=(31+n,39+n,48+n)
                    if x%8==0 or y%8==0:c=(66+n,79+n,88+n)
                    if y>=32 and y%8 in (1,2):c=(88+n,100+n,105+n)
                    if x>=48 and y<16:c=(230,215,158) if opened else (115,164,170) if index%2 else (177,154,101)
                    atlas.append((*c,255))
            png(assets/f'textures/entity/{name}{"_open" if opened else ""}.png',atlas,64,64)
        glow=[((224,215,167,255) if index%2==0 else (139,208,204,255)) if x>=48 and y<16 else (0,0,0,0) for y in range(64) for x in range(64)]
        png(assets/f'textures/entity/{name}_glow.png',glow,64,64)
        write(data/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':[] if index>=3 else [{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:mnemonic_shard','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2}}]}]}]})
    entries=[]
    for item,weight,a,b in [('tnd:mnemonic_shard',5,2,5),('tnd:echo_dust',5,2,6),('minecraft:golden_carrot',4,2,5),('tnd:incubator_nutrient',2,1,2),('tnd:noctilite_crystal',3,1,3),('tnd:auralite_ingot',3,1,3),('tnd:security_processor',2,1,2)]:entries.append({'type':'minecraft:item','name':item,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':a,'max':b}}]})
    write(data/'loot_table/chests/observatory_supplies.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':2,'max':4},'entries':entries}]})
    def shaped(name,pattern,key,count=1):write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'tnd:'+name,'count':count}})
    shaped('observatory_compass',[' F ','NCS',' A '],{'F':'tnd:glass_filament','N':'tnd:noctilite_crystal','C':'minecraft:compass','S':'tnd:security_processor','A':'tnd:auralite_ingot'})
    for name in MACHINES:shaped(name,['UMU','CNC','FRF'],{'U':'tnd:umbra_alloy','M':'tnd:axiomatic_core','C':'tnd:mnemonic_circuit' if name=='mnemonic_sorter' else 'tnd:control_circuit','N':'minecraft:netherite_ingot','F':'tnd:industrial_frame','R':'minecraft:redstone_block'})
    shaped('nocturne_blade',[' I ',' I ',' C '],{'I':'tnd:nocturne_ingot','C':'tnd:mnemonic_circuit'})
    shaped('nocturne_pickaxe',['III',' C ',' C '],{'I':'tnd:nocturne_ingot','C':'tnd:mnemonic_circuit'})
    write(data/'recipe/mnemonic_shard.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:security_processor','tnd:echo_dust','tnd:noctilite_crystal'],'result':{'id':'tnd:mnemonic_shard','count':2}})
    write(data/'recipe/purified_auralite_smelting.json',{'type':'minecraft:smelting','category':'misc','ingredient':'tnd:purified_auralite','result':{'id':'tnd:auralite_ingot','count':2},'experience':.5,'cookingtime':200})
    write(data/'advancement/recipes/observatory_knowledge.json',{'criteria':{'processor':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:security_processor'}]}}},'requirements':[['processor']],'rewards':{'recipes':['tnd:observatory_compass','tnd:mnemonic_shard']+['tnd:'+n for n in MACHINES]+['tnd:nocturne_blade','tnd:nocturne_pickaxe','tnd:purified_auralite_smelting']}})
    write(data/'tags/item/nocturne_materials.json',{'values':['tnd:nocturne_ingot']})
    for tag,names in [('mineable/pickaxe',BLOCKS+MACHINES+['obsidian_stairs','obsidian_slab']),('needs_diamond_tool',['obsidian_metal']),('stairs',['obsidian_stairs']),('slabs',['obsidian_slab'])]:
        path=data.parent/f'minecraft/tags/block/{tag}.json';old=json.loads(path.read_text()) if path.exists() else {'values':[]};old['values']=list(dict.fromkeys(old['values']+['tnd:'+n for n in names]));write(path,old)
    for tag,names in [('pickaxes',['nocturne_pickaxe']),('swords',['nocturne_blade']),('enchantable/mining',['nocturne_pickaxe']),('enchantable/mining_loot',['nocturne_pickaxe']),('enchantable/weapon',['nocturne_blade']),('enchantable/sword',['nocturne_blade']),('enchantable/sharp_weapon',['nocturne_blade']),('enchantable/durability',['nocturne_pickaxe','nocturne_blade'])]:
        path=data.parent/f'minecraft/tags/item/{tag}.json';old=json.loads(path.read_text()) if path.exists() else {'values':[]};old['values']=list(dict.fromkeys(old['values']+['tnd:'+n for n in names]));write(path,old)
    biome=json.loads((data/'worldgen/biome/neon_city.json').read_text());biome['features']=[[] for _ in range(11)];biome['features'][10]=['tnd:black_observatory'];biome['spawners']={k:[] for k in biome['spawners']};biome['attributes']['minecraft:visual/fog_color']='#24333d';biome['attributes']['minecraft:visual/sky_color']='#17232e';biome['attributes']['minecraft:visual/fog_start_distance']=80.;biome['attributes']['minecraft:visual/fog_end_distance']=400.;biome['attributes']['minecraft:audio/ambient_sounds']['loop']='tnd:vestige_wind';write(data/'worldgen/biome/black_observatory.json',biome)
    write(data/'worldgen/configured_feature/black_observatory.json',{'type':'tnd:black_observatory','config':{}});write(data/'worldgen/placed_feature/black_observatory.json',{'feature':'tnd:black_observatory','placement':[]})
    path=data/'dimension/vestige.json';stem=json.loads(path.read_text())
    if stem['generator']['biome_source']['type']!='tnd:black_observatory':stem['generator']['biome_source']={'type':'tnd:black_observatory','outside':stem['generator']['biome_source'],'sanctuary':'tnd:black_observatory'}
    write(path,stem)
    path=data/'worldgen/noise_settings/vestige.json';noise=json.loads(path.read_text());current=noise['noise_router']['final_density']
    if not (isinstance(current,dict) and current.get('argument2',{}).get('argument1',{}).get('type')=='tnd:observatory_blend'):
        blend={'type':'tnd:observatory_blend'}
        noise['noise_router']['final_density']={'type':'minecraft:add','argument1':{'type':'minecraft:mul','argument1':{'type':'minecraft:add','argument1':1.,'argument2':{'type':'minecraft:mul','argument1':-1.,'argument2':blend}},'argument2':current},'argument2':{'type':'minecraft:mul','argument1':blend,'argument2':{'type':'minecraft:y_clamped_gradient','from_y':112,'to_y':144,'from_value':1.,'to_value':-1.}}}
    density=noise['noise_router']['final_density']
    density['argument2']['argument2']={'type':'minecraft:y_clamped_gradient','from_y':112,'to_y':144,'from_value':1.,'to_value':-1.}
    write(path,noise)
    for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+name,'structure':'tnd:test_habitat','max_ticks':1600,'required':True})

def languages(fr,en):
    names=[('Métal d’obsidienne','Obsidian Metal'),('Dalles de l’Observatoire','Observatory Tiles'),('Pilier d’obsidienne','Obsidian Pillar'),('Bas-relief noir','Black Relief'),('Nervure d’obsidienne','Obsidian Ribbing'),('Grille d’obsidienne','Obsidian Grate'),('Écriture lumineuse','Luminous Script'),('Incrustation directrice','Guiding Inlay'),('Sceau ambré','Amber Seal'),('Treillis mémoriel','Memory Lattice'),('Conduit silencieux','Silent Conduit'),('Brasero de l’Observatoire','Observatory Brazier'),('Station de l’Observatoire','Observatory Station'),('Disque d’accord','Attunement Dial'),('Seuil scellé','Sealed Threshold'),('Stèle mémorielle','Memory Stele')]
    for name,(f,e) in zip(BLOCKS,names):fr['block.tnd.'+name]=f;en['block.tnd.'+name]=e
    for name,f,e in [('obsidian_stairs','Escalier d’obsidienne','Obsidian Stairs'),('obsidian_slab','Dalle d’obsidienne','Obsidian Slab')]:fr['block.tnd.'+name]=f;en['block.tnd.'+name]=e
    item_names=[('Boussole du Premier Silence','First Silence Compass'),('Éclat mnémonique','Mnemonic Shard'),('Cœur axiomatique','Axiomatic Core'),('Lingot de nocturne','Nocturne Ingot'),('Circuit mnémonique','Mnemonic Circuit'),('Auralite purifiée','Purified Auralite'),('Mémoire de l’Observatoire','Observatory Memory'),('Lame de nocturne','Nocturne Blade'),('Pioche de nocturne','Nocturne Pickaxe')]
    for name,(f,e) in zip(ITEMS,item_names):fr['item.tnd.'+name]=f;en['item.tnd.'+name]=e
    entity_names=[('Témoin d’obsidienne','Obsidian Witness'),('Lancier prismatique','Prismatic Lancer'),('Chantre mnémonique','Mnemonic Cantor'),('Colosse du Seuil','Threshold Colossus'),('Gardien des Lentilles','Keeper of Lenses'),('Fondeur silencieux','Silent Smelter'),('Hortulan pétrifié','Petrified Hortulan'),('Axiome','Axiom'),('Antiphone','Antiphon'),('Archonte du Premier Silence','Archon of the First Silence')]
    for name,(f,e) in zip(ENTITIES,entity_names):fr['entity.tnd.'+name]=f;en['entity.tnd.'+name]=e;fr['item.tnd.'+name+'_spawn_egg']='Œuf de '+f;en['item.tnd.'+name+'_spawn_egg']=e+' Spawn Egg'
    fr['biome.tnd.black_observatory']='Observatoire du Premier Silence';en['biome.tnd.black_observatory']='Observatory of the First Silence'
    pairs={
      'title':('Observatoire · itinéraire','Observatory · route'),'progress':('%s / %s chambres accordées','%s / %s chambers attuned'),'station':('Station %s · %s','Station %s · %s'),'chamber':('Chambre %s','Chamber %s'),'start':('Interagir','Interact'),'claim':('Butin','Claim'),'leave':('Parvis','Forecourt'),'travel':('Rejoindre','Travel'),'running':('Accord en cours','Attunement in progress'),'retry_help':('Réinitialiser les disques ou relancer une rencontre interrompue. Les adversaires présents empêchent la reprise.','Reset the dials or restart an interrupted encounter. Remaining opponents prevent a restart.'),'alignment':('Accord : %s · %s · %s · %s','Alignment: %s · %s · %s · %s'),'trace_count':('Stèles : %s / 3','Steles: %s / 3'),'enemies':('Adversaires vaincus : %s / %s','Defeated: %s / %s'),'done':('Accord conservé','Attunement preserved'),'available':('Prochaine étape','Next stage'),'locked':('Suivre les étapes précédentes','Follow the preceding stages'),'map_hint':('Sélectionner, puis Rejoindre','Select, then Travel'),'seek_traces':('Trois stèles portent une réponse. Relevés : %s / 3.','Three steles carry an answer. Read: %s / 3.'),'alignment_reset':('Les quatre disques reviennent au premier ton.','The four dials return to the first tone.'),'misaligned':('Les quatre disques ne répondent pas encore ensemble.','The four dials do not yet answer together.'),'complete':('Chambre %s : le passage répond.','Chamber %s: the passage answers.'),'tone':('Accord %s / 4','Tone %s / 4'),'compass_elsewhere':('L’aiguille attend le ciel de Vestige.','The needle awaits the sky of Vestige.'),'compass':('Un seuil répond vers %s, %s, %s.','A threshold answers toward %s, %s, %s.'),'pad_count':('Résonances : %s / 4','Resonances: %s / 4'),'relays':('Relais coupés : %s / 4','Relays disabled: %s / 4'),'wave':('Garde · vague %s / 3','Guard · wave %s / 3'),'filter':('Filtre','Filter'),'reagent':('Réactif','Reagent'),'familiar_memory':('Il reconnaît une lumière qui n’était plus là.','It recognises a light that was no longer there.')}
    acts=[('Parvis des Veilleurs','Forecourt of Witnesses'),('Archives enfouies','Buried Archives'),('Forges du Silence','Forges of Silence'),('Jardins pétrifiés','Petrified Gardens'),('Tribunal des Jumeaux','Tribunal of Twins'),('Cœur de l’Observatoire','Heart of the Observatory')]
    rules=[('Chaque disque tourne seul.','Each dial turns alone.'),('Il entraîne le suivant ; le dernier rejoint le premier.','It drives the next; the last returns to the first.'),('Il entraîne ceux à sa droite.','It drives those to its right.'),('Les disques en miroir répondent ensemble.','Mirrored dials answer together.'),('Il entraîne ses deux voisins, en boucle.','It drives both neighbours in a loop.'),('Le disque opposé avance de deux tons.','The opposite dial advances two tones.')]
    for i,v in enumerate(rules):pairs['rule.'+str(i)]=v
    for i,v in enumerate(acts):pairs['act.'+str(i)]=v
    objectives=[('Lire les trois stèles de cette chambre. Leurs lumières signalent leur emplacement.','Read the three steles in this chamber. Their lights mark their positions.'),('Éveiller les témoins, puis vaincre les gardiens de cette chambre.','Awaken the witnesses, then defeat the guardians of this chamber.'),('Accorder les quatre disques. Certains entraînent leurs voisins ; l’indice décrit leur mouvement. Valider ici.','Align the four dials. Some drive their neighbours; the clue describes their movement. Confirm here.'),('Couper les quatre relais ambrés et vaincre la garde. Les cercles lumineux annoncent les tirs du réseau.','Disable the four amber relays and defeat the guard. Luminous circles announce the network shots.'),('Rejoindre quatre résonances successives, au sol et sur les galeries. La prochaine lumière vous attend.','Reach four successive resonances on the floor and galleries. The next light awaits you.'),('Retrouver et lire trois traces proches. Un familier disponible peut attirer votre attention sur une stèle.','Find and read three nearby traces. An available companion may draw your attention to a stele.'),('Traverser les trois vagues de la garde. Une reprise interrompue ne compte pas comme une victoire.','Overcome three waves of the guard. An interrupted attempt does not count as a victory.'),('Éveiller le gardien du seuil. Observer les marques au sol et profiter des fenêtres où son cœur s’ouvre.','Awaken the threshold guardian. Watch the ground markings and exploit the openings of its core.')]
    for i,v in enumerate(objectives):pairs['objective.'+str(i)]=v;pairs['action.'+str(i)]=('Examiner','Examine') if i in [0,5] else ('Valider l’accord','Confirm alignment') if i in [2,4] else ('Éveiller','Awaken')
    rooms_fr=[
        ['Vestibule des témoins','Garde du premier pas','Quatre voix de pierre','Le regard ambré','Galerie des empreintes','Noms sans visages','Veille du seuil','Colosse du Seuil'],
        ['Vestiges d’une écriture','Les archivistes','Index des voix liées','Archives sous surveillance','Passerelles de mémoire','La page absente','Garde des lentilles','Chambre des Lentilles'],
        ['La chaleur des mains','Ouvriers du silence','Accord des fournaises','Réseau des creusets','Circuit de refroidissement','Empreintes dans le métal','Dernière coulée','Le Fondeur silencieux'],
        ['Racines de pierre','Gardiens du jardin','Les voix en miroir','Serres condamnées','Branches de lumière','L’eau qui manque','La garde sans saison','L’Hortulan pétrifié'],
        ['Deux places, une ombre','Veilleurs du tribunal','Accord des voisins','Les yeux du jugement','Galerie des balances','L’autre témoignage','Garde des deux voix','Axiome et Antiphone'],
        ['Le cercle incomplet','Garde du centre','L’accord contraire','Le dernier regard','Couronne des résonances','La place effacée','Ceux qui sont restés','Le Premier Silence']]
    rooms_en=[
        ['Vestibule of Witnesses','Guard of the First Step','Four Voices of Stone','The Amber Gaze','Gallery of Footprints','Names Without Faces','Threshold Vigil','Threshold Colossus'],
        ['Remains of a Script','The Archivists','Index of Bound Voices','Watched Archives','Memory Walkways','The Missing Page','Guard of the Lenses','Chamber of Lenses'],
        ['The Warmth of Hands','Workers of Silence','Furnace Attunement','Crucible Network','Cooling Circuit','Impressions in Metal','The Last Casting','The Silent Smelter'],
        ['Roots of Stone','Garden Guardians','Mirrored Voices','Condemned Glasshouses','Branches of Light','The Missing Water','Seasonless Guard','The Petrified Hortulan'],
        ['Two Seats, One Shadow','Tribunal Watchers','Neighbour Attunement','Eyes of Judgement','Gallery of Scales','The Other Testimony','Guard of Two Voices','Axiom and Antiphon'],
        ['The Incomplete Circle','Guard of the Centre','The Contrary Accord','The Last Gaze','Crown of Resonances','The Erased Seat','Those Who Remained','The First Silence']]
    for act in range(6):
        for slot in range(8):pairs[f'room.{act*8+slot}']=(rooms_fr[act][slot],rooms_en[act][slot])
    fragments=[
        [('La lumière demeure au niveau des pas.','The light remains at the level of footsteps.'),('Tous les noms furent gravés avant les visages.','All the names were carved before the faces.'),('Le premier témoin tournait le dos à la porte.','The first witness had its back to the door.')],
        [('Ils ne regardaient pas le ciel.','They were not watching the sky.'),('Les mêmes signes reviennent, une ligne plus bas.','The same signs return, one line lower.'),('Le dernier registre ne porte aucune date.','The last register bears no date.')],
        [('Le métal porte encore la chaleur des mains.','The metal still bears the warmth of hands.'),('Aucun conduit ne rejoint la surface.','No conduit reaches the surface.'),('Une empreinte minuscule traverse la coulée.','A tiny footprint crosses the casting.')],
        [('Les racines ont cessé de demander de l’eau.','The roots stopped asking for water.'),('Sous la pierre, les nervures sont encore souples.','Beneath the stone, the veins are still soft.'),('Le jardin n’a jamais connu de soleil.','The garden never knew a sun.')],
        [('Une voix attend toujours la seconde.','One voice still awaits the second.'),('Les deux sièges portent la même usure.','Both seats bear the same wear.'),('Le jugement s’arrête au milieu d’un mot.','The judgement stops halfway through a word.')],
        [('Il manque une place autour du centre.','One place is missing around the centre.'),('Les gravures sont plus profondes du côté vide.','The carvings are deeper on the empty side.'),('Quelqu’un a poli la pierre après le silence.','Someone polished the stone after the silence.')]]
    echoes=[
        [('Les pas gravés vont tous vers l’intérieur.','The carved footsteps all lead inward.'),('Un nom a été repris d’une main différente.','A name was retraced by a different hand.'),('La poussière contourne une place vide.','The dust curves around an empty place.')],
        [('Cette page pèse plus lourd que les autres.','This page weighs more than the others.'),('L’encre s’est effacée, pas les corrections.','The ink faded, but the corrections did not.'),('Sur la tranche : « Ne pas rendre au ciel. »','On the edge: “Do not return to the sky.”')],
        [('Le dernier outil a refroidi dans son moule.','The last tool cooled inside its mould.'),('Deux marques de doigts. Puis une seule.','Two finger marks. Then only one.'),('Le four vide continue de respirer.','The empty furnace keeps breathing.')],
        [('Une feuille noire repose dans une niche sèche.','A black leaf rests in a dry niche.'),('Des cernes minuscules entourent le même hiver.','Tiny growth rings surround the same winter.'),('Il y avait ici quelque chose de très petit.','Something very small once lived here.')],
        [('La seconde voix est inscrite sous la première.','The second voice is written beneath the first.'),('Une balance a été fixée en équilibre.','A scale was fastened in balance.'),('Aucune des deux portes ne porte le mot sortie.','Neither door bears the word exit.')],
        [('Un cercle très ancien a été tracé à nouveau.','An ancient circle was drawn again.'),('Au centre, la poussière n’est pas noire.','At the centre, the dust is not black.'),('La dernière ligne n’a pas été achevée.','The final line was left unfinished.')]]
    for act in range(6):
        for n in range(3):pairs[f'trace.{act}.{n}']=fragments[act][n];pairs[f'echo.{act}.{n}']=echoes[act][n]
    pairs['protected']=('La matière ne cède pas. Les dispositifs répondent encore.','The material will not yield. The devices still respond.')
    pairs['gate_wait']=('Le seuil attend la chambre %s : %s.','The threshold awaits chamber %s: %s.')
    pairs['objective.duo']=('Éveiller les deux gardiens. Leurs préparations alternent ; le survivant change de rythme. Vaincre les deux pour ouvrir le passage.','Awaken both guardians. Their wind-ups alternate; the survivor changes pace. Defeat both to open the passage.')
    pairs['claimed']=('Reçu','Claimed')
    pairs['current_alignment']=('Disques : %s · %s · %s · %s','Dials: %s · %s · %s · %s')
    pairs['survival_required']=('Passer en survie pour éveiller les gardiens.','Switch to Survival to awaken the guardians.')
    pairs['difficulty_required']=('Les gardiens restent silencieux en difficulté Paisible.','The guardians remain silent on Peaceful difficulty.')
    pairs['memory_use']=('Utiliser sur votre familier de niveau 20 : +8 PV et +2 attaque, une fois.','Use on your level-20 companion: +8 health and +2 attack, once.')
    pairs['core_use']=('Composant des machines de l’Observatoire. Consulter le manuel industriel.','Component for Observatory machines. See the industrial manual.')
    pairs['compass_use']=('Utiliser dans Vestige pour relever les coordonnées du parvis.','Use in Vestige to read the forecourt coordinates.')
    for k,(f,e) in pairs.items():fr['observatory.tnd.'+k]=f;en['observatory.tnd.'+k]=e
    machine_names=[('Raffinerie mnémonique','Mnemonic Refinery'),('Creuset de nocturne','Nocturne Crucible'),('Assembleur axiomatique','Axiomatic Assembler'),('Trieur mnémonique','Mnemonic Sorter')]
    help_fr=[['Ferme le cycle des matières anciennes : auralite brute en haut, éclat mnémonique en bas.','Produit 3 auralites purifiées en 6 s pour 6 000 FE. Un éclat est consommé.','Socle : huit charpentes une couche sous la machine. Sorties en bas ; redstone suspend le cycle.'],['Chargez 2 auralites purifiées en haut et 1 alliage ombral en bas.','Produit 2 lingots de nocturne en 9 s pour 12 000 FE. Le métal sert aux outils et aux circuits.','Socle : huit charpentes. Les deux entrées acceptent les entonnoirs ; sorties par-dessous.'],['Chargez 2 lingots de nocturne en haut et 1 circuit de commande en bas.','Produit 2 circuits mnémoniques en 12 s pour 24 000 FE. Ils servent aux équipements avancés.','Socle : huit charpentes. Une sortie pleine ou une réserve insuffisante ne consomme rien.']]
    help_en=[['Closes the ancient material chain: raw auralite above, mnemonic shard below.','Makes 3 purified auralite in 6 s for 6,000 FE. One shard is consumed.','Foundation: eight frames one layer below. Outputs below; redstone suspends work.'],['Load 2 purified auralite above and 1 umbra alloy below.','Makes 2 nocturne ingots in 9 s for 12,000 FE. Used for tools and circuits.','Foundation: eight frames. Hoppers feed both inputs; extract outputs below.'],['Load 2 nocturne ingots above and 1 control circuit below.','Makes 2 mnemonic circuits in 12 s for 24,000 FE. Used for advanced equipment.','Foundation: eight frames. Full output or insufficient energy consumes nothing.']]
    help_fr.append(['Un objet dans l’entrée basse définit le filtre exact. Il reste dans la machine. Chargez les objets à trier en haut.', 'Transfère un objet correspondant vers les sorties en 0,5 s pour 120 FE. Nom et enchantements sont conservés.', 'Socle : huit charpentes. Les entonnoirs alimentent seulement le haut et extraient par-dessous. Le filtre reste manuel.'])
    help_en.append(['One item in the lower input defines the exact filter. It remains in the machine. Feed matching items above.', 'Moves one matching item to the output in 0.5 s for 120 FE. Name and enchantments are preserved.', 'Foundation: eight frames. Hoppers feed only the top and extract below. Set the filter manually.'])
    for i,name in enumerate(MACHINES):
        fr['block.tnd.'+name],en['block.tnd.'+name]=machine_names[i]
        fr['tooltip.tnd.'+name]=help_fr[i][0];en['tooltip.tnd.'+name]=help_en[i][0]
        for row in range(3):fr[f'industry.tnd.help.{name}.{row}']=help_fr[i][row];en[f'industry.tnd.help.{name}.{row}']=help_en[i][row]
    fr['guide.tnd.tab.6']='Observatoire';en['guide.tnd.tab.6']='Observatory'
    guide_fr=['Fabriquez la boussole du Premier Silence avec un processeur de sécurité, une boussole, un filament, une auralite et une noctilite. Utilisez-la dans Vestige : elle indique le seuil le plus proche. Le complexe apparaît dans les nouveaux chunks. Préparez équipement, nourriture et soins du familier pour une longue expédition de fin de progression.', 'Chaque station montre votre itinéraire et son objectif. Les lignes au sol conduisent aux seuils. Lire les stèles, accorder les disques, couper les relais, rejoindre les lumières et vaincre les gardiens ouvre le chemin. Les étapes accomplies restent sauvegardées. Rejoindre permet de retourner aux stations déjà accordées ; Parvis permet de quitter le complexe.', 'Chaque gardien majeur donne un cœur axiomatique à ses participants, une seule fois. Construisez raffinerie, creuset et assembleur : leurs huit charpentes de socle, entrées et coûts figurent au manuel industriel. Une mémoire de l’Observatoire renforce une fois le familier de niveau 20 ou plus. Les ancres des salles de repos acceptent votre prisme de retour.']
    guide_en=['Craft the First Silence Compass with a security processor, compass, filament, auralite and noctilite. Use it in Vestige to find the nearest threshold. The complex appears in new chunks. Bring equipment, food and companion care for a long late-game expedition.', 'Each station shows the route and current objective. Floor inlays guide you to thresholds. Read steles, align dials, disable relays, reach the lights and defeat guardians to open the way. Completed stages persist. Travel returns to attuned stations; Forecourt leaves the complex.', 'Each major guardian grants its participants one axiomatic core, once. Build a refinery, crucible and assembler: their eight-frame foundations, inputs and costs are in the industrial manual. An Observatory Memory upgrades a level-20 companion once. Rest-room anchors accept your recall prism.']
    for i in range(3):fr[f'guide.tnd.page.6.{i}']=guide_fr[i];en[f'guide.tnd.page.6.{i}']=guide_en[i]
    fr['assault.tnd.protocols']='Protocoles installés : %s / 4';en['assault.tnd.protocols']='Installed protocols: %s / 4'
    for n in SOUNDS:fr['subtitles.tnd.'+n]={'observatory_drone':'Une vibration traverse les murs','observatory_threshold':'Un seuil s’éveille','observatory_relay':'Un accord répond','observatory_warning':'Une charge résonne','observatory_impact':'La pierre résonne','observatory_victory':'Un sceau se relâche','observatory_core':'Un cœur change de rythme'}[n];en['subtitles.tnd.'+n]={'observatory_drone':'A vibration crosses the walls','observatory_threshold':'A threshold awakens','observatory_relay':'A tone answers','observatory_warning':'A charge resonates','observatory_impact':'Stone resounds','observatory_victory':'A seal releases','observatory_core':'A core changes rhythm'}[n]
