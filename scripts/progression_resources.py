"""Localized descriptions mirror the server's 0.3 mastery rules."""
def expand(fr,en,ids):
    fr.update({
        'tooltip.tnd.relic':'Récupéré une seule fois dans chaque console naturelle réparée. Trois fragments servent au module de stabilisation.',
        'tooltip.tnd.stabilizer':'Cadence +25 %. Nécessaire pour extraire du diamant. Un seul module par extracteur.',
        'tooltip.tnd.efficiency':'Cadence +50 %. Un seul module par extracteur.',
        'item.tnd.relic_fragment':'Fragment de relique','item.tnd.stabilizer_module':'Module de stabilisation',
        'message.tnd.level_up':'%s · Niveau %s !',
        'message.tnd.console_needs_shard':'Un éclat résonant permettrait de réparer cette console.',
        'message.tnd.console_active':'Console active · %s extracteur(s) calibré(s), cadence +%s %%',
        'message.tnd.sense_cooldown':'La perception se recharge…',
        'message.tnd.sense_found_direction':'Résonance à %s blocs · %s · %s',
        'gui.tnd.sense':'Percevoir','gui.tnd.sense_help':'Cherche une cible de cette maîtrise dans un rayon de %s blocs chargés. Recharge : 5 s.',
        'gui.tnd.extractor.output':'Résultat','gui.tnd.extractor.relic':'Relique : +%s %%',
        'gui.tnd.extractor.mode_help':'Cycle de base ×%s. Videz la sortie pour changer. Le changement remet la progression à zéro.',
        'gui.tnd.extractor.diamond_help':'Cycle de base ×%s. Module de stabilisation obligatoire. Videz la sortie avant de changer.',
        'mode.tnd.iron':'Fer','mode.tnd.copper':'Cuivre','mode.tnd.gold':'Or','mode.tnd.redstone':'Redstone','mode.tnd.diamond':'Diamant',
        'message.tnd.lore.ocean':'« Là où la lumière abandonne les abysses, le bleu demeure à nu. » — La fresque désigne les sols des océans profonds.',
        'message.tnd.lore.lava':'« Sous le feu, une seule peau de pierre nous séparait du rouge. » — Cherchez la première couche solide des lacs de lave.',
        'message.tnd.lore.forest':'« Le vert se nourrit des racines, jamais des branches. » — Quelques arbres des forêts cachent un éclat à leur pied.',
        'message.tnd.lore.fusion':'« Trois couleurs ouvrent le passage ; une quatrième réveille les machines. » — Réparez les consoles avec un éclat résonant pour retrouver leurs fragments.'})
    en.update({
        'tooltip.tnd.relic':'Recovered once from each repaired natural console. Three fragments are used for a stabilizer module.',
        'tooltip.tnd.stabilizer':'+25% throughput. Required to extract diamonds. One module per extractor.',
        'tooltip.tnd.efficiency':'+50% throughput. One module per extractor.',
        'item.tnd.relic_fragment':'Relic Fragment','item.tnd.stabilizer_module':'Stabilizer Module',
        'message.tnd.level_up':'%s · Level %s!',
        'message.tnd.console_needs_shard':'A resonant shard could repair this console.',
        'message.tnd.console_active':'Console online · %s extractor(s) calibrated, throughput +%s %%',
        'message.tnd.sense_cooldown':'Insight is recharging…',
        'message.tnd.sense_found_direction':'Resonance %s blocks away · %s · %s',
        'gui.tnd.sense':'Sense','gui.tnd.sense_help':'Find a target for this mastery within %s loaded blocks. Cooldown: 5 s.',
        'gui.tnd.extractor.output':'Output','gui.tnd.extractor.relic':'Relic: +%s %%',
        'gui.tnd.extractor.mode_help':'Base cycle ×%s. Empty the output before switching. Switching resets progress.',
        'gui.tnd.extractor.diamond_help':'Base cycle ×%s. Requires a stabilizer module. Empty the output before switching.',
        'mode.tnd.iron':'Iron','mode.tnd.copper':'Copper','mode.tnd.gold':'Gold','mode.tnd.redstone':'Redstone','mode.tnd.diamond':'Diamond',
        'message.tnd.lore.ocean':'“Where light abandons the deep, blue remains exposed.” — The mural points to the floors of deep oceans.',
        'message.tnd.lore.lava':'“Beneath the fire, only one stone skin separated us from red.” — Search the first solid layer below lava lakes.',
        'message.tnd.lore.forest':'“Green feeds on roots, never branches.” — Some forest trees hide a shard at their feet.',
        'message.tnd.lore.fusion':'“Three colours open the passage; a fourth awakens the machines.” — Repair consoles with resonant shards to recover their fragments.'})
    activities=[('Miner des minerais naturels.','Mine natural ores.'),('Abattre des arbres.','Harvest trees.'),('Récolter des cultures mûres.','Harvest mature crops.'),('Remonter une prise à la canne.','Reel in a catch.'),('Chasser des animaux.','Hunt animals.'),('Faire naître des animaux.','Breed animals.'),('Vaincre des monstres au contact.','Defeat monsters in melee.'),('Vaincre des monstres avec un projectile.','Defeat monsters with projectiles.'),('Encaisser des attaques de créatures.','Withstand attacks from mobs.'),('Fabriquer des objets variés.','Craft a variety of items.'),('Construire dans de nouvelles zones.','Build in new areas.'),('Explorer de nouvelles régions.','Explore new regions.'),('Préparer ou boire des potions.','Brew or drink potions.'),('Fabriquer et calibrer des machines.','Craft and calibrate machines.'),('Étudier les fresques et les consoles.','Study murals and consoles.')]
    effects=[
        ('+5 % de vitesse de minage par rang.','+5% mining speed per rank.'),
        ('+5 % de vitesse de coupe par rang.','+5% logging speed per rank.'),
        ('Toutes les 5 s : 4 % de chance par rang de faire pousser une culture proche. Aucun XP passif.','Every 5 s: 4% chance per rank to grow a nearby crop. No passive XP.'),
        ('+0,2 chance par rang avec une canne en main.','+0.2 luck per rank while holding a fishing rod.'),
        ('+2 % de dégâts aux animaux par rang.','+2% damage to animals per rank.'),
        ('Les petits obtenus par élevage ont +4 % de vie maximale par rang.','Bred offspring have +4% maximum health per rank.'),
        ('+3 % de dégâts de mêlée aux créatures par rang.','+3% melee damage to creatures per rank.'),
        ('+3 % de dégâts par projectile aux créatures par rang.','+3% projectile damage to creatures per rank.'),
        ('−2 % de dégâts reçus des créatures par rang.','−2% incoming damage from mobs per rank.'),
        ('+5 % d’XP de maîtrise pour toutes les activités par rang.','+5% mastery XP for all activities per rank.'),
        ('+0,15 bloc de portée par rang avec un bloc en main.','+0.15 blocks of reach per rank while holding a block.'),
        ('+1,5 % de vitesse de déplacement par rang.','+1.5% movement speed per rank.'),
        ('Les potions bénéfiques bues durent +5 % par rang.','Beneficial potions you drink last +5% per rank.'),
        ('+5 % de cadence des extracteurs par rang après réglage.','+5% extractor throughput per rank after tuning.'),
        ('Les consoles offrent +2 points de cadence par rang aux extracteurs proches (base : 15 %, maximum : 25 %).','Consoles grant +2 throughput points per rank to nearby extractors (base: 15%, maximum: 25%).')]
    targets=[('minerais','ores'),('troncs','logs'),('cultures mûres','mature crops'),('eau profonde','deep water'),('animaux','animals'),('animaux adultes','adult animals'),('monstres','monsters'),('monstres','monsters'),('monstres','monsters'),('ateliers et fourneaux','workbenches and furnaces'),('bois et ateliers','wood and workbenches'),('coffres et consoles','chests and consoles'),('alambics et verrues du Nether','brewing stands and Nether wart'),('extracteurs','extractors'),('consoles et fresques','consoles and murals')]
    resource={'mining','logging','farming','fishing','hunting'}
    for i,key in enumerate(ids):
        for lang,j in [(fr,0),(en,1)]:
            lang['activity.tnd.'+key]=activities[i][j]
            prefix='talent.tnd.'+key+'.'
            lang[prefix+'efficiency.description']=effects[i][j]
            lang[prefix+'yield.description']=('2 % de chance par rang d’un objet supplémentaire par récolte manuelle.' if j==0 else '2% chance per rank of one extra item per manual harvest.') if key in resource else ('+3 % de cadence des extracteurs par rang après réglage.' if j==0 else '+3% extractor throughput per rank after tuning.') if key=='engineering' else ('+2 % d’XP dans cette maîtrise par rang.' if j==0 else '+2% XP in this mastery per rank.')
            lang[prefix+'insight.description']=('Repère : '+targets[i][j]+'. Bouton Percevoir ou /tnd sense '+key+'. Recharge : 5 s.' if j==0 else 'Find: '+targets[i][j]+'. Sense button or /tnd sense '+key+'. Cooldown: 5 s.')
            lang[prefix+'precision.description']=('+2 blocs de perception par rang.' if j==0 else '+2 blocks of sensing radius per rank.')+(' +1 % de cadence par rang après réglage.' if j==0 else ' +1% throughput per rank after tuning.')*(key=='engineering')
            lang[prefix+'synergy.description']=('+2 % d’XP par rang partagé avec Résonance en %s.' if j==0 else '+2% XP per rank shared with %s Synergy.')+(' Même bonus de vitesse de récolte.' if j==0 else ' Also grants the same harvesting speed bonus.')*(key in {'mining','logging'})+(' Même bonus de cadence après réglage.' if j==0 else ' Also grants the same throughput bonus after tuning.')*(key=='engineering')
