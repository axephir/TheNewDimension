"""An explorable eight-tower district, authored in blocks. No downloaded build."""
from architecture import Building
SIZE=(127,111,127)
# bounds, floors, purpose, neon: distinct skyline and interiors, all stairs physically usable.
TOWERS=[(8,8,39,39,10,'clinic','cyan'),(45,8,80,39,12,'signal','magenta'),
        (88,8,118,42,8,'barracks','amber'),(8,47,40,79,7,'foundry','amber'),
        (87,48,118,80,9,'transit','cyan'),(8,87,40,118,6,'habitat','magenta'),
        (47,88,79,118,10,'iris','cyan'),(87,88,118,118,7,'market','magenta')]

def build(max_floors=12):
    b=Building.__new__(Building);b.name='neon_district';b.size=(127,min(111,max_floors*8+15),127);b.grid={};b.rooms=[]
    p=b.put;q=b.box
    q(2,0,2,124,0,124,'tnd:city_tiles')
    # Native flat streets with cleared headroom, markings, drains and pavements.
    for x0,z0,x1,z1 in [(41,2,44,124),(81,2,86,124),(2,40,124,46),(2,81,124,86),(58,40,68,87)]:
        q(x0,0,z0,x1,0,z1,'tnd:city_asphalt');q(x0,1,z0,x1,5,z1,'minecraft:air')
        if x1-x0>z1-z0:
            for x in range(x0+2,x1,7):q(x,0,(z0+z1)//2,min(x+2,x1),0,(z0+z1)//2,'tnd:neon_amber')
        else:
            for z in range(z0+2,z1,7):q((x0+x1)//2,0,z,(x0+x1)//2,0,min(z+2,z1),'tnd:neon_amber')
    # Main promenade and a signal fountain, not a building-sized empty box.
    q(47,0,48,79,0,79,'tnd:carbon_panel');q(48,0,49,78,0,78,'tnd:city_tiles')
    for x,z in [(50,51),(76,51),(50,76),(76,76)]:
        q(x,1,z,x,6,z,'tnd:reinforced_panel');p(x,7,z,'tnd:neon_cyan')
    for x in range(58,69):
        for z in range(58,69):
            if (x-63)**2+(z-63)**2<=25:p(x,1,z,'tnd:carbon_panel')
    q(62,1,62,64,4,64,'tnd:circuit_panel');q(63,5,63,63,11,63,'tnd:neon_magenta');p(63,12,63,'tnd:relay_coil')
    p(60,2,59,'tnd:memory_terminal',{'entry':'1'})
    archive={'clinic':(2,8),'foundry':(3,4),'barracks':(4,6),'transit':(5,7),'iris':(6,9),'signal':(7,12)}
    for index,(x0,z0,x1,z1,floors,purpose,color) in enumerate(TOWERS):
        floors=min(floors,max_floors)
        height=floors*8
        q(x0,0,z0,x1,height,z1,'minecraft:air')
        # Floors, facades: dark structural piers, alternating glowing window ribbons and copper service ducts.
        for f in range(floors+1):
            y=f*8
            q(x0,y,z0,x1,y,z1,'tnd:reinforced_panel' if f==0 else 'tnd:carbon_panel')
            if f<floors:
                q(x0+1,y,z0+1,x1-1,y,z1-1,'tnd:city_tiles')
                for yy in range(y+1,y+8):
                    for x in range(x0,x1+1):
                        block='tnd:reinforced_panel' if (x-x0)%6 in (0,1) or yy-y in (1,7) else 'minecraft:cyan_stained_glass'
                        for z in [z0,z1]:p(x,yy,z,block)
                    for z in range(z0+1,z1):
                        block='tnd:reinforced_panel' if (z-z0)%6 in (0,1) or yy-y in (1,7) else 'minecraft:purple_stained_glass'
                        for x in [x0,x1]:p(x,yy,z,block)
                for x in range(x0+2,x1-1):
                    if (x-x0)%6 not in (0,1):
                        for z in [z0,z1]:p(x,y+6,z,'tnd:neon_'+color)
                for z in range(z0+2,z1-1,6):
                    for x in [x0,x1]:q(x,y+2,z,x,y+5,z,'tnd:neon_'+color)
                # Ceiling lights and individual furnished zones. Vary upper floors by function.
                for x in range(x0+12,x1-2,7):
                    for z in range(z0+4,z1-2,7):
                        p(x,y+7,z,'tnd:neon_cyan' if f%2==0 else 'tnd:neon_amber')
                        theme=(index+f)%4
                        if purpose=='clinic' or theme==0:
                            q(x,y+1,z,x+2,y+1,z+1,'minecraft:smooth_quartz_slab',{'type':'top','waterlogged':'false'});p(x+3,y+1,z,'tnd:circuit_panel');p(x+3,y+2,z,'tnd:relay_coil')
                        elif purpose=='foundry' or theme==1:
                            q(x,y+1,z,x+2,y+2,z+2,'tnd:vent_panel');p(x+1,y+3,z+1,'tnd:relay_coil');p(x+3,y+1,z,'minecraft:anvil')
                        elif purpose=='barracks' or theme==2:
                            q(x,y+1,z,x+2,y+1,z,'tnd:reinforced_panel');p(x,y+2,z,'tnd:circuit_panel');p(x+2,y+2,z,'tnd:neon_magenta');p(x+1,y+1,z+2,'tnd:cut_alloy_stairs',{'facing':'north','half':'bottom','shape':'straight','waterlogged':'false'})
                        else:
                            p(x,y+1,z,'tnd:archive_shelf');p(x+1,y+1,z,'tnd:archive_shelf');q(x,y+1,z+2,x+2,y+1,z+2,'tnd:cut_alloy_slab',{'type':'top','waterlogged':'false'});p(x,y+1,z+3,'tnd:cut_alloy_stairs',{'facing':'north','half':'bottom','shape':'straight','waterlogged':'false'})
                # Clear broad central circulation between furnished zones.
                mid=(z0+z1)//2;q(x0+1,y+1,mid-1,x1-1,y+3,mid+1,'minecraft:air')
            # External horizontal ledges / roof safety parapet.
            if f>0:
                for x in range(x0-1,x1+2):
                    for z in [z0-1,z1+1]:p(x,y,z,'tnd:cut_alloy_slab',{'type':'bottom','waterlogged':'false'})
                for z in range(z0,z1+1):
                    for x in [x0-1,x1+1]:p(x,y,z,'tnd:cut_alloy_slab',{'type':'bottom','waterlogged':'false'})
            if f==floors:
                for x in range(x0,x1+1):
                    for z in [z0,z1]:p(x,y+1,z,'tnd:reinforced_panel')
                for z in range(z0,z1+1):
                    for x in [x0,x1]:p(x,y+1,z,'tnd:reinforced_panel')
        # A dedicated 2-wide switchback stairwell, every floor including the roof.
        sx,sz=x0+3,z0+4
        for f in range(floors):
            y=f*8
            q(sx-1,y+1,sz-1,sx+7,y+9,sz+6,'minecraft:air')
            # First flight rises east, intermediate landing, second rises west.
            for i in range(4):
                for dz in (0,1):p(sx+i,y+i,sz+dz,'tnd:cut_alloy_stairs',{'facing':'east','half':'bottom','shape':'straight','waterlogged':'false'})
            q(sx+4,y+4,sz,sx+6,y+4,sz+5,'tnd:city_tiles')
            for i in range(4):
                for dz in (4,5):p(sx+3-i,y+4+i,sz+dz,'tnd:cut_alloy_stairs',{'facing':'west','half':'bottom','shape':'straight','waterlogged':'false'})
            # Floor at top return; bottom approach is on opposite side of corridor.
            q(sx-1,y+8,sz-1,sx-1,y+8,sz+6,'tnd:city_tiles')
            q(sx,y+8,sz+2,sx+3,y+8,sz+3,'tnd:city_tiles')
            p(sx+6,y+5,sz+2,'tnd:neon_'+color)
        # Approaches connect stairs and every furnished floor to the street-facing entrances.
        for f in range(floors+1):
            y=f*8
            q(x0+1,y+1,z0+1,x0+2,y+3,z1-1,'minecraft:air')
            mid=(z0+z1)//2;q(x0+1,y+1,mid-1,x1-1,y+3,mid+1,'minecraft:air')
        # Ground entrances to all four sides; no locked decorative doors.
        mx,mz=(x0+x1)//2,(z0+z1)//2
        for z in [z0,z1]:q(mx-1,1,z,mx+1,4,z,'minecraft:air');q(mx-1,5,z,mx+1,5,z,'tnd:neon_'+color)
        for x in [x0,x1]:q(x,1,mz-1,x,4,mz+1,'minecraft:air');q(x,5,mz-1,x,5,mz+1,'tnd:neon_'+color)
        # Roof mechanical rooms and asymmetrical antennae create a recognizable skyline.
        q(x1-9,height+1,z1-9,x1-3,height+3,z1-3,'tnd:vent_panel')
        q(x1-7,height+4,z1-7,x1-5,height+4,z1-5,'tnd:relay_coil')
        for dy in range(1,10):p(x1-4,height+dy,z0+4,'tnd:neon_'+color if dy%3==0 else 'tnd:reinforced_panel')
        p(x1-4,height+10,z0+4,'tnd:neon_amber')
        # Banners are geometric illuminated signs, readable even without text textures.
        for yy in range(13,min(height-5,43)):
            p(x1+1,yy,z0+5,'tnd:neon_'+color if yy%6 in (0,1,2) else 'tnd:carbon_panel')
            p(x1+1,yy,z0+6,'tnd:carbon_panel')
        b.chest(x1-4,z1-4,'city_rooftop',height+1)
        for f in (1,floors//2):b.chest(x1-3,z1-3,'city_salvage',f*8+1)
        if purpose in archive:
            entry,f=archive[purpose];f=min(f,floors);p(x0+12,f*8+1,z0+3,'tnd:memory_terminal',{'entry':str(entry)})
            p(x0+12,f*8,z0+3,'tnd:neon_amber')
    # Glazed elevated streets connect upper floors, with open doorway mouths.
    def bridge(x0,z0,x1,z1,y):
        q(x0,y,z0,x1,y,z1,'tnd:city_tiles');q(x0,y+1,z0,x1,y+4,z1,'minecraft:air')
        if x1-x0>z1-z0:
            for z in [z0,z1]:q(x0,y+1,z,x1,y+3,z,'minecraft:cyan_stained_glass');q(x0,y+4,z,x1,y+4,z,'tnd:neon_cyan')
        else:
            for x in [x0,x1]:q(x,y+1,z0,x,y+3,z1,'minecraft:purple_stained_glass');q(x,y+4,z0,x,y+4,z1,'tnd:neon_magenta')
    bridge(38,20,46,24,24);bridge(79,20,89,24,min(48,(max_floors-1)*8));bridge(20,38,24,48,24);bridge(20,78,24,88,min(48,(max_floors-1)*8));bridge(39,99,48,103,24);bridge(78,99,88,103,min(48,(max_floors-1)*8));bridge(100,41,104,49,24);bridge(100,79,104,89,min(48,(max_floors-1)*8))
    return b
