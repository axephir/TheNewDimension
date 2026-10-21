"""Six real climate cells, biome-specific terrain/surfaces and authored soundscape references."""
import copy,json

BIOMES={
    'silent_expanse':('#747f7f','vestige_wind'),
    'fractured_fields':('#7a737b','fracture_drone'),
    'hollow_shelves':('#657e79','hollow_resonance'),
    'glass_woodland':('#768f94','glass_whispers'),
    'ashen_basin':('#506d68','basin_lament'),
    'obsidian_dunes':('#86745e','dune_rumble')}
BLOCKS=['vitreous_stone','vitrified_ash','ashen_silt','fossil_column','pale_crystal']

def world(write,data):
    load=lambda path:json.loads((data/path).read_text())
    dimension=load('dimension_type/vestige.json')
    dimension['attributes']['minecraft:audio/ambient_sounds']={'loop':'tnd:vestige_wind'}
    write(data/'dimension_type/vestige.json',dimension)
    base=load('worldgen/biome/silent_expanse.json')
    for name,(color,loop) in BIOMES.items():
        biome=copy.deepcopy(base)
        biome['attributes']={'minecraft:visual/fog_color':color,'minecraft:audio/ambient_sounds':{'loop':'tnd:'+loop}}
        biome['features'][10]=['tnd:vestige_landscape','tnd:echo_bloom']
        if name=='glass_woodland':
            biome['attributes'].update({'minecraft:visual/fog_start_distance':18.0,'minecraft:visual/fog_end_distance':130.0,
                'minecraft:visual/ambient_particles':[{'particle':{'type':'minecraft:end_rod'},'probability':.002},{'particle':{'type':'minecraft:white_ash'},'probability':.009}]})
        if name=='ashen_basin':
            biome['effects']['water_color']='#173d3c'
            biome['attributes'].update({'minecraft:visual/fog_start_distance':10.0,'minecraft:visual/fog_end_distance':112.0,
                'minecraft:visual/water_fog_color':'#153432','minecraft:visual/water_fog_end_distance':24.0})
        if name=='obsidian_dunes':
            biome['attributes'].update({'minecraft:visual/fog_end_distance':165.0,
                'minecraft:visual/ambient_particles':[{'particle':{'type':'minecraft:ash'},'probability':.014}]})
        write(data/f'worldgen/biome/{name}.json',biome)
    write(data/'tags/worldgen/biome/is_vestige.json',{'values':['tnd:'+name for name in BIOMES]})
    write(data/'tags/block/vestige_surfaces.json',{'values':['tnd:ashstone','tnd:vitreous_stone','tnd:vitrified_ash','tnd:ashen_silt']})
    write(data/'worldgen/configured_feature/vestige_landscape.json',{'type':'tnd:vestige_landscape','config':{}})
    write(data/'worldgen/placed_feature/vestige_landscape.json',{'feature':'tnd:vestige_landscape','placement':[]})
    write(data/'worldgen/noise/humidity.json',{'firstOctave':-6,'amplitudes':[1.0,.5]})
    write(data/'worldgen/noise/dunes.json',{'firstOctave':-6,'amplitudes':[1.0,.6,.2]})
    settings=load('worldgen/noise_settings/vestige.json');router=settings['noise_router']
    noise=lambda name,xz,y:{'type':'minecraft:noise','noise':'tnd:'+name,'xz_scale':xz,'y_scale':y}
    router['vegetation']=noise('humidity',.45,0)
    def land(top,strength,shape):
        density={'type':'minecraft:add','argument1':{'type':'minecraft:y_clamped_gradient','from_y':8,'to_y':top,'from_value':1.0,'to_value':-1.0},
            'argument2':{'type':'minecraft:mul','argument1':strength,'argument2':shape}}
        return {'type':'minecraft:interpolated','argument':density}
    temperature=router['temperature']
    def weight(value,center,width):
        return {'type':'minecraft:clamp','min':0.0,'max':1.0,'input':{'type':'minecraft:add','argument1':.5,
            'argument2':{'type':'minecraft:mul','argument1':1/width,'argument2':{'type':'minecraft:add','argument1':value,'argument2':-center}}}}
    def blend(a,b,t):
        return {'type':'minecraft:add','argument1':{'type':'minecraft:mul','argument1':{'type':'minecraft:add','argument1':1.0,'argument2':{'type':'minecraft:mul','argument1':-1.0,'argument2':t}},'argument2':a},
            'argument2':{'type':'minecraft:mul','argument1':t,'argument2':b}}
    # Blend densities over climate bands, rather than erecting vertical walls at biome edges.
    new_land=blend(land(152,.55,noise('fracture',.6,.7)),land(108,.24,noise('dunes',.5,0)),weight(temperature,-.14,.18))
    new_land=blend(new_land,land(164,.65,noise('dunes',.9,0)),weight(temperature,.14,.18))
    router['final_density']=blend(router['final_density'],new_land,weight(router['vegetation'],0,.24))
    surface=settings['surface_rule']['sequence']
    for biome,block in [('glass_woodland','vitreous_stone'),('ashen_basin','ashen_silt'),('obsidian_dunes','vitrified_ash')]:
        surface.insert(-1,{'type':'minecraft:condition','if_true':{'type':'minecraft:biome','biome_is':['tnd:'+biome]},
            'then_run':{'type':'minecraft:condition','if_true':{'type':'minecraft:stone_depth','offset':3,'add_surface_depth':False,'secondary_depth_range':0,'surface_type':'floor'},
                'then_run':{'type':'minecraft:block','result_state':{'Name':'tnd:'+block}}}})
    write(data/'worldgen/noise_settings/vestige.json',settings)
    stem=load('dimension/vestige.json');entries=[]
    for humid,names in [([-1,0],list(BIOMES)[:3]),([0,1],list(BIOMES)[3:])]:
        for name,temp in zip(names,[[-1,-.14],[-.14,.14],[.14,1]]):
            entries.append({'biome':'tnd:'+name,'parameters':{'temperature':temp,'humidity':humid,'continentalness':[-1,1],
                'erosion':[-1,1],'depth':[-1,1],'weirdness':[-1,1],'offset':0.0}})
    stem['generator']['biome_source']['biomes']=entries;write(data/'dimension/vestige.json',stem)

def languages(fr,en):
    for name,french,english in [('vitreous_stone','Pierre vitreuse','Vitreous Stone'),('vitrified_ash','Cendre vitrifiée','Vitrified Ash'),
        ('ashen_silt','Limon cendré','Ashen Silt'),('fossil_column','Colonne fossilisée','Fossil Column'),('pale_crystal','Cristal pâle','Pale Crystal')]:
        fr['block.tnd.'+name]=french;en['block.tnd.'+name]=english
    for name,french,english in [('silent_expanse','Étendues silencieuses','Silent Expanse'),('fractured_fields','Champs fracturés','Fractured Fields'),
        ('hollow_shelves','Corniches creuses','Hollow Shelves'),('glass_woodland','Forêt de verre','Glass Woodland'),
        ('ashen_basin','Bassins de cendre','Ashen Basins'),('obsidian_dunes','Dunes vitrifiées','Obsidian Dunes')]:
        fr['biome.tnd.'+name]=french;en['biome.tnd.'+name]=english
    subtitles={
        'low_signal':('Une vibration lointaine résonne','A distant vibration resonates'),
        'vestige_wind':('Le vent traverse les ruines','Wind passes through ruins'),
        'metal_echo':('Un métal résonne au loin','Metal echoes in the distance'),
        'fracture_drone':('Les failles grondent','Faults rumble'),
        'hollow_resonance':('Les profondeurs résonnent','Depths resonate'),
        'glass_whispers':('Les cristaux frémissent','Crystals shimmer'),
        'basin_lament':('Les bassins murmurent','Basins murmur'),
        'dune_rumble':('Les cendres grondent','Ash rumbles'),
        'night_pressure':('Une pression sourde monte','A low pressure rises'),
        'enclosed_hum':('La structure vibre','The structure hums'),
        'distant_pulse':('Une pulsation se propage','A pulse travels'),
        'glass_tinkle':('Des fragments tintent','Fragments chime'),
        'stone_shift':('Une pierre grince','Stone grinds'),
        'submerged_groan':('Une résonance sous les eaux','A resonance beneath the water'),
        'dust_scrape':('Des cendres raclent la pierre','Ash scrapes stone'),
        'reverse_breath':('Un souffle se déforme','A breath distorts'),
        'spatial_chime':('Une résonance se dédouble','A resonance splits'),
        'mastery_ascend':('Une maîtrise progresse','A mastery advances')}
    for key,(french,english) in subtitles.items():fr['subtitles.tnd.'+key]=french;en['subtitles.tnd.'+key]=english
