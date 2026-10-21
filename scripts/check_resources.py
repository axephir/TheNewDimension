#!/usr/bin/env python3
"""Check asset references and binary integrity; runtime codecs still require GameTests."""
import gzip
import importlib.util
import json
import re
import struct
import unittest
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/tnd'
DATA = RES / 'data/tnd'
spec = importlib.util.spec_from_file_location('resources', ROOT / 'scripts/generate_resources.py')
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class Nbt:
    def __init__(self, data): self.data, self.index = data, 0
    def take(self, length):
        if length < 0 or self.index + length > len(self.data): raise ValueError('truncated NBT')
        result = self.data[self.index:self.index + length]
        self.index += length
        return result
    def integer(self): return struct.unpack('>i', self.take(4))[0]
    def string(self): return self.take(struct.unpack('>H', self.take(2))[0]).decode('utf-8')
    def read(self, kind):
        if kind == 3: return self.integer()
        if kind == 8: return self.string()
        if kind == 9:
            subtype, count = self.take(1)[0], self.integer()
            if not 0 <= count < 1_000_000: raise ValueError('invalid NBT list size')
            return [self.read(subtype) for _ in range(count)]
        if kind == 10:
            result = {}
            while (subtype := self.take(1)[0]) != 0:
                name = self.string()
                if name in result: raise ValueError('duplicate NBT field')
                result[name] = self.read(subtype)
            return result
        raise ValueError('unexpected NBT type ' + str(kind))


class Resources(unittest.TestCase):
    def test_crowned_sites_and_character_resolution(self):
        for name in generator.SIZES:
            site=json.loads((DATA/f'worldgen/structure/{name}.json').read_text())
            self.assertEqual(site['type'],'tnd:crowned_site')
            self.assertEqual(site['template'],'tnd:'+name)
            self.assertEqual(site['terrain_adaptation'],'none')
            expected='#minecraft:is_overworld' if name in ['outpost','portal_site'] else '#tnd:ancient_vestige'
            self.assertEqual(site['biomes'],expected)
        for name in ['nightslag','veined_nightslag']:
            self.assertTrue((DATA/f'loot_table/blocks/{name}.json').is_file())
            self.assertTrue((DATA/f'recipe/{name}.json').is_file())
        from character_art import MOBS
        for name in MOBS:
            raw=(ASSETS/f'textures/entity/{name}.png').read_bytes()
            self.assertEqual(struct.unpack('>II',raw[16:24]),(64,64))
        for name in ['umbra_helmet','umbra_chestplate','umbra_leggings','umbra_boots']:
            self.assertEqual(struct.unpack('>II',(ASSETS/f'textures/item/{name}.png').read_bytes()[16:24]),(16,16))
    def test_awakening_progression_and_equipment_assets(self):
        self.assertEqual(generator.SIZES['aureate_citadel'],(95,47,95))
        self.assertEqual(generator.SIZES['echo_conservatory'],(79,45,79))
        repairs=json.loads((DATA/'tags/item/umbra_repairs.json').read_text())
        self.assertEqual(repairs['values'],['tnd:umbra_alloy'])
        for ore in ['auralite_ore','noctilite_ore']:
            loot=json.loads((DATA/f'loot_table/blocks/{ore}.json').read_text())
            self.assertIn('minecraft:fortune',json.dumps(loot));self.assertIn('minecraft:silk_touch',json.dumps(loot))
        for name in ['ore_separator','echo_reaper']:
            self.assertEqual(len(json.loads((ASSETS/f'blockstates/{name}.json').read_text())['variants']),8)
        asset=json.loads((ASSETS/'equipment/umbra.json').read_text())
        for kind,layers in asset['layers'].items():
            for layer in layers:self.assertTrue((ASSETS/f"textures/entity/equipment/{kind}/{layer['texture'][4:]}.png").is_file())
        for path in (DATA/'advancement').rglob('*.json'):
            item=json.loads(path.read_text())
            if item.get('parent','').startswith('tnd:'):self.assertTrue((DATA/f"advancement/{item['parent'][4:]}.json").is_file(),path)
        for name in ['lithic_heart','glass_filament']:self.assertFalse((DATA/f'recipe/{name}.json').exists())

    def test_awakening_guide_and_structure_identity(self):
        for locale in ['fr_fr','en_us']:
            text=json.loads((ASSETS/f'lang/{locale}.json').read_text())
            for page in range(6):
                self.assertIn(f'guide.tnd.tab.{page}',text)
                for row in range(3):self.assertGreater(len(text[f'guide.tnd.page.{page}.{row}']),30)
        greenhouse=generator.build('echo_conservatory')
        self.assertGreater(sum(v[0]=='minecraft:light_blue_stained_glass' for (x,y,z),v in greenhouse.grid.items() if y in [16,25]),500)
        for name in ['aureate_citadel','echo_conservatory']:
            parser=Nbt(gzip.decompress((DATA/f'structure/{name}.nbt').read_bytes()));parser.take(1);parser.string();root=parser.read(10)
            desks=[b for b in root['blocks'] if root['palette'][b['state']]['Name']=='tnd:survey_terminal']
            self.assertEqual(len(desks),1);self.assertEqual(desks[0]['nbt']['SurveySite'],0)
            self.assertGreaterEqual(sum(root['palette'][b['state']]['Name']=='tnd:return_anchor' for b in root['blocks']),1)

    def test_city_content_and_progression(self):
        from city_resources import MOBS,BLOCKS,TOWERS
        city=json.loads((DATA/'worldgen/structure/neon_district.json').read_text())
        self.assertEqual(city['type'],'tnd:crowned_site');self.assertEqual(city['biomes'],'tnd:neon_city')
        placement=json.loads((DATA/'worldgen/structure_set/vestige_sites.json').read_text())['placement']
        radius=__import__('math').ceil(__import__('math').hypot(63.5,63.5))+39
        self.assertGreater((placement['separation']+1)*16,radius*2+1)
        self.assertNotIn('exclusion_zone',placement)
        self.assertFalse((DATA/'worldgen/structure_set/neon_district.json').exists())
        for name in ['portal_monitor','relay_coil']:
            recipe=json.loads((DATA/f'recipe/{name}.json').read_text())
            for item in recipe['key'].values():self.assertNotIn(item,['tnd:ancient_alloy','tnd:resonant_shard','tnd:tempered_alloy','tnd:umbra_alloy'])
        self.assertFalse((DATA/'recipe/nacre_memorial.json').exists());self.assertFalse((DATA/'loot_table/blocks/memory_terminal.json').exists())
        for name in MOBS:
            loot=json.loads((DATA/f'loot_table/entities/{name}.json').read_text())
            self.assertIn('minecraft:killed_by_player',str(loot))
            for suffix in ['','_charged']:self.assertTrue((ASSETS/f'textures/entity/{name}{suffix}.png').is_file())
        for locale in ['fr_fr','en_us']:
            lang=json.loads((ASSETS/f'lang/{locale}.json').read_text())
            for i in range(8):self.assertGreater(len(lang[f'codex.tnd.text.{i}']),350)
        building=generator.city_resources.build()
        entries=[dict(props)['entry'] for block,props,loot in building.grid.values() if block=='tnd:memory_terminal']
        self.assertEqual(sorted(entries),list('1234567'))
        for x0,z0,x1,z1,floors,purpose,color in TOWERS:
            for f in range(floors):
                grid=[v[0] for (x,y,z),v in building.grid.items() if x0<=x<=x1 and z0<=z<=z1 and f*8<y<(f+1)*8]
                self.assertGreater(len(set(grid)),8,(purpose,f))
        self.assertEqual(len(json.loads((ASSETS/'blockstates/portal_controller.json').read_text())['variants']),32)
        tiles=list((DATA/'structure/neon_district').glob('part_*.nbt'));self.assertEqual(len(tiles),16)
        rebuilt={}
        for path in tiles:
            tx,tz=map(int,path.stem.split('_')[1:]);parser=Nbt(gzip.decompress(path.read_bytes()));parser.take(1);parser.string();root=parser.read(10)
            for cell in root['blocks']:
                x,y,z=cell['pos'];state=root['palette'][cell['state']]
                rebuilt[x+tx*32,y,z+tz*32]=(state['Name'],tuple(sorted(state.get('Properties',{}).items())),cell.get('nbt',{}).get('LootTable'))
        self.assertEqual(rebuilt,building.grid,'Native generation tiles reconstruct every authored block and chest without gaps')


    def test_industrial_survival_chain(self):
        from industry_resources import BLOCKS,ITEMS,MACHINES
        for name in BLOCKS+ITEMS:
            if name not in ['iron_grit','copper_grit','gold_grit']:self.assertTrue((DATA/f'recipe/{name}.json').is_file(),name)
        for name in MACHINES:
            self.assertEqual(len(json.loads((ASSETS/f'blockstates/{name}.json').read_text())['variants']),8)
            for locale in ['fr_fr','en_us']:
                text=json.loads((ASSETS/f'lang/{locale}.json').read_text())
                for row in range(3):self.assertGreater(len(text[f'industry.tnd.help.{name}.{row}']),40)
        recipe=json.loads((DATA/'recipe/industrial_quarry.json').read_text())
        self.assertIn('minecraft:netherite_ingot',recipe['key'].values())
        self.assertIn('tnd:umbra_alloy',recipe['key'].values())
        for grit in ['iron_grit','copper_grit','gold_grit']:
            self.assertFalse((DATA/f'recipe/{grit}.json').exists(),'Grit is obtained only by processing raw material')
            self.assertEqual(json.loads((DATA/f'recipe/{grit}_smelting.json').read_text())['experience'],0)

    def test_json_and_namespaces(self):
        paths = list(RES.rglob('*.json'))
        self.assertGreater(len(paths), 100)
        for path in paths:
            with self.subTest(path=path.relative_to(RES)):
                json.loads(path.read_text(encoding='utf-8'))
                self.assertRegex(path.name, r'^[a-z0-9_.]+$')

    def test_all_registered_content_has_assets(self):
        for block in generator.BLOCKS:
            self.assertTrue((ASSETS / f'blockstates/{block}.json').is_file(), block)
        for name in generator.BLOCKS + generator.ITEMS:
            self.assertTrue((ASSETS / f'items/{name}.json').is_file(), name)
            self.assertTrue((ASSETS / f'models/item/{name}.json').is_file(), name)

    def test_model_references(self):
        for path in ASSETS.glob('models/**/*.json'):
            model = json.loads(path.read_text())
            if model.get('parent', '').startswith('tnd:'):
                self.assertTrue((ASSETS / ('models/' + model['parent'][4:] + '.json')).is_file(), path)
            for element in model.get('elements',[]):
                for face in element.get('faces',{}).values():
                    self.assertTrue(face['texture'].startswith('#'),(path,'element faces must reference declared texture slots'))
                    self.assertIn(face['texture'][1:],model.get('textures',{}),path)
            for texture in model.get('textures', {}).values():
                if texture.startswith('tnd:'):
                    self.assertTrue((ASSETS / ('textures/' + texture[4:] + '.png')).is_file(), path)
        for path in (ASSETS / 'blockstates').glob('*.json'):
            for variant in json.loads(path.read_text())['variants'].values():
                self.assertTrue((ASSETS / ('models/' + variant['model'][4:] + '.json')).is_file())

    def test_png_checksums_and_pixel_payload(self):
        for path in (ASSETS / 'textures').rglob('*.png'):
            data = path.read_bytes()
            self.assertEqual(data[:8], b'\x89PNG\r\n\x1a\n')
            index, compressed, dimensions = 8, bytearray(), None
            while index < len(data):
                size = struct.unpack('>I', data[index:index + 4])[0]
                kind, payload = data[index + 4:index + 8], data[index + 8:index + 8 + size]
                crc = struct.unpack('>I', data[index + 8 + size:index + 12 + size])[0]
                self.assertEqual(zlib.crc32(kind + payload), crc, str(path))
                if kind == b'IHDR': dimensions = struct.unpack('>IIBBBBB', payload)
                if kind == b'IDAT': compressed.extend(payload)
                index += 12 + size
            size=64 if path.relative_to(ASSETS/'textures').parts[0]=='entity' else 16
            self.assertEqual(dimensions, (size, size, 8, 6, 0, 0, 0))
            self.assertEqual(len(zlib.decompress(compressed)), size * (1 + size * 4))

    def test_nbt_structures(self):
        for path in (DATA / 'structure').rglob('*.nbt'):
            parser = Nbt(gzip.decompress(path.read_bytes()))
            self.assertEqual(parser.take(1), b'\x0a'); parser.string()
            root = parser.read(10)
            self.assertEqual(parser.index, len(parser.data))
            self.assertEqual(root['DataVersion'], 4903)
            self.assertEqual(len(root['size']), 3)
            seen = set()
            for block in root['blocks']:
                position = tuple(block['pos'])
                self.assertNotIn(position, seen)
                seen.add(position)
                self.assertTrue(all(0 <= v < dimension for v, dimension in zip(position, root['size'])))
                self.assertTrue(0 <= block['state'] < len(root['palette']))
            if path.stem == 'portal_site':
                self.assertTrue(any(root['palette'][b['state']]['Name'] == 'tnd:portal_controller' for b in root['blocks']))
            if path.stem == 'outpost':
                self.assertTrue(any(b.get('nbt', {}).get('LootTable') == 'tnd:chests/outpost' for b in root['blocks']))

    def test_buildings_have_connected_furnished_interiors(self):
        from collections import deque
        for name in generator.SIZES:
            building=generator.build(name); grid=building.grid; width,height,depth=building.size
            def empty(pos):
                block=grid.get(pos,('minecraft:air',))[0]
                return block=='minecraft:air' or block.endswith(('_door','_bars',':lantern'))
            def stand(pos):
                x,y,z=pos
                return (0<=x<width and 1<=y<height-1 and 0<=z<depth and empty(pos)
                    and empty((x,y+1,z)) and not empty((x,y-1,z)))
            start=(width//2,1,2);self.assertTrue(stand(start),name)
            seen={start};queue=deque([start])
            while queue:
                x,y,z=queue.popleft()
                for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
                    for dy in [0,1,-1,-2,-3]:
                        pos=(x+dx,y+dy,z+dz)
                        if pos not in seen and stand(pos):seen.add(pos);queue.append(pos)
            for x0,z0,x1,z1,h,floor in building.rooms:
                for x in range(x0+1,x1):
                    for z in range(z0+1,z1):
                        pos=(x,floor+1,z)
                        if stand(pos):self.assertIn(pos,seen,(name,'inaccessible interior',pos))
                        # An enclosed room must have its own roof, including above open galleries.
                        self.assertTrue(any(not empty((x,roof,z)) for roof in range(floor+h,height)),(name,'missing roof',x,z))
            materials={entry[0] for entry in grid.values()}
            self.assertGreaterEqual(len(materials),17,name)
            self.assertGreater(sum(1 for entry in grid.values() if entry[0].endswith('_lamp')),12,name)
        cache=json.loads((DATA/'loot_table/chests/outpost.json').read_text())
        self.assertNotIn('tnd:field_journal',json.dumps(cache))

    def test_vault_authorship_and_reward_chain(self):
        building=generator.build('resonance_vault')
        self.assertEqual(len(building.rooms),5)
        parser=Nbt(gzip.decompress((DATA/'structure/resonance_vault.nbt').read_bytes()));parser.take(1);parser.string();root=parser.read(10)
        cores=[b for b in root['blocks'] if root['palette'][b['state']]['Name']=='tnd:vault_core']
        self.assertEqual(len(cores),1);self.assertEqual(cores[0]['nbt']['TrialSite'],1)
        self.assertEqual(sum(v[0]=='tnd:vault_seal' for v in building.grid.values()),3)
        for name in ['vault_core','vault_seal']:
            self.assertEqual(len(json.loads((ASSETS/f'blockstates/{name}.json').read_text())['variants']),48)
        for locale in ['fr_fr','en_us']:
            self.assertIn('message.tnd.lore.vault',json.loads((ASSETS/f'lang/{locale}.json').read_text()))
        recipe=json.loads((DATA/'recipe/resonant_aegis.json').read_text())
        self.assertIn('tnd:resonance_heart',recipe['ingredients'])
        self.assertFalse((DATA/'recipe/resonance_heart.json').exists())
        self.assertEqual(json.loads((DATA/'worldgen/structure/resonance_vault.json').read_text())['biomes'],'#tnd:ancient_vestige')

    def test_atmosphere_and_biome_content(self):
        biomes={'silent_expanse','fractured_fields','hollow_shelves','glass_woodland','ashen_basin','obsidian_dunes'}
        stem=json.loads((DATA/'dimension/vestige.json').read_text())
        self.assertEqual({b['biome'][4:] for b in stem['generator']['biome_source']['biomes']},biomes|{'neon_city'})
        sounds=json.loads((ASSETS/'sounds.json').read_text());self.assertEqual(len(sounds),52)
        loops=set()
        for biome in biomes:
            data=json.loads((DATA/f'worldgen/biome/{biome}.json').read_text())
            loop=data['attributes']['minecraft:audio/ambient_sounds']['loop'][4:];loops.add(loop);self.assertIn(loop,sounds)
            self.assertIn('tnd:vestige_landscape',sum(data['features'],[]))
        self.assertEqual(len(loops),6)
        report=json.loads((ASSETS/'sound_design_report.json').read_text())
        self.assertEqual(set(report),set(sounds))
        for name,event in sounds.items():
            for sound in event['sounds']:
                self.assertTrue((ASSETS/('sounds/'+sound['name'][4:]+'.ogg')).read_bytes().startswith(b'OggS'))
                self.assertIn(event['subtitle'],json.loads((ASSETS/'lang/fr_fr.json').read_text()))
            self.assertGreater(report[name]['rms'],.02);self.assertLess(report[name]['peak'],.65)
            if report[name]['loop']:self.assertLess(report[name]['splice_delta'],.03)

    def test_recipes_only_use_known_local_items(self):
        registered = set(generator.BLOCKS + generator.ITEMS)
        for path in (DATA / 'recipe').glob('*.json'):
            recipe = json.loads(path.read_text())
            if recipe['result']['id'].startswith('tnd:'):self.assertIn(recipe['result']['id'][4:], registered)
            for ingredient in recipe.get('ingredients',[recipe.get('ingredient','')]):
                if ingredient.startswith('tnd:'): self.assertIn(ingredient[4:], registered)

    def test_localized_static_messages(self):
        fr = json.loads((ASSETS / 'lang/fr_fr.json').read_text())
        en = json.loads((ASSETS / 'lang/en_us.json').read_text())
        self.assertEqual(set(fr), set(en))
        for path in (ROOT / 'src/main/java').rglob('*.java'):
            for key in re.findall(r'Component.translatable\("([a-zA-Z0-9_.]+)"\)', path.read_text()):
                if key.startswith(('gui.tnd.', 'message.tnd.', 'block.tnd.', 'itemGroup.tnd', 'direction.tnd.')):
                    self.assertIn(key, fr, str(path))

    def test_all_playable_trees_and_console_states(self):
        ids=['mining','logging','farming','fishing','hunting','breeding','melee','ranged','defense',
             'crafting','building','exploration','alchemy','engineering','ancient_tech']
        for language in ['fr_fr','en_us']:
            text=json.loads((ASSETS/f'lang/{language}.json').read_text())
            for mastery in ids:
                self.assertIn('activity.tnd.'+mastery,text)
                for talent in ['efficiency','yield','insight','precision','synergy']:
                    description=text[f'talent.tnd.{mastery}.{talent}.description']
                    self.assertGreater(len(description),15)
                    self.assertNotIn('ultérieurement',description)
            for item in ['relic','stabilizer','efficiency']:self.assertIn('tooltip.tnd.'+item,text)
            for mode in ['iron','copper','gold','redstone','diamond']:self.assertIn('mode.tnd.'+mode,text)
        variants=json.loads((ASSETS/'blockstates/ancient_console.json').read_text())['variants']
        self.assertEqual(set(variants),{'lit=false','lit=true'})
        self.assertNotEqual(variants['lit=false'],variants['lit=true'])
        knowledge=json.loads((DATA/'advancement/recipes/ancient_knowledge.json').read_text())
        for recipe in knowledge['rewards']['recipes']:self.assertTrue((DATA/f'recipe/{recipe[4:]}.json').is_file())
        stabilizer=json.loads((DATA/'recipe/stabilizer_module.json').read_text())
        self.assertEqual(stabilizer['ingredients'].count('tnd:relic_fragment'),3)
        for name in generator.SIZES:
            self.assertTrue(any(v[0]=='tnd:ancient_console' for v in generator.build(name).grid.values()),name)

    def test_encounter_assets_and_spawn_scope(self):
        for name in ['shardling','shardling_bonded','rift_sentinel','rift_sentinel_charged']:
            self.assertTrue((ASSETS/f'textures/entity/{name}.png').is_file())
        for biome in ['silent_expanse','fractured_fields','hollow_shelves']:
            data=json.loads((DATA/f'worldgen/biome/{biome}.json').read_text())
            self.assertEqual({e['type'] for e in data['spawners']['monster']},{'tnd:rift_sentinel','tnd:lithoguard'}|({'tnd:glass_stalker'} if biome in ['fractured_fields','hollow_shelves'] else set()))
            self.assertEqual([e['type'] for e in data['spawners']['creature']],['tnd:shardling'])
            self.assertIn('tnd:echo_bloom',sum(data['features'],[]))
            self.assertLessEqual(data['creature_spawn_probability'],.02)
        variants=json.loads((ASSETS/'blockstates/echo_bloom.json').read_text())['variants']
        self.assertEqual(set(variants),{f'charge={i}' for i in range(4)})
        for entry in variants.values():self.assertTrue((ASSETS/f"models/{entry['model'][4:]}.json").is_file())
        for path in (DATA/'neoforge/biome_modifier').glob('*.json'):
            self.assertNotIn('tnd:rift_sentinel',path.read_text())
            self.assertNotIn('tnd:shardling',path.read_text())
        for locale in ['fr_fr','en_us']:
            language=json.loads((ASSETS/f'lang/{locale}.json').read_text())
            for entity in ['shardling','rift_sentinel']:self.assertIn('entity.tnd.'+entity,language)
            for item in ['echo_dust','echo_lens']:self.assertIn('tooltip.tnd.'+item,language)

    def test_expedition_processing_and_progression_assets(self):
        for state in ['', '_open', '_enraged']:
            self.assertTrue((ASSETS/f'textures/entity/fractured_custodian{state}.png').is_file())
        self.assertEqual(len(json.loads((ASSETS/'blockstates/resonance_forge.json').read_text())['variants']),8)
        forge=json.loads((DATA/'recipe/resonance_forge.json').read_text())
        self.assertEqual(forge['key']['M'],'tnd:inertial_matrix')
        self.assertFalse((DATA/'recipe/inertial_matrix.json').exists())
        self.assertEqual(json.loads((DATA/'loot_table/entities/fractured_custodian.json').read_text())['pools'],[])
        for name in ['vestige','sanctuary','custodian','forge','tempered']:
            advancement=json.loads((DATA/f'advancement/expedition/{name}.json').read_text())
            self.assertIn('done',advancement['criteria'])
            self.assertNotIn('experience',advancement.get('rewards',{}))
        for name in ['resonant_pickaxe','resonant_blade']:
            self.assertEqual(json.loads((ASSETS/f'models/item/{name}.json').read_text())['parent'],'minecraft:item/handheld')
        self.assertEqual(json.loads((DATA/'tags/item/resonant_tool_materials.json').read_text())['values'],['tnd:tempered_alloy'])

    def test_fieldwork_structure_and_recall_resources(self):
        observatory=generator.build('observatory')
        self.assertEqual(len(observatory.rooms),5)
        self.assertTrue(any(v[0]=='tnd:return_anchor' for v in observatory.grid.values()))
        parser=Nbt(gzip.decompress((DATA/'structure/observatory.nbt').read_bytes()));parser.take(1);parser.string();root=parser.read(10)
        desks=[b for b in root['blocks'] if root['palette'][b['state']]['Name']=='tnd:survey_terminal']
        self.assertEqual(len(desks),1);self.assertEqual(desks[0]['nbt']['SurveySite'],1)
        self.assertEqual(len(json.loads((ASSETS/'blockstates/survey_terminal.json').read_text())['variants']),4)
        self.assertFalse((DATA/'recipe/cartographer_trophy.json').exists())
        self.assertEqual(json.loads((DATA/'worldgen/structure/observatory.json').read_text())['biomes'],'#tnd:ancient_vestige')
        for name in ['return_anchor','recall_prism']:
            recipe=json.loads((DATA/f'recipe/{name}.json').read_text());self.assertIn('tnd:tempered_alloy',recipe['key'].values())
        for lang in ['fr_fr','en_us']:
            text=json.loads((ASSETS/f'lang/{lang}.json').read_text())
            for mission in ['survey','atlas','restoration','night_harvest','sentinels','observatory','gear','cartographer']:
                self.assertIn('expedition.tnd.'+mission+'.description',text)
                self.assertLessEqual(len(text['expedition.tnd.'+mission+'.short']),35)

    def test_clock_portal_spacing_and_runtime_tests(self):
        timeline = json.loads((DATA / 'timeline/vestige_day.json').read_text())
        self.assertEqual(timeline['period_ticks'], 48000)
        self.assertEqual(timeline['clock'], 'tnd:vestige')
        portal = json.loads((DATA / 'worldgen/structure_set/portal_site.json').read_text())['placement']
        self.assertGreaterEqual(portal['separation'] * 16, 10000)
        self.assertLessEqual(portal['spacing'] * 16, 15000)
        sites=json.loads((DATA/'worldgen/structure_set/vestige_sites.json').read_text())
        self.assertEqual({e['structure'] for e in sites['structures']},{'tnd:relay','tnd:archive','tnd:foundry','tnd:resonance_vault','tnd:observatory','tnd:aureate_citadel','tnd:echo_conservatory','tnd:neon_district'})
        from math import ceil,hypot
        largest_radius=max(ceil(hypot(size[0]/2,size[2]/2))+39 for size in generator.SIZES.values())
        self.assertGreater((sites['placement']['separation']+1)*16,2*largest_radius+1)
        self.assertGreaterEqual(json.loads((DATA/'worldgen/structure_set/outpost.json').read_text())['placement']['exclusion_zone']['chunk_count'],12)
        for name in ['relay','archive','foundry','observatory']:self.assertFalse((DATA/f'worldgen/structure_set/{name}.json').exists())
        tests = list((DATA / 'test_instance').glob('*.json'))
        self.assertEqual({path.stem for path in tests}, {'dimension', 'extractor', 'portal_resistance',
                         'portal_terrain', 'portal_arrival', 'portal_no_space', 'portal_round_trip', 'habitat_ores', 'buildings', 'outpost_terrain', 'mastery_actions', 'console', 'extraction_modes', 'talent_menus', 'talent_effects', 'companion_bond','companion_follow','companion_travel','companion_cold_arrival','sentinel_dodge','sentinel_hit','sentinel_interrupt','echo_lens','echo_deposits','vault_progress','vault_recovery','vault_safety','vault_rotations','resonant_aegis','biome_palette','vestige_landscapes','mastery_celebration','custodian_phase','custodian_dodge','custodian_hit','custodian_crown','custodian_stagger','custodian_progress','custodian_recovery','forge_processing','forge_recovery','forge_hoppers','field_objectives','field_actions','field_completion','recall_success','recall_movement','recall_damage','recall_blocked','recall_replaced','recall_guards','observatory','survey_authority'}|set(generator.awakening_resources.TESTS)|set(generator.industry_resources.TESTS)|set(generator.city_resources.TESTS))
        for path in tests: self.assertTrue(json.loads(path.read_text())['required'])
        self.assertTrue((ASSETS / 'sounds/ambient/low_signal.ogg').read_bytes().startswith(b'OggS'))


if __name__ == '__main__': unittest.main(verbosity=2)
