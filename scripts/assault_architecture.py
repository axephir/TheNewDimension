"""Three inhabited assault routes, applied after streets and skyways so circulation stays open."""
def decorate(b,max_floors):
 from city_architecture import TOWERS
 for index,(x0,z0,x1,z1,n,purpose,color) in enumerate(TOWERS):
  if purpose not in ['clinic','barracks','iris']:continue
  family={'clinic':'choir','barracks':'ward','iris':'shade'}[purpose];floors=min(n,max_floors);mx,mz=(x0+x1)//2,(z0+z1)//2;p=b.put;q=b.box
  for f in range(floors):
   y=f*8
   # Distinct occupied rooms on either side of a broad through corridor.
   q(x0+11,y+1,z0+2,x0+11,y+5,mz-3,'minecraft:iron_bars',{'east':'true','west':'true','north':'false','south':'false','waterlogged':'false'})
   q(x0+11,y+1,z0+4,x0+11,y+3,z0+6,'minecraft:air')
   if purpose=='clinic':
    q(x1-9,y+1,z0+4,x1-4,y+1,z0+6,'minecraft:smooth_quartz');q(x1-9,y+2,z0+4,x1-4,y+4,z0+4,'minecraft:cyan_stained_glass')
    p(x1-6,y+2,z0+6,'tnd:circuit_panel');p(x1-5,y+3,z0+6,'tnd:relay_coil')
   elif purpose=='barracks':
    for z in range(z0+4,mz-3,3):q(x1-9,y,z,x1-3,y,z,'tnd:neon_amber');q(x1-5,y+1,z,x1-5,y+3,z,'tnd:vent_panel')
   else:
    q(x1-10,y+1,z0+5,x1-5,y+3,z0+7,'tnd:archive_shelf');q(x1-10,y+2,z0+7,x1-5,y+2,z0+7,'tnd:circuit_panel')
    q(x1-11,y+1,z1-9,x1-6,y+1,z1-7,'tnd:cut_alloy_slab',{'type':'top','waterlogged':'false'})
   # Windows and room lamps respond to an intrusion relay. No edits to ordinary player lights.
   for x in range(x0+12,x1-2,7):
    for z in range(z0+4,z1-2,7):p(x,y+7,z,'tnd:assault_lamp',{'lit':'true'})
   q(mx-1,y+1,mz-1,mx+1,y+4,mz+1,'minecraft:air')
   p(mx,y+1,mz,'tnd:assault_node',{'family':family,'summit':'false','lit':'false'})
   if f in (1,floors//2,floors-1):b.chest(x0+13,z0+7,'assault_reserves',y+1)
  # A real rooftop fighting floor, open stair landing, machinery cover and a raised signal dais.
  y=floors*8;q(mx-8,y,mz-8,mx+8,y,mz+8,'tnd:reinforced_panel');q(mx-7,y+1,mz-7,mx+7,y+5,mz+7,'minecraft:air')
  for dx,dz in [(-7,-7),(-7,7),(7,-7),(7,7)]:q(mx+dx,y+1,mz+dz,mx+dx,y+3,mz+dz,'tnd:vent_panel');p(mx+dx,y+4,mz+dz,'tnd:neon_'+color)
  for d in range(-7,8):
   for z in [-8,8]:p(mx+d,y,mz+z,'tnd:neon_'+color)
   for x in [-8,8]:p(mx+x,y,mz+d,'tnd:neon_'+color)
  p(mx,y+1,mz,'tnd:assault_node',{'family':family,'summit':'true','lit':'false'})
