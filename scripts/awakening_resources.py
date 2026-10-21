"""Complete, bilingual 0.9 survival resources; stable previous identifiers are untouched."""
import json
from awakening_art import BLOCKS,ITEMS,DECOR,ENTITIES,paint
SHAPES={f'{base}_{shape}':(base,shape) for base in ['citadel_bricks','umbra_tiles','conservatory_tiles'] for shape in ['stairs','slab']}
TESTS=['awakening_materials', 'awakening_ores', 'lithoguard_hit', 'lithoguard_dodge', 'lithoguard_patterns', 'stalker_hit', 'stalker_interrupt', 'awakened_loot', 'separator_processing', 'separator_hoppers', 'reaper_night', 'reaper_guards', 'reaper_save', 'awakening_equipment', 'gravitas_wave', 'citadel_layout', 'conservatory_layout']
SOUNDS=['lithoguard_wake','lithoguard_charge','lithoguard_impact','lithoguard_step','lithoguard_fall','stalker_charge','stalker_shot','stalker_hurt','stalker_fall','separator_complete','reaper_harvest','hammer_charge','hammer_impact','ruin_chime','expedition_complete','anchor_depart']

def content(write,png,assets,data):
    for name in ENTITIES:png(assets/f'textures/entity/{name}.png',paint(name))
    png(assets/'textures/entity/equipment/humanoid/umbra_armor.png',paint('umbra_armor'))
    write(assets/'equipment/umbra.json',{'layers':{'humanoid':[{'texture':'tnd:umbra_armor'}],'humanoid_leggings':[{'texture':'tnd:umbra_leggings_armor'}]}})
    # Equipment assets resolve their own layer-specific folders.
    png(assets/'textures/entity/equipment/humanoid_leggings/umbra_leggings_armor.png',paint('umbra_leggings_armor'))
    for name in ['auralite_pickaxe','auralite_blade','umbra_pickaxe','umbra_blade','gravitas_hammer']:
        write(assets/f'models/item/{name}.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'tnd:item/'+name}})
    def faces(t):return {d:{'texture':t} for d in ['north','south','east','west','up','down']}
    def box(a,b,t):return {'from':a,'to':b,'faces':faces(t)}
    write(assets/'models/block/harmonic_chime.json',{'textures':{'base':'tnd:block/gilded_pillar','crystal':'tnd:block/noctilite_ore','particle':'tnd:block/gilded_pillar'},'elements':[
        box([2,0,2],[14,3,14],'#base'),box([2,3,6],[4,15,10],'#base'),box([12,3,6],[14,15,10],'#base'),box([2,14,6],[14,16,10],'#base'),box([6,5,6],[10,12,10],'#crystal')]})
    for name in ['ore_separator','echo_reaper']:
        write(assets/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':f'tnd:block/{name}'+('_active' if lit else ''),'y':angle} for f,angle in [('north',0),('east',90),('south',180),('west',270)] for lit in [False,True]}})
        for lit in [False,True]:
            write(assets/('models/block/'+name+('_active.json' if lit else '.json')),{'parent':'minecraft:block/cube','textures':{'frame':'tnd:block/gilded_trim','body':f'tnd:block/{name}','top':'tnd:block/runic_lamp' if lit else 'tnd:block/latticed_alloy','particle':f'tnd:block/{name}'},'elements':[
                box([0,0,0],[16,3,16],'#frame'),box([1,3,1],[15,13,15],'#body'),box([0,13,0],[16,16,16],'#top')]})
    for ore,raw in [('auralite_ore','raw_auralite'),('noctilite_ore','raw_noctilite')]:
        write(data/f'loot_table/blocks/{ore}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[
            {'type':'minecraft:item','name':'tnd:'+ore,'conditions':[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]},
            {'type':'minecraft:item','name':'tnd:'+raw,'functions':[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'}]}]}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    write(data.parent/'c/tags/block/ores.json',{'values':['tnd:auralite_ore','tnd:noctilite_ore']})
    write(data/'tags/item/auric_repairs.json',{'values':['tnd:auralite_ingot']});write(data/'tags/item/umbra_repairs.json',{'values':['tnd:umbra_alloy']})
    write(data.parent/'minecraft/tags/block/needs_iron_tool.json',{'values':['tnd:auralite_ore','tnd:noctilite_ore','tnd:blue_ore','tnd:red_ore','tnd:green_ore','tnd:resonant_ore']})
    recipes={
        'ore_separator':(['TAT','RFR','TAT'],{'T':'tnd:tempered_alloy','A':'tnd:auralite_ingot','R':'tnd:resonant_shard','F':'minecraft:blast_furnace'},1),
        'echo_reaper':(['TGT','CHC','TAT'],{'T':'tnd:tempered_alloy','G':'tnd:glass_filament','C':'tnd:composite_crystal','H':'tnd:lithic_heart','A':'tnd:auralite_ingot'},1),
        'umbra_alloy':(['NTN','TGT','NTN'],{'N':'tnd:noctilite_crystal','T':'tnd:tempered_alloy','G':'tnd:glass_filament'},4),
        'auralite_pickaxe':(['AAA',' S ',' S '],{'A':'tnd:auralite_ingot','S':'minecraft:stick'},1),
        'auralite_blade':([' A ',' A ',' S '],{'A':'tnd:auralite_ingot','S':'minecraft:stick'},1),
        'umbra_pickaxe':(['AAA',' S ',' S '],{'A':'tnd:umbra_alloy','S':'minecraft:stick'},1),
        'umbra_blade':([' A ',' A ',' S '],{'A':'tnd:umbra_alloy','S':'minecraft:stick'},1),
        'gravitas_hammer':(['AAA',' H ',' S '],{'A':'tnd:umbra_alloy','H':'tnd:lithic_heart','S':'minecraft:stick'},1),
        'umbra_helmet':(['AAA','A A'],{'A':'tnd:umbra_alloy'},1),
        'umbra_chestplate':(['A A','AAA','AAA'],{'A':'tnd:umbra_alloy'},1),
        'umbra_leggings':(['AAA','A A','A A'],{'A':'tnd:umbra_alloy'},1),
        'umbra_boots':(['A A','A A'],{'A':'tnd:umbra_alloy'},1),
        'auralite_block':(['AAA','AAA','AAA'],{'A':'tnd:auralite_ingot'},1),
        'umbra_block':(['AAA','AAA','AAA'],{'A':'tnd:umbra_alloy'},1),
        'harmonic_chime':([' A ','ANA',' A '],{'A':'tnd:auralite_ingot','N':'tnd:noctilite_crystal'},1),
        'lithic_trophy':([' H ','AAA',' T '],{'H':'tnd:lithic_heart','A':'tnd:auralite_ingot','T':'tnd:tempered_alloy'},1),
    }
    for name,(pattern,key,count) in recipes.items():write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'tnd:'+name,'count':count}})
    for name,item in [('auralite_block','auralite_ingot'),('umbra_block','umbra_alloy')]:write(data/f'recipe/{item}_from_block.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:'+name],'result':{'id':'tnd:'+item,'count':9}})
    for raw,made in [('raw_auralite','auralite_ingot'),('auralite_grit','auralite_ingot'),('raw_noctilite','noctilite_crystal'),('noctilite_grit','noctilite_crystal')]:
        for kind,ticks in [('smelting',200),('blasting',100)]:write(data/f'recipe/{raw}_{kind}.json',{'type':'minecraft:'+kind,'ingredient':'tnd:'+raw,'result':{'id':'tnd:'+made},'experience':.5,'cookingtime':ticks})
    for name in DECOR:
        if name in ['auralite_block','umbra_block','lithic_trophy']:continue
        write(data/f'recipe/{name}.json',{'type':'minecraft:stonecutting','ingredient':'tnd:ancient_alloy' if name in ['gilded_pillar','gilded_trim','latticed_alloy','verdigris_panel'] else 'tnd:ashstone','result':{'id':'tnd:'+name,'count':1}})
    for name in SHAPES:
        base,shape=SHAPES[name];write(data/f'recipe/{name}.json',{'type':'minecraft:stonecutting','ingredient':'tnd:'+base,'result':{'id':'tnd:'+name,'count':2 if shape=='slab' else 1}})
    write(data/'advancement/recipes/awakening_knowledge.json',{'criteria':{'mineral':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['tnd:raw_auralite','tnd:raw_noctilite','tnd:ashstone','tnd:lithic_heart','tnd:glass_filament']}]}}},'requirements':[['mineral']],'rewards':{'recipes':['tnd:'+name for name in recipes]+['tnd:'+name for name in DECOR+list(SHAPES) if name not in recipes]}})
    for name,loot in [('lithoguard','lithic_heart'),('glass_stalker','glass_filament')]:
        write(data/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':[
            {'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:'+loot,'functions':[{'function':'minecraft:set_count','count':1 if name=='lithoguard' else {'type':'minecraft:uniform','min':1,'max':2}}]}]},
            {'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:raw_auralite' if name=='lithoguard' else 'tnd:noctilite_crystal','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]}]})
    for name,icon,label,criterion in [
        ('awakening_minerals','raw_auralite','Deux couleurs oubliées',{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['tnd:raw_auralite','tnd:raw_noctilite']}]}}),
        ('awakening_umbra','umbra_chestplate','Revêtu de la nuit',{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:umbra_chestplate'}]}}),
        ('awakening_hammer','gravitas_hammer','Faire trembler Vestige',{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:gravitas_hammer'}]}}),
        ('awakening_automation','echo_reaper','Le jardin se souvient',{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:echo_reaper'}]}})]:
        write(data/f'advancement/{name}.json',{'parent':'tnd:expedition/tempered','display':{'icon':{'id':'tnd:'+icon},'title':{'translate':'advancement.tnd.'+name},'description':{'translate':'advancement.tnd.'+name+'.description'},'frame':'goal','show_toast':True,'announce_to_chat':False},'criteria':{'earned':criterion},'requirements':[['earned']]})
    for biome in ['silent_expanse','fractured_fields','hollow_shelves','glass_woodland','ashen_basin','obsidian_dunes']:
        path=data/f'worldgen/biome/{biome}.json';obj=json.loads(path.read_text());spawns=obj['spawners']['monster']
        spawns.append({'type':'tnd:lithoguard','weight':5,'minCount':1,'maxCount':1})
        if biome in ['glass_woodland','hollow_shelves','fractured_fields']:spawns.append({'type':'tnd:glass_stalker','weight':9,'minCount':1,'maxCount':1})
        write(path,obj)

def languages(fr,en):
    names={
      'auralite_ore':('Minerai d’auralite','Auralite Ore'),'noctilite_ore':('Minerai de noctilite','Noctilite Ore'),
      'citadel_bricks':('Briques de citadelle','Citadel Bricks'),'carved_citadel_bricks':('Briques de citadelle sculptées','Carved Citadel Bricks'),'cracked_citadel_bricks':('Briques de citadelle fissurées','Cracked Citadel Bricks'),
      'gilded_pillar':('Pilier doré','Gilded Pillar'),'gilded_trim':('Corniche dorée','Gilded Trim'),'auralite_block':('Bloc d’auralite','Block of Auralite'),'umbra_block':('Bloc d’alliage ombral','Block of Umbra Alloy'),'umbra_tiles':('Carreaux ombraux','Umbra Tiles'),'etched_umbra':('Pierre ombrale gravée','Etched Umbra'),
      'crystal_mosaic':('Mosaïque cristalline','Crystal Mosaic'),'conservatory_tiles':('Carreaux de conservatoire','Conservatory Tiles'),'verdigris_panel':('Panneau vert-de-gris','Verdigris Panel'),'latticed_alloy':('Alliage ajouré','Latticed Alloy'),'runic_lamp':('Lampe runique','Runic Lamp'),'amber_lamp':('Lampe ambrée','Amber Lamp'),'violet_lamp':('Lampe violette','Violet Lamp'),'lithic_trophy':('Effigie lithique','Lithic Effigy'),
      'harmonic_chime':('Carillon harmonique','Harmonic Chime'),'ore_separator':('Séparateur de minerais','Ore Separator'),'echo_reaper':('Récolteur d’écho','Echo Reaper'),
      'raw_auralite':('Auralite brute','Raw Auralite'),'raw_noctilite':('Noctilite brute','Raw Noctilite'),'auralite_grit':('Granulés d’auralite','Auralite Grit'),'noctilite_grit':('Granulés de noctilite','Noctilite Grit'),'auralite_ingot':('Lingot d’auralite','Auralite Ingot'),'noctilite_crystal':('Cristal de noctilite','Noctilite Crystal'),'umbra_alloy':('Alliage ombral','Umbra Alloy'),'lithic_heart':('Cœur lithique','Lithic Heart'),'glass_filament':('Filament de verre','Glass Filament'),
      'auralite_pickaxe':('Pioche d’auralite','Auralite Pickaxe'),'auralite_blade':('Lame d’auralite','Auralite Blade'),'umbra_pickaxe':('Pioche ombrale','Umbra Pickaxe'),'umbra_blade':('Lame ombrale','Umbra Blade'),'gravitas_hammer':('Marteau de gravité','Gravitas Hammer'),'umbra_helmet':('Casque ombral','Umbra Helmet'),'umbra_chestplate':('Plastron ombral','Umbra Chestplate'),'umbra_leggings':('Jambières ombrales','Umbra Leggings'),'umbra_boots':('Bottes ombrales','Umbra Boots'),
      'lithoguard_spawn_egg':('Œuf de Lithogarde','Lithoguard Spawn Egg'),'glass_stalker_spawn_egg':('Œuf de Traqueur de verre','Glass Stalker Spawn Egg')}
    for name,(f,e) in names.items():
        prefix='block' if name in BLOCKS else 'item';fr[f'{prefix}.tnd.{name}']=f;en[f'{prefix}.tnd.{name}']=e
    for name,(base,shape) in SHAPES.items():
        f,e=names[base];fr['block.tnd.'+name]=('Escalier : ' if shape=='stairs' else 'Dalle : ')+f.lower();en['block.tnd.'+name]=e+(' Stairs' if shape=='stairs' else ' Slab')
    pairs={
      'entity.tnd.lithoguard':('Lithogarde','Lithoguard'),'entity.tnd.glass_stalker':('Traqueur de verre','Glass Stalker'),
      'gui.tnd.machine.subtitle.0':('Écho et métaux bruts','Echo and raw metals'),'gui.tnd.machine.subtitle.1':('Doubler le minerai brut','Double the raw mineral'),'gui.tnd.machine.subtitle.2':('Agrégats voisins · nuit','Nearby aggregates · night'),
      'gui.tnd.machine.help.0':('Métal brut ou granulés + poussière d’écho. 20 s par transformation. Entrée : dessus ; combustible : côtés ; sortie : dessous. Signal redstone = pause.','Raw metal or grit + echo dust. 20 s per operation. Input: top; fuel: sides; output: bottom. Redstone signal pauses.'),
      'gui.tnd.machine.help.1':('Auralite ou noctilite brute + poussière d’écho → 2 granulés. 12 s. Cuire les granulés ou les passer à la forge. Entonnoirs et pause redstone.','Raw auralite or noctilite + echo dust → 2 grit. 12 s. Smelt the grit or use the forge. Hopper access and redstone pause.'),
      'gui.tnd.machine.help.2':('Vestige, la nuit : récolte un agrégat mûr toutes les 10 s dans un carré de 5 × 5, à la hauteur de la machine. 1 charbon = 8 récoltes. 1 poussière par récolte. Aucun XP automatique. Entonnoirs : combustible dessus/côtés, sortie dessous.','Vestige at night: harvest one mature aggregate every 10 s in a 5 × 5 bed at machine height. 1 coal = 8 harvests. 1 dust per harvest. No automatic XP. Hoppers: fuel top/sides, output bottom.'),
      'gui.tnd.reaper.state.0':('Aucun agrégat mûr à portée','No mature aggregate in range'),'gui.tnd.reaper.state.1':('Ajouter du charbon','Add coal'),'gui.tnd.reaper.state.2':('Sortie pleine','Output full'),'gui.tnd.reaper.state.3':('Pause redstone','Redstone pause'),'gui.tnd.reaper.state.4':('Récolte en cours','Harvesting'),'gui.tnd.reaper.state.5':('Nécessite Vestige','Requires Vestige'),'gui.tnd.reaper.state.6':('En attente de la nuit','Waiting for night'),
      'gui.tnd.reaper.input':('Zone 5 × 5','5 × 5 bed'),'gui.tnd.reaper.fuel':('Charbon','Coal'),
      'tooltip.tnd.gravitas_hammer':('Maintenez 1 s au sol : onde frontale contre les monstres à 5 blocs. Recharge 6 s ; 3 points de durabilité.','Hold 1 s on the ground: frontal wave against monsters within 5 blocks. Cooldown 6 s; 3 durability.'),
      'tooltip.tnd.raw_auralite':('Affleure dans les étendues, champs fracturés et dunes de Vestige. Pioche en fer ou mieux.','Exposed in Vestige expanses, fractured fields and dunes. Iron pickaxe or better.'),
      'tooltip.tnd.raw_noctilite':('Affleure dans les forêts de verre, bassins et corniches de Vestige. Pioche en fer ou mieux.','Exposed in Vestige glass woods, basins and shelves. Iron pickaxe or better.'),
      'tooltip.tnd.umbra_alloy':('Noctilite + alliage trempé + filament de Traqueur. Répare les équipements ombraux.','Noctilite + tempered alloy + Stalker filament. Repairs Umbra equipment.'),
      'tooltip.tnd.lithic_heart':('Butin du Lithogarde. Sert au récolteur et au marteau.','Lithoguard loot. Used in the reaper and hammer.'),
      'gui.tnd.guide':('Guide de Vestige','Vestige Guide'),'gui.tnd.inventory_guide':('Guide : progression, combats et machines','Guide: progression, combat and machines'),
    }
    guide=[
      ('Voyager','Travel',[
        ('Trois éclats de couleur forment un cristal composite. Les fresques indiquent les habitats des minerais.','Three coloured shards form a composite crystal. Murals explain the mineral habitats.'),
        ('Explorez les six biomes de Vestige. Les grandes ruines apparaissent dans de nouveaux territoires.','Explore Vestige’s six biomes. Large ruins appear in unexplored terrain.'),
        ('Consultez les pupitres naturels. Liez un prisme à une ancre pour préparer le trajet de retour.','Use natural survey desks. Bind a prism to an anchor before a long journey.')]),
      ('Combattre','Combat',[
        ('Lithogarde : quittez les cercles durant ses 2 s de préparation. Frappez pendant sa récupération.','Lithoguard: leave its circles during the 2 s wind-up. Strike during its recovery.'),
        ('Traqueur : trois marques annoncent sa salve. Un coup d’au moins 3 dégâts interrompt la préparation.','Stalker: three marks announce its salvo. A hit of at least 3 damage interrupts the wind-up.'),
        ('Marteau : maintenez 1 s au sol. Onde frontale à 5 blocs ; recharge 6 s, coût 3 de durabilité.','Hammer: hold 1 s on the ground. Frontal 5-block wave; 6 s cooldown, 3 durability cost.')]),
      ('Matériaux','Materials',[
        ('Auralite dorée : sols exposés des étendues, champs et dunes. Pioche en fer. Donne des outils rapides.','Golden auralite: exposed expanse, field and dune floors. Iron pickaxe. Makes fast tools.'),
        ('Noctilite violette : sols exposés des bois de verre, bassins et corniches. Cuire pour obtenir le cristal.','Violet noctilite: exposed glass woods, basin and shelf floors. Smelt for crystals.'),
        ('4 cristaux + 4 alliages trempés + 1 filament = 4 alliages ombraux. Armure, outils et marteau.','4 crystals + 4 tempered alloys + 1 filament = 4 Umbra alloys. Armor, tools and hammer.')]),
      ('Machines','Machines',[
        ('Séparateur : 1 minerai brut + 1 poussière → 2 granulés en 12 s. Cuire ensuite les granulés.','Separator: 1 raw mineral + 1 dust → 2 grit in 12 s. Smelt the grit afterwards.'),
        ('Forge : poussière d’écho + métal brut. Minerais dessus, poussière sur les côtés, sortie dessous.','Forge: echo dust + raw metal. Input above, dust through sides, output underneath.'),
        ('Récolteur : carré 5 × 5 à sa hauteur, nuit de Vestige. 1 charbon = 8 récoltes ; 10 s par agrégat mûr.','Reaper: 5 × 5 bed at its height, Vestige night. 1 coal = 8 harvests; 10 s per mature aggregate.')]),
      ('Expéditions','Expeditions',[
        ('L’onglet sous les maîtrises ouvre huit objectifs. Les pupitres donnent accès aux mêmes notes.','The tab below masteries opens eight objectives. Desks show the same personal notes.'),
        ('Récupérez chaque récompense avec son bouton. Les infobulles précisent le butin et les biomes visités.','Claim each reward using its button. Tooltips list rewards and recorded biomes.'),
        ('Les notes et récompenses survivent à la mort. Sept récompenses débloquent le trophée du cartographe.','Notes and rewards survive death. Seven claimed rewards unlock the cartographer trophy.')]),
      ('Revenir','Return',[
        ('Dans Vestige, cliquez une ancre avec le prisme. Maintenez-le 5 s sans bouger pour revenir.','In Vestige, click an anchor with the prism. Hold it for 5 s without moving to return.'),
        ('Coût : 2 poussières après l’arrivée. Recharge : 60 s. Bouger, subir des dégâts ou relâcher annule.','Cost: 2 dust after arrival. Cooldown: 60 s. Moving, damage or releasing cancels.'),
        ('Une ancre détruite ou une arrivée bloquée refuse le retour sans frais. Les portails relient les dimensions.','A destroyed anchor or blocked arrival refuses recall without cost. Portals link dimensions.')]),
    ]
    for page,(f,e,cards) in enumerate(guide):
        pairs['guide.tnd.tab.'+str(page)]=(f,e)
        for row,pair in enumerate(cards):pairs[f'guide.tnd.page.{page}.{row}']=pair
    pairs['hud.tnd.day']=('Vestige · jour','Vestige · day');pairs['hud.tnd.night']=('Vestige · nuit','Vestige · night');pairs['hud.tnd.attack']=('Attaque annoncée — esquivez !','Incoming attack — dodge!');pairs['hud.tnd.opening']=('Ouverture — frappez !','Opening — strike!')
    pairs['expedition.tnd.gear.short']=('Outil résonant ou ombral.','Own a resonant or Umbra tool.')
    pairs['expedition.tnd.gear.description']=('Portez une pioche ou lame résonante, une pioche ou lame ombrale, ou le marteau de gravité en survie.','Carry a resonant pickaxe/blade, Umbra pickaxe/blade or Gravitas Hammer in survival.')
    subtitles=[('Le Lithogarde se réveille','Lithoguard wakes'),('La pierre se tend','Stone strains'),('Le sol se fracture','Ground fractures'),('Pas de pierre','Stone footstep'),('Le Lithogarde s’effondre','Lithoguard falls'),('Le verre se hérisse','Glass bristles'),('Une aiguille se brise','A needle shatters'),('Le Traqueur crisse','Stalker screeches'),('Le Traqueur se fragmente','Stalker fragments'),('Le séparateur termine','Separator completes'),('Le récolteur aspire un écho','Reaper gathers an echo'),('Le marteau accumule sa force','Hammer charges'),('Une onde frappe','Shockwave strikes'),('Le carillon résonne','Chime resonates'),('Un objectif est accompli','Objective complete'),('L’ancre rappelle','Anchor recalls')]
    for name,pair in zip(SOUNDS,subtitles):pairs['subtitles.tnd.'+name]=pair
    for name,f,e,fd,ed in [
      ('awakening_minerals','Deux couleurs oubliées','Two Forgotten Colours','Trouvez de l’auralite ou de la noctilite brute dans Vestige.','Find raw auralite or noctilite in Vestige.'),
      ('awakening_umbra','Revêtu de la nuit','Clad in Night','Obtenez un plastron ombral.','Obtain an Umbra chestplate.'),
      ('awakening_hammer','Faire trembler Vestige','Shake Vestige','Forgez un marteau de gravité.','Forge a Gravitas Hammer.'),
      ('awakening_automation','Le jardin se souvient','The Garden Remembers','Construisez un récolteur d’écho.','Build an Echo Reaper.')]:
        pairs['advancement.tnd.'+name]=(f,e);pairs['advancement.tnd.'+name+'.description']=(fd,ed)
    for k,(f,e) in pairs.items():fr[k]=f;en[k]=e
