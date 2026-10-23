"""Original foundry mechanisms, original character atlases and deliberate equipment tempering."""
import json,math,random
BLOCKS=['foundry_valve','foundry_rotor','foundry_press','mnemonic_tempering_forge']
ITEMS=['foundry_heart']
ENTITIES=['foundry_warden','foundry_arbalist','foundry_mender','first_founder','furnace_regulator']
SOUNDS=['foundry_valve','foundry_ignite','foundry_work','foundry_release','foundry_temper']
TESTS=['foundry_circuit','foundry_generation','foundry_route','foundry_restore','foundry_bosses','foundry_patterns','foundry_shortcut','tempering_process','tempering_guards','tempering_restore','tempering_energy']
def content(write,png,assets,data):
    def box(a,b,tex):
        w,h,d=[b[i]-a[i] for i in range(3)]
        return {'from':a,'to':b,'faces':{side:{'texture':tex,'uv':[0,0,min(16,w if side in ['north','south','up','down'] else d),min(16,d if side in ['up','down'] else h)]} for side in ['north','south','east','west','up','down']}}
    gauge=[]
    for y in range(16):
        for x in range(16):
            c=(32,38,42) if (x+y)%3 else (40,47,51)
            if x in [0,15] or y in [0,15]:c=(75,83,86)
            # 0 north, I east, II south, III west; actual pointer position uses the same order.
            north=(5<=x<=9 and y in [1,4]) or (x in [5,9] and 1<=y<=4)
            east=x==13 and 6<=y<=9
            south=x in [6,9] and 12<=y<=14
            west=x in [1,3,5] and 6<=y<=9
            if north or east or south or west:c=(218,199,145)
            gauge.append((*c,255))
    png(assets/'textures/block/foundry_valve_plate.png',gauge)
    tex={'gauge':'tnd:block/foundry_valve_plate','body':'tnd:block/obsidian_relief','frame':'tnd:block/obsidian_ribbed','light':'tnd:block/amber_seal','dark':'tnd:block/obsidian_metal','particle':'tnd:block/obsidian_metal'}
    for name in BLOCKS[:3]:
        for lit in [False,True]:
            for step in range(4):
                t=dict(tex);t['light']='tnd:block/'+('amber_seal' if lit else 'silent_conduit')
                e=[box([1,0,1],[15,2,15],'#frame')]
                if name=='foundry_valve':
                    e.append({'from':[0,10.01,0],'to':[16,10.02,16],'faces':{'up':{'texture':'#gauge','uv':[0,0,16,16]}}})
                    e+=[box([6,2,6],[10,9,10],'#body'),box([3,9,3],[13,11,13],'#frame'),box([7,11,2],[9,13,14],'#light'),box([2,11,7],[14,13,9],'#light'),box([6,13,6],[10,15,10],'#body')]
                    # Offset pointer makes the four settings visually distinct from every viewing angle.
                    e.append(box([[6,14,1],[11,14,6],[6,14,11],[1,14,6]][step],[[10,16,5],[15,16,10],[10,16,15],[5,16,10]][step],'#light'))
                elif name=='foundry_rotor':
                    e.append({'from':[0,10.01,0],'to':[16,10.02,16],'faces':{'up':{'texture':'#gauge','uv':[0,0,16,16]}}})
                    e+=[box([6,2,6],[10,16,10],'#body'),box([3,6,3],[13,12,13],'#frame')]
                    for n in range(2):
                        blade=box([7,3,6],[9,13,10],'#light');blade['rotation']={'origin':[8,8,8],'axis':'z','angle':[-45,-22.5,0,22.5][step],'rescale':False}
                        if n<2:blade['from']=[3,7,6] if n==0 else [7,3,6];blade['to']=[13,9,10] if n==0 else [9,13,10];e.append(blade)
                    e+=[box([6,6,0],[10,10,4],'#body'),box([7,7,-2],[9,9,1],'#light')]
                else:
                    up=[0,2,5,2][step];e+=[box([2,2,2],[14,4,14],'#body'),box([1,4,2],[3,16,4],'#frame'),box([13,4,12],[15,16,14],'#frame'),box([1,15,2],[15,17,14],'#frame'),box([6,9+up,6],[10,17,10],'#body'),box([3,6+up,3],[13,9+up,13],'#frame'),box([5,5,5],[11,6,11],'#light')]
                write(assets/f'models/block/{name}_{step}_{int(lit)}.json',{'textures':t,'elements':e})
        write(assets/f'blockstates/{name}.json',{'variants':{f'lit={str(lit).lower()},step={s}':{'model':f'tnd:block/{name}_{s}_{int(lit)}'} for lit in [False,True] for s in range(4)}})
        write(assets/f'models/block/{name}.json',{'parent':f'tnd:block/{name}_0_1'})
    name=BLOCKS[3]
    for lit in [False,True]:
        t=dict(tex);t['light']='tnd:block/'+('luminous_script' if lit else 'memory_lattice')
        e=[box([0,0,0],[16,3,16],'#frame'),box([2,3,2],[14,7,14],'#body'),box([3,7,3],[13,8,13],'#light'),box([1,3,12],[3,20,14],'#frame'),box([13,3,12],[15,20,14],'#frame'),box([1,18,12],[15,21,14],'#frame'),box([6,12,6],[10,20,10],'#body'),box([3,10,3],[13,13,13],'#frame'),box([5,9,0],[11,11,1],'#light')]
        write(assets/f'models/block/{name}_{int(lit)}.json',{'textures':t,'elements':e})
    write(assets/f'models/block/{name}.json',{'parent':f'tnd:block/{name}_0'})
    write(assets/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':f'tnd:block/{name}_{int(lit)}','y':r} for f,r in [('north',0),('east',90),('south',180),('west',270)] for lit in [False,True]}})
    for name in BLOCKS:
        write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
    for name in ['mnemonic_tempering_forge']:
        write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    # The unique heart has its own native-pixel silhouette: black iron cage, amber molten centre.
    p=[(0,0,0,0)]*256;rng=random.Random('first-foundry-heart')
    for y in range(1,15):
        for x in range(1,15):
            dx,dy=x-7.5,y-7.5;r=abs(dx)+abs(dy)
            if r>8:continue
            noise=rng.randint(-9,9)
            c=(24+noise,29+noise,34+noise) if r>6 else (84+noise,91+noise,94+noise) if (x+y)%4==0 else (142+noise,89+noise,35+noise)
            if abs(dx)<2.5 and abs(dy)<4:c=(240+noise//2,181+noise,86+noise)
            if x in (4,11) and 3<=y<=12:c=(37,43,48)
            if (x,y) in [(5,2),(10,3),(3,6),(12,10)]:c=(181,195,193)
            p[y*16+x]=(*c,255)
    png(assets/'textures/item/foundry_heart.png',p)
    write(assets/'models/item/foundry_heart.json',{'parent':'minecraft:item/generated','textures':{'layer0':'tnd:item/foundry_heart'}});write(assets/'items/foundry_heart.json',{'model':{'type':'minecraft:model','model':'tnd:item/foundry_heart'}})
    for k,name in enumerate(ENTITIES):
        for opened in [False,True]:
            rng=random.Random(name);atlas=[]
            for y in range(64):
                for x in range(64):
                    n=rng.randint(-6,6);c=(30+n,35+n,39+n)
                    if x%8 in [0,1] or y%8==0:c=(66+n,73+n,78+n)
                    if y>=32 and y%8 in [1,2]:c=(99+n,103+n,101+n)
                    if (x*3+y*7)%43==0:c=(115+n,105+n,82+n)
                    if x>=48 and y<16:c=(245,224,162) if opened else (219,157,69) if k in [0,3] else (112,197,202)
                    atlas.append((*c,255))
            png(assets/f'textures/entity/{name}{"_open" if opened else ""}.png',atlas,64,64)
        color=(242,176,76) if k in [0,3] else (129,219,218)
        png(assets/f'textures/entity/{name}_glow.png',[(*color,255) if x>=48 and y<16 else (0,0,0,0) for y in range(64) for x in range(64)],64,64)
        egg=[(*((57,65,72) if (x+y)%3 else color),255) if ((x-7.5)/5)**2+((y-7.5)/6.5)**2<=1 else (0,0,0,0) for y in range(16) for x in range(16)]
        png(assets/f'textures/item/{name}_spawn_egg.png',egg);write(assets/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'tnd:item/{name}_spawn_egg'}});write(assets/f'items/{name}_spawn_egg.json',{'model':{'type':'minecraft:model','model':f'tnd:item/{name}_spawn_egg'}})
        pools=[] if k>=3 else [{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:mnemonic_shard','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]}]
        write(data/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
    write(data/'recipe/mnemonic_tempering_forge.json',{'type':'minecraft:crafting_shaped','pattern':['NCN','UHU','FOF'],'key':{'N':'tnd:nocturne_ingot','C':'tnd:mnemonic_circuit','U':'tnd:umbra_alloy','H':'tnd:foundry_heart','F':'tnd:industrial_frame','O':'minecraft:netherite_ingot'},'result':{'id':'tnd:mnemonic_tempering_forge','count':1}})
    write(data/'advancement/recipes/foundry_tempering.json',{'criteria':{'heart':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:foundry_heart'}]}}},'requirements':[['heart']],'rewards':{'recipes':['tnd:mnemonic_tempering_forge']}})
    tag=data.parent/'minecraft/tags/block/mineable/pickaxe.json';v=json.loads(tag.read_text());v['values']=list(dict.fromkeys(v['values']+['tnd:mnemonic_tempering_forge']));write(tag,v)
    for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','function':'tnd:'+name,'environment':'minecraft:default','structure':'tnd:test_habitat','max_ticks':1600,'required':True})
def languages(fr,en):
    def pair(key,f,e):fr[key]=f;en[key]=e
    names=[('foundry_valve','Vanne de la fournaise','Furnace Valve'),('foundry_rotor','Rotor de mémoire','Memory Rotor'),('foundry_press','Presse des anciens','Ancient Press'),('mnemonic_tempering_forge','Forge de trempe mnémonique','Mnemonic Tempering Forge')]
    for name,f,e in names:pair('block.tnd.'+name,f,e)
    pair('item.tnd.foundry_heart','Cœur de la première fournaise','Heart of the First Furnace')
    for name,f,e in zip(ENTITIES,['Garde de fournaise','Arbalétrier de la forge','Ouvrier de maintenance','Le Premier Fondeur','Le Régulateur'],['Furnace Warden','Foundry Arbalist','Maintenance Worker','The First Founder','The Regulator']):pair('entity.tnd.'+name,f,e);pair('item.tnd.'+name+'_spawn_egg','Œuf : '+f,f+' Spawn Egg')
    pair('observatory.tnd.foundry.act','La Forge du Premier Feu','Forge of the First Fire')
    rooms=[('Vestibule des Soufflets','Vestibule of Bellows'),('Halle des Marteaux','Hall of Hammers'),('Distribution Prismatique','Prismatic Distribution'),('Batteries de Garde','Watch Batteries'),('Puits du Premier Feu','Well of the First Fire'),('Chambre des Scories','Chamber of Slag'),('Enclumes de Veille','Watchful Anvils'),('Cœur de la Fournaise','Heart of the Furnace')]
    objectives=[('Règle les trois vannes sur II et lis les trois stèles. Le premier souffle attend.','Set the three valves to II and read the three steles. The first breath waits.'),('Vaincs les six gardes. Les ouvriers réparent leurs alliés : surveille leurs faisceaux.','Defeat six guards. Workers repair their allies: watch their beams.'),('Accorde les quatre prismes. Le panneau conserve les valeurs demandées et leur état réel.','Align the four prisms. The panel retains the target and current values.'),('Coupe les quatre relais et vaincs leurs gardes pour libérer la distribution.','Cut four relays and defeat their guards to release distribution.'),('Active le parcours et suis les quatre résonances sur les deux galeries. Sur ton chemin, règle les trois vannes sur II.','Start the route and follow four resonances on both galleries. On the way, set the three valves to II.'),('Suis les marques I, III et II des trois vannes. Retrouve aussi les trois échos.','Follow the I, III and II marks on the valves. Find the three echoes too.'),('Traverse trois vagues. Les presses et les ouvriers reprennent leur ouvrage.','Survive three waves. Presses and workers resume their task.'),('Affronte le Fondeur et le Régulateur. Les vannes ouvrent leurs cœurs ; leurs propres attaques peuvent les fragiliser.','Face the Founder and Regulator. Valves open their cores; their own attacks can weaken them.')]
    for i,((f,e),(fo,eo)) in enumerate(zip(rooms,objectives)):pair('observatory.tnd.foundry.room.'+str(i),f,e);pair('observatory.tnd.foundry.objective.'+str(i),fo,eo)
    pairs={
      'trace.0':('« Les marteaux frappent encore. Rien ne doit sortir. »','“The hammers still strike. Nothing must leave.”'),
      'trace.1':('« Nous avions demandé une copie. Le métal a refusé. »','“We had asked for a copy. The metal refused.”'),
      'trace.2':('« Le dernier souffle n’était pas le nôtre. »','“The final breath was not ours.”'),
      'echo.0':('« Ne compte plus les ouvriers. Compte les pas. »','“Stop counting the workers. Count the footsteps.”'),
      'echo.1':('« Il a gardé la chaleur pour quelqu’un. »','“It kept the warmth for someone.”'),
      'echo.2':('« Le Régulateur n’a jamais accepté le silence. »','“The Regulator never accepted the silence.”'),
      'seek_valves':('Les trois vannes attendent leur accord.','The three valves await alignment.'),'stable':('La fournaise reprend son souffle.','The furnace breathes again.'),'setting':('Vanne : %s / III','Valve: %s / III'),'valves':('Vannes accordées : %s / 3','Aligned valves: %s / 3'),
      'shortcut':('Passage supérieur : %s / 10','Upper shortcut: %s / 10'),
      'vent_idle':('Le cœur demeure silencieux.','The core remains silent.'),'vent_wait':('Pression : encore %s s','Pressure: %s s remaining'),'vent_open':('Les cœurs sont ouverts.','The cores are open.'),'vent':('Purge disponible dans : %s s','Vent ready in: %s s'),
      'crossed':('Leur ouvrage se retourne contre eux.','Their own work turns against them.'),
      'hint.valve':('Clic droit · régler la vanne','Right-click · adjust the valve'),'hint.vent':('Clic droit · purger les cœurs','Right-click · vent the cores'),'hint.prism':('Clic droit · orienter le prisme','Right-click · turn the prism'),
      'attack.0':('SCEAU — quitte la marque','SEAL — leave the mark'),'attack.1':('ONDE — saute entre les anneaux','WAVE — jump across the rings'),'attack.3':('FAILLE — quitte les deux axes','FAULT — leave both axes'),'attack.4':('TRIPLE SCEAU — quitte les trois marques','TRIPLE SEAL — leave all three marks'),
      'attack.5':('MARTEAU — éloigne-toi de la double marque','HAMMER — leave the double mark'),'attack.6':('INCISION — quitte le couloir lumineux','INCISION — leave the bright lane')}
    for k,(f,e) in pairs.items():pair('observatory.tnd.foundry.'+k,f,e)
    ui={
      'cost':('80 000 FE · 8 éclats · 2 auralites · 10 s','80,000 FE · 8 shards · 2 auralite · 10 s'),'equipment':('Équipement','Equipment'),'result':('Résultat','Result'),'energy':('%s / 128 000 FE','%s / 128,000 FE'),
      'start':('Confirmer la trempe','Confirm tempering'),'cancel':('Annuler le travail','Cancel work'),'permanent':('Choix permanent','Permanent choice'),'one_choice':('Une seule trempe par arme ou outil.','One temper per weapon or tool.'),
      'choice.0':('Rupture','Rupture'),'choice.1':('Célérité','Swiftness'),'choice.2':('Ancrage','Anchoring'),
      'effect.0':('+12 % de dégâts au corps à corps. Actif en main principale. Choix permanent, exclusif.','+12% melee damage. Active in main hand. Permanent, exclusive choice.'),
      'effect.1':('+10 % de vitesse d’attaque. Actif en main principale. Choix permanent, exclusif.','+10% attack speed. Active in main hand. Permanent, exclusive choice.'),
      'effect.2':('+15 % de résistance au recul. Actif en main principale. Choix permanent, exclusif.','+15% knockback resistance. Active in main hand. Permanent, exclusive choice.'),
      'structure_help':('Socle : huit carreaux d’obsidienne en carré 3 × 3, une couche sous la machine. Garde le centre libre pour la sortie. À chaque coin : un pilier d’obsidienne au niveau de la machine, puis un sceau d’ambre au-dessus. Entrée d’équipement par le haut, réactifs sur les côtés, sortie par-dessous. Relie une alimentation FE. Choisis une trempe puis confirme. Nom, enchantements et usure sont conservés.','Build eight Ancient Obsidian Tiles in a 3 × 3 ring one block below the forge. Keep the centre free for output. At each corner: one Obsidian Pillar at machine level, then an Amber Seal above it. Equipment enters above, reagents at the sides, output below. Connect FE power. Choose a temper, then confirm. Name, enchantments and wear are preserved.')}
    states=[('Socle incomplet','Incomplete foundation'),('Socle incomplet','Incomplete foundation'),('Suspendu par redstone','Paused by redstone'),('Arme ou outil requis','Weapon or tool required'),('Choisis une trempe','Choose a temper'),('Réactifs insuffisants','Insufficient reagents'),('Récupère la sortie','Collect the output'),('Énergie insuffisante','Insufficient energy'),('Prête à confirmer','Ready to confirm'),('Trempe en cours…','Tempering…')]
    for i,v in enumerate(states):ui['status.'+str(i)]=v
    for k,(f,e) in ui.items():pair('gui.tnd.tempering.'+k,f,e)
    for name,f,e in zip(SOUNDS,['Une vanne ancienne tourne','La première fournaise respire','Les presses travaillent','Une purge libère les cœurs','Le métal reçoit sa trempe'],['An ancient valve turns','The first furnace breathes','Ancient presses work','A vent opens the cores','Metal receives its temper']):pair('subtitles.tnd.'+name,f,e)
    pair('tooltip.tnd.mnemonic_tempering_forge','Une seule empreinte demeure dans le métal.','Only one imprint remains in the metal.')
    fr['guide.tnd.page.6.1']+=' La Forge du Premier Feu attend après le Colosse. Ses vannes suivent les marques au sol. Le cœur de son duo permet de bâtir une forge de trempe ; un choix permanent accompagne chaque arme ou outil.'
    en['guide.tnd.page.6.1']+=' The Forge of the First Fire waits beyond the Colossus. Its valves follow floor marks. Its duo grants a heart for a tempering forge; each weapon or tool accepts one permanent choice.'
