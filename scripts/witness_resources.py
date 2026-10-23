"""First-act mechanisms: native models reuse the original 16px stone/metal material library."""
BLOCKS=['observatory_filament','observatory_coupler','observatory_reliquary']
SOUNDS=['witness_current','witness_bridge','witness_reliquary','witness_awaken']
TESTS=['witness_circuit','witness_discoveries','witness_bridge','witness_gates','witness_boss','witness_restore','witness_generation']
def content(write,assets,data):
    def box(a,b,tex):
        w,h,d=[b[i]-a[i] for i in range(3)]
        return {'from':a,'to':b,'faces':{side:{'texture':tex,'uv':[0,0,w if side in ['north','south','up','down'] else d,d if side in ['up','down'] else h]} for side in ['north','south','east','west','up','down']}}
    def model(name,elements,light):write(assets/f'models/block/{name}.json',{'textures':{'body':'tnd:block/obsidian_relief','stone':'tnd:block/obsidian_tiles','frame':'tnd:block/obsidian_ribbed','light':'tnd:block/'+light,'particle':'tnd:block/obsidian_metal'},'elements':elements})
    for power,light in enumerate(['obsidian_relief','guiding_inlay','amber_seal','luminous_script']):
        model('observatory_filament_'+str(power),[box([0,0,0],[16,16,16],'#stone'),box([3,16,7],[10,16.04,9],'#light'),box([9,16,5],[11,16.04,11],'#light'),box([11,16,6],[12,16.04,10],'#light'),box([12,16,7],[13,16.04,9],'#light')],light)
    write(assets/'blockstates/observatory_filament.json',{'variants':{f'facing={f},power={p}':{'model':'tnd:block/observatory_filament_'+str(p),'y':r} for p in range(4) for f,r in [('east',0),('south',90),('west',180),('north',270)]}})
    write(assets/'models/block/observatory_filament.json',{'parent':'tnd:block/observatory_filament_1'})
    for lit in [False,True]:
        suffix='_active' if lit else ''
        model('observatory_coupler'+suffix,[box([1,0,1],[15,3,15],'#frame'),box([5,3,5],[11,10,11],'#body'),box([3,9,3],[13,12,13],'#frame'),box([4,12,6],[10,14,10],'#light'),box([10,11,4],[12,15,12],'#light'),box([12,12,6],[14,14,10],'#light')],'luminous_script' if lit else 'silent_conduit')
        for stage in range(4):
            up=stage*2.5
            model('observatory_reliquary_'+str(stage)+suffix,[box([1,0,1],[15,3,15],'#frame'),box([2,3,2],[4,11,14],'#body'),box([12,3,2],[14,11,14],'#body'),box([4,3,12],[12,11,14],'#body'),box([4,4,4],[12,9,12],'#light'),box([1,11+up,1],[15,14+up,15],'#frame'),box([6,14+up,6],[10,15+up,10],'#light')]+[box([x,9,z],[x+1,11+up,z+1],'#frame') for x in [3,12] for z in [3,12]],'luminous_script' if lit else 'memory_lattice')
    write(assets/'blockstates/observatory_coupler.json',{'variants':{f'lit={str(lit).lower()},tone={tone}':{'model':'tnd:block/observatory_coupler'+('_active' if lit else ''),'y':[270,0,90,180][tone]} for lit in [False,True] for tone in range(4)}})
    write(assets/'blockstates/observatory_reliquary.json',{'variants':{f'lit={str(lit).lower()},tone={stage}':{'model':'tnd:block/observatory_reliquary_'+str(stage)+('_active' if lit else '')} for lit in [False,True] for stage in range(4)}})
    write(assets/'models/block/observatory_reliquary.json',{'parent':'tnd:block/observatory_reliquary_0'})
    # A real prismatic silhouette replaces the original dial cube, retaining its native tone state.
    prism=box([5,7,5],[11,14,11],'#light');prism['rotation']={'origin':[8,10.5,8],'axis':'z','angle':45,'rescale':False}
    model('observatory_dial',[box([1,0,1],[15,3,15],'#frame'),box([6,3,6],[10,7,10],'#body'),prism,box([11,7,6],[15,10,10],'#light')],'luminous_script')
    for name in BLOCKS:
        write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
        write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name})
    for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+name,'structure':'tnd:test_habitat','max_ticks':1600,'required':True})

def languages(fr,en):
    pairs={
      'objective.0':('Écoute les trois stèles. Leurs réponses réveillent les gravures du parvis.','Listen to the three steles. Their answers awaken the forecourt engravings.'),
      'objective.1':('Traverse la nef et vaincs ses six veilleurs. Observe les lignes qui reprennent vie.','Cross the nave and defeat its six watchers. Watch the engravings come alive.'),
      'objective.2':('Oriente les quatre prismes pour relier le faisceau au récepteur. Un accord stable ouvre le seuil.','Turn the four prisms to connect the beam to its receiver. A stable alignment opens the threshold.'),
      'objective.3':('Coupe les quatre relais et vaincs leurs gardes. Quelque chose attend derrière une paroi scellée.','Disable the four relays and defeat their guards. Something waits behind a sealed wall.'),
      'objective.4':('Active le parcours puis rejoins les quatre résonances dans l’ordre. Les coupleurs des galeries déploient un passage.','Start the route and reach its four resonances in order. Gallery couplers deploy a crossing.'),
      'objective.5':('Rétablis le courant avec les trois coupleurs. Les gravures indiquent leur orientation. Retrouve aussi les trois échos.','Restore the current using three couplers. Engravings indicate their direction. Find the three echoes too.'),
      'objective.6':('Survis aux trois vagues de veilleurs. Chaque victoire alimente les sceaux de la salle.','Survive three waves of watchers. Each victory feeds the chamber seals.'),
      'objective.7':('Éveille le Colosse du Seuil. Lis ses marques, exploite ses ouvertures et traverse ses trois phases.','Awaken the Threshold Colossus. Read its marks, exploit its openings and overcome three phases.'),
      'seek_circuit':('Les gravures conduisent aux trois coupleurs.','The engravings lead to three couplers.'),
      'bridge_building':('La passerelle prend forme…','The crossing is taking shape…'),
      'bridge_ready':('Le passage est stable.','The crossing is stable.'),
      'bridge_count':('Passerelle : %s / 10','Crossing: %s / 10'),
      'circuit_count':('Coupleurs accordés : %s / 3','Aligned couplers: %s / 3'),
      'discoveries':('Réceptacles découverts : %s / 3','Receptacles discovered: %s / 3'),
      'current_stable':('Le courant tient.','The current holds.'),
      'coupler_turn':('Le flux cherche son chemin.','The flow seeks its path.'),
      'sealed.0':('Trois voix attendent encore leur réponse.','Three voices still await an answer.'),
      'sealed.1':('Une surveillance retient le verrou.','A watchful current holds the lock.'),
      'sealed.2':('Aucune vibration ne traverse le socle.','No vibration crosses the plinth.'),
      'remembered':('Tu as déjà recueilli cette trace.','You have already collected this trace.'),
      'lore.0':('« Nous avions laissé une place. Elle est restée vide. »','“We had left a place. It remained empty.”'),
      'lore.1':('« L’ordre fut maintenu après le dernier ordre. »','“Order endured beyond the final command.”'),
      'lore.2':('« Ce courant ne mène plus là où nous l’avions dirigé. »','“This current no longer leads where we sent it.”'),
      'awakening':('Le Témoin se souvient de ton pas.','The Witness remembers your footfall.'),
      'phase.1':('Les sceaux se fissurent.','The seals are cracking.'),
      'phase.2':('Quelque chose répond sous la pierre.','Something answers beneath the stone.'),
      'attack.0':('FRAPPE — quitte la marque','STRIKE — leave the mark'),
      'attack.1':('ONDE — franchis les anneaux en sautant','WAVE — jump across the rings'),
      'attack.3':('FAILLES — quitte les deux axes','FAULTS — leave both axes'),
      'attack.4':('TRIPLE IMPACT — évite les trois sceaux','TRIPLE IMPACT — avoid all three seals'),
      'hint.coupler':('Clic droit · orienter le coupleur','Right-click · turn the coupler'),
      'hint.bridge':('Clic droit · déployer la passerelle','Right-click · deploy the crossing'),
      'hint.reliquary':('Clic droit · examiner le réceptacle','Right-click · examine the receptacle'),
      'hint.prism':('Clic droit · orienter le prisme','Right-click · turn the prism')}
    for key,(f,e) in pairs.items():fr['observatory.tnd.witness.'+key]=f;en['observatory.tnd.witness.'+key]=e
    for name,f,e in [('observatory_filament','Gravure conductrice','Conducting Inlay'),('observatory_coupler','Coupleur ancien','Ancient Coupler'),('observatory_reliquary','Réceptacle de témoin','Witness Receptacle')]:fr['block.tnd.'+name]=f;en['block.tnd.'+name]=e
    for name,f,e in [('witness_current','Un courant s’accorde','A current aligns'),('witness_bridge','Un passage se déploie','A crossing unfolds'),('witness_reliquary','Un réceptacle répond','A receptacle answers'),('witness_awaken','Une masse ancienne s’éveille','An ancient mass awakens')]:fr['subtitles.tnd.'+name]=f;en['subtitles.tnd.'+name]=e
    fr['guide.tnd.page.6.1']+=' Dans les nouveaux parvis, les faisceaux montrent les accords. Les coupleurs des galeries déploient une passerelle ; ceux de la salle des échos suivent les gravures au sol. Trois réceptacles facultatifs donnent chacun une récompense personnelle unique.'
    en['guide.tnd.page.6.1']+=' In new forecourts, beams reveal alignments. Gallery couplers deploy a crossing; echo-room couplers follow floor engravings. Three optional receptacles each grant a unique personal reward.'

    fr['observatory.tnd.action.4']='Activer le parcours'
    en['observatory.tnd.action.4']='Start the route'
