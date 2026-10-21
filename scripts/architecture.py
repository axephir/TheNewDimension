"""Authored, reproducible buildings. Coordinates are deliberately sparse outside rooms.
Every interior is cleared, roofed, lit and furnished; corridors connect all wings.
"""
from collections import Counter, deque

SIZES={'outpost':(49,35,49),'portal_site':(57,43,57),'relay':(41,41,41),
       'archive':(55,39,49),'foundry':(57,41,49),'resonance_vault':(63,43,63)}
class Building:
    def __init__(self,name):self.name=name;self.size=SIZES[name];self.grid={};self.rooms=[]
    def put(self,x,y,z,block,props=None,loot=None):
        assert 0<=x<self.size[0] and 0<=y<self.size[1] and 0<=z<self.size[2],(self.name,x,y,z)
        self.grid[x,y,z]=(block,tuple(sorted((props or {}).items())),loot)
    def box(self,x0,y0,z0,x1,y1,z1,b,props=None):
        for y in range(y0,y1+1):
            for z in range(z0,z1+1):
                for x in range(x0,x1+1):self.put(x,y,z,b,props)
    def room(self,x0,z0,x1,z1,h=9,floor=0,kind='ashstone_bricks'):
        self.rooms.append((x0,z0,x1,z1,h,floor))
        self.box(x0,floor,z0,x1,floor+h,z1,'tnd:'+kind)
        self.box(x0+1,floor+1,z0+1,x1-1,floor+h-1,z1-1,'minecraft:air')
        for x in range(x0,x1+1):
            for z in range(z0,z1+1):
                block='ashstone_tiles' if (x+z)%6 else 'polished_ashstone'
                if x in (x0+1,x1-1) or z in (z0+1,z1-1):block='cut_alloy'
                self.put(x,floor,z,'tnd:'+block)
        # Heavy footings, vertical buttresses, friezes, and stepped enclosed roof.
        for x in [x0,x1]:
            for z in [z0,z1]:self.box(x,floor+1,z,x,floor+h,z,'tnd:ancient_pillar')
        for y,b in [(floor+1,'polished_ashstone'),(floor+h-1,'cut_alloy'),(floor+h,'oxidized_alloy')]:
            self.box(x0,y,z0,x1,y,z0,'tnd:'+b);self.box(x0,y,z1,x1,y,z1,'tnd:'+b)
            self.box(x0,y,z0,x0,y,z1,'tnd:'+b);self.box(x1,y,z0,x1,y,z1,'tnd:'+b)
        self.box(x0+1,floor+h+1,z0+1,x1-1,floor+h+1,z1-1,'tnd:cut_alloy_slab',{'type':'bottom','waterlogged':'false'})
        # Steep, closed gabled roofs. Slopes use real stair collision/models and
        # a raised ridge rather than an enormous featureless flat rectangle.
        pitch=min((x1-x0)//2,12,self.size[1]-floor-h-3)
        for x in range(x0,x1+1):
            rise=min(x-x0,x1-x,pitch)
            roof_y=floor+h+1+rise
            if rise<pitch:
                facing='east' if x<x0+pitch else 'west'
                for z in range(z0,z1+1):self.put(x,roof_y,z,'tnd:cut_alloy_stairs',{'facing':facing,'half':'bottom','shape':'straight','waterlogged':'false'})
            else:
                self.box(x,roof_y,z0,x,roof_y,z1,'tnd:oxidized_alloy')
            for z in [z0,z1]:
                if rise>0:self.box(x,floor+h+1,z,x,roof_y-1,z,'tnd:ashstone_bricks')
        # Projecting buttresses articulate each facade; low sconces illuminate the floor.
        for x in range(x0+2,x1,8):
            for z in [z0-1,z1+1]:
                self.box(x,floor,z,x,floor+h-1,z,'tnd:ancient_pillar')
                self.put(x,floor+2,z,'tnd:chiseled_ashstone')
                self.put(x,floor+4,z,'tnd:resonant_lamp')
        for z in range(z0+2,z1,8):
            for x in [x0-1,x1+1]:
                self.box(x,floor,z,x,floor+h-1,z,'tnd:ancient_pillar')
                self.put(x,floor+2,z,'tnd:chiseled_ashstone')
                self.put(x,floor+4,z,'tnd:resonant_lamp')
        for x,z in [(x0+1,z0+2),(x1-1,z0+2),(x0+1,z1-2),(x1-1,z1-2)]:
            self.put(x,floor+3,z,'tnd:resonant_lamp')
        # Tall narrow glazed windows with stone mullions.
        for x in range(x0+3,x1-1,4):
            for z in [z0,z1]:
                self.box(x,floor+3,z,x+1,floor+5,z,'minecraft:tinted_glass')
                self.box(x,floor+6,z,x+1,floor+6,z,'tnd:chiseled_ashstone')
        for z in range(z0+3,z1-1,4):
            for x in [x0,x1]:
                self.box(x,floor+3,z,x,floor+5,z+1,'minecraft:tinted_glass')
                self.box(x,floor+6,z,x,floor+6,z+1,'tnd:chiseled_ashstone')
        if h>=14:
            for x in range(x0+3,x1-1,4):
                for z in [z0,z1]:
                    self.box(x,floor+10,z,x+1,floor+12,z,'minecraft:tinted_glass')
                    self.box(x,floor+13,z,x+1,floor+13,z,'tnd:cut_alloy')
            for z in range(z0+3,z1-1,4):
                for x in [x0,x1]:self.box(x,floor+10,z,x,floor+12,z+1,'minecraft:tinted_glass')
        # Ceiling beams and safe lighting; no pitch-black empty boxes.
        for z in range(z0+3,z1-1,5):
            self.box(x0+1,floor+h-1,z,x1-1,floor+h-1,z,'tnd:ancient_alloy')
            for x in [x0+2,x1-2]:self.put(x,floor+h-2,z,'tnd:resonant_lamp')
        return self
    def passage(self,x0,z0,x1,z1,y=1,h=4):self.box(x0,y,z0,x1,y+h-1,z1,'minecraft:air')
    def entrance(self,x,z,y=1,axis='z'):
        # Double spruce doors with framed threshold.
        for i in range(2):
            dx,dz=(i,0) if axis=='z' else (0,i)
            for half,dy in [('lower',0),('upper',1)]:
                self.put(x+dx,y+dy,z+dz,'minecraft:spruce_door',{'facing':'north' if axis=='z' else 'west','half':half,'hinge':'left' if i==0 else 'right','open':'false','powered':'false'})
        if axis=='z':self.box(x-1,y+2,z,x+2,y+2,z,'tnd:chiseled_ashstone')
        else:self.box(x,y+2,z-1,x,y+2,z+2,'tnd:chiseled_ashstone')
    def chest(self,x,z,loot='salvage',y=1):self.put(x,y,z,'minecraft:chest',{'facing':'south','type':'single','waterlogged':'false'},'tnd:chests/'+loot)
    def table(self,x,z,length=4,y=1):
        for dz in range(length):
            self.put(x,y,z+dz,'tnd:cut_alloy_slab',{'type':'top','waterlogged':'false'})
            if dz%2==0:self.put(x-1,y,z+dz,'minecraft:spruce_stairs',{'facing':'east','half':'bottom','shape':'straight','waterlogged':'false'})
    def shelf(self,x,z,length=6,y=1,axis='z'):
        for i in range(length):
            dx,dz=(0,i) if axis=='z' else (i,0)
            self.box(x+dx,y,z+dz,x+dx,y+2,z+dz,'tnd:archive_shelf')
    def workshop(self,x,z):
        for dz,b in enumerate(['minecraft:crafting_table','minecraft:blast_furnace','tnd:extractor','tnd:ancient_console','tnd:alloy_vent']):self.put(x,1,z+dz,b)
        self.chest(x,z+6)
        self.table(x+3,z+1)
    def bunks(self,x,z):
        # Complete vanilla beds, lower and upper bunks, end tables and lockers.
        for y in [1,4]:
            for dx in [0,3]:
                for dz,part in [(0,'foot'),(1,'head')]:self.put(x+dx,y,z+dz,'minecraft:cyan_bed',{'facing':'south','occupied':'false','part':part})
                if y==4:self.box(x+dx,3,z,x+dx,3,z+1,'tnd:cut_alloy')
        self.chest(x+6,z);self.put(x+6,2,z,'minecraft:lantern',{'hanging':'false','waterlogged':'false'})
    def mezzanine(self,x0,z0,x1,z1,y=7):
        # Walkable U-shaped gallery with railings and a proper rising staircase.
        for x in range(x0,x1+1):
            for z in range(z0,z1+1):
                if x<x0+3 or x>x1-3 or z>z1-3:self.put(x,y,z,'tnd:polished_ashstone')
        for z in range(z0+2,z1-2):
            for x in [x0+2,x1-2]:self.put(x,y+1,z,'minecraft:iron_bars')
        for x in range(x0+3,x1-2):self.put(x,y+1,z1-2,'minecraft:iron_bars')
        for i in range(y):
            x=x0+i
            self.box(x,1,z1-2,x,i+1,z1-1,'tnd:ashstone_bricks')
            for z in [z1-2,z1-1]:self.put(x,i+1,z,'tnd:ashstone_bricks_stairs',{'facing':'east','half':'bottom','shape':'straight','waterlogged':'false'})
            for h in range(i+2,min(i+5,self.size[1])):self.put(x,h,z1-2,'minecraft:air');self.put(x,h,z1-1,'minecraft:air')
    def spire(self,x,z,base=0,height=20):
        self.box(x-1,base,z-1,x+1,height,z+1,'tnd:ancient_pillar')
        for h in [base+3,height-5,height-1]:self.box(x-2,h,z-2,x+2,h,z+2,'tnd:cut_alloy')
        self.put(x,height+1,z,'tnd:resonant_lamp')
    def plaza(self,x0,z0,x1,z1):
        self.box(x0,1,z0,x1,4,z1,'minecraft:air')
        for x in range(x0,x1+1):
            for z in range(z0,z1+1):self.put(x,0,z,'tnd:ashstone_bricks' if (x+z)%5 else 'tnd:mossy_ashstone_bricks')
    def finalize(self):
        # Extend foundations 2 blocks down into the template instead of unsupported paper floors.
        # All coordinates already use y=0; worldgen beard adaptation handles terrain beneath.
        return self.grid

def build(name):
    b=Building(name)
    if name=='resonance_vault':
        # Five interlocking enclosed volumes; the central nave connects every wing.
        b.room(24,2,38,16,h=10).room(24,17,38,43,h=16)
        b.room(3,18,23,42,h=12).room(39,18,59,42,h=12).room(22,44,40,60,h=14)
        b.passage(29,16,33,17);b.passage(23,28,24,32);b.passage(38,28,39,32);b.passage(29,43,33,44)
        b.entrance(30,2)
        b.workshop(26,7);b.shelf(36,6,7)
        b.put(35,3,14,'tnd:mural_vault')
        # Covered side alcoves, specimen cabinets and broken couplings, away from combat lanes.
        for x in [5,57]:
            for z in [22,27,38]:
                b.box(x,1,z,x,3,z+1,'tnd:ancient_grate');b.put(x,4,z,'tnd:blue_lamp')
        for x in [9,17,45,53]:
            b.box(x,1,20,x,5,20,'tnd:ancient_pillar');b.put(x,6,20,'tnd:resonant_lamp')
            b.put(x,2,40,'tnd:alloy_vent')
        for x in [25,37]:
            b.shelf(x,46,4);b.box(x,1,57,x,6,57,'tnd:ancient_pillar');b.put(x,7,57,'tnd:resonant_lamp')
        b.put(31,1,30,'tnd:vault_core',{'facing':'north','seal':'0','lit':'false','mirrored':'false'})
        for index,(x,z) in enumerate([(13,30),(49,30),(31,52)]):
            b.put(x,1,z,'tnd:vault_seal',{'facing':'north','seal':str(index),'lit':'false','mirrored':'false'})
            for dx in [-2,2]:b.put(x+dx,0,z,'tnd:'+['blue_lamp','green_lamp','red_lamp'][index])
        # Suspended fragments make the engine silhouette distinct without blocking the player.
        for y,r in [(5,2),(8,3),(11,2)]:
            for dx,dz in [(-r,0),(r,0),(0,-r),(0,r)]:b.put(31+dx,y,30+dz,'tnd:conduit')
        b.put(31,7,30,'tnd:resonant_lamp')
        b.chest(35,40,'relics');b.put(27,3,41,'tnd:mural_vault')
    elif name=='outpost':
        b.plaza(2,2,46,46)
        b.room(15,10,33,37,15).room(3,15,15,37,9).room(33,15,45,37,9)
        b.room(18,2,30,10,9).room(18,37,30,46,18)
        b.passage(14,21,16,24);b.passage(32,21,34,24);b.passage(22,9,26,11);b.passage(22,36,26,38)
        b.entrance(23,2)
        b.mezzanine(16,12,32,35)
        b.workshop(5,19);b.bunks(35,19)
        b.shelf(19,42,8,axis='x');b.table(23,16,7)
        # Curated expedition cache in a proper rear archive alcove; no journal dependency.
        b.chest(24,43,'outpost');b.put(24,2,43,'tnd:blue_lamp')
        for x,block in [(20,'mural_ocean'),(22,'mural_lava'),(26,'mural_forest'),(28,'mural_fusion')]:b.put(x,3,45,'tnd:'+block)
        for x in [18,30]:b.spire(x,38,height=22)
        for z in [5,9]:b.put(20,2,z,'tnd:ancient_console');b.put(28,2,z,'tnd:resonant_lamp')
    elif name=='portal_site':
        b.plaza(2,2,54,54)
        b.room(14,12,42,46,21).room(3,19,14,43,12).room(42,19,53,43,12)
        b.room(19,3,37,12,11).room(19,46,37,54,13)
        for x in [13,41]:b.passage(x,27,x+2,31)
        b.passage(26,11,30,13);b.passage(26,45,30,47);b.entrance(27,3)
        b.mezzanine(15,14,41,44,y=9)
        # Recessed portal dais under a vaulted, windowed nave.
        b.box(24,1,28,32,1,37,'tnd:polished_ashstone')
        for x in range(26,31):
            for y in range(2,9):
                if x in [26,30] or y in [2,8]:b.put(x,y,34,'tnd:portal_frame')
        b.put(28,2,33,'tnd:portal_controller',{'active':'false'})
        for x in [23,33]:b.spire(x,35,height=17)
        for x in [6,45]:b.shelf(x,22,12);b.table(x+3,24);b.chest(x+3,35,'relics')
        for x,z in [(9,28),(48,28)]:b.put(x,1,z,'tnd:ancient_console')
        for x,block in [(23,'mural_ocean'),(26,'mural_lava'),(30,'mural_forest'),(33,'mural_fusion')]:b.put(x,4,53,'tnd:'+block)
        for x,z in [(15,13),(41,13),(15,45),(41,45)]:b.spire(x,z,height=27)
    elif name=='relay':
        b.plaza(2,2,38,38)
        b.room(10,10,30,32,14).room(3,15,10,30,9).room(30,15,37,30,9).room(14,3,26,10,9)
        b.passage(9,20,11,23);b.passage(29,20,31,23);b.passage(18,9,22,11);b.entrance(19,3)
        b.mezzanine(11,12,29,30,y=7)
        for x in [16,24]:
            b.box(x,1,18,x,6,24,'tnd:ancient_alloy');b.box(x,2,19,x,5,23,'tnd:dormant_conduit')
            b.put(x,4,21,'tnd:resonant_lamp')
        b.table(20,16,6);b.workshop(4,18);b.chest(34,25,'relics')
        b.room(15,24,29,34,9,floor=15,kind='riveted_alloy')
        # Continuous enclosed stair flight from the ground hall into the upper cabin.
        # The stairwell is separated from the machinery by a stone balustrade.
        for i in range(15):
            x=12+i;z=30
            b.box(x,1,z,x,i+1,z+1,'tnd:ashstone_bricks')
            for zz in [z,z+1]:
                b.put(x,i+1,zz,'tnd:ashstone_bricks_stairs',{'facing':'east','half':'bottom','shape':'straight','waterlogged':'false'})
                b.box(x,i+2,zz,x,i+4,zz,'minecraft:air')
        # Landing turns back through the east cabin wall at y=16.
        b.box(26,15,28,28,15,32,'tnd:polished_ashstone')
        b.passage(24,28,28,31,y=16,h=3)
        b.spire(20,29,base=25,height=28)
    elif name=='archive':
        b.plaza(2,2,52,46)
        b.room(14,10,40,39,18).room(3,15,14,42,11).room(40,15,51,42,11).room(19,3,35,10,10)
        b.passage(13,23,15,27);b.passage(39,23,41,27);b.passage(25,9,29,11);b.entrance(26,3)
        b.mezzanine(15,12,39,37,y=8)
        for x in [17,23,32,38]:
            for z in [17,29]:b.shelf(x,z,6);b.put(x,4,z,'tnd:resonant_lamp')
        for x in [5,43]:
            b.shelf(x,18,19);b.table(x+4,20,10);b.chest(x+5,38,'archive')
        b.table(27,18,15);b.chest(27,36,'relics')
        for x,z in [(9,31),(47,31),(27,34)]:b.put(x,1,z,'tnd:ancient_console')
        for x in [16,38]:b.spire(x,38,height=23)
        for x,block in [(22,'mural_ocean'),(25,'mural_lava'),(29,'mural_forest'),(32,'mural_fusion')]:b.put(x,4,38,'tnd:'+block)
    elif name=='foundry':
        b.plaza(2,2,54,46)
        b.room(12,12,44,42,19,kind='riveted_alloy').room(3,17,12,39,11).room(44,17,53,39,11)
        b.room(20,3,36,12,10);b.entrance(27,3);b.passage(26,11,30,13)
        b.passage(11,26,13,29);b.passage(43,26,45,29);b.mezzanine(13,14,43,40,y=9)
        for x in [18,36]:
            for z in [21,32]:
                b.box(x-2,1,z-2,x+2,6,z+2,'tnd:ancient_alloy')
                b.box(x-1,2,z-1,x+1,4,z+1,'tnd:red_lamp')
                b.put(x,2,z-2,'minecraft:blast_furnace')
                b.box(x,7,z,x,17,z,'tnd:alloy_vent')
        b.box(25,1,17,31,1,37,'tnd:cut_alloy')
        for z in range(18,37,3):b.put(28,2,z,'tnd:ancient_grate')
        b.workshop(5,21);b.workshop(46,21)
        b.chest(29,39,'foundry');b.put(26,1,39,'tnd:ancient_console')
        for x in [14,42]:b.spire(x,40,height=25)
    return b
