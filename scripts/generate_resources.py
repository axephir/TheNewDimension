#!/usr/bin/env python3
"""Original vanilla-scale pixel art and authored architecture. No downloaded texture pack or vanilla world dump.

Only Python's standard library is required. Set --audio to regenerate the optional
original Ogg ambience with ffmpeg. JSON formats target Minecraft 26.2 (data 107.1).
"""
from pathlib import Path
import argparse
import gzip
import hashlib
import json
import math
import random
import struct
import subprocess
import tempfile
import wave
import zlib
import sys
sys.path.insert(0, str(Path(__file__).resolve().parent))
from pixel_art import EXTRA_BLOCKS, SHAPES, pixels
from architecture import SIZES, build
from progression_resources import expand as expand_progression_languages
import creature_resources
import dungeon_resources
import atmosphere_resources
import expedition_resources
import fieldwork_resources
import awakening_resources
import character_art
import industry_resources
import city_resources
import metropolis_resources
SHAPES.update(awakening_resources.SHAPES)

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
DATA = ROOT / 'data/tnd'
ASSETS = ROOT / 'assets/tnd'
BLOCKS = ['ashstone', 'ancient_alloy', 'resonant_ore', 'conduit',
          'blue_ore', 'red_ore', 'green_ore', 'portal_frame',
          'mural_ocean', 'mural_lava', 'mural_forest', 'mural_fusion', 'extractor', 'portal_controller', 'echo_bloom', 'vault_core', 'vault_seal', 'mural_vault','resonance_forge'] + awakening_resources.BLOCKS + fieldwork_resources.BLOCKS + atmosphere_resources.BLOCKS + EXTRA_BLOCKS + list(SHAPES) + industry_resources.BLOCKS + city_resources.BLOCKS
ITEMS = ['blue_shard', 'red_shard', 'green_shard', 'resonant_shard',
         'composite_crystal', 'locator_core', 'efficiency_module', 'field_journal', 'detector', 'relic_fragment', 'stabilizer_module', 'echo_dust', 'echo_lens', 'shardling_spawn_egg', 'rift_sentinel_spawn_egg', 'resonance_heart', 'resonant_aegis','inertial_matrix','tempered_alloy','resonant_pickaxe','resonant_blade','recall_prism'] + awakening_resources.ITEMS + industry_resources.ITEMS + city_resources.ITEMS + metropolis_resources.ITEMS


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def png(path, pixels, width=16, height=16):
    def chunk(kind, payload):
        return struct.pack('>I', len(payload)) + kind + payload + struct.pack('>I', zlib.crc32(kind + payload))
    rows = b''.join(b'\0' + bytes(sum(pixels[y * width:(y + 1) * width], ())) for y in range(height))
    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    data += chunk(b'IDAT', zlib.compress(rows, 9)) + chunk(b'IEND', b'')
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def texture(name, item=False):
    png(ASSETS / 'textures' / ('item' if item else 'block') / (name + '.png'), pixels(name, item), 16, 16)


def content():
    # NeoForge supplies pack type specific formats (data 107.1, resources 88.0).
    write(ROOT / 'pack.mcmeta', {'pack': {'description': 'The New Dimension — Symbiose 0.12 / Minecraft 26.2'}})
    for block in BLOCKS:
        if block not in SHAPES: texture(block)
        variants = {'': {'model': f'tnd:block/{block}'}}
        if block == 'portal_controller': variants = {'active=false': {'model': 'tnd:block/portal_controller'}, 'active=true': {'model': 'tnd:block/conduit'}}
        if block == 'ancient_console': variants = {'lit=false': {'model':'tnd:block/ancient_console'}, 'lit=true': {'model':'tnd:block/ancient_console_active'}}
        write(ASSETS / f'blockstates/{block}.json', {'variants': variants})
        write(ASSETS / f'models/block/{block}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'tnd:block/{block}'}})
        write(ASSETS / f'models/item/{block}.json', {'parent': f'tnd:block/{block}'})
        write(ASSETS / f'items/{block}.json', {'model': {'type': 'minecraft:model', 'model': f'tnd:item/{block}'}})
        if block in SHAPES:
            base, shape = SHAPES[block]
            tex = {key: f'tnd:block/{base}' for key in ['top','bottom','side']}
            if shape == 'slab':
                write(ASSETS / f'models/block/{block}.json', {'parent':'minecraft:block/slab','textures':tex})
                write(ASSETS / f'models/block/{block}_top.json', {'parent':'minecraft:block/slab_top','textures':tex})
                variants = {'type=bottom':{'model':f'tnd:block/{block}'},'type=top':{'model':f'tnd:block/{block}_top'},'type=double':{'model':f'tnd:block/{base}'}}
            else:
                for suffix, parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:
                    write(ASSETS / f'models/block/{block}{suffix}.json', {'parent':f'minecraft:block/{parent}','textures':tex})
                variants={}
                for facing,rotation in [('east',0),('south',90),('west',180),('north',270)]:
                    for half in ['bottom','top']:
                        for form in ['straight','inner_left','inner_right','outer_left','outer_right']:
                            suffix='' if form=='straight' else '_'+form.split('_')[0]
                            angle=(rotation+(-90 if form.endswith('left') else 0)+(90 if half=='top' and form!='straight' else 0))%360
                            variants[f'facing={facing},half={half},shape={form}']={'model':f'tnd:block/{block}{suffix}','x':180 if half=='top' else 0,'y':angle,'uvlock':True}
            write(ASSETS / f'blockstates/{block}.json',{'variants':variants})
        if block in ('portal_frame', 'portal_controller'):
            continue
        drop = {'blue_ore': 'blue_shard', 'red_ore': 'red_shard', 'green_ore': 'green_shard',
                'resonant_ore': 'resonant_shard'}.get(block, block)
        entry = {'type': 'minecraft:item', 'name': f'tnd:{drop}'}
        if drop != block:
            entry = {'type': 'minecraft:alternatives', 'children': [
                {'type': 'minecraft:item', 'name': f'tnd:{block}', 'conditions': [{
                    'condition': 'minecraft:match_tool', 'predicate': {'predicates': {'minecraft:enchantments': [
                        {'enchantments': 'minecraft:silk_touch', 'levels': {'min': 1}}]}}}]},
                {'type': 'minecraft:item', 'name': f'tnd:{drop}', 'functions': [{
                    'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:ore_drops'}]}]}
        if block in SHAPES and SHAPES[block][1]=='slab':
            entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':f'tnd:{block}','properties':{'type':'double'}}]}]
        write(DATA / f'loot_table/blocks/{block}.json', {'type': 'minecraft:block', 'pools': [{
            'rolls': 1, 'entries': [entry], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    texture('ancient_console_active')
    write(ASSETS / 'models/block/ancient_console_active.json', {'parent':'minecraft:block/cube_all','textures':{'all':'tnd:block/ancient_console_active'}})
    for item in ITEMS:
        texture(item, item=True)
        write(ASSETS / f'models/item/{item}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'tnd:item/{item}'}})
        write(ASSETS / f'items/{item}.json', {'model': {'type': 'minecraft:model', 'model': f'tnd:item/{item}'}})
    creature_resources.content(write,png,ASSETS,DATA)
    dungeon_resources.content(write,texture,ASSETS,DATA)
    expedition_resources.content(write,png,ASSETS,DATA)
    fieldwork_resources.content(write,png,ASSETS,DATA)
    for n in ['resonance_forge_active','resonance_forge_top']:texture(n)
    for tag, values in {'mineable/pickaxe': BLOCKS, 'needs_iron_tool': ['blue_ore', 'red_ore', 'green_ore', 'resonant_ore']}.items():
        write(ROOT / f'data/minecraft/tags/block/{tag}.json', {'replace': False, 'values': [f'tnd:{v}' for v in values]})
    for shape,tag in [('stairs','stairs'),('slab','slabs')]:
        write(ROOT/f'data/minecraft/tags/block/{tag}.json',{'replace':False,'values':['tnd:'+n for n,(base,kind) in SHAPES.items() if kind==shape]})
    for tag in ('dragon_immune', 'wither_immune'):
        write(ROOT / f'data/minecraft/tags/block/{tag}.json', {'replace': False, 'values': ['tnd:portal_frame', 'tnd:portal_controller']})
    recipes = {
        'composite_crystal': (['blue_shard', 'red_shard', 'green_shard'], 'composite_crystal'),
        'field_journal': (['minecraft:book', 'minecraft:amethyst_shard'], 'field_journal'),
        'efficiency_module': (['resonant_shard', 'minecraft:copper_ingot', 'minecraft:redstone'], 'efficiency_module'),
        'ancient_alloy': (['resonant_shard', 'minecraft:iron_ingot'], 'ancient_alloy'),
        'extractor': (['ancient_alloy', 'resonant_shard', 'minecraft:iron_pickaxe', 'minecraft:redstone'], 'extractor'),
        'detector': (['locator_core', 'composite_crystal'], 'detector'),
        'echo_lens': (['echo_dust','echo_dust','echo_dust','composite_crystal','minecraft:amethyst_shard'], 'echo_lens'),
        'stabilizer_module': (['relic_fragment','relic_fragment','relic_fragment','composite_crystal','minecraft:diamond','minecraft:copper_ingot'], 'stabilizer_module')}
    for name, (ingredients, result) in recipes.items():
        write(DATA / f'recipe/{name}.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
            'ingredients': [i if ':' in i else 'tnd:' + i for i in ingredients], 'result': {'id': 'tnd:' + result}})

    for block in EXTRA_BLOCKS:
        ingredient = 'tnd:ancient_alloy' if any(k in block for k in ['alloy','conduit','lamp','console','shelf','grate','vent']) else 'tnd:ashstone'
        write(DATA / f'recipe/{block}.json', {'type':'minecraft:stonecutting','ingredient':ingredient,'result':{'id':f'tnd:{block}','count':1}})
    for block, (base, shape) in SHAPES.items():
        write(DATA / f'recipe/{block}.json', {'type':'minecraft:stonecutting','ingredient':f'tnd:{base}','result':{'id':f'tnd:{block}','count':2 if shape=='slab' else 1}})

    # Visiting the ruins teaches the recipes through Minecraft's own recipe book.
    write(DATA / 'advancement/recipes/ancient_knowledge.json', {
        'criteria': {'shard': {'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'tnd:resonant_shard'}]}}},
        'requirements':[['shard']],
        'rewards':{'recipes':['tnd:'+p.stem for p in sorted((DATA/'recipe').glob('*.json'))]}})


def world():
    write(DATA / 'world_clock/vestige.json', {})
    # 26.2 timelines replace the pre-26.x fixed_time/skylight day APIs.
    tracks = {
        'minecraft:visual/sun_angle': {'keyframes': [{'ticks': 12000, 'value': 360.0}, {'ticks': 12000, 'value': 0.0}]},
        'minecraft:visual/moon_angle': {'keyframes': [{'ticks': 12000, 'value': 540.0}, {'ticks': 12000, 'value': 180.0}]},
        'minecraft:visual/star_angle': {'keyframes': [{'ticks': 12000, 'value': 360.0}, {'ticks': 12000, 'value': 0.0}]}}
    for name, day, night in [('minecraft:gameplay/sky_light_level', 10.0, 2.0),
                             ('minecraft:visual/sky_light_factor', 0.85, 0.22),
                             ('minecraft:visual/star_brightness', 0.0, 0.8)]:
        tracks[name] = {'keyframes': [{'ticks': 1000, 'value': day}, {'ticks': 23000, 'value': day},
                                     {'ticks': 25000, 'value': night}, {'ticks': 47000, 'value': night}]}
    write(DATA / 'timeline/vestige_day.json', {'clock': 'tnd:vestige', 'period_ticks': 48000, 'tracks': tracks,
        'time_markers': {'tnd:day': 2000, 'tnd:night': 26000, 'tnd:noon': 12000, 'tnd:midnight': 36000}})
    write(DATA / 'tags/timeline/in_vestige.json', {'values': ['tnd:vestige_day']})
    write(DATA / 'dimension_type/vestige.json', {
        'ambient_light': 0.08, 'coordinate_scale': 1.0, 'default_clock': 'tnd:vestige',
        'has_ceiling': False, 'has_ender_dragon_fight': False, 'has_skylight': True,
        'height': 256, 'logical_height': 256, 'min_y': 0,
        'infiniburn': '#minecraft:infiniburn_overworld', 'monster_spawn_block_light_limit': 0,
        'monster_spawn_light_level': 0, 'timelines': '#tnd:in_vestige',
        'attributes': {
            'minecraft:visual/sky_color': '#657275', 'minecraft:visual/fog_color': '#747f7f',
            'minecraft:visual/fog_start_distance': 24.0, 'minecraft:visual/fog_end_distance': 150.0,
            'minecraft:visual/sky_fog_end_distance': 180.0,
            'minecraft:visual/ambient_particles': [{'particle': {'type':'minecraft:white_ash'}, 'probability':0.012}],
            'minecraft:visual/cloud_color': '#503c5668', 'minecraft:visual/cloud_height': 196.0,
            'minecraft:visual/ambient_light_color': '#131c29',
            'minecraft:gameplay/bed_rule': {'can_set_spawn': 'always', 'can_sleep': 'never',
                'error_message': {'translate': 'message.tnd.no_sleep'}},
            'minecraft:audio/ambient_sounds': {'mood': {'block_search_extent': 8, 'offset': 2.0,
                'sound': 'tnd:low_signal', 'tick_delay': 6000},
                'loop': 'tnd:vestige_wind', 'additions': {'sound':'tnd:metal_echo','tick_chance':0.00035}}}})
    for name, amplitudes in [('fracture', [1.0, 0.6, 0.2]), ('temperature', [1.0, 0.5])]:
        write(DATA / f'worldgen/noise/{name}.json', {'firstOctave': -7, 'amplitudes': amplitudes})
    noise = lambda name, xz, y: {'type': 'minecraft:noise', 'noise': 'tnd:' + name, 'xz_scale': xz, 'y_scale': y}
    router = {key: 0.0 for key in ['barrier', 'fluid_level_floodedness', 'fluid_level_spread', 'lava',
        'continents', 'erosion', 'depth', 'ridges', 'vegetation', 'vein_toggle', 'vein_ridged', 'vein_gap']}
    router['temperature'] = noise('temperature', 0.25, 0.0)
    router['preliminary_surface_level'] = 80.0
    router['final_density'] = {'type': 'minecraft:interpolated', 'argument': {'type': 'minecraft:add',
        'argument1': {'type': 'minecraft:y_clamped_gradient', 'from_y': 8, 'to_y': 184,
                      'from_value': 1.0, 'to_value': -1.0},
        'argument2': {'type': 'minecraft:mul', 'argument1': 1.3, 'argument2': noise('fracture', 0.7, 1.7)}}}
    # Keep at least 48 blocks of sky above terrain so the largest roofs never
    # intersect the world's build ceiling, even on an extreme noise peak.
    router['final_density']={'type':'minecraft:add','argument1':router['final_density'],
        'argument2':{'type':'minecraft:y_clamped_gradient','from_y':176,'to_y':208,
                     'from_value':0.0,'to_value':-8.0}}
    write(DATA / 'worldgen/noise_settings/vestige.json', {
        'aquifers_enabled': False, 'default_block': {'Name': 'tnd:ashstone'},
        'default_fluid': {'Name': 'minecraft:air'}, 'disable_mob_generation': False,
        'legacy_random_source': False, 'noise': {'height': 256, 'min_y': 0, 'size_horizontal': 1, 'size_vertical': 2},
        'noise_router': router, 'ore_veins_enabled': False, 'sea_level': 0, 'spawn_target': [],
        'surface_rule': {'type': 'minecraft:sequence', 'sequence': [
            {'type': 'minecraft:condition', 'if_true': {'type': 'minecraft:vertical_gradient',
                'random_name': 'tnd:bedrock', 'true_at_and_below': {'absolute': 0}, 'false_at_and_above': {'absolute': 5}},
             'then_run': {'type': 'minecraft:block', 'result_state': {'Name': 'minecraft:bedrock'}}},
            {'type': 'minecraft:block', 'result_state': {'Name': 'tnd:ashstone'}}]}})
    biomes = [('silent_expanse', '#747f7f'), ('fractured_fields', '#7a737b'), ('hollow_shelves', '#657e79')]
    for name, color in biomes:
        write(DATA / f'worldgen/biome/{name}.json', {
            'has_precipitation': False, 'temperature': 0.3, 'downfall': 0.0,
            'effects': {'water_color': '#284753'}, 'attributes': {'minecraft:visual/fog_color': color},
            'carvers': [], 'features': [[], [], [], [], [], [], ['tnd:resonant_ore'], [], [], [], ['tnd:echo_bloom']],
            'creature_spawn_probability':0.02,
            'spawn_costs': {'tnd:rift_sentinel':{'energy_budget':0.1,'charge':1.0}}, 'spawners': {group: ([{'type':'tnd:shardling','weight':3,'minCount':1,'maxCount':1}] if group=='creature' else [{'type':'tnd:rift_sentinel','weight':3,'minCount':1,'maxCount':1}] if group=='monster' else []) for group in ['ambient', 'axolotls', 'creature',
                'misc', 'monster', 'underground_water_creature', 'water_ambient', 'water_creature']}})
    entries = []
    for index, (name, _) in enumerate(biomes):
        entries.append({'biome': 'tnd:' + name, 'parameters': {
            'temperature': [[-1.0, -0.2], [-0.2, 0.2], [0.2, 1.0]][index],
            'humidity': [-1.0, 1.0], 'continentalness': [-1.0, 1.0], 'erosion': [-1.0, 1.0],
            'depth': [-1.0, 1.0], 'weirdness': [-1.0, 1.0], 'offset': 0.0}})
    write(DATA / 'dimension/vestige.json', {'type': 'tnd:vestige', 'generator': {
        'type': 'minecraft:noise', 'settings': 'tnd:vestige',
        'biome_source': {'type': 'minecraft:multi_noise', 'biomes': entries}}})
    write(DATA / 'worldgen/configured_feature/resonant_ore.json', {'type': 'minecraft:ore', 'config': {
        'size': 6, 'discard_chance_on_air_exposure': 0.0,
        'targets': [{'target': {'predicate_type': 'minecraft:block_match', 'block': 'tnd:ashstone'},
                     'state': {'Name': 'tnd:resonant_ore'}}]}})
    write(DATA / 'worldgen/placed_feature/resonant_ore.json', {'feature': 'tnd:resonant_ore', 'placement': [
        {'type': 'minecraft:count', 'count': 8}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform',
            'min_inclusive': {'absolute': 8}, 'max_inclusive': {'absolute': 150}}}, {'type': 'minecraft:biome'}]})
    ore_worldgen()
    atmosphere_resources.world(write,DATA)


def ore_worldgen():
    for color, biomes in {'blue':['#minecraft:is_ocean'], 'red':['#minecraft:is_overworld'],
            'green':['#minecraft:is_forest','#minecraft:is_taiga','#minecraft:is_jungle']}.items():
        write(DATA / f'tags/worldgen/biome/{color}_ore.json', {'values':biomes})
        write(DATA / f'worldgen/configured_feature/{color}_ore.json', {'type':f'tnd:{color}_habitat_ore','config':{}})
        write(DATA / f'worldgen/placed_feature/{color}_ore.json', {'feature':f'tnd:{color}_ore','placement':[]})
        write(DATA / f'neoforge/biome_modifier/{color}_ore.json', {'type':'neoforge:add_features',
            'biomes':f'#tnd:{color}_ore','features':[f'tnd:{color}_ore'],'step':'top_layer_modification'})
    write(DATA / 'tags/block/ore_substrates.json', {'values':['#minecraft:stone_ore_replaceables',
        '#minecraft:deepslate_ore_replaceables','minecraft:sand','minecraft:gravel','minecraft:clay',
        'minecraft:dirt','minecraft:grass_block','minecraft:rooted_dirt','minecraft:podzol',
        'minecraft:coarse_dirt','minecraft:moss_block']})


# Minimal NBT writer for original, reproducibly generated structure templates.
def nbt_string(value):
    b = value.encode('utf-8')
    return struct.pack('>H', len(b)) + b


def nbt_payload(kind, value):
    if kind == 3: return struct.pack('>i', value)
    if kind == 8: return nbt_string(value)
    if kind == 9:
        subtype, entries = value
        return bytes([subtype]) + struct.pack('>i', len(entries)) + b''.join(nbt_payload(subtype, v) for v in entries)
    if kind == 10:
        return b''.join(bytes([k]) + nbt_string(n) + nbt_payload(k, v) for n, (k, v) in value.items()) + b'\0'
    raise ValueError(kind)


def structure(name, size):
    building=build(name)
    palette=[]; indices={}; blocks=[]
    for (x,y,z),(block,props,loot) in sorted(building.finalize().items(),key=lambda e:(e[0][1],e[0][2],e[0][0])):
        key=(block,props)
        if key not in indices:
            indices[key]=len(palette)
            state={'Name':(8,block)}
            if props:state['Properties']=(10,{k:(8,v) for k,v in props})
            palette.append(state)
        entry={'pos':(9,(3,[x,y,z])),'state':(3,indices[key])}
        if loot:entry['nbt']=(10,{'id':(8,'minecraft:chest'),'LootTable':(8,loot)})
        if block=='tnd:vault_core':entry['nbt']=(10,{'id':(8,'tnd:vault_core'),'TrialSite':(3,1)})
        if block=='tnd:survey_terminal':entry['nbt']=(10,{'id':(8,'tnd:survey_terminal'),'SurveySite':(3,1 if name=='observatory' else 0)})
        blocks.append(entry)
    root={'DataVersion':(3,4903),'size':(9,(3,list(size))),'palette':(9,(10,palette)),
          'blocks':(9,(10,blocks)),'entities':(9,(10,[]))}
    path=DATA/f'structure/{name}.nbt';path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(gzip.compress(b'\x0a\x00\x00'+nbt_payload(10,root),mtime=0))
    write(DATA/f'worldgen/template_pool/{name}.json',{'fallback':'minecraft:empty','elements':[{'weight':1,
        'element':{'element_type':'minecraft:single_pool_element','location':f'tnd:{name}',
                   'processors':'minecraft:empty','projection':'rigid'}}]})
    biomes='#minecraft:is_overworld' if name in ('outpost','portal_site') else '#tnd:is_vestige'
    write(DATA/f'worldgen/structure/{name}.json',{'type':'tnd:crowned_site','template':f'tnd:{name}','biomes':biomes,
        'spawn_overrides':{},'step':'surface_structures','terrain_adaptation':'none'})
    spacing={'outpost':(64,40),'portal_site':(768,640),'relay':(26,14),'archive':(32,18),'foundry':(30,16),'resonance_vault':(30,16),'observatory':(30,16),'aureate_citadel':(30,16),'echo_conservatory':(30,16)}[name]
    write(DATA/f'worldgen/structure_set/{name}.json',{'placement':{'type':'minecraft:random_spread',
        'salt':{'outpost':845331,'portal_site':934281,'relay':129811,'archive':129812,'foundry':129813,'resonance_vault':129814,'observatory':129815,'aureate_citadel':129816,'echo_conservatory':129817}[name],
        'spacing':spacing[0],'separation':spacing[1]},'structures':[{'structure':f'tnd:{name}','weight':1}]})


def structures():
    for name, size in SIZES.items():
        structure(name, size)
    # A shared grid prevents different building types from intersecting each other.
    # Separate independent sets can pick neighboring chunks even with large spacing.
    for name in ['relay','archive','foundry','resonance_vault','observatory','aureate_citadel','echo_conservatory']:
        (DATA/f'worldgen/structure_set/{name}.json').unlink(missing_ok=True)
    write(DATA/'worldgen/structure_set/vestige_sites.json',{'placement':{
        'type':'minecraft:random_spread','salt':129811,'spacing':20,'separation':13},
        'structures':[{'structure':f'tnd:{name}','weight':1} for name in ['relay','archive','foundry','resonance_vault','observatory','aureate_citadel','echo_conservatory']]})
    path=DATA/'worldgen/structure_set/outpost.json';outpost=json.loads(path.read_text())
    outpost['placement']['exclusion_zone']={'other_set':'tnd:portal_site','chunk_count':12}
    write(path,outpost)
    write(DATA / 'loot_table/chests/outpost.json', {'type': 'minecraft:chest', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'tnd:locator_core'}]},
        {'rolls': 3, 'entries': [{'type':'minecraft:item','name':'minecraft:bread'}, {'type':'minecraft:item','name':'minecraft:torch'}, {'type':'minecraft:item','name':'minecraft:iron_ingot'}]}]})
    for loot, entries in {'salvage':['minecraft:iron_ingot','minecraft:coal','tnd:ashstone_bricks','tnd:ancient_alloy'],
        'relics':['tnd:resonant_shard','tnd:blue_shard','tnd:efficiency_module','minecraft:amethyst_shard'],
        'archive':['minecraft:paper','minecraft:book','tnd:resonant_shard','tnd:archive_shelf'],
        'citadel':['tnd:raw_auralite','tnd:raw_noctilite','tnd:relic_fragment','minecraft:bread','tnd:echo_dust'],
        'conservatory':['tnd:echo_bloom','tnd:raw_noctilite','minecraft:coal','tnd:glass_filament','tnd:echo_dust'],
        'observatory':['minecraft:paper','minecraft:amethyst_shard','tnd:relic_fragment','tnd:star_chart','tnd:echo_dust'],
        'foundry':['minecraft:raw_iron','minecraft:copper_ingot','tnd:ancient_alloy','tnd:efficiency_module']}.items():
        write(DATA / f'loot_table/chests/{loot}.json', {'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3,'max':6},
            'entries':[{'type':'minecraft:item','name':item,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':4}}]} for item in entries]}]})
    write(DATA / 'tags/worldgen/structure/portals.json', {'values': ['tnd:portal_site']})
    # Real engine tests: functions are registered through BuiltInRegistries.TEST_FUNCTION.
    empty = {'DataVersion': (3, 4903), 'size': (9, (3, [5, 5, 5])),
             'palette': (9, (10, [{'Name': (8, 'minecraft:air')}])),
             'blocks': (9, (10, [])), 'entities': (9, (10, []))}
    (DATA / 'structure/test_empty.nbt').write_bytes(gzip.compress(b'\x0a\x00\x00' + nbt_payload(10, empty), mtime=0))
    for name,size in [('test_habitat',[32,24,32]),('test_buildings',[66,45,66]),('test_awakening',[99,50,99])]:
        root=dict(empty);root['size']=(9,(3,size))
        (DATA/f'structure/{name}.nbt').write_bytes(gzip.compress(b'\x0a\x00\x00'+nbt_payload(10,root),mtime=0))
    for name, maximum in [('dimension', 200), ('extractor', 3000), ('portal_resistance', 100),
                          ('portal_terrain', 200), ('portal_arrival', 100),
                          ('portal_no_space', 200), ('portal_round_trip', 400),
                          ('habitat_ores', 200), ('buildings', 200), ('outpost_terrain',200), ('mastery_actions',100), ('console',100), ('extraction_modes',100), ('talent_menus',100), ('talent_effects',100), ('companion_bond',100), ('companion_follow',150), ('companion_travel',200), ('companion_cold_arrival',150), ('sentinel_dodge',100), ('sentinel_hit',100), ('sentinel_interrupt',100), ('echo_lens',100), ('echo_deposits',100), ('vault_progress',400), ('vault_recovery',400), ('vault_safety',200), ('vault_rotations',200), ('resonant_aegis',100), ('biome_palette',200), ('vestige_landscapes',200), ('mastery_celebration',100),('custodian_phase',100),('custodian_dodge',100),('custodian_hit',100),('custodian_crown',100),('custodian_stagger',100),('custodian_progress',300),('custodian_recovery',400),('forge_processing',500),('forge_recovery',1400),('forge_hoppers',600),('field_objectives',200),('field_actions',200),('field_completion',100),('recall_success',200),('recall_movement',200),('recall_damage',200),('recall_blocked',200),('recall_replaced',200),('recall_guards',100),('observatory',200),('survey_authority',200)]:
        write(DATA / f'test_instance/{name}.json', {'type': 'minecraft:function', 'environment': 'minecraft:default',
            'function': f'tnd:{name}', 'structure': 'tnd:'+('test_habitat' if name in ('custodian_phase','custodian_dodge','custodian_hit','custodian_crown','custodian_stagger','habitat_ores','outpost_terrain','sentinel_dodge','sentinel_hit','sentinel_interrupt','companion_follow','companion_travel','companion_cold_arrival','echo_deposits','vestige_landscapes') else 'test_buildings' if name in ('observatory','custodian_progress','custodian_recovery','buildings','vault_progress','vault_recovery','vault_safety','vault_rotations') else 'test_empty'), 'max_ticks': maximum, 'required': True})


    for name in awakening_resources.TESTS:
        write(DATA/f'test_instance/{name}.json',{'type':'minecraft:function','environment':'minecraft:default','function':'tnd:'+name,'structure':'tnd:'+('test_awakening' if name in ['citadel_layout','conservatory_layout'] else 'test_habitat' if name in ['lithoguard_hit','lithoguard_dodge','lithoguard_patterns','stalker_hit','stalker_interrupt','awakened_loot','gravitas_wave'] else 'test_empty'),'max_ticks':400,'required':True})


def languages():
    fr = {'itemGroup.tnd': 'The New Dimension', 'gui.tnd.masteries': 'Maîtrises',
          'gui.tnd.level': 'Niveau %s', 'gui.tnd.experience': 'XP %s / %s', 'gui.tnd.points': '%s points disponibles',
          'gui.tnd.speed': 'Vitesse : +%s %%', 'gui.tnd.yield': 'Chance de bonus : %s %%',
          'gui.tnd.talent_rank': '%s %s/%s · %s pt', 'message.tnd.dimension_missing': 'La dimension Vestige est introuvable.',
          'message.tnd.unsafe_return': 'Zone de retour aquatique. Utilisez /execute in minecraft:overworld run tp pour choisir un lieu sûr.',
          'message.tnd.no_sleep': 'Ici, rien ne semble dormir.'}
    names = ['Minage', 'Bûcheronnage', 'Agriculture', 'Pêche', 'Chasse', 'Élevage', 'Combat rapproché',
             'Combat à distance', 'Défense', 'Artisanat', 'Construction', 'Exploration', 'Alchimie', 'Ingénierie', 'Technologies anciennes']
    ids = ['mining', 'logging', 'farming', 'fishing', 'hunting', 'breeding', 'melee', 'ranged', 'defense',
           'crafting', 'building', 'exploration', 'alchemy', 'engineering', 'ancient_tech']
    fr.update({'mastery.tnd.' + key: name for key, name in zip(ids, names)})
    for key, label in {'efficiency': 'Efficacité', 'yield': 'Rendement', 'insight': 'Perception',
                       'precision': 'Précision', 'synergy': 'Résonance'}.items(): fr['talent.tnd.' + key] = label
    for key, label in {'efficiency': '+5 % de vitesse de récolte par rang.', 'yield': '+2 % de chance d’un objet supplémentaire par récolte et par rang.',
                       'insight': 'Débloque /tnd sense mining ou logging. Recharge : 5 secondes.',
                       'precision': '+2 blocs de rayon de perception par rang.',
                       'synergy': '+2 % de vitesse par rang partagé avec Résonance en ingénierie.'}.items():
        fr['talent.tnd.' + key + '.description'] = label
    for key, label in {'efficiency': '+5 % de cadence des extracteurs par rang.', 'yield': '+3 % de cadence par rang grâce à la réduction des temps morts.',
                       'insight': 'Débloque /tnd sense engineering : détecte les extracteurs proches.',
                       'precision': '+1 % de cadence et +2 blocs de perception par rang.',
                       'synergy': '+2 % de cadence par rang partagé avec Résonance en minage.'}.items():
        fr['talent.tnd.engineering.' + key + '.description'] = label
    fr['gui.tnd.calibration'] = 'Synergie possible : +%s %%'
    labels = ['Pierre cendrée', 'Alliage ancien', 'Minerai résonant', 'Conduit', 'Minerai abyssal',
              'Minerai pyrique', 'Minerai sylvestre', 'Cadre de portail ancien', 'Fresque des abysses',
              'Fresque du feu', 'Fresque des forêts', 'Fresque de fusion', 'Extracteur minier', 'Mécanisme de portail']
    fr.update({'block.tnd.' + k: v for k, v in zip(BLOCKS, labels)})
    labels = ['Éclat abyssal', 'Éclat pyrique', 'Éclat sylvestre', 'Éclat résonant', 'Cristal composite',
              'Noyau de localisation', "Module d’efficacité", 'Carnet de terrain', 'Détecteur ancien']
    fr.update({'item.tnd.' + k: v for k, v in zip(ITEMS, labels)})
    fr.update({'gui.tnd.extractor.help': 'Charbon · Module · Fer brut',
               'gui.tnd.extractor.period': 'Cycle : %s secondes',
               'subtitles.tnd.low_signal': 'Une vibration traverse la matière'})
    fr.update({'message.tnd.detector_dimension': 'Le noyau ne reconnaît pas cet espace.',
        'message.tnd.detector_silent': 'Aucune résonance dans cette région.',
        'message.tnd.detector_exact': 'Convergence : X %s · Z %s',
        'message.tnd.detector_direction': 'Résonance : %s · %s',
        'message.tnd.signal_strong': 'signal convergent', 'message.tnd.signal_faint': 'signal lointain',
        'message.tnd.portal_active': 'Le passage est stabilisé. Il restera ouvert.',
        'message.tnd.portal_blocked': 'Arrivée obstruée : dégagez de l’espace autour du mécanisme.',
        'message.tnd.portal_no_site': 'Impossible d’installer le passage de retour à cet endroit.',
        'message.tnd.portal_unavailable': 'Le passage correspondant manque. Contactez un administrateur.'})
    fr.update({'message.tnd.sense_locked': 'Le talent Perception est nécessaire.',
        'message.tnd.sense_empty': 'Aucune résonance proche.',
        'message.tnd.sense_found': 'Une présence à environ %s blocs · %s',
        'direction.tnd.above': 'plus haut', 'direction.tnd.below': 'plus bas', 'direction.tnd.level': 'à cette hauteur'})
    for key, label in {'east': 'Est', 'southeast': 'Sud-est', 'south': 'Sud', 'southwest': 'Sud-ouest',
                       'west': 'Ouest', 'northwest': 'Nord-ouest', 'north': 'Nord', 'northeast': 'Nord-est'}.items():
        fr['direction.tnd.' + key] = label
    fr.update({'block.tnd.'+k:k.replace('_',' ').capitalize() for k in EXTRA_BLOCKS+list(SHAPES)})
    fr.update({
        'gui.tnd.ancient_short':'Tech. anciennes',
        'gui.tnd.inventory_masteries':'Maîtrises — niveaux, expérience et talents',
        'gui.tnd.mastery_summary':'%s\nNiveau %s · %s XP\n%s points disponibles',
        'gui.tnd.talent_cost':'Coût : %s points par rang',
        'gui.tnd.prerequisite':'Prérequis : %s (rang 1)',
        'gui.tnd.mastery_hint':'Survolez un talent pour découvrir ses effets.',
        'gui.tnd.mastery_planned':'Cette maîtrise sera disponible ultérieurement.',
        'gui.tnd.extractor.fuel':'Charbon','gui.tnd.extractor.module':'Module','gui.tnd.extractor.output':'Fer brut',
        'subtitles.tnd.vestige_wind':'Un souffle parcourt les ruines',
        'subtitles.tnd.metal_echo':'Un écho métallique résonne au loin'})
    labels={'ashstone_bricks':'Briques cendrées','cracked_ashstone_bricks':'Briques cendrées fissurées',
        'mossy_ashstone_bricks':'Briques cendrées moussues','polished_ashstone':'Pierre cendrée polie',
        'ashstone_tiles':'Dallage cendré','chiseled_ashstone':'Pierre cendrée sculptée','ancient_pillar':'Pilier ancien',
        'riveted_alloy':'Alliage riveté','cut_alloy':'Alliage taillé','oxidized_alloy':'Alliage patiné',
        'ancient_grate':'Grille ancienne','dormant_conduit':'Conduit dormant','resonant_lamp':'Lanterne résonante',
        'blue_lamp':'Lanterne abyssale','red_lamp':'Lanterne pyrique','green_lamp':'Lanterne sylvestre',
        'archive_shelf':'Rayonnage ancien','ancient_console':'Console ancienne','alloy_vent':'Évent en alliage'}
    for key,value in labels.items():fr['block.tnd.'+key]=value
    for key,(base,shape) in SHAPES.items():fr['block.tnd.'+key]=('Escalier — ' if shape=='stairs' else 'Dalle — ')+labels.get(base,base.replace('_',' ')).lower()
    en = dict(fr)
    en.update({'gui.tnd.masteries': 'Masteries', 'gui.tnd.level': 'Level %s', 'gui.tnd.points': '%s available points',
               'gui.tnd.speed': 'Speed: +%s %%', 'gui.tnd.yield': 'Bonus chance: %s %%',
               'message.tnd.no_sleep': 'Nothing here seems to sleep.',
               'message.tnd.dimension_missing': 'The Vestige dimension could not be found.',
               'message.tnd.unsafe_return': 'The return site is underwater. Choose a safe Overworld destination with /execute in minecraft:overworld run tp.',
               'message.tnd.detector_dimension': 'The core does not recognize this space.',
               'message.tnd.detector_silent': 'No resonance in this region.',
               'message.tnd.detector_exact': 'Convergence: X %s · Z %s',
               'message.tnd.detector_direction': 'Resonance: %s · %s',
               'message.tnd.signal_strong': 'convergent signal', 'message.tnd.signal_faint': 'distant signal',
               'message.tnd.portal_active': 'The passage is stable. It will remain open.',
               'message.tnd.portal_blocked': 'Arrival obstructed: clear some space around the mechanism.',
               'message.tnd.portal_no_site': 'Cannot place a return passage safely at this location.',
               'message.tnd.portal_unavailable': 'The paired passage is missing. Contact an administrator.',
               'message.tnd.sense_locked': 'The Insight talent is required.',
               'message.tnd.sense_empty': 'No nearby resonance.',
               'message.tnd.sense_found': 'A presence about %s blocks away · %s',
               'direction.tnd.above': 'above', 'direction.tnd.below': 'below', 'direction.tnd.level': 'at this height',
               'gui.tnd.calibration': 'Potential synergy: +%s %%'})
    english = ['Mining', 'Logging', 'Farming', 'Fishing', 'Hunting', 'Breeding', 'Melee', 'Ranged',
               'Defense', 'Crafting', 'Building', 'Exploration', 'Alchemy', 'Engineering', 'Ancient technology']
    en.update({'mastery.tnd.' + k: v for k, v in zip(ids, english)})
    for key in BLOCKS: en['block.tnd.' + key] = key.replace('_', ' ').title()
    for key in ITEMS: en['item.tnd.' + key] = key.replace('_', ' ').title()
    for key in ['efficiency', 'yield', 'insight', 'precision', 'synergy']: en['talent.tnd.' + key] = key.title()
    for key in ['east', 'southeast', 'south', 'southwest', 'west', 'northwest', 'north', 'northeast']:
        en['direction.tnd.' + key] = key.title()
    for key, description in {'efficiency': '+5% manual harvesting speed per rank.',
        'yield': '+2% chance of one extra drop per manual harvest and rank.',
        'insight': 'Unlock /tnd sense mining or logging. Cooldown: 5 seconds.',
        'precision': '+2 blocks of sensing radius per rank.',
        'synergy': '+2% speed per rank shared with Engineering Synergy.'}.items():
        en['talent.tnd.' + key + '.description'] = description
    for key, description in {'efficiency': '+5% extractor throughput per rank.',
        'yield': '+3% throughput per rank by reducing idle time.',
        'insight': 'Unlock /tnd sense engineering to detect nearby extractors.',
        'precision': '+1% throughput and +2 blocks of sensing radius per rank.',
        'synergy': '+2% throughput per rank shared with Mining Synergy.'}.items():
        en['talent.tnd.engineering.' + key + '.description'] = description
    en.update({'gui.tnd.extractor.help': 'Coal · Module · Raw iron', 'gui.tnd.extractor.period': 'Cycle: %s seconds',
               'gui.tnd.talent_rank': '%s %s/%s · %s pt',
               'subtitles.tnd.low_signal': 'A vibration passes through matter'})
    en.update({'gui.tnd.ancient_short':'Ancient tech.',
        'gui.tnd.inventory_masteries':'Masteries — levels, experience and talents',
        'gui.tnd.mastery_summary':'%s\nLevel %s · %s XP\n%s available points',
        'gui.tnd.talent_cost':'Cost: %s points per rank','gui.tnd.prerequisite':'Requires: %s (rank 1)',
        'gui.tnd.mastery_hint':'Hover over a talent to discover its effects.',
        'gui.tnd.mastery_planned':'This mastery will be available in a future update.',
        'gui.tnd.extractor.fuel':'Coal','gui.tnd.extractor.module':'Module','gui.tnd.extractor.output':'Raw iron',
        'subtitles.tnd.vestige_wind':'Wind passes through the ruins','subtitles.tnd.metal_echo':'Metal echoes in the distance'})
    expand_progression_languages(fr,en,ids)
    creature_resources.languages(fr,en)
    dungeon_resources.languages(fr,en)
    expedition_resources.languages(fr,en)
    fieldwork_resources.languages(fr,en)
    atmosphere_resources.languages(fr,en)
    awakening_resources.languages(fr,en)
    industry_resources.languages(fr,en)
    city_resources.languages(fr,en)
    metropolis_resources.languages(fr,en)
    write(ASSETS / 'lang/fr_fr.json', fr)
    write(ASSETS / 'lang/en_us.json', en)


def audio():
    import soundscape_audio
    soundscape_audio.generate(ASSETS,write)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--audio', action='store_true')
    options = parser.parse_args()
    content(); world(); structures(); awakening_resources.content(write,png,ASSETS,DATA); character_art.content(png,ASSETS); industry_resources.content(write,png,ASSETS,DATA); city_resources.content(write,png,ASSETS,DATA,nbt_payload); metropolis_resources.content(write,png,ASSETS,DATA,nbt_payload); languages()
    if options.audio:
        audio()
        import awakening_audio
        awakening_audio.generate(ASSETS,write)
        import city_audio
        city_audio.generate(ASSETS,write)
        import metropolis_audio
        metropolis_audio.generate(ASSETS,write)
    print('Generated 16px blocks/items, 64px character atlases, nine ancient sites and Nacre-9.')
