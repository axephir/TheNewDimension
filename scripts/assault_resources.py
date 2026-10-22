"""Original Nacre assault equipment, finite encounter nodes and restoration workshop."""
import json
SETS=['ward','shade','choir'];PARTS=['helmet','chestplate','leggings','boots']
BOSSES=['nacre_marshal','glass_prowler','choir_surgeon']
BLOCKS=['assault_lamp','crystal_restorer']
ITEMS=[s+'_'+p for s in SETS for p in PARTS]+[s+'_memory' for s in SETS]+[b+'_spawn_egg' for b in BOSSES]
TESTS=['assault_'+n for n in ['equipment','repair','repair_guards','node','boss','loot','templates','blackout','familiar']]
COLORS={'ward':(224,159,76),'shade':(170,116,204),'choir':(78,196,182)}
def content(write,png,assets,data):
 from character_art import armor
 from city_resources import character
 from industry_resources import art
 from armor_icons import pixels as inventory_pixels
 for set,boss in zip(SETS,BOSSES):
  glow=COLORS[set]
  name=set+'_memory';png(assets/f'textures/item/{name}.png',art('control_circuit'));write(assets/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'tnd:item/'+name}});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
  write(data/f'recipe/salvage_{set}_memory.json',{'type':'minecraft:crafting_shapeless','ingredients':['tnd:'+name,'minecraft:redstone'],'result':{'id':'tnd:control_circuit','count':4}})
  for charged in [False,True]:
   pixels=character('neon_enforcer' if set=='ward' else 'signal_reaver',charged)
   pixels=[((*tuple(int(c*.35+g*.65) for c,g in zip(p[:3],glow)),p[3]) if max(p[:3])-min(p[:3])>35 else p) for p in pixels]
   png(assets/f'textures/entity/{boss}{"_charged" if charged else ""}.png',pixels,64,64)
  for leggings in [False,True]:
   pixels=armor(leggings)
   pixels=[(int(p[0]*.6+glow[0]*.25),int(p[1]*.6+glow[1]*.25),int(p[2]*.6+glow[2]*.25),p[3]) for p in pixels]
   png(assets/f'textures/entity/equipment/{"humanoid_leggings" if leggings else "humanoid"}/{set}.png',pixels,64,64)
  write(assets/f'equipment/{set}.json',{'layers':{'humanoid':[{'texture':'tnd:'+set}],'humanoid_leggings':[{'texture':'tnd:'+set}]}})
  for part in PARTS:
   pixels=inventory_pixels(set+'_'+part)
   name=set+'_'+part;png(assets/f'textures/item/{name}.png',pixels);write(assets/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'tnd:item/'+name}});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
  write(data/f'loot_table/entities/{boss}.json',{'type':'minecraft:entity','pools':[
   {'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:'+set+'_memory'}]},
   {'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'}],'entries':[{'type':'minecraft:item','name':'tnd:security_processor','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':3,'max':6}}]}]},
   {'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance','chance':.2}],'entries':[{'type':'minecraft:item','name':'tnd:'+set+'_'+p,'weight':1} for p in PARTS]}]})
  name=boss+'_spawn_egg';png(assets/f'textures/item/{name}.png',art('control_circuit'));write(assets/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'tnd:item/'+name}});write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:item/'+name}})
 write(data/'tags/item/assault_repairs.json',{'values':[]})
 for tag,parts in [('head_armor',['helmet']),('chest_armor',['chestplate']),('leg_armor',['leggings']),('foot_armor',['boots']),('enchantable/armor',PARTS),('enchantable/durability',PARTS),('enchantable/equippable',PARTS)]:
  path=data.parent/f'minecraft/tags/item/{tag}.json';existing=json.loads(path.read_text()) if path.exists() else {'values':[]};existing['values']=list(dict.fromkeys(existing['values']+['tnd:'+s+'_'+p for s in SETS for p in parts]));write(path,existing)
 # A geometric relay with readable coloured face; block/item texels remain 16px.
 for name in ['assault_node','assault_lamp','crystal_restorer']:
  png(assets/f'textures/block/{name}.png',art('electric_kiln' if name=='crystal_restorer' else 'control_circuit'))
  write(assets/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/'+name}})
 write(assets/'models/block/assault_lamp_off.json',{'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/carbon_panel'}})
 write(assets/'blockstates/assault_lamp.json',{'variants':{'lit=true':{'model':'tnd:block/assault_lamp'},'lit=false':{'model':'tnd:block/assault_lamp_off'}}})
 write(assets/'blockstates/assault_node.json',{'variants':{f'family={s},summit={str(roof).lower()},lit={str(lit).lower()}':{'model':'tnd:block/assault_node'} for s in SETS for roof in [False,True] for lit in [False,True]}})
 def box(a,b,t):return {'from':a,'to':b,'faces':{d:{'texture':t} for d in ['north','south','east','west','up','down']}}
 for lit in [False,True]:
  elements=[box([0,0,0],[16,3,16],'#frame'),box([2,3,2],[14,9,14],'#body'),box([1,9,1],[15,11,15],'#frame'),box([1,3,1],[3,16,3],'#frame'),box([13,3,13],[15,16,15],'#frame'),box([5,12,5],[11,16,11],'#crystal')]
  elements[-1]['rotation']={'origin':[8,14,8],'axis':'y','angle':45,'rescale':False}
  if lit:elements[-1]['light_emission']=10
  write(assets/f'models/block/crystal_restorer{"_active" if lit else ""}.json',{'textures':{'body':'tnd:block/crystal_restorer','frame':'tnd:block/industrial_frame','crystal':'tnd:block/neon_cyan' if lit else 'tnd:block/pale_crystal','particle':'tnd:block/crystal_restorer'},'elements':elements})
 write(assets/'blockstates/crystal_restorer.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':'tnd:block/crystal_restorer'+('_active' if lit else ''),'y':a} for f,a in [('north',0),('east',90),('south',180),('west',270)] for lit in [False,True]}})
 path=data.parent/'minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(path.read_text());tag['values']=list(dict.fromkeys(tag['values']+['tnd:assault_lamp','tnd:crystal_restorer']));write(path,tag)
 write(data/'advancement/recipes/assault_memories.json',{'criteria':{'memory':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['tnd:'+s+'_memory' for s in SETS]}]}}},'requirements':[['memory']],'rewards':{'recipes':['tnd:salvage_'+s+'_memory' for s in SETS]}})
 for name in ['assault_lamp','crystal_restorer']:
  write(assets/f'models/item/{name}.json',{'parent':'tnd:block/'+name})
  write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'tnd:block/'+name}});write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'tnd:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 write(data/'recipe/crystal_restorer.json',{'type':'minecraft:crafting_shaped','pattern':['ACA','INI','FCF'],'key':{'A':'tnd:umbra_alloy','C':'tnd:control_circuit','I':'tnd:inertial_matrix','N':'minecraft:netherite_ingot','F':'tnd:industrial_frame'},'result':{'id':'tnd:crystal_restorer'}})
 write(data/'advancement/recipes/crystal_restorer.json',{'criteria':{'crystal':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:security_processor'}]}}},'requirements':[['crystal']],'rewards':{'recipes':['tnd:crystal_restorer']}})
 write(data/'recipe/assault_lamp.json',{'type':'minecraft:crafting_shaped','pattern':[' G ','CRC',' G '],'key':{'G':'minecraft:glass','C':'minecraft:copper_ingot','R':'minecraft:redstone'},'result':{'id':'tnd:assault_lamp','count':4}})
 write(data/'loot_table/chests/assault_reserves.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':4,'max':7},'entries':[{'type':'minecraft:item','name':n,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':4}}]} for n in ['tnd:blue_shard','tnd:red_shard','tnd:green_shard','tnd:ion_filament','tnd:security_processor','tnd:tempered_alloy','minecraft:gold_ingot','minecraft:cooked_beef']]}]})
 for name in TESTS:write(data/f'test_instance/{name}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+name,'structure':'tnd:test_habitat','max_ticks':4000 if name=='assault_blackout' else 1000,'required':True})
def languages(fr,en):
 values={'ward':('du Rempart','of the Ward'),'shade':('de la Traque','of the Shade'),'choir':('de la Suture','of the Choir')}
 names={'helmet':('Heaume','Helmet'),'chestplate':('Plastron','Chestplate'),'leggings':('Jambières','Leggings'),'boots':('Bottes','Boots')}
 for s in SETS:
  fr['item.tnd.'+s+'_memory']='Mémoire '+values[s][0];en['item.tnd.'+s+'_memory']='Memory '+values[s][1]
  for p in PARTS:fr['item.tnd.'+s+'_'+p]=names[p][0]+' '+values[s][0];en['item.tnd.'+s+'_'+p]=names[p][1]+' '+values[s][1]
 for key,a,b in [('block.tnd.assault_node','Relais de confinement','Containment Relay'),('block.tnd.assault_lamp','Luminaire de réserve','Reserve Luminaire'),('block.tnd.crystal_restorer','Restaurateur cristallin','Crystal Restorer'),('entity.tnd.nacre_marshal','Maréchal de Nacre','Nacre Marshal'),('entity.tnd.glass_prowler','Traqueur prismatique','Glass Prowler'),('entity.tnd.choir_surgeon','Chirurgien du Chœur','Choir Surgeon'),('assault.tnd.set.ward','Set complet : Résistance I · Résistance au feu I','Full set: Resistance I · Fire Resistance I'),('assault.tnd.set.shade','Set complet : Vitesse I · Sauts améliorés I','Full set: Speed I · Jump Boost I'),('assault.tnd.set.choir','Set complet : Régénération I · Célérité I','Full set: Regeneration I · Haste I'),('assault.tnd.repair','Réparation : restaurateur cristallin + cristaux','Repair: crystal restorer + crystals'),('assault.tnd.crystal','Cristal','Crystal'),('assault.tnd.result','Réparé','Restored'),('assault.tnd.memory.ward','Familier : +6 PV maximum','Familiar: +6 maximum health'),('assault.tnd.memory.shade','Familier : allure accrue','Familiar: increased movement speed'),('assault.tnd.memory.choir','Familier : +2 armure','Familiar: +2 armor'),('assault.tnd.memory_use','Clic droit sur votre familier. Une installation par mémoire.','Use on your familiar. Each memory can be installed once.'),('assault.tnd.memory_salvage','Doublon + redstone → 4 circuits de commande','Duplicate + redstone → 4 control circuits'),('assault.tnd.protocols','Protocoles installés : %s / 3','Installed protocols: %s / 3'),('assault.tnd.protocol_installed','Un nouveau motif traverse sa carapace.','A new pattern runs across its shell.'),('assault.tnd.protocol_refused','Cette mémoire ne trouve pas de prise.','This memory finds no purchase.')]:fr[key]=a;en[key]=b
 help=[('Équipement usé en haut ; cristal en bas. Seules les pièces de tour sont acceptées.','Worn tower gear above, crystal below. Other equipment is not accepted.'),('Socle : 8 charpentes autour, une couche sous la machine. Une réparation prend 4 s et 2 000 FE.','Base: eight frames around, one layer below. One repair takes 4 s and 2,000 FE.'),('Éclat coloré : +100 ; résonant ou noctilite : +150 ; cristal composite : +250. Nom et enchantements conservés. Récupérez la pièce à droite.','Coloured shard: +100; resonant or noctilite: +150; composite crystal: +250. Names and enchantments stay. Collect gear on the right.')]
 for i,(a,b) in enumerate(help):fr[f'industry.tnd.help.crystal_restorer.{i}']=a;en[f'industry.tnd.help.crystal_restorer.{i}']=b
 for boss in BOSSES:fr['item.tnd.'+boss+'_spawn_egg']='Œuf : '+fr['entity.tnd.'+boss];en['item.tnd.'+boss+'_spawn_egg']=en['entity.tnd.'+boss]+' Spawn Egg'
