"""Endgame, persistent factory anchors and isolated narrative projection resources."""
import json, random
BLOCKS=['spatial_anchor','spatial_restorer','convergence_reactor']
ITEMS=['spatial_lens']
TESTS=['echo_respawn','echo_camera','anchor_energy','anchor_limits','anchor_recovery','piston_provenance','echo_journals','echo_reference','echo_copy','echo_return','echo_interrupt','echo_corruption','echo_choices','spatial_lens','rare_anomaly']

def content(write,png,assets,data):
    def box(a,b,t,light=0):
        out={'from':a,'to':b,'faces':{d:{'texture':t} for d in ['north','south','east','west','up','down']}}
        if light:out['light_emission']=light
        return out
    for name in BLOCKS:
        rng=random.Random(name);pixels=[]
        for y in range(16):
            for x in range(16):
                n=rng.randrange(-6,7);c=(48+n,60+n,68+n,255)
                if x in (0,1,14,15) or y in (0,1,14,15):c=(112+n,129+n,135+n,255)
                if 4<=x<=11 and 4<=y<=11:c=(24+n,39+n,47+n,255)
                if (x+y)%7==0 and 4<=x<=11 and 4<=y<=11:c=(127,209,210,255)
                if x in (2,13) and y in (2,13):c=(202,179,131,255)
                pixels.append(c)
        png(assets/f'textures/block/{name}.png',pixels)
        elements=[box([0,0,0],[16,2,16],'#frame'),box([1,2,1],[15,12,15],'#body'),box([2,12,2],[14,14,14],'#frame')]
        if name=='spatial_anchor':
            elements += [box([7,13,7],[9,16,9],'#light',7)]
            for x,z in [(2,2),(12,2),(2,12),(12,12)]:elements+=[box([x,5,z],[x+2,16,z+2],'#frame')]
        elif name=='spatial_restorer':
            elements += [box([3,14,3],[13,15,13],'#light',10),box([5,15,5],[11,16,11],'#body')]
            for x in [1,13]:elements+=[box([x,3,0],[x+2,13,2],'#frame')]
        else:
            elements += [box([4,14,4],[12,16,12],'#light',10)]
            for x,z in [(0,0),(14,0),(0,14),(14,14)]:elements+=[box([x,2,z],[x+2,15,z+2],'#frame')]
        model={'textures':{'body':'tnd:block/'+name,'frame':'tnd:block/industrial_frame','light':'tnd:block/neon_cyan','particle':'tnd:block/'+name},'elements':elements}
        write(assets/f'models/block/{name}.json',model)
        if name=='spatial_anchor':
            idle=json.loads(json.dumps(model));idle['textures']['light']='tnd:block/carbon_panel';write(assets/f'models/block/{name}_idle.json',idle)
            states={'lit=true':{'model':'tnd:block/'+name},'lit=false':{'model':'tnd:block/'+name+'_idle'}}
        else:states={'':{'model':'tnd:block/'+name}}
        write(assets/f'blockstates/{name}.json',{'variants':states})
        write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    pixels=[]
    for y in range(16):
        for x in range(16):
            d=abs(x-7.5)+abs(y-7.5);c=(0,0,0,0)
            if d<=7:c=(159,184,194,255) if d>5 else (62,127,145,255) if d>3 else (189,242,230,255)
            if d<5 and (x==5 or y==5):c=(235,245,230,255)
            pixels.append(c)
    png(assets/'textures/item/spatial_lens.png',pixels)
    write(assets/'blockstates/phase_lattice.json',{'variants':{'':{'model':'tnd:block/phase_lattice'}}})
    write(assets/'models/block/phase_lattice.json',{'textures':{'particle':'tnd:block/gate_light'},'elements':[]})
    recipes={
      'spatial_anchor':(['NCN','OAO','FCF'],{'N':'tnd:noctilite_crystal','C':'tnd:control_circuit','O':'tnd:copper_coil','A':'minecraft:netherite_ingot','F':'tnd:industrial_frame'}),
      'spatial_restorer':(['UAU','CIC','FRF'],{'U':'tnd:umbra_alloy','A':'tnd:auralite_ingot','C':'tnd:control_circuit','I':'tnd:inertial_matrix','F':'tnd:industrial_frame','R':'tnd:resonance_heart'}),
      'convergence_reactor':(['NIN','CWC','URU'],{'N':'minecraft:netherite_ingot','I':'tnd:inertial_matrix','C':'tnd:composite_crystal','W':'minecraft:wither_skeleton_skull','U':'tnd:umbra_alloy','R':'minecraft:nether_star'})}
    for name,(pattern,key) in recipes.items():
        write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'tnd:'+name,'count':1}})
        write(data/f'advancement/recipes/{name}.json',{'criteria':{'item':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':{'spatial_anchor':'tnd:umbra_alloy','spatial_restorer':'tnd:inertial_matrix','convergence_reactor':'tnd:spatial_restorer'}[name]}]}}},'requirements':[['item']],'rewards':{'recipes':[f'tnd:{name}']}})
    # The source world is never written by projection code. This separate level contains no structures, ores, monsters or exportable resources.
    dimension=json.loads((data/'dimension_type/vestige.json').read_text());dimension.update({'min_y':-64,'height':384,'logical_height':384,'default_clock':'tnd:aftermath','timelines':'#tnd:in_aftermath','has_skylight':False,'ambient_light':.22})
    dimension['attributes']={'minecraft:visual/sky_color':'#18262a','minecraft:visual/fog_color':'#6b7977','minecraft:visual/fog_start_distance':48.,'minecraft:visual/fog_end_distance':128.,'minecraft:visual/ambient_light_color':'#7b9391','minecraft:gameplay/bed_rule':{'can_set_spawn':'never','can_sleep':'never','error_message':{'translate':'story.tnd.no_sleep'}}}
    write(data/'dimension_type/aftermath.json',dimension);write(data/'world_clock/aftermath.json',{});write(data/'tags/timeline/in_aftermath.json',{'values':[]})
    biome={'has_precipitation':False,'temperature':.3,'downfall':0.,'effects':{'water_color':'#46585a'},'attributes':{'minecraft:visual/ambient_particles':[{'particle':{'type':'minecraft:white_ash'},'probability':.004}]},'carvers':[],'features':[[] for _ in range(11)],'spawn_costs':{},'spawners':{n:[] for n in ['ambient','axolotls','creature','misc','monster','underground_water_creature','water_ambient','water_creature']}}
    write(data/'worldgen/biome/aftermath.json',biome)
    write(data/'dimension/aftermath.json',{'type':'tnd:aftermath','generator':{'type':'minecraft:flat','settings':{'biome':'tnd:aftermath','layers':[{'block':'minecraft:air','height':1}],'structure_overrides':[],'features':False,'lakes':False}}})
    for tag in ['mineable/pickaxe','needs_diamond_tool']:
        p=data.parent/f'minecraft/tags/block/{tag}.json';s=json.loads(p.read_text()) if p.exists() else {'values':[]};s['values']+=['tnd:'+n for n in BLOCKS];write(p,s)
    for test in TESTS:write(data/f'test_instance/{test}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+test,'structure':'tnd:test_habitat','max_ticks':2400 if test.startswith('echo_') else 800,'required':True})

def languages(fr,en):
    names={'spatial_anchor':('Ancre d’atelier','Workshop Anchor'),'spatial_restorer':('Restaurateur spatial','Spatial Restorer'),'convergence_reactor':('Réacteur de convergence','Convergence Reactor'),'phase_lattice':('Projection tétrale','Tetrad Projection')}
    for n,(a,b) in names.items():fr['block.tnd.'+n]=a;en['block.tnd.'+n]=b
    fr['item.tnd.spatial_lens']='Lentille de quatrième axe';en['item.tnd.spatial_lens']='Fourth-Axis Lens'
    fr['biome.tnd.aftermath']='Rémanence';en['biome.tnd.aftermath']='Aftermath'
    anchor={'start':('Activer l’ancre','Activate anchor'),'stop':('Suspendre','Suspend'),'chunks':('%s / 9 secteurs actifs','%s / 9 active sectors'),'cost':('16 FE/t · 9 secteurs','16 FE/t · 9 sectors'),'limit':('2 par joueur','2 per player'),'assembly':('Socle : 8 charpentes · 4 bobines relais','Base: 8 frames · 4 relay coils'),
    'status.0':('Ancre suspendue','Anchor suspended'),'status.1':('Socle incomplet : 8 charpentes autour, une couche sous l’ancre ; 4 bobines aux angles, à sa hauteur.','Incomplete base: 8 frames around one layer below; 4 relay coils at the corners, at anchor height.'),'status.2':('Suspendue par la redstone','Paused by redstone'),'status.3':('Le propriétaire doit être connecté','Owner must be online'),'status.4':('Énergie insuffisante','Not enough energy'),'status.5':('Limite d’ancres atteinte','Anchor limit reached'),'status.6':('L’atelier travaille pendant vos expéditions','Your workshop works during expeditions')}
    story={
    'title':('Convergence','Convergence'),'busy':('Une autre convergence est en préparation. Réessayez dans un instant.','Another convergence is being prepared. Try again shortly.'),'prepare_failed':('Les références n’ont pas pu être vérifiées. Rien n’a été activé.','The references could not be verified. Nothing has been activated.'),
    'signal':('Un signal persiste dans les ruines. Le restaurateur vous attend.','A signal persists among the ruins. The restorer awaits you.'),'detached':('Le silence est total. Explorez ce qui reste.','Silence. Explore what remains.'),'return_blocked':('Le point de retour est obstrué. Le journal est conservé ; réessayez avec /tnd recover.','The return point is obstructed. Your journal is retained; try /tnd recover again.'),
    'folded_exit':('L’espace se replie. Retrouvez le restaurateur, ou utilisez /tnd recover.','Space folds back. Find the restorer, or use /tnd recover.'),'revelation':('La base est intacte. Ce que vous avez vu était une autre section du même espace. Le restaurateur a retenu votre axe d’origine. Une lentille vous attend dans son interface.','Your base is intact. What you saw was another cross-section of the same space. The restorer retained your original axis. A lens awaits in its interface.'),
    'lens':('Recevoir une lentille liée','Receive a bound lens'),'accept':('Restaurer la configuration','Restore configuration'),'refuse':('Continuer à explorer','Keep exploring'),'activate':('Établir la convergence','Establish convergence'),'cancel':('Annuler la préparation','Cancel preparation'),'close':('Fermer','Close'),
    'finished':('Votre axe d’origine est stable. La configuration ruinée est restée isolée.\n\nLa lentille permet de percevoir les volumes cachés pendant quinze secondes. Accroupi, elle effectue un pas spatial vers un sol libre, jusqu’à huit blocs.\n\nUne lentille de remplacement invalide la précédente. Seul son propriétaire peut l’utiliser.','Your original axis is stable. The ruined configuration remained isolated.\n\nThe lens reveals hidden volumes for fifteen seconds. While sneaking, it makes a spatial step onto clear ground up to eight blocks away.\n\nA replacement lens invalidates the previous one. Only its owner can use it.'),
    'restore_text':('CONFIGURATION SPATIALE ANTÉRIEURE IDENTIFIÉE.\n\nVotre présence relie les lignes. Les ruines appartiennent à une autre section de l’espace.\n\nRestauration possible.\n\nAccepter vous ramène à votre base et à vos ressources. Refuser vous laisse explorer ; vous pourrez revenir ici sans limite de temps.','PREVIOUS SPATIAL CONFIGURATION IDENTIFIED.\n\nLines converge around one fixed point: your presence. What appears destroyed belongs to another cross-section of space.\n\nRestoration available.\n\nAccept to return to your base and resources. Decline to keep exploring; you may return here whenever you wish.'),
    'preparing':('Les plans se superposent. Le restaurateur fixe une référence avant d’ouvrir le quatrième axe.\n\nVous pouvez continuer à circuler près du réacteur. Éloignez-vous de plus de trente-deux blocs ou utilisez le bouton Annuler pour interrompre la préparation.\n\nAucune décision ne vous sera imposée après la convergence.','The planes overlap. The restorer fixes a reference before opening the fourth axis.\n\nYou may move near the reactor. Moving more than thirty-two blocks away or cancelling this preparation aborts it.\n\nNo decision will be imposed after convergence.'),
    'reactor_intro':('LES HUIT ARCHIVES SONT UNE SEULE PHRASE.\n\nLes derniers habitants de Nacre ont relié leurs machines jusqu’à épuiser la frontière entre les axes. Le réacteur peut reproduire leur expérience.\n\nAssemblez son socle et un restaurateur à douze blocs maximum. Retrouvez les huit archives avant de poursuivre. Le restaurateur doit rester en place.','THE EIGHT ARCHIVES FORM ONE SENTENCE.\n\nNacre’s final inhabitants linked their machines until the boundary between axes wore away. This reactor can repeat their experiment.\n\nAssemble its base and a restorer within twelve blocks. Find all eight archives before proceeding. Keep the restorer in place.'),
    'restorer_intro':('DISPOSITIF SANS FONCTION CONNUE.\n\nLe noyau réagit à votre présence, puis se tait. Ses anneaux ne tournent autour d’aucun axe visible. Une inscription demeure :\n\n« Quand le monde cessera de tenir dans vos trois directions, revenez au point qui n’a pas bougé. »\n\nConsultez les pages suivantes pour le socle et le réacteur.','DEVICE OF UNKNOWN PURPOSE.\n\nThe core responds to your presence, then falls silent. Its rings turn around no visible axis. One inscription remains:\n\n“When the world no longer fits inside your three directions, return to the point that did not move.”\n\nSee the following pages for its base and reactor.'),
    'assembly_restorer':('RESTAURATEUR · CONSTRUCTION\n\nPlacez le restaurateur au centre d’un socle 3 × 3. Une couche en dessous, disposez huit charpentes industrielles autour de son centre.\n\nÀ la hauteur du restaurateur, placez quatre bobines relais aux angles du socle. Laissez l’avant accessible.\n\nLe restaurateur doit se trouver à douze blocs maximum du réacteur.','RESTORER · ASSEMBLY\n\nPlace the restorer at the centre of a 3 × 3 base. One layer below, put eight industrial frames around its centre.\n\nAt restorer height, place four relay coils at the base corners. Leave the front accessible.\n\nThe restorer must be within twelve blocks of the reactor.'),
    'assembly_reactor':('RÉACTEUR · CONSTRUCTION\n\nUne couche sous le réacteur, formez le contour d’un carré 5 × 5 avec seize charpentes industrielles.\n\nAux quatre angles, montez trois panneaux de carbone. Coiffez chaque colonne d’une bobine relais. Le réacteur est au centre, un bloc au-dessus des charpentes.\n\nLes huit archives doivent être réunies par la personne qui l’active.','REACTOR · ASSEMBLY\n\nOne layer below the reactor, make a 5 × 5 border with sixteen industrial frames.\n\nAt each corner, build three carbon-panel blocks upwards. Cap each column with a relay coil. The reactor sits at the centre, one block above the frames.\n\nThe player activating it must have gathered all eight archives.'),
    'ready.0':('Réacteur et restaurateur prêts · 8/8 archives','Reactor and restorer ready · 8/8 archives'),'ready.1':('Requis : Overworld, hors observation','Required: Overworld, outside spectator mode'),'ready.2':('Archives retrouvées : %s / 8','Archives found: %s / 8'),'ready.3':('Socle du réacteur incomplet','Reactor base incomplete'),'ready.4':('Restaurateur assemblé à moins de 12 blocs requis','An assembled restorer within 12 blocks is required'),'ready.5':('Convergence en préparation','Convergence preparing'),'ready.6':('Votre configuration est stable','Your configuration is stable'),
    'restorer_ready':('Socle complet · noyau silencieux','Base complete · core silent'),'restorer_incomplete':('Socle incomplet · consulter la page 2','Incomplete base · see page 2'),'cinema':('Le quatrième axe cède.','The fourth axis gives way.'),'skip':('Accroupir : reprendre le contrôle','Sneak: regain control'),'guidance':('Signal du restaurateur · %s m · X %s, Z %s','Restorer signal · %s m · X %s, Z %s'),'restoring':('Les lignes retrouvent leur place…','The lines fall back into place…'),'no_sleep':('Ce lieu n’appartient pas au temps.','This place is outside time.')}
    story.update({'lens_inactive':('Cette lentille n’est plus liée. Retrouvez votre restaurateur.','This lens is unbound. Return to your restorer.'),'chapter.0':('Résonance','Resonance'),'chapter.1':('Restaurateur','Restorer'),'chapter.2':('Réacteur','Reactor'),'restorer':('Restaurateur spatial','Spatial Restorer'),'reactor':('Réacteur de convergence','Convergence Reactor'),'step_blocked':('Aucun sol dégagé dans les huit prochains blocs.','No clear ground within the next eight blocks.'),'perception':('Un quatrième axe se dessine.','A fourth axis takes shape.')})
    for n,a,b in [('spatial_anchor','8 charpentes sous le contour, 4 bobines aux angles. 16 FE/t. Deux ancres par propriétaire connecté.','8 frames below the border, 4 coils at the corners. 16 FE/t. Two anchors per online owner.'),('spatial_restorer','Un noyau silencieux. Consultez son interface pour construire le socle et découvrir son usage.','A silent core. Consult its interface to build its base and discover its purpose.'),('convergence_reactor','Fin de parcours : huit archives, un socle 5 × 5 et un restaurateur assemblé à moins de 12 blocs.','Endgame: eight archives, a 5 × 5 base and an assembled restorer within 12 blocks.'),('spatial_lens','Clic droit : perception, 15 s. Accroupi : pas spatial, 8 blocs maximum. Recharge partagée.','Right-click: 15 s perception. Sneak: spatial step, up to 8 blocks. Shared cooldown.')]:fr['tooltip.tnd.'+n]=a;en['tooltip.tnd.'+n]=b
    story['no_reference']=('Aucun retour en attente. Les journaux déjà terminés restent conservés.','No pending return. Completed journals remain retained.')
    for prefix,pairs in [('anchor',anchor),('story',story)]:
        for key,(a,b) in pairs.items():fr[f'{prefix}.tnd.{key}']=a;en[f'{prefix}.tnd.{key}']=b

    # Later records reinterpret the earlier evacuation story; the fourth axis is spatial, never time travel.
    fr['codex.tnd.text.4']=fr['codex.tnd.text.4'].replace('dont les responsables sont morts depuis longtemps','dont les responsables ne répondent plus depuis longtemps')
    en['codex.tnd.text.4']=en['codex.tnd.text.4'].replace('whose commanders are long dead','whose commanders no longer answer')
    fr['codex.tnd.text.3'] += " Les plans d’un autre dispositif sont joints au registre. Un restaurateur, dit l’étiquette. Sa matrice s’obtient auprès du Conservateur. Nous l’avons reconstruit sans jamais parvenir à lui faire produire quoi que ce soit. Conservez-le : son silence ne prouve pas son inutilité."
    en['codex.tnd.text.3'] += ' Plans for another device accompany the register. A restorer, says the label. Its matrix comes from the Custodian. We rebuilt it without ever making it produce anything. Keep it: silence does not prove uselessness.'
    fr['codex.tnd.text.6'] += " Une correction avant de terminer : aucun convoi n’a quitté Nacre. Les habitants sont partis un par un, sans traverser les portes. Les relevés d’occupation affichent encore leur présence ; ils ne tiennent simplement plus dans nos trois directions. IRIS conservait un compagnon dans sa capsule centrale, avec un ordinateur d’ouverture et des nutriments dans l’annexe. Il faudra d’abord gagner sa confiance en le laissant se défendre."
    en['codex.tnd.text.6'] += ' One correction before the end: no convoy left Nacre. People departed one by one, without passing through doors. Occupancy readings still register their presence; they simply no longer fit inside our three directions. IRIS kept a companion in the central capsule, with a release computer and nutrients in the annex. First earn its trust by letting it defend itself.'
    fr['codex.tnd.text.7'] += " Annexe retrouvée : nous n’avons pas disparu. Nous avons rejoint le quatrième axe spatial. Nous voyons vos chemins comme vous voyez les lignes d’un dessin. Ce n’est pas le temps qui se plie. Le réacteur superpose des sections d’un même volume ; le restaurateur retient celle où vous avez commencé. Reconstruisez son socle avant de tenter la convergence. Lorsqu’une voix vous dira que tout est perdu, cherchez le point qui ne bouge pas."
    en['codex.tnd.text.7'] += ' Recovered appendix: we did not vanish. We reached the fourth spatial axis. We see your paths as you see lines in a drawing. Time is not what bends. The reactor overlaps cross-sections of one volume; the restorer retains the one where you began. Rebuild its base before attempting convergence. When a voice says everything is lost, find the point that does not move.'
