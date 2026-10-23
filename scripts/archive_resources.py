"""Original archive prisms, native pixel relic and authored character atlases. No outside art."""
import json,math,random
BLOCKS=['archive_mirror','archive_anchor']
ITEMS=['silent_archive_lens']
ENTITIES=['archive_keeper','archive_eraser','silent_conservator']
SOUNDS=['archive_turn','archive_memory','archive_atlas','archive_awakening','archive_relic']
TESTS=['archive_scene','archive_circuit','archive_echo','archive_generation','archive_paths','archive_route','archive_restore','archive_boss','archive_patterns','archive_shortcut','archive_familiar','relic_unlock','relic_authority','relic_dodge','relic_guard','relic_companion','relic_restore']
def content(write,png,assets,data):
    def box(a,b,t):return {'from':a,'to':b,'faces':{s:{'texture':t} for s in ['up','down','north','south','east','west']}}
    # Etched gauge: zero and three native Roman marks around the dark metal rim.
    pixels=[];rng=random.Random('archive-gauge')
    for y in range(16):
        for x in range(16):
            v=rng.randrange(-5,6);c=(33+v,40+v,46+v)
            if x in [0,15] or y in [0,15]:c=(87+v,97+v,103+v)
            if x in [6,9] and y in [1,3] or x in [13] and 6<=y<=10 or x in [6,9] and 12<=y<=14 or x in [1,3,5] and 6<=y<=10:c=(222,207,154)
            pixels.append((*c,255))
    png(assets/'textures/block/archive_gauge.png',pixels)
    for name in BLOCKS:
        for lit in [False,True]:
            for step in range(4):
                t={'particle':'tnd:block/obsidian_metal','frame':'tnd:block/obsidian_relief','dark':'tnd:block/obsidian_metal','light':'tnd:block/'+('luminous_script' if lit else 'memory_lattice'),'gauge':'tnd:block/archive_gauge'}
                e=[box([1,0,1],[15,3,15],'#frame'),box([3,3,3],[13,5,13],'#dark'),box([6,5,6],[10,10,10],'#frame')]
                if name=='archive_mirror':
                    e += [box([0,5,0],[16,6,16],'#gauge'),box([6,6,6],[10,8,10],'#frame'),box([7,8,7],[9,16,9],'#light'),box([4,10,6],[12,14,10],'#frame'),box([5,11,5],[11,13,6],'#light')]
                    e.append(box([[7,13,1],[11,13,7],[7,13,11],[1,13,7]][step],[[9,15,5],[15,15,9],[9,15,15],[5,15,9]][step],'#light'))
                else:
                    for x in [2,12]:e += [box([x,5,2],[x+2,16,4],'#frame'),box([x,5,12],[x+2,16,14],'#frame')]
                    e += [box([2,15,2],[14,17,14],'#frame'),box([5,8,5],[11,14,11],'#light'),box([7,6,0],[9,12,2],'#light')]
                write(assets/f'models/block/{name}_{step}_{int(lit)}.json',{'textures':t,'elements':e})
        write(assets/f'blockstates/{name}.json',{'variants':{f'lit={str(lit).lower()},step={step}':{'model':f'tnd:block/{name}_{step}_{int(lit)}'} for lit in [False,True] for step in range(4)}})
        write(assets/f'models/block/{name}.json',{'parent':f'tnd:block/{name}_0_1'});write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
    # Asymmetric chipped cage, inset lens, restrained highlight and pitted iron at native 16px.
    p=[(0,0,0,0)]*256;rng=random.Random('silent-lens')
    for y in range(1,15):
        for x in range(1,15):
            dx,dy=x-7.5,y-7.5;r=dx*dx+dy*dy
            if r>45 or (x,y) in [(2,5),(12,3),(13,10)]:continue
            v=rng.randint(-8,8);c=(38+v,46+v,53+v)
            if r>30:c=(85+v,97+v,105+v)
            if r<20:c=(91+v,169+v,177+v)
            if r<8:c=(221+v,226+v,197+v)
            if (x+y)%5==0 and r>20:c=(153+v,136+v,98+v)
            if (x,y) in [(5,3),(4,4),(3,6),(6,6)]:c=(232,236,218)
            p[y*16+x]=(*c,255)
    png(assets/'textures/item/silent_archive_lens.png',p);write(assets/'models/item/silent_archive_lens.json',{'parent':'minecraft:item/generated','textures':{'layer0':'tnd:item/silent_archive_lens'}});write(assets/'items/silent_archive_lens.json',{'model':{'type':'minecraft:model','model':'tnd:item/silent_archive_lens'}})
    for k,name in enumerate(ENTITIES):
        for opened in [False,True]:
            rng=random.Random(name);atlas=[]
            for y in range(64):
                for x in range(64):
                    n=rng.randint(-5,5);c=(26+n,33+n,39+n)
                    if x%8 in [0,1] or y%8==0:c=(78+n,87+n,93+n)
                    if y>=32 and y%8 in [2,3]:c=(114+n,119+n,115+n)
                    if (x*5+y*3)%47==0:c=(141+n,125+n,90+n)
                    if x>=48 and y<16:c=(249,236,186) if opened else (133,209,215) if k!=1 else (177,149,218)
                    atlas.append((*c,255))
            png(assets/f'textures/entity/{name}{"_open" if opened else ""}.png',atlas,64,64)
        color=(162,224,225) if k!=1 else (187,163,228)
        png(assets/f'textures/entity/{name}_glow.png',[(*color,255) if x>=48 and y<16 else (0,0,0,0) for y in range(64) for x in range(64)],64,64)
        egg=[(*((48,58,67) if (x+y)%3 else color),255) if ((x-7.5)/5)**2+((y-7.5)/6.5)**2<=1 else (0,0,0,0) for y in range(16) for x in range(16)]
        png(assets/f'textures/item/{name}_spawn_egg.png',egg);write(assets/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'tnd:item/{name}_spawn_egg'}});write(assets/f'items/{name}_spawn_egg.json',{'model':{'type':'minecraft:model','model':f'tnd:item/{name}_spawn_egg'}})
        pools=[] if k==2 else [{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:mnemonic_shard','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]}]
        write(data/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
    for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','function':'tnd:'+name,'environment':'minecraft:default','structure':'tnd:test_habitat','max_ticks':1800,'required':True})
def languages(fr,en):
    def pair(k,f,e):fr[k]=f;en[k]=e
    def t(k,f,e):pair('observatory.tnd.archive.'+k,f,e)
    for name,f,e in zip(BLOCKS,['Miroir des absents','Ancre de mémoire'],['Mirror of the Absent','Memory Anchor']):pair('block.tnd.'+name,f,e)
    pair('item.tnd.silent_archive_lens','Lentille du silence','Lens of Silence')
    for name,f,e in zip(ENTITIES,['Veilleur des archives','Effaceur de mémoire','Le Conservateur'],['Archive Keeper','Memory Eraser','The Conservator']):pair('entity.tnd.'+name,f,e);pair('item.tnd.'+name+'_spawn_egg','Œuf : '+f,f+' Spawn Egg')
    t('relic_use','Utiliser pour déverrouiller les reliques. Un seul accord actif.','Use to unlock relics. One active attunement.')
    t('act','III · Les Archives du Silence','III · The Silent Archives')
    rooms=['Portique des absents','Galerie des Veilleurs','Salle des Réfractions','Chambre des Effacements','Atlas suspendu','Le Palimpseste','Procession des Copies','Le Conservateur']
    erooms=['Portico of the Absent','Gallery of Keepers','Hall of Refractions','Chamber of Erasures','Suspended Atlas','The Palimpsest','Procession of Copies','The Conservator']
    goals=['Lisez les trois stèles et orientez les trois miroirs selon les marques au sol.','Les gardiens se réveillent. Sortez des marques avant leur effacement.','Orientez les miroirs : I, III, II. Revenez à la station pour confirmer.','Désarmez les quatre relais et éliminez leurs gardiens.','Activez l’ancre à droite, puis suivez les quatre résonances sur les deux galeries.','Retrouvez l’ordre des trois stèles grâce aux marques I, II, III. Une erreur reprend seulement la séquence.','Trois vagues finies. Les couloirs tracés annoncent les tirs des effaceurs.','Réveillez le Conservateur. Ses trois ancres exposent son noyau ; une recharge leur est commune.']
    egoals=['Read three stelae and turn the mirrors to match the floor marks.','The keepers awaken. Leave the marks before their erasure.','Turn the mirrors to I, III, II, then confirm at the station.','Disarm four relays and defeat their keepers.','Activate the anchor on the right, then follow four resonances across two galleries.','Find the stela order using I, II, III. A mistake restarts only the sequence.','Three finite waves. Marked corridors warn of the erasers’ shots.','Awaken the Conservator. Three anchors expose its core and share a cooldown.']
    for i in range(8):t('room.'+str(i),rooms[i],erooms[i]);t('objective.'+str(i),goals[i],egoals[i])
    for n,(f,e) in enumerate([('« Ce nom occupait davantage de place. »','“This name once took more space.”'),('Le relief montre une porte. La porte montre un mur.','The carving shows a door. The door shows a wall.'),('Sous la gravure : une autre gravure, identique, sans vous.','Beneath the engraving: the same engraving, without you.')]):t('trace.'+str(n),f,e)
    for n,(f,e) in enumerate([('« Nous nous souvenions pourtant du ciel. »','“And yet we remembered the sky.”'),('« Aucun témoin. Consigner leur présence. »','“No witnesses. Record their presence.”'),('« La première absence précède l’ouverture. »','“The first absence precedes the opening.”')]):t('echo.'+str(n),f,e)
    entries={
    'mirror_setting':('Orientation : %s','Orientation: %s'),'seek_mirrors':('Les marques au pied des miroirs indiquent leur accord.','The floor marks indicate each mirror’s alignment.'),'seek_echo':('Les marques ordonnent les souvenirs.','The marks order the memories.'),'mirrors':('Miroirs accordés : %s / 3','Aligned mirrors: %s / 3'),
    'manual_holding':('Ancre maintenue pour 180 s. Suivez la lumière.','Anchor held for 180 s. Follow the light.'),'pet_holding':('Votre familier maintient l’ancre. Suivez les galeries.','Your familiar is holding the anchor. Follow the galleries.'),'relay_needed':('L’ancre s’est éteinte. Votre parcours est conservé.','The anchor has faded. Your route progress is saved.'),'relay_time':('Ancre : %s s','Anchor: %s s'),'bridge_count':('Atlas · passage restauré : %s / 10','Atlas · restored crossing: %s / 10'),
    'echo_discord':('Un souvenir ne trouve pas sa place. Reprenez par I.','A memory fails to fit. Start again at I.'),'echo_progress':('Mémoire ordonnée : %s / 3','Ordered memory: %s / 3'),'echo_count':('Souvenirs ordonnés : %s / 3','Ordered memories: %s / 3'),
    'seal_wait':('Ancre en repos : %s s','Anchor resting: %s s'),'seal_open':('Le noyau est exposé.','The core is exposed.'),'seal_count':('Recharge des ancres : %s s','Anchor cooldown: %s s'),
    'attack.7':('Anneaux : gagnez le centre ou une ouverture lumineuse.','Rings: reach the centre or a luminous opening.'),'attack.8':('Effacement : quittez les trois couloirs marqués.','Erasure: leave the three marked lanes.'),'attack.9':('Copies : écartez-vous des cinq cercles.','Copies: leave the five circles.'),'phase.1':('Les anneaux ne tournent plus dans le même sens.','The rings no longer turn the same way.'),'phase.2':('Une dernière mémoire tient le noyau.','One last memory holds the core.'),
    'hint.mirror':('Clic droit : tourner le miroir · marques au sol','Right-click: turn mirror · floor marks'),'hint.anchor':('Clic droit : maintenir l’ancre · familier facultatif','Right-click: hold anchor · familiar optional'),'hint.seal':('Clic droit : exposer le noyau','Right-click: expose the core'),
    'relic_title':('Résonance des reliques','Relic Attunement'),'socket_hint':('Un seul accord actif · %s pour invoquer','One active attunement · %s to invoke'),'locked_hint':('Une lentille attend au-delà des Archives.','A lens awaits beyond the Archives.'),'invoke':('Invoquer la relique','Invoke Relic'),'attuned':('Accordée','Attuned'),'cooldown':('Recharge : %s s','Cooldown: %s s'),'relic_used':('Résonance · recharge %s s','Resonance · cooldown %s s'),
    'dodge_ground':('La traversée exige un appui au sol.','The crossing requires solid footing.'),'dodge_blocked':('Le passage est obstrué.','The passage is obstructed.'),'pet_needed':('Votre familier doit être déployé, debout et proche.','Your familiar must be deployed, standing and nearby.'),'pet_space':('Aucun espace sûr pour votre familier.','No safe space for your familiar.'),
    'relic.0.name':('Traversée','Crossing'),'relic.0.short':('Esquive de 4 blocs. Recharge : 12 s.','Dodge up to 4 blocks. Cooldown: 12 s.'),'relic.0.help':('Avance jusqu’à 4 blocs au sol, sans traverser les murs ni les vides.','Moves up to 4 blocks over solid ground, stopping at walls and gaps.'),
    'relic.1.name':('Égide','Aegis'),'relic.1.short':('Protection pendant 3 s. Recharge : 30 s.','Protection for 3 s. Cooldown: 30 s.'),'relic.1.help':('Résistance II et Absorption II pendant 3 secondes. Recharge partagée.','Resistance II and Absorption II for 3 seconds. Shared cooldown.'),
    'relic.2.name':('Rappel','Recall'),'relic.2.short':('Rappel offensif du familier. Recharge : 20 s.','Offensive familiar recall. Cooldown: 20 s.'),'relic.2.help':('Rappelle votre familier à moins de 48 blocs sur une case sûre. Force II pendant 5 s et frappe une cible visible proche.','Recalls your familiar within 48 blocks to safe ground. Strength II for 5 s and strikes a nearby visible enemy.')}
    for k,(f,e) in entries.items():t(k,f,e)
    pair('key.tnd.archive_relic','Invoquer la relique','Invoke Relic');pair('key.category.tnd.relics','The New Dimension · Reliques','The New Dimension · Relics')
    for name,f,e in zip(SOUNDS,['Un miroir s’oriente','Une mémoire s’accorde','L’atlas résonne','Le Conservateur s’éveille','Une relique répond'],['A mirror turns','A memory aligns','The atlas resonates','The Conservator awakens','A relic answers']):pair('subtitles.tnd.'+name,f,e)
