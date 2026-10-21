"""0.9 authored citadel and conservatory, with accessible rooms and native stair galleries."""
def build_citadel(b):
    b.plaza(32,1,62,93)
    b.room(33,3,61,25,h=15,kind='citadel_bricks')
    b.room(29,25,65,67,h=27,kind='citadel_bricks')
    b.room(8,7,33,24,h=14,kind='carved_citadel_bricks').room(61,7,86,24,h=14,kind='carved_citadel_bricks')
    b.room(4,26,29,66,h=16,kind='citadel_bricks').room(65,26,90,66,h=16,kind='citadel_bricks')
    b.room(5,67,30,91,h=16,kind='carved_citadel_bricks').room(64,67,89,91,h=16,kind='carved_citadel_bricks')
    b.room(30,67,64,91,h=18,kind='umbra_tiles')
    b.entrance(46,3)
    for x in [33,61]:b.passage(x-1,14,x+1,18)
    b.passage(45,24,49,26)
    for x in [29,65]:b.passage(x-1,44,x+1,48)
    for x in [16,76]:b.passage(x-2,65,x+2,68)
    for x in [30,64]:b.passage(x-1,77,x+1,81)
    b.passage(45,66,49,68)
    wide_gallery(b,30,27,64,65,10)
    # A procession of paired columns, gilded capitals and suspended lamps.
    for x in [35,59]:
        for z in [32,40,48,56]:
            b.box(x,1,z,x,15,z,'tnd:gilded_pillar')
            b.box(x-1,15,z-1,x+1,15,z+1,'tnd:gilded_trim')
            b.put(x,16,z,'tnd:amber_lamp')
    for x in range(31,64):
        for z in range(27,65):
            if (x-47)**2+(z-46)**2<54:b.put(x,0,z,'tnd:crystal_mosaic' if (x+z)%3 else 'tnd:umbra_tiles')
    # Sunken-looking central reliquary is decorative, without holes or forced combat.
    for y in [6,9,12,15]:b.put(47,y,46,'tnd:auralite_block' if y%2 else 'tnd:runic_lamp')
    for x in [43,51]:
        for z in [42,50]:b.put(x,7,z,'tnd:latticed_alloy');b.put(x,8,z,'tnd:runic_lamp')
    b.put(47,1,40,'tnd:harmonic_chime');b.put(47,1,54,'tnd:survey_terminal',{'facing':'north'})
    b.put(31,11,35,'tnd:return_anchor');b.put(63,11,59,'tnd:star_chart')
    # Library and archive: benches, crossed stacks, study tables and readable charts.
    for x in [7,25]:
        for z in [31,40,49,58]:b.shelf(x,z,4);b.put(x+1 if x==7 else x-1,4,z,'tnd:amber_lamp')
    for z in [35,48]:b.table(15,z,7)
    b.chest(25,61,'citadel');b.put(8,3,45,'tnd:star_chart')
    # Barracks and kitchen, including complete upper/lower bunks.
    for z in [31,44,57]:b.bunks(69,z)
    b.table(81,34,14);b.put(86,1,31,'minecraft:smoker');b.put(86,1,33,'minecraft:cauldron');b.chest(86,61,'salvage')
    # Gate armories and two rear workshops.
    for x in [12,66]:
        b.workshop(x,11);b.shelf(x+16,11,7);b.chest(x+17,20,'citadel')
    for x in [9,68]:
        b.workshop(x,72);b.table(x+9,73,10);b.shelf(x+15,72,10)
        for z in [73,78,84]:b.put(x+3,3,z,'tnd:etched_umbra');b.put(x+3,4,z,'tnd:violet_lamp')
    b.chest(39,86,'citadel');b.chest(55,86,'citadel')
    b.put(47,1,84,'tnd:return_anchor');b.put(47,2,88,'tnd:lithic_trophy')
    for x in [35,59]:b.shelf(x,72,12)
    for x,z in [(7,8),(87,8),(5,65),(89,65)]:b.spire(x,z,height=38)
    # Layered facade inlays retain every doorway and window.
    for x in [37,41,53,57]:
        b.box(x,2,2,x,11,2,'tnd:gilded_pillar');b.put(x,12,2,'tnd:amber_lamp')
    for x in [30,64]:
        for z in [31,38,53,60]:b.put(x,3,z,'tnd:carved_citadel_bricks');b.put(x,4,z,'tnd:runic_lamp')
    return b

def build_conservatory(b):
    b.plaza(24,1,54,76)
    b.room(25,3,53,20,h=12,kind='conservatory_tiles')
    b.room(23,20,55,58,h=24,kind='conservatory_tiles')
    b.room(4,22,23,57,h=15,kind='verdigris_panel').room(55,22,74,57,h=15,kind='verdigris_panel')
    b.room(24,58,54,74,h=15,kind='umbra_tiles')
    b.entrance(38,3);b.passage(37,19,41,21)
    for x in [23,55]:b.passage(x-1,37,x+1,41)
    b.passage(37,57,41,59);wide_gallery(b,24,22,54,56,9)
    # Glass roofs with repeated copper ribs. All are closed even in the central ridge.
    for x0,z0,x1,z1,h,_ in b.rooms[1:4]:
        pitch=min((x1-x0)//2,12,b.size[1]-h-3)
        for x in range(x0+1,x1):
            roof=h+1+min(x-x0,x1-x,pitch)
            for z in range(z0+1,z1):
                if z%5!=0 and x not in [x0+2,x1-2]:
                    b.put(x,roof,z,'minecraft:light_blue_stained_glass')
                    b.put(x,h+1,z,'minecraft:light_blue_stained_glass')
    # Raised mineral beds leave a generous two-block walking route around each aisle.
    for wing in [0,1]:
        x0=8 if wing==0 else 59
        for z in [26,32,45,51]:
            for x in range(x0,x0+11):
                b.put(x,1,z,'tnd:verdigris_panel')
                if x%2==0:b.put(x,2,z,'tnd:echo_bloom',{'charge':str((x+z)%4)})
            b.put(x0-1,2,z,'tnd:violet_lamp')
        b.chest(x0,54,'conservatory')
    # A central glass organ, four towers, study alcoves and native machinery fittings.
    for x,z in [(32,32),(46,32),(32,47),(46,47)]:
        b.box(x,1,z,x,15,z,'tnd:gilded_pillar');b.put(x,16,z,'tnd:runic_lamp')
    for x in range(35,44):
        for z in range(36,43):b.put(x,0,z,'tnd:crystal_mosaic')
    for dx,h in [(-3,7),(-1,11),(1,13),(3,9)]:
        b.box(39+dx,5,39,39+dx,h,39,'tnd:pale_crystal');b.put(39+dx,h+1,39,'tnd:violet_lamp')
    b.put(39,1,33,'tnd:harmonic_chime');b.put(39,1,48,'tnd:survey_terminal',{'facing':'north'})
    b.put(25,10,30,'tnd:return_anchor');b.put(39,1,69,'tnd:return_anchor')
    b.workshop(28,7);b.table(48,8,8);b.chest(49,17,'conservatory')
    for x in [28,50]:b.shelf(x,62,9)
    b.table(36,63,7);b.chest(46,70,'conservatory');b.put(27,3,66,'tnd:star_chart')
    for x,z in [(5,23),(73,23),(24,59),(54,59)]:b.spire(x,z,height=31)
    return b


def refine_legacy(b):
    # Palette/detail pass only: no collision, storage, trial, portal or access geometry changes.
    for (x,y,z),(block,props,loot) in list(b.grid.items()):
        new=block
        if block=='tnd:chiseled_ashstone':new='tnd:carved_citadel_bricks'
        elif block=='tnd:ancient_pillar' and y%7==0:new='tnd:gilded_trim'
        elif block=='tnd:ashstone_bricks' and y>0 and (x*3+z*5+y)%19==0:new='tnd:cracked_citadel_bricks'
        elif block=='tnd:ashstone_tiles' and y==0 and (x+z)%11==0:new='tnd:crystal_mosaic'
        elif block=='tnd:resonant_lamp' and (x+z+y)%5==0:new='tnd:violet_lamp' if b.name=='resonance_vault' else 'tnd:amber_lamp'
        if new!=block:b.grid[x,y,z]=(new,props,loot)
    return b


def wide_gallery(b,x0,z0,x1,z1,y):
    b.mezzanine(x0,z0,x1,z1,y=y)
    # Two clear walking lanes let ordinary mobs and players pass an anchor or buttress.
    for x,z in [(x,z) for x in [x0+2,x1-2] for z in range(z0+2,z1-2)]+[(x,z1-2) for x in range(x0+3,x1-2)]:
        if b.grid.get((x,y+1,z),('minecraft:air',))[0]=='minecraft:iron_bars':b.put(x,y+1,z,'minecraft:air')
    for z in range(z0,z1+1):
        for x in [x0+3,x1-3]:b.put(x,y,z,'tnd:polished_ashstone')
    for x in range(x0,x1+1):b.put(x,y,z1-3,'tnd:polished_ashstone')
    for z in range(z0+2,z1-3):
        for x in [x0+3,x1-3]:b.put(x,y+1,z,'minecraft:iron_bars')
    for x in range(x0+y+2,x1-3):b.put(x,y+1,z1-3,'minecraft:iron_bars')
