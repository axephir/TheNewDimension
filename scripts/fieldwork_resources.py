"""0.8 observatory, bounded expeditions, safe recall and original vanilla-scale decoration."""
BLOCKS=['survey_terminal','return_anchor','cartographer_trophy','celestial_tiles','charted_stone','star_chart','brass_lattice','observatory_lamp']
def content(write,png,assets,data):
    def faces(t):return {d:{'texture':t} for d in ['north','east','south','west','up','down']}
    write(assets/'models/block/survey_terminal.json',{'textures':{'base':'tnd:block/brass_lattice','map':'tnd:block/star_chart','particle':'tnd:block/charted_stone'},
        'elements':[{'from':[2,0,2],'to':[14,8,14],'faces':faces('#base')},
                    {'from':[1,8,1],'to':[15,10,15],'rotation':{'origin':[8,8,8],'axis':'x','angle':22.5,'rescale':False},'faces':faces('#map')}]})
    write(assets/'blockstates/survey_terminal.json',{'variants':{f'facing={face}':{'model':'tnd:block/survey_terminal','y':angle} for face,angle in [('north',0),('east',90),('south',180),('west',270)]}})
    recipes={
      'return_anchor':(['TCT','RAR','TCT'],{'T':'tnd:tempered_alloy','C':'tnd:composite_crystal','R':'tnd:resonant_shard','A':'tnd:ancient_alloy'}),
      'recall_prism':([' T ','ECE',' T '],{'T':'tnd:tempered_alloy','E':'tnd:echo_dust','C':'minecraft:compass'}),
      'survey_terminal':(['PPP',' A ','AAA'],{'P':'minecraft:paper','A':'tnd:ancient_alloy'}),
    }
    for name,(pattern,keys) in recipes.items():write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':keys,'result':{'id':'tnd:'+name,'count':1}})
    write(data/'advancement/recipes/fieldwork_knowledge.json',{'criteria':{'alloy':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:tempered_alloy'}]}}},'requirements':[['alloy']],'rewards':{'recipes':['tnd:return_anchor','tnd:recall_prism']}})
    for name in ['celestial_tiles','charted_stone','star_chart','brass_lattice','observatory_lamp']:
        write(data/f'recipe/{name}.json',{'type':'minecraft:stonecutting','ingredient':'tnd:ancient_alloy' if name in ['brass_lattice','observatory_lamp'] else 'tnd:ashstone','result':{'id':'tnd:'+name,'count':1}})

def languages(fr,en):
    pairs={
      'message.tnd.lore.observatory':('« Le ciel a disparu. Nous cartographions ses échos. » — Relevez les six biomes, réparez les consoles et consultez le pupitre. Les ancres offrent un retour tant que leur cœur demeure intact.','“The sky is gone. We map its echoes.” — Survey six biomes, repair consoles and consult the desk. Anchors offer a return while their core remains intact.'),
      'gui.tnd.expeditions':('Expéditions','Expeditions'),
      'gui.tnd.inventory_expeditions':('Expéditions : découvertes et récompenses','Expeditions: discoveries and rewards'),
      'gui.tnd.expedition.summary':('Récompenses récupérées : %s / 8','Rewards claimed: %s / 8'),
      'gui.tnd.expedition.claim':('Récupérer','Claim'),
      'gui.tnd.expedition.claimed':('Obtenue','Claimed'),
      'gui.tnd.expedition.pending':('En cours','In progress'),
      'gui.tnd.expedition.page':('Page %s / 2','Page %s / 2'),
      'gui.tnd.expedition.rewards':('Récompense :','Reward:'),
      'message.tnd.expedition_ready':('Un objectif est prêt ! Ouvrez les expéditions.','Objective complete! Open your expeditions.'),
      'message.tnd.expedition_claimed':('Récompense obtenue : %s','Reward claimed: %s'),
      'block.tnd.survey_terminal':('Pupitre de relevés','Survey Desk'),
      'block.tnd.return_anchor':('Ancre de retour','Return Anchor'),
      'block.tnd.cartographer_trophy':('Trophée du cartographe','Cartographer Trophy'),
      'block.tnd.celestial_tiles':('Carreaux célestes','Celestial Tiles'),
      'block.tnd.charted_stone':('Pierre astronomique','Charted Stone'),
      'block.tnd.star_chart':('Carte stellaire','Star Chart'),
      'block.tnd.brass_lattice':('Treillis de laiton','Brass Lattice'),
      'block.tnd.observatory_lamp':('Lampe d’observatoire','Observatory Lamp'),
      'item.tnd.recall_prism':('Prisme de retour','Recall Prism'),
      'tooltip.tnd.recall_prism':('Liez-le à une ancre dans Vestige. Maintenez 5 s.','Bind to a Vestige anchor. Hold for 5 seconds.'),
      'tooltip.tnd.recall_cost':('Coût : 2 poussières. Recharge : 60 s. Bouger annule.','Cost: 2 dust. Cooldown: 60 s. Moving cancels.'),
      'tooltip.tnd.recall_target':('Ancre : %s / %s / %s','Anchor: %s / %s / %s'),
      'tooltip.tnd.return_anchor':('Dans Vestige : clic avec un prisme pour le lier.','In Vestige: click while holding a prism to bind it.'),
      'tooltip.tnd.cartographer_trophy':('Trophée lumineux des huit expéditions terminées.','Luminous trophy for completing eight expeditions.'),
      'message.tnd.recall_bind_help':('Dans Vestige : tenez un prisme et cliquez l’ancre.','In Vestige: hold a prism and click the anchor.'),
      'message.tnd.recall_bound':('Prisme lié. Maintenez-le 5 s pour revenir (2 échos).','Prism bound. Hold 5 s to return (2 dust).'),
      'message.tnd.recall_dimension':('Le prisme s’utilise uniquement dans Vestige.','The prism works only within Vestige.'),
      'message.tnd.recall_unbound':('Liez d’abord le prisme à une ancre de retour.','First bind the prism to a return anchor.'),
      'message.tnd.recall_fuel':('Il faut deux poussières d’écho dans l’inventaire.','Two echo dust are required in your inventory.'),
      'message.tnd.recall_interrupted':('Retour interrompu. Aucune poussière consommée.','Recall interrupted. No dust consumed.'),
      'message.tnd.recall_channel':('Restez immobile pendant 5 secondes…','Remain still for 5 seconds…'),
      'message.tnd.recall_blocked':('Arrivée obstruée. Aucune poussière consommée.','Arrival obstructed. No dust consumed.'),
      'message.tnd.recall_missing':('L’ancre a été détruite ou remplacée. Reliez le prisme.','Anchor destroyed or replaced. Bind the prism again.'),
      'message.tnd.recall_arrived':('Retour à l’ancre. Prisme en recharge : 60 s.','Returned to the anchor. Cooldown: 60 seconds.'),
    }
    missions=[
      ('survey','Premiers relevés','First Surveys','Visiter 3 biomes de Vestige.','Visit 3 Vestige biomes.','Découvrez trois biomes distincts en survie. Les biomes déjà relevés sont cochés.','Discover three different biomes in survival. Recorded biomes are checked.'),
      ('atlas','Les six visages de Vestige','Six Faces of Vestige','Visiter les 6 biomes.','Visit all 6 biomes.','Parcourez les six biomes de Vestige. Un biome ne compte qu’une fois.','Travel through all six Vestige biomes. Each biome counts once.'),
      ('restoration','Les machines se souviennent','Machines Remember','Réparer 3 consoles anciennes.','Repair 3 ancient consoles.','Réparez trois consoles distinctes de Vestige avec des éclats résonants. Les consoles posées ne comptent pas.','Repair three different Vestige consoles with resonant shards. Player-placed consoles do not count.'),
      ('night_harvest','Récoltes de la nuit','Night Harvests','Récolter 6 agrégats distincts.','Harvest 6 different aggregates.','Récoltez six agrégats d’écho mûrs à des emplacements distincts, la nuit de Vestige.','Harvest six mature echo aggregates at different positions during Vestige’s night.'),
      ('sentinels','Faire taire les veilleurs','Silence the Watchers','Vaincre 3 sentinelles libres.','Defeat 3 roaming sentinels.','Vainquez trois sentinelles de Vestige. Les gardiens des épreuves ne comptent pas.','Defeat three Vestige sentinels. Trial guardians do not count.'),
      ('observatory','Le ciel oublié','The Forgotten Sky','Utiliser un pupitre naturel.','Use a natural survey desk.','Trouvez un observatoire ancien et utilisez son pupitre. Un pupitre fabriqué donne accès aux expéditions mais ne valide pas cette découverte.','Find an ancient observatory and use its survey desk. Crafted desks open expeditions but do not count as discoveries.'),
      ('gear','Métal résonant','Resonant Metal','Posséder un outil résonant.','Own a resonant tool.','Ayez une pioche ou une lame résonante dans votre inventaire en survie. Les outils des anciennes versions comptent aussi.','Carry a resonant pickaxe or blade in survival. Tools from earlier versions also count.'),
      ('cartographer','Cartographe de Vestige','Vestige Cartographer','Récupérer les 7 récompenses.','Claim the 7 previous rewards.','Terminez les sept autres objectifs et récupérez leurs récompenses pour obtenir le trophée lumineux du cartographe.','Complete and claim the seven other objectives to earn the luminous Cartographer Trophy.'),
    ]
    for key,f,e,fs,es,fd,ed in missions:
        pairs['expedition.tnd.'+key]=(f,e);pairs['expedition.tnd.'+key+'.short']=(fs,es);pairs['expedition.tnd.'+key+'.description']=(fd,ed)
    for key,(f,e) in pairs.items():fr[key]=f;en[key]=e
