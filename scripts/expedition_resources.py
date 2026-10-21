"""0.7 final sanctuary encounter, processing chain and original 16px equipment art."""
def content(write,png,assets,data):
    import random
    for state in ['', '_open','_enraged']:
        rng=random.Random('custodian'+state);pixels=[]
        glow=(123,219,190) if state=='_open' else (225,143,91) if state=='_enraged' else (170,148,205)
        for y in range(16):
            for x in range(16):
                c=(111,125,119);n=rng.choice([-7,0,0,6]);c=tuple(v+n for v in c)
                if x%4==0 or y%4==0:c=(48,63,65)
                if (x,y) in [(1,1),(6,1),(1,6),(6,6),(9,1),(14,6)]:c=(195,168,106)
                if y>=8 and x>=8:
                    shift=38 if (x+y)%4==0 else -32 if (x+y)%4==2 else 0
                    c=tuple(max(0,min(255,v+shift)) for v in glow)
                if y==4 and x<8:c=(155,164,144)
                if 4<x<8 and y in [5,6]:c=(169,145,93) if x==5 else (68,91,90)
                if x in [2,6] and 9<=y<=14:c=(153,162,142)
                if x in [3,7] and y in [10,13]:c=(78,114,109)
                if x in [2,5,10,13] and y in [3,4]:c=(183,193,167)
                pixels.append((*c,255))
        png(assets/f'textures/entity/fractured_custodian{state}.png',pixels)
    write(data/'loot_table/entities/fractured_custodian.json',{'type':'minecraft:entity','pools':[]})
    variants={}
    for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
        for lit in [False,True]:variants[f'facing={facing},lit={str(lit).lower()}']={'model':'tnd:block/resonance_forge'+('_active' if lit else ''),'y':angle}
    write(assets/'blockstates/resonance_forge.json',{'variants':variants})
    for suffix in ['', '_active']:
        write(assets/f'models/block/resonance_forge{suffix}.json',{'parent':'minecraft:block/cube',
            'textures':{'particle':'tnd:block/resonance_forge','down':'tnd:block/cut_alloy','up':'tnd:block/resonance_forge_top',
             'north':'tnd:block/resonance_forge'+suffix,'south':'tnd:block/riveted_alloy','west':'tnd:block/alloy_vent','east':'tnd:block/alloy_vent'}})
    for tool in ['resonant_blade','resonant_pickaxe']:
        write(assets/f'models/item/{tool}.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'tnd:item/'+tool}})
    recipes={
        'resonance_forge':['AAA','CMC','ARA'],
        'resonant_pickaxe':['TTT',' S ',' S '],
        'resonant_blade':[' T ',' T ',' S '],
    }
    for name,pattern in recipes.items():
        keys={'A':'tnd:ancient_alloy','C':'tnd:composite_crystal','M':'tnd:inertial_matrix','R':'minecraft:redstone'} if name=='resonance_forge' else {'T':'tnd:tempered_alloy','S':'tnd:resonant_shard'}
        write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':keys,'result':{'id':'tnd:'+name,'count':1}})
    write(data/'tags/item/resonant_tool_materials.json',{'values':['tnd:tempered_alloy']})
    root=data.parent/'minecraft/tags/item'
    for tag,tool in [('pickaxes','resonant_pickaxe'),('swords','resonant_blade')]:write(root/f'{tag}.json',{'replace':False,'values':['tnd:'+tool]})
    # Real native advancement criteria; finite XP rewards remain server-controlled in the core.
    for name,parent,item,title,desc,background in [
        ('vestige',None,'tnd:composite_crystal','Entrer dans le silence','Traversez le portail vers Vestige.','minecraft:gui/advancements/backgrounds/stone'),
        ('sanctuary','tnd:expedition/vestige','tnd:resonance_heart','Trois échos','Récupérez le trésor des trois épreuves.',None),
        ('custodian','tnd:expedition/sanctuary','tnd:inertial_matrix','Briser la couronne','Récupérez la matrice du Conservateur brisé.',None),
        ('forge','tnd:expedition/custodian','tnd:resonance_forge','Une industrie réveillée','Construisez une forge résonante.',None),
        ('tempered','tnd:expedition/forge','tnd:tempered_alloy','Le métal se souvient','Obtenez un alliage trempé.',None)]:
        display={'icon':{'id':item},'title':{'translate':'advancement.tnd.'+name},'description':{'translate':'advancement.tnd.'+name+'.description'},'frame':'challenge' if name=='custodian' else 'task','show_toast':True,'announce_to_chat':False,'hidden':False}
        if background:display['background']=background
        criterion={'trigger':'minecraft:changed_dimension','conditions':{'to':'tnd:vestige'}} if name=='vestige' else {'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item}]}}
        adv={'display':display,'criteria':{'done':criterion},'requirements':[['done']]}
        if parent:adv['parent']=parent
        if name=='custodian':adv['rewards']={'recipes':['tnd:resonance_forge','tnd:resonant_pickaxe','tnd:resonant_blade']}
        write(data/f'advancement/expedition/{name}.json',adv)

def languages(fr,en):
    pairs={
      'entity.tnd.fractured_custodian':('Conservateur brisé','Fractured Custodian'),
      'block.tnd.resonance_forge':('Forge résonante','Resonance Forge'),
      'item.tnd.inertial_matrix':('Matrice d’inertie','Inertial Matrix'),
      'item.tnd.tempered_alloy':('Alliage trempé','Tempered Alloy'),
      'item.tnd.resonant_pickaxe':('Pioche résonante','Resonant Pickaxe'),
      'item.tnd.resonant_blade':('Lame résonante','Resonant Blade'),
      'tooltip.tnd.inertial_matrix':('Trésor du Conservateur. Fabrique la forge résonante.','Shared Custodian treasure. Builds a Resonance Forge.'),
      'tooltip.tnd.resonance_forge':('20 s + 1 poussière : métaux bruts ×2 ou alliage trempé.','20 s + 1 dust: double raw metals or temper ancient alloy.'),
      'tooltip.tnd.forge_ports':('Entonnoirs : matière dessus, écho côté, sortie dessous.','Hoppers: input above, dust on the sides, output below.'),
      'tooltip.tnd.forge_pause':('Signal redstone : pause. ? dans la forge : recettes.','Redstone: pause. Hover ? in the forge for recipes.'),
      'tooltip.tnd.tempered_alloy':('Forge : alliage ancien + 1 poussière. Outils et réparation.','Forge: ancient alloy + 1 dust. Crafts and repairs tools.'),
      'message.tnd.custodian_hint':('Poussière en main : clic sur le noyau (coût : 2).','Hold echo dust and click the core (cost: 2).'),
      'message.tnd.custodian_fuel':('Deux poussières d’écho sont nécessaires. Aucun combustible consommé.','Two echo dust are required. No fuel consumed.'),
      'message.tnd.custodian_empty':('Le Conservateur de ce sanctuaire a déjà été vaincu. Sa récompense est commune au groupe.','This sanctuary’s Custodian has already fallen. Its reward is shared by the party.'),
      'message.tnd.custodian_start':('Esquivez les tracés, puis frappez le cœur ouvert.','Evade the markings, then strike the exposed core.'),
      'message.tnd.custodian_mark':('Fracture : quittez les cercles lumineux !','Fracture: leave the glowing circles!'),
      'message.tnd.custodian_ring':('Couronne : rejoignez le centre ou sortez du cercle !','Crown: reach the centre or leave the outer circle!'),
      'message.tnd.custodian_open':('Le cœur est exposé : frappez maintenant !','The core is exposed: strike now!'),
      'message.tnd.custodian_phase':('La couronne se fissure : deux zones à esquiver !','The crown fractures: two marked areas to evade!'),
      'message.tnd.custodian_bar_open':('Conservateur brisé — cœur exposé','Fractured Custodian — exposed core'),
      'message.tnd.custodian_victory':('Conservateur vaincu ! Récupérez la matrice au noyau.','Custodian defeated! Claim the matrix from the core.'),
      'message.tnd.custodian_reward':('Matrice et six alliages obtenus. Fabriquez la forge.','Matrix and six alloys claimed. Craft the forge.'),
      'gui.tnd.forge.help':('20 s et 1 poussière par cycle. Fer / cuivre / or brut → 2 lingots. Alliage ancien → 1 alliage trempé. Entonnoirs : matière dessus, poussière sur les côtés, sortie dessous. Redstone : pause.','20 s and 1 dust per cycle. Raw iron / copper / gold → 2 ingots. Ancient alloy → 1 tempered alloy. Hoppers: input above, dust on the sides, output below. Redstone: pause.'),
      'gui.tnd.forge.subtitle':('1 poussière · 20 s / cycle','1 dust · 20 s / cycle'),
      'gui.tnd.forge.input':('Matière','Input'),
      'gui.tnd.forge.fuel':('Écho','Echo'),
      'gui.tnd.forge.state.0':('Insérez un métal brut ou alliage.','Insert raw metal or ancient alloy.'),
      'gui.tnd.forge.state.1':('Ajoutez de la poussière d’écho.','Add echo dust.'),
      'gui.tnd.forge.state.2':('Videz la sortie.','Clear the output.'),
      'gui.tnd.forge.state.3':('Pause : signal redstone.','Paused: redstone signal.'),
      'gui.tnd.forge.state.4':('Transformation en cours…','Processing…'),
      'advancement.tnd.vestige':('Entrer dans le silence','Enter the Silence'),
      'advancement.tnd.vestige.description':('Traversez le portail vers Vestige.','Travel through the portal to Vestige.'),
      'advancement.tnd.sanctuary':('Trois échos','Three Echoes'),
      'advancement.tnd.sanctuary.description':('Récupérez le trésor des trois épreuves.','Claim the treasure of the three trials.'),
      'advancement.tnd.custodian':('Briser la couronne','Break the Crown'),
      'advancement.tnd.custodian.description':('Récupérez la matrice du Conservateur brisé.','Claim the Fractured Custodian’s matrix.'),
      'advancement.tnd.forge':('Une industrie réveillée','Industry Awakens'),
      'advancement.tnd.forge.description':('Construisez une forge résonante.','Build a Resonance Forge.'),
      'advancement.tnd.tempered':('Le métal se souvient','Metal Remembers'),
      'advancement.tnd.tempered.description':('Obtenez un alliage trempé.','Obtain tempered alloy.'),
      'subtitles.tnd.custodian_awaken':('Une couronne s’éveille','A crown awakens'),
      'subtitles.tnd.custodian_fracture':('La fracture se concentre','A fracture gathers'),
      'subtitles.tnd.custodian_crown':('La couronne se déploie','The crown expands'),
      'subtitles.tnd.custodian_impact':('La résonance se brise','Resonance shatters'),
      'subtitles.tnd.custodian_fall':('Le Conservateur s’éteint','The Custodian falls silent'),
      'subtitles.tnd.forge_complete':('La forge transforme','The forge transmutes'),
    }
    for k,(f,e) in pairs.items():fr[k]=f;en[k]=e
    fr['message.tnd.lore.vault']+=' Le trésor récupéré, cliquez sur le noyau avec une poussière en main (coût : deux poussières) pour éveiller le Conservateur. Ses fractures visent le sol ; sa couronne laisse un abri au centre. Frappez le cœur après chaque attaque.'
    en['message.tnd.lore.vault']+=' After claiming the treasure, hold echo dust and click the core (cost: two dust) to awaken the Custodian. Its fractures target the ground; its crown leaves a safe centre. Strike its core after each attack.'
    fr['message.tnd.vault_reward']='Cœur, 4 fragments, 8 poussières. Poussière en main : boss (2).'
    fr['message.tnd.vault_start']='Épreuve %s : %s gardien(s). Esquivez ou interrompez.'
    fr['message.tnd.vault_progress']='Ailes : %s / 3. Sceaux : bleu, vert, rouge.'
    en['message.tnd.vault_reward']='Heart, 4 fragments, 8 dust. Hold dust: awaken the boss (2).'
    en['message.tnd.vault_start']='Trial %s: %s guardian(s). Evade or interrupt.'
    en['message.tnd.vault_progress']='Wings: %s / 3. Seals: blue, green, red.'
