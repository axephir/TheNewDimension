"""Advanced specializations, cosmetic milestones and restrained diegetic text."""
BRANCHES=[
('mining_prospector','Prospecteur','Prospector','La portée du repérage minier augmente de 4 blocs.','Mining sensing reaches 4 blocks farther.'),
('mining_seismic','Pulsation tellurique','Seismic pulse','Pioche en main : Célérité II pendant 15 s.','Hold a pickaxe: Haste II for 15 s.'),
('logging_forester','Relève forestière','Forester','Accroupi, couper un tronc naturel au sol replante une pousse prise dans votre inventaire, si le sol convient.','Sneak while harvesting a natural ground-level log to replant a sapling from your inventory on suitable ground.'),
('logging_grove','Élan du bosquet','Grove momentum','Hache en main : Célérité II pendant 25 s et repérage des troncs.','Hold an axe: Haste II for 25 s and log sensing.'),
('farming_seedkeeper','Gardien des semences','Seedkeeper','Accroupi, récolter blé, carottes, pommes de terre ou betteraves replante une graine de votre inventaire.','Sneak-harvesting wheat, carrots, potatoes or beetroot replants a seed from your inventory.'),
('farming_verdant','Souffle fertile','Verdant breath','Consomme 4 poudres d’os : fait avancer jusqu’à 8 cultures proches d’un stade de croissance.','Consume 4 bone meal: advance up to 8 nearby crops by one growth stage.'),
('fishing_diver','Poumons des profondeurs','Deep lungs','Respiration aquatique tant que vous êtes dans l’eau.','Water Breathing while you are in water.'),
('fishing_current','Courant favorable','Favourable current','Chance II et Respiration aquatique pendant 30 s.','Luck II and Water Breathing for 30 s.'),
('hunting_tracker','Pisteur','Tracker','La portée du repérage des proies augmente de 4 blocs.','Prey sensing reaches 4 blocks farther.'),
('hunting_focus','Approche silencieuse','Silent approach','Invisibilité et Vitesse I pendant 10 s. L’armure reste visible.','Invisibility and Speed I for 10 s. Armour remains visible.'),
('breeding_caretaker','Lignée robuste','Healthy lineage','Les petits issus de vos reproductions ont 20 % de santé maximale supplémentaire.','Offspring from animals you breed gain 20% maximum health.'),
('breeding_herd','Soin du troupeau','Herd care','Consomme 4 blés : soigne 2 cœurs à jusqu’à 12 animaux proches blessés, hors familiers.','Consume 4 wheat: heal 2 hearts on up to 12 injured nearby animals, excluding familiars.'),
('melee_duelist','Duelliste','Duelist','+12 % de dégâts de mêlée contre une cible sans autre hostile à moins de 3 blocs. Hors joueurs.','+12% melee damage against a target with no other hostile within 3 blocks. Excludes players.'),
('melee_repulse','Onde de recul','Repulse','Repousse les hostiles visibles à moins de 5 blocs et les ralentit pendant 4 s.','Push visible hostiles within 5 blocks away and slow them for 4 s.'),
('ranged_marksman','Tir lointain','Marksman','+15 % de dégâts de projectile à partir de 12 blocs. Hors joueurs.','+15% projectile damage from at least 12 blocks away. Excludes players.'),
('ranged_pin','Tir d’arrêt','Pinning shot','Le prochain projectile qui touche dans les 20 s ralentit sa cible pendant 6 s. Hors joueurs.','The next projectile hit within 20 s slows its target for 6 s. Excludes players.'),
('defense_bulwark','Ancrage','Bulwark','+25 % de résistance au recul lorsque vos pieds touchent le sol.','+25% knockback resistance while grounded.'),
('defense_aegis','Égide fugace','Fleeting aegis','Absorption II pendant 10 s : jusqu’à 4 cœurs temporaires.','Absorption II for 10 s: up to 4 temporary hearts.'),
('crafting_artisan','Ouvrage durable','Artisan','Les équipements neufs que vous fabriquez gagnent 10 % de durabilité maximale. Non cumulable.','New undamaged equipment you craft gains 10% maximum durability. Does not stack.'),
('crafting_inspiration','Entretien d’atelier','Workshop maintenance','Consomme un alliage trempé : répare 25 % de la durabilité maximale de l’objet en main.','Consume one tempered alloy: repair 25% of the held item’s maximum durability.'),
('building_architect','Architecte','Architect','+0,75 bloc de portée quand vous tenez un bloc de construction.','+0.75 block interaction range while holding a building block.'),
('building_scaffold','Pas suspendu','Suspended step','Chute lente pendant 30 s.','Slow Falling for 30 s.'),
('exploration_wayfarer','Grand air','Wayfarer','Déplacement légèrement plus rapide sous le ciel ouvert.','Move slightly faster beneath open sky.'),
('exploration_return','Second souffle','Second wind','Vitesse II et Sauts améliorés I pendant 20 s.','Speed II and Jump Boost I for 20 s.'),
('alchemy_distiller','Distillation fine','Fine distillation','Les potions bénéfiques que vous buvez durent 20 % de plus, en complément d’Efficacité.','Beneficial potions you drink last 20% longer, in addition to Efficiency.'),
('alchemy_purify','Purge','Purification','Consomme une fiole de miel : retire Poison, Wither et Faiblesse. Rend une bouteille vide.','Consume one honey bottle: remove Poison, Wither and Weakness. Returns an empty bottle.'),
('engineering_tuner','Réglage de précision','Precision tuning','Interagir avec un extracteur lui ajoute 10 % de calibration permanente. Non cumulable.','Interacting with an extractor adds 10% permanent calibration. Does not stack.'),
('engineering_overclock','Circuit étalonné','Calibrated circuit','Visez un extracteur à moins de 5 blocs. Consomme un circuit de contrôle pour porter sa calibration de relique à 25 %.','Aim at an extractor within 5 blocks. Consume a control circuit to raise its relic calibration to 25%.'),
('ancient_tech_interpreter','Lecteur de traces','Trace reader','La portée du repérage ancien augmente de 4 blocs.','Ancient sensing reaches 4 blocks farther.'),
('ancient_tech_resonance','Contre-fréquence','Counter-frequency','Repère les traces anciennes et affaiblit pendant 8 s les hostiles visibles à moins de 8 blocs.','Sense ancient traces and weaken visible hostiles within 8 blocks for 8 s.')]

TESTS=['discovery_'+n for n in ['menu','abilities','costs','crops','forestry','prestige','engineering','purify','herd','anomalies','artisan','players']]
def content(write,png,assets,data):
    for test in TESTS:write(data/f'test_instance/{test}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+test,'structure':'tnd:test_habitat','max_ticks':800,'required':True})
    for i in range(4):
        name=f'prestige_seal_{i}';colors=[(92,161,143),(99,160,194),(163,119,194),(210,171,82)];c=colors[i]
        pixels=[]
        for y in range(16):
            for x in range(16):
                edge=x in (0,15) or y in (0,15);ring=abs(x-7.5)+abs(y-7.5)
                lit=5<ring<7 or x in (7,8) and 4<=y<=11 or i>0 and y in (7,8) and 4<=x<=11
                noise=(x*17+y*23)%9
                pixels.append((*((tuple(min(255,v+25) for v in c)) if lit else (29+noise,39+noise,44+noise) if not edge else (16,24,29)),255))
        png(assets/f'textures/block/{name}.png',pixels)
        write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':f'tnd:block/{name}'}}})
        write(assets/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'tnd:block/{name}'}})
        write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'tnd:block/{name}'}})
        write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'tnd:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    p=data.parent/'minecraft/tags/block/mineable/pickaxe.json';import json;s=json.loads(p.read_text());s['values'] = list(dict.fromkeys(s['values']+[f'tnd:prestige_seal_{i}' for i in range(4)]));write(p,s)

def languages(fr,en):
    for key,a,b,da,db in BRANCHES:
        fr['talent.tnd.'+key]=a;en['talent.tnd.'+key]=b;fr['talent.tnd.'+key+'.description']=da;en['talent.tnd.'+key+'.description']=db
    text={
    'mastery.tnd.tab.0':('Bases','Foundations'),'mastery.tnd.tab.1':('Spécialités','Specialties'),'mastery.tnd.tab.2':('Distinctions','Distinctions'),
    'mastery.tnd.required_level':('Niveau requis : %s','Required level: %s'),'mastery.tnd.unlock':('Niv. %s · %s points','Lv. %s · %s points'),'mastery.tnd.learned':('◆ Talent acquis','◆ Talent learned'),
    'ability.tnd.activate':('Utiliser la capacité','Use ability'),'ability.tnd.cooldown':('Recharge : %s s','Cooldown: %s s'),'ability.tnd.context':('Conditions non réunies. Consultez le talent dans vos maîtrises.','Requirements not met. Check the talent in your masteries.'),'ability.tnd.used':('%s','%s'),
    'ability.tnd.help':('Recharge commune : 60 s. Lisez le talent au-dessus pour ses effets et les ressources nécessaires.','Shared cooldown: 60 s. Hover over the talent above for effects and resource requirements.'),
    'prestige.tnd.hide':('Masquer mon titre','Hide my title'),'prestige.tnd.milestone':('%s · niv. %s %s','%s · lv. %s %s'),'prestige.tnd.cosmetic':('Titres et sceaux · aucun bonus de puissance','Titles and seals · purely cosmetic'),'prestige.tnd.help':('Équipe ce titre. Au premier choix de chaque palier, vous recevez un sceau décoratif lumineux. Un sceau par palier et par joueur, conservé acquis à la mort.','Equip this title. The first selection at each milestone grants a luminous decorative seal. One seal per milestone per player; the claim survives death.'),'prestige.tnd.title':('%s · %s','%s · %s'),
    'prestige.tnd.rank.0':('Initié','Initiate'),'prestige.tnd.rank.1':('Veilleur','Watcher'),'prestige.tnd.rank.2':('Éminence','Eminent'),'prestige.tnd.rank.3':('Légende','Legend')}
    for k,(a,b) in text.items():fr[k]=a;en[k]=b
    for i,(a,b) in enumerate([('Sceau de l’Initié','Initiate’s Seal'),('Sceau du Veilleur','Watcher’s Seal'),('Sceau de l’Éminence','Eminent Seal'),('Sceau de la Légende','Legend’s Seal')]):fr[f'block.tnd.prestige_seal_{i}']=a;en[f'block.tnd.prestige_seal_{i}']=b
    # The interface names what can be observed. It does not solve the device or explain the city in advance.
    mysterious={
    'block.tnd.spatial_restorer':('Noyau muet','Silent Core'),'item.tnd.spatial_lens':('Lentille opaline','Opaline Lens'),
    'story.tnd.restorer':('Noyau muet','Silent Core'),'story.tnd.chapter.0':('Signal','Signal'),'story.tnd.chapter.1':('Socle','Base'),
    'story.tnd.signal':('Quelque chose répond, au milieu des ruines.','Something answers among the ruins.'),
    'story.tnd.detached':('Le vent s’est arrêté.','The wind has stopped.'),
    'story.tnd.revelation':('La fissure du seuil. La lumière dans votre atelier. Tout est là. Le noyau émet encore une note, puis se tait.','The crack in the threshold. The light in your workshop. Everything is here. The core sounds one last note, then falls silent.'),
    'story.tnd.accept':('Suivre le signal','Follow the signal'),
    'story.tnd.restore_text':('… RÉPONSE REÇUE\n\nUne ligne persiste dans les relevés. Elle ne rejoint aucune des ruines. Lorsque vous approchez la main, son tracé cesse de trembler.\n\nLe noyau attend.','… RESPONSE RECEIVED\n\nOne line persists in the readings. It touches none of the ruins. When your hand approaches, it stops trembling.\n\nThe core waits.'),
    'story.tnd.preparing':('PORTEUSE DÉTECTÉE\n\nLes anneaux accélèrent sans bruit. Huit impulsions, puis une neuvième. La dernière ne vient pas du réacteur.\n\nVous pouvez interrompre la liaison avec Annuler ou en vous éloignant de plus de 32 blocs.','CARRIER DETECTED\n\nThe rings accelerate without a sound. Eight pulses, then a ninth. The last one does not come from the reactor.\n\nCancel or move more than 32 blocks away to interrupt the link.'),
    'story.tnd.reactor_intro':('08 / PORTEUSES ATTENDUES\n\nLes huit archives emploient le même intervalle. Sous les relevés, quelqu’un a gravé une seule consigne :\n\n« Garder le point fixe en ligne. »','08 / CARRIERS EXPECTED\n\nAll eight records use the same interval. Beneath the readings, someone has scratched one instruction:\n\n“Keep the fixed point online.”'),
    'story.tnd.restorer_intro':('PÉRIPHÉRIQUE NON IDENTIFIÉ\n\nUn cercle manque au schéma. Le noyau réagit à votre main, mais aucun ordre ne s’inscrit.\n\nSur la bordure, presque effacé :\n« Ne pas déplacer le point fixe. »','UNIDENTIFIED PERIPHERAL\n\nA circle is missing from the diagram. The core responds to your hand, but records no command.\n\nAlmost erased along the rim:\n“Do not move the fixed point.”'),
    'story.tnd.assembly_restorer':('NOYAU · MONTAGE\n\nPlacez le noyau au centre d’un socle 3 × 3. Une couche en dessous : huit charpentes industrielles autour du centre.\n\nÀ hauteur du noyau : quatre bobines relais aux angles. Gardez l’avant accessible.\n\nLa liaison au réacteur porte jusqu’à douze blocs.','CORE · ASSEMBLY\n\nPlace the core at the centre of a 3 × 3 base. One layer below: eight industrial frames around the centre.\n\nAt core height: four relay coils at the corners. Keep the front accessible.\n\nThe reactor link reaches twelve blocks.'),
    'story.tnd.ready.0':('Liaison stable · 8/8 porteuses','Link stable · 8/8 carriers'),'story.tnd.ready.4':('Noyau assemblé requis à moins de 12 blocs','Assembled core required within 12 blocks'),
    'story.tnd.ready.6':('Signal stabilisé','Signal stabilized'),'story.tnd.restorer_incomplete':('Socle incomplet · onglet Socle','Base incomplete · Base tab'),
    'story.tnd.cinema':('…','…'),'story.tnd.guidance':('Une note persiste · %s m · X %s, Z %s','A note persists · %s m · X %s, Z %s'),
    'story.tnd.perception':('Une ligne apparaît entre les lignes.','A line appears between the lines.'),
    'story.tnd.finished':('La note demeure dans la lentille. À travers elle, certains contours se prolongent au-delà de la pierre.\n\nUtilisation : clic droit pour percevoir pendant 15 s ; accroupi, clic droit pour franchir jusqu’à 8 blocs vers un sol libre.\n\nUne lentille de remplacement délie la précédente.','The note remains within the lens. Through it, some edges continue beyond the stone.\n\nUse: right-click for 15 s perception; sneak and right-click to step up to 8 blocks onto clear ground.\n\nA replacement lens unbinds the previous one.'),
    'tooltip.tnd.spatial_restorer':('Un cercle incomplet. Son écran conserve un schéma de montage.','An incomplete circle. Its screen retains an assembly diagram.'),
    'tooltip.tnd.convergence_reactor':('Huit porteuses. Un point fixe à proximité. Le schéma de montage est sur l’écran.','Eight carriers. A fixed point nearby. The assembly diagram remains on its screen.'),
    'codex.tnd.locked_text':('Le relevé est incomplet. D’autres terminaux portent ce symbole dans les quartiers de Nacre.','The reading is incomplete. Other terminals carry this symbol throughout Nacre.'),
    'echo.tnd.examine':('Clic droit : écouter cette trace','Right-click: listen to this trace'),
    'echo.tnd.trace.0':('L’affichage indique une charge. Aucun courant ne passe.','The display shows a charge. No current passes.'),
    'echo.tnd.trace.1':('La même poussière, jusqu’à la dernière étagère. Aucune empreinte.','The same dust, down to the last shelf. No footprints.'),
    'echo.tnd.trace.2':('Une vibration traverse le métal. Elle s’interrompt lorsque vous reculez.','A vibration passes through the metal. It stops when you step away.'),
    'echo.tnd.trace.3':('Le cercle est intact. Tout autour, la pierre s’est ouverte.','The circle is intact. All around it, the stone has split.'),
    'echo.tnd.count':('Traces écoutées : %s / 4','Traces heard: %s / 4')}
    for k,(a,b) in mysterious.items():fr[k]=a;en[k]=b
    for i,a,b in [
    (3,' En marge du registre : un cercle, huit repères, une bobine dessinée quatre fois. Le nom du dispositif a été gratté.',' In the margin: a circle, eight marks, a coil drawn four times. The device’s name has been scratched away.'),
    (6,' Annexe IRIS, dernière relève : « Le petit sujet respire encore. J’ai laissé sa ration là où les lampes ne portent pas. Ne le réveillez pas au milieu des sirènes. »',' IRIS annex, final shift: “The small subject is still breathing. I left its ration beyond the reach of the lamps. Do not wake it among the sirens.”'),
    (7,' Au verso, une autre écriture : « Nous entendons vos pas. Ne cherchez pas nos portes. Gardez un point fixe. » Le dernier mot traverse la feuille sans y laisser de trou.',' On the back, another hand: “We hear your footsteps. Do not look for our doors. Keep a fixed point.” The final word passes through the page without leaving a hole.')]:
        fr[f'codex.tnd.text.{i}']+=a;en[f'codex.tnd.text.{i}']+=b

    contexts={
    'mining':('Tenez une pioche pour utiliser cette capacité.','Hold a pickaxe to use this ability.'),
    'logging':('Tenez une hache pour utiliser cette capacité.','Hold an axe to use this ability.'),
    'farming':('Cultures non mûres à moins de 5 blocs et 4 poudres d’os nécessaires.','Requires unripe crops within 5 blocks and 4 bone meal.'),
    'breeding':('Animaux blessés à moins de 8 blocs et 4 blés nécessaires.','Requires injured animals within 8 blocks and 4 wheat.'),
    'melee':('Aucun hostile visible à moins de 5 blocs.','No visible hostile within 5 blocks.'),
    'crafting':('Tenez un objet abîmé et emportez un alliage trempé.','Hold a damaged item and carry one tempered alloy.'),
    'alchemy':('Poison, Wither ou Faiblesse et une fiole de miel nécessaires.','Requires Poison, Wither or Weakness and one honey bottle.'),
    'engineering':('Visez un extracteur à calibrer à moins de 5 blocs ; circuit de contrôle requis.','Aim at an extractor needing calibration within 5 blocks; requires a control circuit.')}
    for m in ['fishing','hunting','ranged','defense','building','exploration','ancient_tech']:contexts[m]=('Cette capacité nécessite sa spécialisation et le mode Survie.','Requires its specialization and Survival mode.')
    for m,(a,b) in contexts.items():fr['ability.tnd.context.'+m]=a;en['ability.tnd.context.'+m]=b
    fr['ability.tnd.help']+=' Capacités réservées au mode Survie.';en['ability.tnd.help']+=' Abilities require Survival mode.'
    summaries=[
    ('Repérage minier : +4 blocs.','Mining sensing: +4 blocks.'),('Pioche · Célérité II, 15 s.','Pickaxe · Haste II, 15 s.'),
    ('Pousse de votre inventaire · Coupe accroupie.','Your own sapling · Sneak harvest.'),('Hache · Célérité II, 25 s et repérage.','Axe · Haste II, 25 s and sensing.'),
    ('Vos graines · Replantation en récoltant accroupi.','Your own seeds · Sneak to harvest and replant.'),('4 poudres d’os · 8 cultures, un stade.','4 bone meal · 8 crops, one growth stage.'),
    ('Respiration aquatique lorsque vous nagez.','Water Breathing while in water.'),('Chance II et respiration, 30 s.','Luck II and Water Breathing, 30 s.'),
    ('Repérage des proies : +4 blocs.','Prey sensing: +4 blocks.'),('Approche invisible et rapide, 10 s.','Invisible, swift approach, 10 s.'),
    ('Descendance : +20 % de santé maximale.','Offspring: +20% maximum health.'),('4 blés · Soigne les animaux proches.','4 wheat · Heal nearby animals.'),
    ('Cible isolée : +12 % de dégâts, hors joueurs.','Isolated target: +12% damage, excludes players.'),('Repousse et ralentit les hostiles visibles.','Push back and slow visible hostiles.'),
    ('Tir à plus de 12 blocs : +15 % de dégâts.','Shots beyond 12 blocks: +15% damage.'),('Prochain projectile : ralentit sa cible.','Next projectile hit slows its target.'),
    ('Au sol : +25 % de résistance au recul.','Grounded: +25% knockback resistance.'),('4 cœurs temporaires, 10 s.','4 temporary hearts, 10 s.'),
    ('Équipements neufs : +10 % de durabilité.','New crafted equipment: +10% durability.'),('1 alliage trempé · Répare 25 % de l’objet en main.','1 tempered alloy · Repair 25% of the held item.'),
    ('Bloc en main : +0,75 bloc de portée.','Hold a block: +0.75 block reach.'),('Chute lente, 30 s.','Slow Falling, 30 s.'),
    ('Pas plus rapides sous le ciel ouvert.','Faster steps beneath open sky.'),('Vitesse II et saut, 20 s.','Speed II and jump, 20 s.'),
    ('Potions bénéfiques : +20 % de durée.','Beneficial potions: +20% duration.'),('1 miel · Retire Poison, Wither et Faiblesse.','1 honey · Remove Poison, Wither and Weakness.'),
    ('Extracteur : +10 % de calibration permanente.','Extractor: +10% permanent calibration.'),('1 circuit · Calibration de relique à 25 %.','1 circuit · Raise relic calibration to 25%.'),
    ('Repérage ancien : +4 blocs.','Ancient sensing: +4 blocks.'),('Repère les traces et affaiblit les hostiles.','Sense traces and weaken nearby hostiles.')]
    for (branch,*_), (a,b) in zip(BRANCHES,summaries):fr['talent.tnd.'+branch+'.summary']=a;en['talent.tnd.'+branch+'.summary']=b
    fr['prestige.tnd.cosmetic']='Titres et sceaux · cosmétiques';en['prestige.tnd.cosmetic']='Titles and seals · cosmetic'
    fr['gui.tnd.talent_cost']='Coût : %s pt par rang';en['gui.tnd.talent_cost']='Cost: %s pt per rank'
