package fr.axephir.tnd.technology;

import fr.axephir.tnd.core.*;
import fr.axephir.tnd.progression.PlayerProgression;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.world.chunk.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.*;
import java.util.*;

/** Small paid factory districts, not unrestricted world loading. Tickets never reinstate themselves after a crash. */
public final class SpatialAnchors {
    public static final DeferredBlock<SpatialAnchorBlock> BLOCK=ModContent.BLOCKS.registerBlock("spatial_anchor",SpatialAnchorBlock::new,p->p.strength(6,18).requiresCorrectToolForDrops().lightLevel(s->s.getValue(SpatialAnchorBlock.LIT)?10:1));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SpatialAnchorEntity>> ENTITY=MachineRegistry.ENTITIES.register("spatial_anchor",()->new BlockEntityType<>(SpatialAnchorEntity::new,BLOCK.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<SpatialAnchorMenu>> MENU=ModContent.MENUS.register("spatial_anchor",()->new MenuType<>(SpatialAnchorMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<List<GlobalPos>>> OWNED=PlayerProgression.TYPES.register("factory_anchors",()->AttachmentType.builder(()->List.<GlobalPos>of()).serialize(GlobalPos.CODEC.listOf().validate(l->l.size()<=2?com.mojang.serialization.DataResult.success(l):com.mojang.serialization.DataResult.error(()->"At most two factory anchors per owner")).fieldOf("anchors")).copyOnDeath().build());
    public static final TicketController TICKETS=new TicketController(Identifier.fromNamespaceAndPath("tnd","factory"),(w,h)->{
        // No orphan tickets can keep machines running before their owner logs back in and validates both sites.
        for(var p:List.copyOf(h.getBlockTickets().keySet()))h.removeAllTickets(p);for(var id:List.copyOf(h.getEntityTickets().keySet()))h.removeAllTickets(id);
    });
    static final Map<MinecraftServer,Set<SpatialAnchorEntity>> ACTIVE=new WeakHashMap<>();
    public static void register(IEventBus bus){ModContent.ITEMS.registerSimpleBlockItem("spatial_anchor",BLOCK);
        bus.addListener((RegisterTicketControllersEvent e)->e.register(TICKETS));bus.addListener((RegisterCapabilitiesEvent e)->e.registerBlockEntity(Capabilities.Energy.BLOCK,ENTITY.get(),(be,side)->be.port));
    }
    public static List<GlobalPos> validate(ServerPlayer p){var valid=new ArrayList<GlobalPos>();for(var ref:p.getData(OWNED)){var w=p.level().getServer().getLevel(ref.dimension());if(w==null)continue;w.getChunkAt(ref.pos());if(w.getBlockEntity(ref.pos()) instanceof SpatialAnchorEntity be&&p.getUUID().equals(be.owner()))valid.add(ref);}p.setData(OWNED,List.copyOf(valid));return valid;}
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)for(var ref:validate(p)){var w=p.level().getServer().getLevel(ref.dimension());if(w.getBlockEntity(ref.pos()) instanceof SpatialAnchorEntity be)be.tick();}}
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){var active=ACTIVE.get(p.level().getServer());if(active!=null)for(var be:List.copyOf(active))if(p.getUUID().equals(be.owner()))be.release();}}
    @SubscribeEvent public void stopped(ServerStoppedEvent e){ACTIVE.remove(e.getServer());}
}
