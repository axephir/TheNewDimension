"""Nacre's combat depths and three maintained industrial material paths. Original native-pixel assets."""
import json, math, random, colorsys
METALS=['ferronyte','thalassite','asterium']
PARTS=['sword','pickaxe','axe','shovel','hoe','helmet','chestplate','leggings','boots']
MACHINES=['hydrocentrifuge','pressure_kiln','abyssal_dredger','abyssal_bore','asterium_replicator','closed_cycle_turbine','precision_assembler']
ORES=['ferronyte_ore','deepslate_ferronyte_ore','thalassite_nodule','asterium_nodule']
BLOCKS=ORES+MACHINES+['depths_terminal']
MATERIALS=['raw_ferronyte','raw_thalassite','raw_asterium','ferronyte_grit','thalassite_grit','ferronyte_ingot','thalassite_ingot','asterium_ingot','pressure_circuit','mineral_matrix','maintenance_kit','resonant_fuel_cell','sundered_drive','hardened_drill_bit']
ENTITIES=['depths_sentinel','depths_hunter','depths_rifle','depths_bastion','depths_lens','depths_reaper','depths_sovereign']
ITEMS=MATERIALS+[m+'_'+p for m in METALS for p in PARTS]+[e+'_spawn_egg' for e in ENTITIES]
TESTS=['deep_processing','deep_guards','deep_energy','deep_hoppers','deep_service','deep_turbine','deep_restore','deep_bore','deep_bore_guards','deep_dredger','deep_equipment','deep_worldgen','depths_architecture','depths_entrance','depths_ledger','depths_encounter','depths_rewards','depths_restore','depths_migration','depths_boss_patterns']
COLORS=[(179,126,94),(71,180,187),(183,143,222)]
def shade(c,n):return tuple(max(0,min(255,v+n)) for v in c[:3])
def sprite(name):
    p=[(0,0,0,0)]*256;rng=random.Random('nacre-depths-'+name);mi=next((i for i,m in enumerate(METALS) if m in name),2);color=COLORS[mi]
    def dot(x,y,c):
        if 0<=x<16 and 0<=y<16:p[y*16+x]=(*c[:3],255)
    def shape(points,c):
        for x,y in points:dot(x,y,c)
    if any(name.endswith('_'+part) for part in ['helmet','chestplate','leggings','boots']):
        from armor_icons import pixels
        part=name.split('_')[-1];base=pixels(['ward','choir','shade'][mi]+'_'+part)
        # Preserve authored relief and bevels; shift coloured plates into the material's palette.
        return [(*shade(color,int((max(q[:3])+min(q[:3]))/2)-130),q[3]) if q[3] else q for q in base]
    tool=next((x for x in ['pickaxe','sword','axe','shovel','hoe'] if name.endswith('_'+x)),None)
    if tool:
        for i in range(3,13):
            x,y=i,15-i;dot(x-1,y,(33,27,27));dot(x,y,(104,77,54));dot(x+1,y, (62,49,42));
            if i%3==0:dot(x,y,(151,119,74))
        if tool=='sword':points=[(x,y) for y in range(1,10) for x in range(6,15) if abs(x+y-14)<=1 and x>=7 and y<=8];points +=[(6,6),(7,7),(8,8),(9,9)]
        elif tool=='pickaxe':points=[(x,y) for x in range(3,15) for y in range(1,7) if y==2+abs(x-8)//4 or y==3+abs(x-8)//4 or x in [3,14] and y in [4,5,6]]
        elif tool=='axe':points=[(x,y) for x in range(5,13) for y in range(1,8) if x>=7 or y in [2,3,4,5]]
        elif tool=='shovel':points=[(x,y) for x in range(8,15) for y in range(1,8) if abs(x-11)+abs(y-4)<=4]
        else:points=[(x,y) for x in range(5,15) for y in [2,3]]+[(5,4),(5,5),(6,4)]
        occupied=set(points)
        for x,y in points:dot(x,y,shade(color,48 if (x-1,y) not in occupied or (x,y-1) not in occupied else -55 if (x+1,y) not in occupied else rng.choice([-18,-5,0,8])))
        for x,y in [(9,5),(10,4)]:dot(x,y,shade(color,70))
        return p
    if name.endswith('_ingot'):
        for y in range(5,12):
            for x in range(2,14):
                if (y==5 and x<5) or (y>9 and x>11):continue
                dot(x,y,shade(color,45 if y==5 or x==3 and y<9 else -60 if y>=10 or x==13 else -12 if (x+y)%4==0 else 0))
        for x in range(5,12):dot(x,6,shade(color,65))
    elif name.endswith('_grit') or name.startswith('raw_'):
        for cx,cy,r in [(6,6,4),(10,9,4),(4,11,2)]:
            for y in range(cy-r,cy+r+1):
                for x in range(cx-r,cx+r+1):
                    if abs(x-cx)+abs(y-cy)>r+1:continue
                    c=shade(color,rng.choice([-35,-18,0,15]));c=(44,49,53) if not name.endswith('_grit') and (x*3+y)%7<2 else c;dot(x,y,c)
            dot(cx-1,cy-2,shade(color,70))
    elif name=='hardened_drill_bit':
        for y in range(1,15):
            for x in range(3,13):
                if abs(x-8)>(15-y)//3+1:continue
                dot(x,y,shade((105,164,168),35 if (x+y)%4==0 else -35 if x>8 else 0))
    elif name.endswith('_spawn_egg'):
        for y in range(1,15):
            for x in range(2,14):
                if ((x-7.5)/5)**2+((y-8)/6.5)**2<1:dot(x,y,shade(color,-15) if (x*7+y*3)%9<3 else (40,45,53))
    else:
        for y in range(3,13):
            for x in range(2,14):
                edge=x in [2,13] or y in [3,12];dot(x,y,(102,113,121) if edge else (31,43,51))
                if (x+y)%4==0 and not edge:dot(x,y,shade(color,-30))
        if name=='maintenance_kit':
            for y in range(5,11):dot(7,y,(207,185,131));dot(8,y,(207,185,131))
            for x in range(5,11):dot(x,7,(207,185,131));dot(x,8,(207,185,131))
        elif name=='resonant_fuel_cell':
            for x in range(5,11):
                for y in range(4,12):dot(x,y,shade(COLORS[1],35 if x==5 else -20 if x==10 else 0))
        else:
            for i in range(4,12):dot(i,7,shade(color,50));dot(7,i,shade(color,50))
            for x,y in [(4,4),(11,4),(4,11),(11,11)]:dot(x,y,(235,209,150))
    return p

def content(write,png,assets,data):
    from character_art import armor
    def item(name,block=False):
        write(assets/f'models/item/{name}.json',{'parent':f'tnd:block/{name}'} if block else {'parent':'minecraft:item/handheld' if any(name.endswith('_'+p) for p in PARTS[:5]) else 'minecraft:item/generated','textures':{'layer0':f'tnd:item/{name}'}})
        write(assets/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'tnd:item/{name}'}})
    def tag(base,name,values):
        path=base/f'{name}.json';old=json.loads(path.read_text()) if path.exists() else {'values':[]};old['values']=list(dict.fromkeys(old['values']+values));write(path,old)
    def shaped(name,pattern,key,count=1):write(data/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'id':'tnd:'+name,'count':count}})
    def box(a,b,t,emit=0):return {'from':a,'to':b,'light_emission':emit,'faces':{side:{'texture':t,'uv':[0,0,16,16]} for side in ['north','south','east','west','up','down']}}
    for name in ITEMS:png(assets/f'textures/item/{name}.png',sprite(name));item(name)
    for mi,m in enumerate(METALS):
        color=COLORS[mi]
        for leg in [False,True]:
            p=armor(leg);p=[(*shade(color,(max(c[:3])+min(c[:3]))//2-135),c[3]) for c in p];png(assets/f'textures/entity/equipment/{"humanoid_leggings" if leg else "humanoid"}/{m}.png',p,64,64)
        write(assets/f'equipment/{m}.json',{'layers':{'humanoid':[{'texture':'tnd:'+m}],'humanoid_leggings':[{'texture':'tnd:'+m}]}})
        write(data/f'tags/item/{m}_repairs.json',{'values':['tnd:'+m+'_ingot']})
        patterns={'sword':[' I ',' I ',' S '],'pickaxe':['III',' S ',' S '],'axe':['II ','IS ',' S '],'shovel':[' I ',' S ',' S '],'hoe':['II ',' S ',' S '],'helmet':['III','I I'],'chestplate':['I I','III','III'],'leggings':['III','I I','I I'],'boots':['I I','I I']}
        for part,pattern in patterns.items():shaped(m+'_'+part,pattern,{'I':'tnd:'+m+'_ingot',**({'S':'minecraft:stick'} if part in PARTS[:5] else {})})
        for t,parts in [('head_armor',['helmet']),('chest_armor',['chestplate']),('leg_armor',['leggings']),('foot_armor',['boots']),('enchantable/armor',PARTS[5:]),('enchantable/durability',PARTS),('enchantable/equippable',PARTS[5:]),('enchantable/mining',PARTS[1:5]),('enchantable/mining_loot',PARTS[1:5]),('enchantable/weapon',['sword','axe']),('enchantable/sharp_weapon',['sword','axe']),('enchantable/sword',['sword']),('swords',['sword']),('pickaxes',['pickaxe']),('axes',['axe']),('shovels',['shovel']),('hoes',['hoe'])]:tag(data.parent/'minecraft/tags/item',t,['tnd:'+m+'_'+p for p in parts])
    for name in BLOCKS:
        rng=random.Random(name);p=[];color=COLORS[0 if 'ferronyte' in name else 1 if 'thalassite' in name or name=='abyssal_dredger' else 2]
        for y in range(16):
            for x in range(16):
                n=rng.choice([-7,-3,0,2,5]);c=shade((43,49,55),n)
                if name in ORES:
                    base=(67,68,72) if 'deepslate' in name else (104,106,107) if 'ore' in name else (49,62,69)
                    c=shade(base,n*2)
                    for cx,cy in [(4,4),(11,6),(7,11)]:
                        if abs(x-cx)+abs(y-cy)<=2:c=shade(color,42 if x<cx or y<cy else -38 if x>cx else n)
                else:
                    if x in [0,15] or y in [0,15]:c=(82,92,101)
                    if y in [3,12] and 2<x<13:c=shade(color,-40)
                    if 4<=x<=11 and 5<=y<=10:c=(15,23,31)
                    if 5<=x<=10 and y==7 or x==6 and 6<=y<=9:c=shade(color,32)
                    if (x,y) in [(2,2),(13,2),(2,13),(13,13)]:c=(181,190,186)
                p.append((*c,255))
        png(assets/f'textures/block/{name}.png',p)
        if name in ORES:write(assets/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/'+name}});write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':'tnd:block/'+name}}})
        elif name=='depths_terminal':
            tex={'body':'tnd:block/obsidian_relief','frame':'tnd:block/obsidian_ribbed','display':'tnd:block/depths_terminal','particle':'tnd:block/obsidian_metal'}
            write(assets/f'models/block/{name}.json',{'textures':tex,'elements':[box([1,0,1],[15,3,15],'#frame'),box([4,3,6],[12,10,13],'#body'),box([1,9,2],[15,17,7],'#frame'),box([2,10,1.9],[14,16,2],'#display',8),box([2,6,1],[14,7,6],'#display')]});write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':'tnd:block/'+name}}})
        else:
            for lit in [False,True]:
                e=[box([0,0,0],[16,2,16],'#frame'),box([1,2,1],[15,13,15],'#body'),box([0,13,0],[16,15,16],'#frame'),box([4,5,0],[12,10,1],'#screen',10 if lit else 0)]
                if name=='hydrocentrifuge':e +=[box([3,15,3],[13,18,13],'#body'),box([6,18,6],[10,21,10],'#frame')]
                elif name=='pressure_kiln':e +=[box([2,15,2],[5,21,5],'#frame'),box([11,15,11],[14,21,14],'#frame'),box([2,4,2],[14,5,14],'#glow',10 if lit else 0)]
                elif name in ['abyssal_dredger','abyssal_bore']:e +=[box([6,-3,6],[10,0,10],'#frame'),box([5,15,5],[11,20,11],'#body'),box([7,20,7],[9,23,9],'#glow',8 if lit else 0)]
                elif name=='asterium_replicator':
                    e +=[box([2,15,2],[4,22,4],'#frame'),box([12,15,12],[14,22,14],'#frame'),box([5,17,5],[11,21,11],'#glow',12 if lit else 0)];e[-1]['rotation']={'origin':[8,19,8],'axis':'y','angle':45}
                elif name=='closed_cycle_turbine':e +=[box([2,15,3],[14,20,13],'#body'),box([0,16,5],[2,22,11],'#frame'),box([14,16,5],[16,22,11],'#frame'),box([4,20,4],[12,21,12],'#glow',9 if lit else 0)]
                else:e +=[box([1,15,12],[3,22,14],'#frame'),box([13,15,12],[15,22,14],'#frame'),box([1,21,12],[15,23,14],'#frame'),box([6,17,4],[10,21,8],'#glow',8 if lit else 0)]
                write(assets/f'models/block/{name}{"_active" if lit else ""}.json',{'textures':{'body':'tnd:block/'+name,'frame':'tnd:block/obsidian_ribbed','screen':'tnd:block/'+name,'glow':'tnd:block/'+('luminous_script' if lit else 'silent_conduit'),'particle':'tnd:block/'+name},'elements':e})
            write(assets/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(l).lower()}':{'model':f'tnd:block/{name}'+('_active' if l else ''),'y':rot} for f,rot in [('north',0),('east',90),('south',180),('west',270)] for l in [False,True]}})
        item(name,True)
        if name in MACHINES:write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'tnd:'+name}]}]})
    tag(data.parent/'minecraft/tags/block','mineable/pickaxe',['tnd:'+n for n in BLOCKS]);tag(data.parent/'minecraft/tags/block','needs_diamond_tool',['tnd:'+n for n in ORES[:3]])
    for base in ['c','neoforge']:tag(data.parent/f'{base}/tags/block','ores',['tnd:'+n for n in ORES[:3]])
    for name in ORES[:3]:
        raw='raw_thalassite' if name=='thalassite_nodule' else 'raw_ferronyte'
        write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'tnd:'+name,'conditions':[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]},{'type':'minecraft:item','name':'tnd:'+raw,'functions':[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'}]}]}]}]})
    for m in METALS:
        for kind,ticks in [('smelting',200),('blasting',100)]:write(data/f'recipe/{m}_{kind}.json',{'type':'minecraft:'+kind,'ingredient':'tnd:raw_'+m,'result':{'id':'tnd:'+m+'_ingot'},'experience':.7,'cookingtime':ticks})
    recipes={
     'pressure_circuit':(['TQT','CRC','TQT'],{'T':'tnd:thalassite_ingot','Q':'minecraft:quartz','C':'tnd:control_circuit','R':'minecraft:redstone_block'},2),
     'maintenance_kit':([' F ','QCQ',' F '],{'F':'tnd:ferronyte_ingot','Q':'minecraft:quartz','C':'tnd:copper_coil'},1),
     'mineral_matrix':(['ADA','QNQ','ADA'],{'A':'tnd:purified_auralite','D':'minecraft:diamond','Q':'minecraft:quartz','N':'minecraft:netherite_scrap'},2),
     'resonant_fuel_cell':(['CTC','CCC'],{'C':'minecraft:coal_block','T':'tnd:thalassite_ingot'},1),
     'hardened_drill_bit':([' T ','DND',' F '],{'T':'tnd:thalassite_ingot','D':'minecraft:diamond','N':'minecraft:netherite_ingot','F':'tnd:ferronyte_ingot'},1),
     'hydrocentrifuge':(['FFF','WCW','OHO'],{'F':'tnd:ferronyte_ingot','W':'minecraft:water_bucket','C':'tnd:control_circuit','O':'tnd:copper_coil','H':'tnd:industrial_frame'},1),
     'pressure_kiln':(['FTF','BCB','OHO'],{'F':'tnd:ferronyte_ingot','T':'tnd:thalassite_ingot','B':'minecraft:blast_furnace','C':'tnd:pressure_circuit','O':'tnd:copper_coil','H':'tnd:industrial_frame'},1),
     'abyssal_dredger':(['TDT','FCF','OHO'],{'T':'tnd:thalassite_ingot','D':'tnd:hardened_drill_bit','F':'tnd:industrial_frame','C':'tnd:pressure_circuit','O':'tnd:copper_coil','H':'minecraft:hopper'},1),
     'abyssal_bore':(['ADA','NSN','FCF'],{'A':'tnd:asterium_ingot','D':'tnd:hardened_drill_bit','N':'minecraft:netherite_block','S':'tnd:sundered_drive','F':'tnd:industrial_frame','C':'tnd:pressure_circuit'},1),
     'asterium_replicator':(['AMA','NSN','FCF'],{'A':'tnd:asterium_ingot','M':'tnd:mineral_matrix','N':'minecraft:netherite_ingot','S':'tnd:sundered_drive','F':'tnd:industrial_frame','C':'tnd:mnemonic_circuit'},1),
     'closed_cycle_turbine':(['TAT','OSO','FCF'],{'T':'tnd:thalassite_ingot','A':'tnd:asterium_ingot','O':'tnd:copper_coil','S':'tnd:sundered_drive','F':'tnd:industrial_frame','C':'tnd:pressure_circuit'},1),
     'precision_assembler':(['FTF','CNC','FHF'],{'F':'tnd:ferronyte_ingot','T':'tnd:thalassite_ingot','C':'tnd:pressure_circuit','N':'minecraft:netherite_ingot','H':'tnd:industrial_frame'},1)}
    for n,(p,k,c) in recipes.items():shaped(n,p,k,c)
    for m in METALS:
        names=[m+'_'+p for p in PARTS]+[m+'_smelting',m+'_blasting']
        if m=='ferronyte':names+=list(recipes)
        write(data/f'advancement/recipes/{m}_industry.json',{'criteria':{'ore':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:raw_'+m}]}}},'requirements':[['ore']],'rewards':{'recipes':['tnd:'+n for n in names]}})
    for f,step,biomes in [('ferronyte_veins','underground_ores','#minecraft:is_overworld'),('thalassite_floor','top_layer_modification','#minecraft:is_ocean')]:
        write(data/f'worldgen/configured_feature/{f}.json',{'type':'tnd:'+f,'config':{}});write(data/f'worldgen/placed_feature/{f}.json',{'feature':'tnd:'+f,'placement':[{'type':'minecraft:count','count':1},{'type':'minecraft:biome'}]});write(data/f'neoforge/biome_modifier/{f}.json',{'type':'neoforge:add_features','biomes':biomes,'features':['tnd:'+f],'step':step})
    for k,name in enumerate(ENTITIES):
        color=COLORS[k%3]
        for opened in [False,True]:
            rng=random.Random(name);atlas=[]
            for y in range(64):
                for x in range(64):
                    n=rng.choice([-5,-2,0,3]);c=shade((35,42,49),n)
                    if x%8==0 or y%8==0:c=(20,25,32)
                    elif x%8==1 or y%8==1:c=(86,99,110)
                    if y>=32 and y%8 in [3,4] and x%8 in [3,4]:c=shade(color,-35)
                    if (x+3*y)%53==0:c=shade(color,-60)
                    if x>=48 and y<16:c=shade(color,55 if opened else 5)
                    atlas.append((*c,255))
            png(assets/f'textures/entity/{name}{"_open" if opened else ""}.png',atlas,64,64)
        png(assets/f'textures/entity/{name}_glow.png',[(*shade(color,20),255) if x>=48 and y<16 else (0,0,0,0) for y in range(64) for x in range(64)],64,64)
        pools=[] if k>=3 else [{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance','chance':.4}],'entries':[{'type':'minecraft:item','name':'tnd:'+n} for n in ['raw_ferronyte','raw_thalassite','maintenance_kit','pressure_circuit']]}]
        write(data/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
    entries=[]
    for name,weight,lo,hi in [('tnd:ferronyte_ingot',8,3,8),('tnd:thalassite_ingot',5,2,5),('tnd:maintenance_kit',7,2,4),('tnd:pressure_circuit',4,1,3),('tnd:mineral_matrix',2,1,2),('minecraft:diamond',3,1,4),('minecraft:netherite_scrap',1,1,2),('minecraft:cooked_beef',7,8,16),('minecraft:golden_carrot',4,3,7),('minecraft:experience_bottle',4,4,10)]:entries.append({'type':'minecraft:item','name':name,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]})
    write(data/'loot_table/chests/nacre_depths.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':5,'max':8},'entries':entries},{'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.16}],'entries':[{'type':'minecraft:item','name':'tnd:'+m+'_'+p} for m in METALS[:2] for p in PARTS]}]})
    for name in ['assault_reserves','city_salvage','city_rooftop','observatory_supplies']:
        path=data/f'loot_table/chests/{name}.json'
        if not path.exists():continue
        v=json.loads(path.read_text());v['pools']=[p for p in v['pools'] if p.get('entries')!=entries[:4]];v['pools'].append({'rolls':1,'entries':entries[:4]});write(path,v)
    for n in TESTS:write(data/f'test_instance/{n}.json',{'type':'minecraft:function','function':'tnd:'+n,'environment':'minecraft:default','structure':'tnd:test_habitat','max_ticks':1800,'required':True})

def languages(fr,en):
    def pair(k,f,e):fr[k]=f;en[k]=e
    metalfr=['ferronyte','thalassite','astérium']
    for m,f in zip(METALS,metalfr):
        for p,fp,ep in zip(PARTS,['Épée','Pioche','Hache','Pelle','Houe','Casque','Plastron','Jambières','Bottes'],['Sword','Pickaxe','Axe','Shovel','Hoe','Helmet','Chestplate','Leggings','Boots']):pair('item.tnd.'+m+'_'+p,fp+' en '+f,m.capitalize()+' '+ep)
    for m,f in zip(METALS,metalfr):
        for suffix,fp,ep in [('_ingot','Lingot de ',' Ingot'),('_grit','Granulés de ',' Grit')]:
            if m=='asterium' and suffix=='_grit':continue
            pair('item.tnd.'+m+suffix,fp+f,m.capitalize()+ep)
        pair('item.tnd.raw_'+m,f.capitalize()+' brute','Raw '+m.capitalize())
    for n,f,e in [('pressure_circuit','Circuit pressurisé','Pressure Circuit'),('mineral_matrix','Matrice minérale','Mineral Matrix'),('maintenance_kit','Kit de maintenance','Maintenance Kit'),('resonant_fuel_cell','Cellule résonante','Resonant Fuel Cell'),('sundered_drive','Moteur disjoint','Sundered Drive'),('hardened_drill_bit','Trépan renforcé','Hardened Drill Bit')]:pair('item.tnd.'+n,f,e)
    for n,f,e in zip(BLOCKS,['Minerai de ferronyte','Minerai de ferronyte des abîmes','Nodule de thalassite','Nodule d’astérium','Hydrocentrifugeuse','Four à pression','Drague abyssale','Foreuse abyssale','Réplicateur d’astérium','Turbine à cycle fermé','Assembleur de précision','Terminal des soutes'],['Ferronyte Ore','Deepslate Ferronyte Ore','Thalassite Nodule','Asterium Nodule','Hydrocentrifuge','Pressure Kiln','Abyssal Dredger','Abyssal Bore','Asterium Replicator','Closed-cycle Turbine','Precision Assembler','Depths Terminal']):pair('block.tnd.'+n,f,e)
    for n,f,e in zip(ENTITIES,['Sentinelle des soutes','Chasseur disjoint','Fusilier de pression','Le Bastion enseveli','La Lentille noyée','Le Faucheur de Nacre','Le Souverain des soutes'],['Depths Sentinel','Sundered Hunter','Pressure Rifleman','The Buried Bastion','The Drowned Lens','The Nacre Reaper','The Sovereign Below']):pair('entity.tnd.'+n,f,e);pair('item.tnd.'+n+'_spawn_egg','Œuf : '+f,e+' Spawn Egg')
    for i,(f,e) in enumerate([('Entr.','Feed'),('Réac.','Reag.'),('Kit','Kit')]):pair('deep.tnd.slot.'+str(i),f,e)
    for k,f,e in [('foundation','Socle · vue du dessus','Base · top view'),('outputs','Produits','Outputs'),('wear','Intégrité : %s %%','Integrity: %s %%'),('next_recipe','Changer de recette','Switch recipe'),('redstone','Signal redstone = pause','Redstone signal = pause')]:pair('deep.tnd.'+k,f,e)
    states=[('Prête','Ready'),('Montage incomplet · voir ?','Incomplete assembly · see ?'),('Pause redstone','Redstone pause'),('Ajouter les entrées requises','Add required inputs'),('Réserve FE insuffisante','Insufficient FE reserve'),('Sortie ou réserve saturée','Output or reserve full'),('Maintenance requise : ajouter un kit','Maintenance required: add a kit'),('Recherche · ou zone épuisée','Searching · or area exhausted'),('Zone voisine non chargée','Neighbouring area unloaded'),('Forage à démarrer manuellement','Start excavation manually'),('Overworld requis ; drague en océan profond','Requires Overworld; dredger in deep ocean'),('Cycle en cours','Cycle running'),('Terrain protégé : cible ignorée','Protected terrain: target skipped')]
    for i,(f,e) in enumerate(states):pair('deep.tnd.state.'+str(i),f,e)
    guides=[
      ('Socle : 8 charpentes en carré 3×3 sous la machine ; 4 piliers d’angle de 2 blocs. Brut + sable / éclat de prismarine → 3 granulés. 6 000 / 10 000 FE. Kit en Service.','Base: 8 frames in a 3×3 ring below; 4 corner posts, 2 blocks high. Raw + sand / prismarine shard → 3 grit. 6,000 / 10,000 FE. Kit in Service.'),
      ('Socle 3×3 (8 charpentes) et 4 piliers de 2 blocs. 4 granulés + charbon / quartz → 4 lingots. 6 000 / 10 000 FE. Entrées par le haut et côté ; produits par dessous.','3×3 ring (8 frames), four 2-block posts. 4 grit + coal / quartz → 4 ingots. 6,000 / 10,000 FE. Input above and side; outputs below.'),
      ('Socle : carré 3×3 de 8 charpentes sous la drague. Posez-la 2 blocs au-dessus du fond, en océan, Y ≤ 55, avec 8 blocs d’eau au-dessus. Trépan : Réactif. Zone 17×17×3, nodules réels, 16 000 FE/nodule.','Base: eight frames in a 3×3 ring below. Place 2 blocks above the seabed, ocean, Y ≤ 55, with 8 water blocks above. Bit in Reagent. 17×17×3 real nodules, 16,000 FE/nodule.'),
      ('Derrick : cadre 7×7 sous la machine, 4 piliers d’angle de 5 blocs, poutre de 7 blocs à Y+5 sur l’axe X. Trépan : Réactif. Zone 17×17 ; 2 blocs/s. 900 FE/pierre, 12 000 FE/minerai.','Derrick: 7×7 frame below, four 5-block corner posts, 7-block beam at Y+5 along X. Bit in Reagent. 17×17; 2 blocks/s. 900 FE/stone, 12,000 FE/ore.'),
      ('Socle 3×3 (8 charpentes), 4 piliers de 2 blocs. 1 astérium brut + 1 matrice minérale → 3 lingots ; 96 000 FE, 16 secondes. Intégrité : −4 % / cycle. Les bruts viennent des boss.','3×3 ring (8 frames), four 2-block posts. 1 raw asterium + 1 mineral matrix → 3 ingots; 96,000 FE, 16 seconds. Integrity: −4% / cycle. Raw comes from bosses.'),
      ('Socle 3×3 (8 charpentes), 4 piliers de 2 blocs. Cellule + seau d’eau → 640 000 FE en 20 s. Le seau vide sort dessous. Production : 1 600 FE/t ; câbles industriels. −2 % intégrité/cellule.','3×3 ring (8 frames), four 2-block posts. Cell + water bucket → 640,000 FE in 20s. Empty bucket exits below. Output: 1,600 FE/t; industrial cables. −2% integrity/cell.'),
      ('Socle 3×3 (8 charpentes), 4 piliers de 2 blocs. Choisir une recette : débris + 4 auralites purifiées → 4 matrices ; 2 ferronytes + 4 quartz → 4 kits ; thalassite + 2 blocs charbon → cellule.','3×3 ring (8 frames), four 2-block posts. Choose recipe: scrap + 4 purified auralite → 4 matrices; 2 ferronyte + 4 quartz → 4 kits; thalassite + 2 coal blocks → cell.')]
    for n,(f,e) in zip(MACHINES,guides):pair('deep.tnd.guide.'+n,f,e)
    for m,f,e in [('ferronyte','Set complet : résistance au feu en s’accroupissant','Full set: fire resistance while crouching'),('thalassite','Set complet : respiration et nage améliorées sous l’eau','Full set: underwater breathing and faster swimming'),('asterium','Set complet : Résistance I','Full set: Resistance I')]:pair('deep.tnd.set.'+m,f,e)
    for n,f,e in [('raw_ferronyte','Veines de l’Overworld, Y −48 à 16. Traitement industriel : rendement ×3.','Overworld veins, Y −48 to 16. Industrial treatment: ×3 yield.'),('raw_thalassite','Nodules visibles au fond des océans, Y ≤ 45. Minage ou drague.','Exposed seabed nodules, Y ≤ 45. Mine or dredge.'),('raw_asterium','Nodule des soutes : vaincre le boss puis interagir. Récompense personnelle unique.','Depths nodule: defeat its boss, then interact. Unique personal reward.'),('maintenance_kit','Entretien automatique : emplacement Service. Restaure l’intégrité de la machine.','Automatic servicing: Service slot. Restores machine integrity.'),('hardened_drill_bit','512 blocs. À placer en Réactif de la foreuse ou de la drague.','512 blocks. Put in Reagent of the bore or dredger.'),('sundered_drive','Un par boss des soutes. Composant des machines de dernier palier.','One per depths boss. Required for final-tier machines.')]:pair('deep.tnd.item.'+n,f,e)
    for k,f,e in [('title','Les Soutes de Nacre','The Depths of Nacre'),('engage','Engager le combat','Engage encounter'),('extract','Extraire le nodule','Extract nodule'),('reset','Reprendre une tentative interrompue','Restart interrupted attempt'),('claim_locked','Le nodule ne vous répond pas.','The nodule does not respond.'),('cleared','Secteur %s sécurisé. Les scellés cèdent.','Sector %s secured. The seals yield.'),('progress','Ennemis vaincus : %s / %s','Enemies defeated: %s / %s'),('brief','Un seul passage. Huit secteurs. Aucun mécanisme à résoudre. Les nodules s’ouvrent après la chute de leur gardien.','One passage. Eight sectors. No mechanisms to solve. Nodules open after their guardian falls.')]:pair('depths.tnd.'+k,f,e)
    for i,(f,e) in enumerate(zip(['Le débarcadère oublié','Le Bastion enseveli','Les conduits noyés','La Lentille noyée','Les fours silencieux','Le Faucheur de Nacre','L’arsenal disjoint','Le Souverain des soutes'],['The Forgotten Landing','The Buried Bastion','The Drowned Conduits','The Drowned Lens','The Silent Furnaces','The Nacre Reaper','The Sundered Arsenal','The Sovereign Below'])):pair('depths.tnd.chamber.'+str(i),f,e)
