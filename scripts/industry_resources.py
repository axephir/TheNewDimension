"""Industrial survival progression, original 16px machinery and bilingual construction instructions."""
import random
MACHINES=['coal_dynamo','solar_controller','industrial_accumulator','electric_crusher','electric_kiln','industrial_quarry','cultivator','rancher','pastoral_station']
BLOCKS=MACHINES+['industrial_frame','energy_cable','solar_panel']
ITEMS=['control_circuit','copper_coil','diamond_drill_bit','iron_grit','copper_grit','gold_grit']
TESTS=['industry_quarry_hopper','industry_energy_guards','industry_farm_guards','industry_ranch_cap','industry_milking','industry_energy','industry_generation','industry_processing','industry_hoppers','industry_quarry','industry_quarry_guards','industry_farming','industry_ranching','industry_pasture','industry_recovery','industry_solar']
NAMES=[('Dynamo à charbon','Coal Dynamo'),('Contrôleur solaire','Solar Controller'),('Accumulateur industriel','Industrial Accumulator'),('Broyeur électrique','Electric Crusher'),('Four électrique','Electric Kiln'),('Carrière industrielle','Industrial Quarry'),('Cultivateur','Cultivator'),('Éleveur automatique','Rancher'),('Station pastorale','Pastoral Station'),('Charpente industrielle','Industrial Frame'),('Câble énergétique','Energy Cable'),('Panneau solaire','Solar Panel'),('Circuit de commande','Control Circuit'),('Bobine de cuivre','Copper Coil'),('Trépan diamanté','Diamond Drill Bit'),('Granulés de fer','Iron Grit'),('Granulés de cuivre','Copper Grit'),('Granulés d’or','Gold Grit')]

def art(name):
    rng=random.Random(name);out=[];accent=(173,118,57) if name not in ['cultivator','rancher','pastoral_station','solar_panel','solar_controller'] else (83,139,109) if name!='solar_panel' else (57,84,134)
    for y in range(16):
        for x in range(16):
            n=rng.randint(-8,8);c=tuple(max(0,v+n) for v in (61,70,72))
            if name in ITEMS:
                c=(0,0,0,0)
                if name.endswith('_grit') and ((x-8)**2+(y-9)**2<42 or (x in [3,11] and y in [3,4])):c={'iron_grit':(196,163,142),'copper_grit':(199,116,70),'gold_grit':(236,191,69)}[name]
                elif name=='control_circuit' and 2<x<13 and 2<y<13:c=(44,96,64) if (x+y)%3 else (208,165,74)
                elif name=='copper_coil' and 2<x<13 and 2<y<13 and ((x-8)**2+(y-8)**2>9):c=(190+min(n,0),110,59) if x%3 else (234,151,87)
                elif name=='diamond_drill_bit' and abs(x-8)<(15-y)//2+1 and y>2:c=(83,189,183) if y<8 else (118,130,134)
            elif name=='industrial_frame':
                if x<3 or x>12 or y<3 or y>12 or abs(x-y)<2 or abs(15-x-y)<2:c=(112+n,122+n,119+n)
                else:c=(36+n,43+n,45+n)
                if x in [1,14] and y in [1,14]:c=(201,166,93)
            elif name=='solar_panel':c=(35+n,53+n,100+n) if x%5 and y%5 else (137,155,180)
            elif name=='energy_cable':
                c=(50+n,56+n,58+n) if 4<x<11 else (116+n,71+n,40+n)
                if y%5==0:c=(176,122,64)
            else:
                if x<2 or x>13 or y<2 or y>13:c=(105+n,115+n,113+n)
                if y in [3,12]:c=accent
                if 4<=x<=11 and 5<=y<=10:c=(24+n,34+n,36+n)
                if (x,y) in [(2,2),(13,2),(2,13),(13,13)]:c=(192,179,140)
                if 5<=x<=10 and y==7:c=accent
                if name=='industrial_accumulator' and 5<=x<=10 and y in [5,7,9]:c=(182,151,60)
                if name=='electric_crusher' and y in [5,9] and 4<=x<=11:c=(147,157,154) if x%2 else (29,39,41)
                if name=='electric_kiln' and 5<=x<=10 and 6<=y<=9:c=(134+n,69+n,35+n)
            out.append(c if len(c)==4 else (*c,255))
    return out

def content(write,png,assets,data):
    for name in BLOCKS+ITEMS:png(assets/f'textures/{"item" if name in ITEMS else "block"}/{name}.png',art(name))
    def box(a,b,t):return {'from':a,'to':b,'faces':{d:{'texture':t} for d in ['north','south','east','west','up','down']}}
    for name in MACHINES:
        write(assets/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':f'tnd:block/{name}'+('_active' if lit else ''),'y':angle} for f,angle in [('north',0),('east',90),('south',180),('west',270)] for lit in [False,True]}})
        for lit in [False,True]:
            elements=[box([0,0,0],[16,2,16],'#frame'),box([1,2,1],[15,14,15],'#body'),box([0,14,0],[16,16,16],'#frame'),box([3,4,0],[13,11,1],'#body')]
            if name=='coal_dynamo':elements+=[box([11,16,11],[15,21,15],'#frame')]
            if name=='industrial_quarry':elements+=[box([5,16,5],[11,21,11],'#frame')]
            if lit:elements+=[box([6,5,-.05],[10,7,.05],'#light')]
            write(assets/f'models/block/{name}{"_active" if lit else ""}.json',{'textures':{'body':f'tnd:block/{name}','frame':'tnd:block/industrial_frame','light':'tnd:block/amber_lamp','particle':f'tnd:block/{name}'},'elements':elements})
    # A stout insulated conduit remains readable in a vanilla 16px palette.
    write(assets/'models/block/energy_cable.json',{'textures':{'cable':'tnd:block/energy_cable','particle':'tnd:block/energy_cable'},'elements':[box([5,0,5],[11,16,11],'#cable'),box([0,5,5],[16,11,11],'#cable'),box([5,5,0],[11,11,16],'#cable')]})
    recipes={
      'industrial_frame':(['ICI','CSC','ICI'],{'I':'minecraft:iron_ingot','C':'minecraft:copper_ingot','S':'minecraft:smooth_stone'},2),
      'control_circuit':(['RGR','QAQ','RGR'],{'R':'minecraft:redstone','G':'minecraft:gold_ingot','Q':'minecraft:quartz','A':'minecraft:amethyst_shard'},2),
      'copper_coil':([' C ','CIC',' C '],{'C':'minecraft:copper_ingot','I':'minecraft:iron_ingot'},2),
      'diamond_drill_bit':([' D ','DTD',' T '],{'D':'minecraft:diamond','T':'tnd:tempered_alloy'},4),
      'energy_cable':(['RRR','CCC','RRR'],{'R':'minecraft:dried_kelp','C':'minecraft:copper_ingot'},8),
      'solar_panel':(['GGG','LLL','QQQ'],{'G':'minecraft:glass','L':'minecraft:lapis_lazuli','Q':'minecraft:quartz'},2),
      'coal_dynamo':(['FCF','OBO','FRF'],{'F':'tnd:industrial_frame','C':'tnd:control_circuit','O':'tnd:copper_coil','B':'minecraft:blast_furnace','R':'minecraft:redstone_block'},1),
      'solar_controller':(['PAP','OCO','FRF'],{'P':'tnd:solar_panel','A':'tnd:auralite_ingot','O':'tnd:copper_coil','C':'tnd:control_circuit','F':'tnd:industrial_frame','R':'minecraft:redstone_block'},1),
      'industrial_accumulator':(['CRC','FOF','CRC'],{'C':'minecraft:copper_block','R':'minecraft:redstone_block','F':'tnd:industrial_frame','O':'tnd:control_circuit'},1),
      'electric_crusher':(['TPT','FCF','OHO'],{'T':'tnd:tempered_alloy','P':'minecraft:diamond_pickaxe','F':'tnd:industrial_frame','C':'tnd:control_circuit','O':'tnd:copper_coil','H':'minecraft:hopper'},1),
      'electric_kiln':(['BFB','OCO','BHB'],{'B':'minecraft:nether_bricks','F':'minecraft:blast_furnace','O':'tnd:copper_coil','C':'tnd:control_circuit','H':'tnd:industrial_frame'},1),
      'industrial_quarry':(['UDU','TNT','FCF'],{'U':'tnd:umbra_alloy','D':'minecraft:diamond_block','T':'tnd:tempered_alloy','N':'minecraft:netherite_ingot','F':'tnd:industrial_frame','C':'tnd:control_circuit'},1),
      'cultivator':(['IHI','FCF','ODO'],{'I':'minecraft:iron_block','H':'minecraft:iron_hoe','F':'tnd:industrial_frame','C':'tnd:control_circuit','O':'tnd:copper_coil','D':'minecraft:dispenser'},1),
      'rancher':(['HBH','FCF','ODO'],{'H':'minecraft:hay_block','B':'minecraft:golden_carrot','F':'tnd:industrial_frame','C':'tnd:control_circuit','O':'tnd:copper_coil','D':'minecraft:dispenser'},1),
      'pastoral_station':(['SHS','FCF','OBO'],{'S':'minecraft:shears','H':'minecraft:hopper','F':'tnd:industrial_frame','C':'tnd:control_circuit','O':'tnd:copper_coil','B':'minecraft:bucket'},1),
    }
    for name,(pattern,key,count) in recipes.items():write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'tnd:'+name,'count':count}})
    for raw,made in [('iron_grit','iron_ingot'),('copper_grit','copper_ingot'),('gold_grit','gold_ingot')]:
        for kind,ticks in [('smelting',200),('blasting',100)]:write(data/f'recipe/{raw}_{kind}.json',{'type':'minecraft:'+kind,'ingredient':'tnd:'+raw,'result':{'id':'minecraft:'+made},'experience':0,'cookingtime':ticks})
    write(data/'advancement/recipes/industry_knowledge.json',{'criteria':{'copper':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:copper_ingot'}]}}},'requirements':[['copper']],'rewards':{'recipes':['tnd:'+name for name in recipes]}})
    write(data/'advancement/industrial_age.json',{'parent':'tnd:expedition/tempered','display':{'icon':{'id':'tnd:coal_dynamo'},'title':{'translate':'advancement.tnd.industrial_age'},'description':{'translate':'advancement.tnd.industrial_age.description'},'frame':'goal','show_toast':True,'announce_to_chat':False},'criteria':{'machine':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:coal_dynamo'}]}}},'requirements':[['machine']]})
    for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+name,'structure':'tnd:test_habitat','max_ticks':700,'required':True})

def languages(fr,en):
    for name,(f,e) in zip(BLOCKS+ITEMS,NAMES):fr[f'{"block" if name in BLOCKS else "item"}.tnd.{name}']=f;en[f'{"block" if name in BLOCKS else "item"}.tnd.{name}']=e
    pairs={'industry.tnd.guide':('Manuel industriel','Industrial Handbook'),'industry.tnd.open':('Industrie','Industry'),'industry.tnd.input':('Entrées','Inputs'),'industry.tnd.arm':('Démarrer le forage','Arm excavation'),'industry.tnd.stop':('Suspendre le forage','Suspend excavation'),'industry.tnd.drill':('%s blocs · prof. %s','%s blocks · depth %s'),'advancement.tnd.industrial_age':('L’âge industriel','The Industrial Age'),'advancement.tnd.industrial_age.description':('Construisez votre première dynamo à charbon.','Build your first coal dynamo.')}
    pairs.update({
      'industry.tnd.bit':('%s blocs','%s blocks'),'industry.tnd.bit_wait':('Trépan à charger','Load a drill bit'),'industry.tnd.depth':('Profondeur : %s','Depth: %s'),
      'industry.tnd.area.solar_controller':('Centrale 3 × 3','3 × 3 array'),'industry.tnd.area.industrial_accumulator':('Réserve FE','FE reserve'),'industry.tnd.area.cultivator':('Zone 9 × 9','9 × 9 bed'),
      'industry.tnd.detail.coal_dynamo':('32 FE/t. 8 000 FE par charbon.','32 FE/t. 8,000 FE per coal.'),
      'industry.tnd.detail.solar_controller':('8 panneaux, ciel ouvert, soleil. 32 FE/t.','8 panels, open sky, sunshine. 32 FE/t.'),
      'industry.tnd.detail.industrial_accumulator':('Les machines reçoivent la réserve par câble.','Machines receive stored energy through cables.'),
      'industry.tnd.detail.rancher':('2 aliments, 1 couple. Rayon : 5 blocs.','2 food items, 1 pair. Radius: 5 blocks.')})
    states=[('Prête','Ready'),('Ajouter les consommables','Add consumables'),('Stockage saturé','Storage full'),('Pause redstone','Redstone pause'),('Installation en activité','Installation operating'),('Énergie insuffisante','Insufficient energy'),('Montage incomplet : voir ?','Incomplete assembly: see ?'),('Aucune cible disponible','No available target'),('En attente du soleil','Waiting for sunshine'),('Nécessite l’Overworld','Requires the Overworld'),('Forage désarmé','Excavation disarmed'),('Zone entièrement explorée','Area fully explored'),('Zone non chargée','Area not loaded')]
    for i,p in enumerate(states):pairs[f'industry.tnd.state.{i}']=p
    help={
    'coal_dynamo':[
      ('1 charbon ou charbon de bois = 8 000 FE. Production : 32 FE/t. Le combustible entamé est conservé si le réseau est plein.','1 coal or charcoal = 8,000 FE. Output: 32 FE/t. Remaining fuel is retained when the network is full.'),
      ('Reliez les blocs par des câbles. Les consommateurs sont prioritaires ; l’accumulateur reçoit le surplus.','Connect blocks with energy cables. Consumers take priority; accumulators store the surplus.'),
      ('Entrées par le dessus ou les côtés, sorties par dessous. Signal redstone = pause. Travail seulement en zone chargée.','Inputs above or on sides, outputs below. Redstone signal pauses. Operates only in loaded chunks.')],
    'solar_controller':[
      ('Placez 8 panneaux autour du contrôleur, à sa hauteur : un carré 3 × 3 complet, contrôleur au centre.','Place 8 panels around the controller at its height: a complete 3 × 3 square, controller in the centre.'),
      ('Tout le carré doit voir le ciel. Overworld, soleil et temps sec : 32 FE/t. Aucune production nocturne.','The entire square must see the sky. Overworld, daylight and dry weather: 32 FE/t. No nighttime output.'),
      ('L’auralite exige une expédition dans Vestige. Accumulez l’énergie pendant le jour pour alimenter l’usine la nuit.','Auralite requires a Vestige expedition. Store daytime production to power your factory at night.')],
    'industrial_accumulator':[
      ('Stocke 1 000 000 FE. Distribue jusqu’à 256 FE/t aux consommateurs reliés par câble.','Stores 1,000,000 FE. Distributes up to 256 FE/t to consumers connected by cable.'),
      ('Câbles : réseau de 128 positions explorées maximum par source. Les batteries ne se rechargent pas entre elles.','Cables: at most 128 explored positions per source. Batteries never recharge one another.'),
      ('Comparateur = niveau de charge. Signal redstone = suspension des sorties. Recharge encore possible.','Comparator = charge level. Redstone signal suspends output. Charging remains possible.')],
    'electric_crusher':[
      ('Construisez 8 charpentes sous la machine : bordure d’un carré 3 × 3, centre libre pour un entonnoir.','Build 8 frames beneath the machine: the border of a 3 × 3 square, centre free for a hopper.'),
      ('1 fer, cuivre ou or brut → 2 granulés. 2 400 FE et 4 s. Les granulés se cuisent au four.','1 raw iron, copper or gold → 2 grit. 2,400 FE and 4 s. Smelt the grit in a furnace.'),
      ('Les lingots et granulés ne peuvent pas être rebroyés. La forge résonante garde sa filière à poussière d’écho.','Ingots and grit cannot be crushed again. The resonance forge keeps its echo-dust processing route.')],
    'electric_kiln':[
      ('Même socle de 8 charpentes en 3 × 3 que le broyeur. Connectez une source d’énergie ou un accumulateur.','Same 8-frame 3 × 3 foundation as the crusher. Connect an energy source or accumulator.'),
      ('1 200 FE, 3 s par objet. Métaux bruts et granulés, sable, pavés, pierre, argile et netherrack.','1,200 FE, 3 s per item. Raw metals and grit, sand, cobblestone, stone, clay and netherrack.'),
      ('Sans combustible ni XP. Le bois ne donne pas de charbon ici : pas de boucle énergétique gratuite.','No fuel or XP. Wood does not produce charcoal here: no free energy loop.')],
    'industrial_quarry':[
      ('37 charpentes : bordure 7 × 7 un bloc plus bas (24), 4 piliers hauts de 3 aux angles (12), 1 au-dessus du contrôleur.','37 frames: 7 × 7 border one block below (24), four 3-high corner pillars (12), 1 above the controller.'),
      ('Armez le forage. Zone 7 × 7 centrée, dès 2 blocs plus bas jusqu’au fond. Retire réellement roche, terre et minerais !','Arm excavation. Centred 7 × 7 area, from 2 blocks below to bedrock. Actually removes rock, soil and ores!'),
      ('1 trépan = 1 024 blocs. 480 FE/roche ; 4 800 FE/minerai. Jamais de coffre, construction exotique ou chunk forcé.','1 drill bit = 1,024 blocks. 480 FE/rock; 4,800 FE/ore. Never mines chests, exotic building blocks or forced chunks.')],
    'cultivator':[
      ('Récolte un plant mûr toutes les 2 s dans un carré 9 × 9 à sa hauteur. Blé, carottes, pommes de terre, betteraves.','Harvests one mature crop every 2 s in a 9 × 9 square at its height. Wheat, carrots, potatoes, beetroot.'),
      ('800 FE par récolte. Garde une graine ou un légume pour replanter. Préparez et irriguez vous-même les terres.','800 FE per harvest. Retains one seed or vegetable to replant. Prepare and irrigate farmland yourself.'),
      ('Respecte la croissance naturelle ; aucun XP automatique. Exportez les récoltes par entonnoir vers l’éleveur.','Respects natural growth; no automatic XP. Export crops through hoppers into the rancher.')],
    'rancher':[
      ('Deux aliments compatibles et 1 600 FE mettent un couple adulte en amour en 5 s. Rayon : 5 blocs, hauteur ±2.','Two compatible food items and 1,600 FE put an adult pair in love in 5 s. Range: 5 blocks, height ±2.'),
      ('Animaux de même espèce, disponibles et proches. Leur IA doit pouvoir les réunir dans un enclos accessible.','Animals must be the same species, available and nearby. Their AI must be able to meet in an accessible pen.'),
      ('Délais vanilla conservés. Arrêt à 16 animaux dans la zone : agrandissez ou répartissez vos enclos.','Vanilla cooldowns remain. Stops at 16 animals in the area: expand or divide your pens.')],
    'pastoral_station':[
      ('Cisailles dans l’entrée haute : tond les moutons adultes disponibles pour 600 FE et use l’outil.','Shears in the upper input: shears eligible adult sheep for 600 FE and wears the tool.'),
      ('Seaux dans l’autre entrée : trait une vache adulte pour 400 FE. Délai partagé : 60 s par vache.','Buckets in the other input: milks an adult cow for 400 FE. Shared cooldown: 60 s per cow.'),
      ('Ramasse les objets au sol à 5 blocs pour 120 FE par pile, notamment laine et œufs réellement pondus.','Collects dropped items within 5 blocks for 120 FE per stack, including wool and actually laid eggs.')]
    }
    for name,rows in help.items():
        for i,pair in enumerate(rows):pairs[f'industry.tnd.help.{name}.{i}']=pair
    for name in BLOCKS:pairs['tooltip.tnd.'+name]=('Industrie : instructions dans le guide de l’inventaire et le bouton ? de la machine.','Industry: instructions in the inventory handbook and the machine’s ? button.')
    pairs['tooltip.tnd.diamond_drill_bit']=('Un trépan permet 1 024 blocs de forage, sauvegardés dans la carrière.','One drill bit enables 1,024 mined blocks, saved in the quarry.')
    for k,(f,e) in pairs.items():fr[k]=f;en[k]=e
