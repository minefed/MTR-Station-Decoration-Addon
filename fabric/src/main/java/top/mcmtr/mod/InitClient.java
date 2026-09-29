package top.mcmtr.mod;

import org.mtr.core.data.Station;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.MinecraftClientHelper;
import org.mtr.mapping.registry.RegistryClient;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.render.RenderPIDS;
import top.mcmtr.core.operation.MSDDataRequest;
import top.mcmtr.mod.blocks.BlockCatenaryWithModel;
import top.mcmtr.mod.client.MSDMinecraftClientData;
import top.mcmtr.mod.config.Config;
import top.mcmtr.mod.items.ItemBlockClickingBase;
import top.mcmtr.mod.items.ItemHold;
import top.mcmtr.mod.packet.MSDPacketRequestData;
import top.mcmtr.mod.render.RenderCatenaryModel;
import top.mcmtr.mod.render.RenderCustomText;
import top.mcmtr.mod.render.RenderYamanoteRailwaySign;

public class InitClient {
    private static long lastMillis = 0;
    private static long gameMillis = 0;
    private static long lastUpdatePacketMillis = 0;
    private static final Long2ObjectOpenHashMap<Station> STATION_CACHE = new Long2ObjectOpenHashMap<>();
    private static MinecraftClientData stationCacheData;
    public static final RegistryClient REGISTRY_CLIENT = new RegistryClient(Init.REGISTRY);

    public static void init() {
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.NEW_CATENARY_NODE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RIGID_CATENARY_NODE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_LONG);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_LONG_TOP);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_SHORT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_SHORT_TOP);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_LONG_COUNTERWEIGHT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_LONG_COUNTERWEIGHT_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_SHORT_COUNTERWEIGHT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_WITH_SHORT_COUNTERWEIGHT_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_NODE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_NODE_STYLE_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_NODE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_NODE_STYLE_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ELECTRIC_NODE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.TRANS_CATENARY_NODE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.SURVEILLANCE_CAMERAS);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.YUUNI_PIDS);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.YUUNI_2_PIDS);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.STANDING_SIGN);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.STANDING_SIGN_1);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.STANDING_SIGN_POLE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.YUUNI_TICKET);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.HALL_SEAT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.YUUNI_TICKET);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.HALL_SEAT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DECORATION_BOOK);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DECORATION_CEILING);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DECORATION_CEILING_LIGHT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DECORATION_PC);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DECORATION_STAIR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DISPLAY_BOARD_HORIZONTALLY);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.DISPLAY_BOARD_VERTICALLY);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_FLAT);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_FLAT_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_POLE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_POLE_TOP_MIDDLE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_POLE_TOP_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_RACK_1);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_RACK_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_RACK_BOTH_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_RACK_POLE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_RACK_POLE_BOTH_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.CATENARY_RACK_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ELECTRIC_POLE_ANOTHER_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ELECTRIC_POLE_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ELECTRIC_POLE_TOP_BOTH_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ELECTRIC_POLE_TOP_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_RACK);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_RACK_BOTH_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_RACK_POLE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_RACK_POLE_BOTH_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.SHORT_CATENARY_RACK_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.SURVEILLANCE_CAMERAS_WALL);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.HALL_SEAT_SIDE);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.HALL_SEAT_SIDE_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_START);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_END);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_START_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_END_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_1);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_3);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_MIRROR_1);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_MIRROR_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_MIRROR_3);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_CORNER);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_CORNER_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_CORNER_MIRROR);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.RAILING_STAIR_CORNER_MIRROR_2);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_4);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_5);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_MIRROR_4);
        REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getTranslucent(), Blocks.RAILING_STAIR_GLASS_MIRROR_5);

        REGISTRY_CLIENT.registerItemModelPredicate(Items.CATENARY_CONNECTOR, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.ELECTRIC_CONNECTOR, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RIGID_SOFT_CATENARY_CONNECTOR, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RIGID_CATENARY_CONNECTOR, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.CATENARY_REMOVER, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RIGID_CATENARY_REMOVER, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());

        REGISTRY_CLIENT.registerItemModelPredicate(Items.SURVEILLANCE_CAMERAS, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.HALL_SEAT, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DECORATION_BOOK, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DECORATION_CEILING, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DECORATION_CEILING_LIGHT, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DECORATION_FLOOR, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DECORATION_PC, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DECORATION_STAIR, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DISPLAY_BOARD_HORIZONTALLY, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.DISPLAY_BOARD_VERTICALLY, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RAILING_STAIR_FLAT, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RAILING_STAIR_FLAT_MIRROR, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RAILING_STAIR_GLASS, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.RAILING_STAIR_GLASS_MIRROR, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.STANDING_SIGN, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.STANDING_SIGN_1, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());
        REGISTRY_CLIENT.registerItemModelPredicate(Items.STANDING_SIGN_POLE, new Identifier(Init.MOD_ID, "hold"), checkItemHoldTag());

        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_2_EVEN, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_2_ODD, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_3_EVEN, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_3_ODD, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_4_EVEN, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_4_ODD, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_5_EVEN, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_5_ODD, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_6_EVEN, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_6_ODD, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_7_EVEN, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_RAILWAY_SIGN_ENTITY_7_ODD, RenderYamanoteRailwaySign::new);
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.STANDING_SIGN, argument -> new RenderCustomText<>(argument, 3, 8F, 14.5F, 7.01F, 15F, 11F, true, 2F, 3.1F, 6.2F, 0.004F, 0xFFFFFF, 0x000000, 0x000000));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.STANDING_SIGN_1, argument -> new RenderCustomText<>(argument, 1, 2.5F, 9.25F, 7.65F, 4F, 11F, true, 1.6F, 1.6F, 3.2F, 0.0625F, 0xFFFFFF));

        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YUUNI_PIDS, argument -> new RenderPIDS<>(argument, 2.5F, 7.5F, 6F, 6.5F, 27, true, 1.25F));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YUUNI_2_PIDS, argument -> new RenderPIDS<>(argument, 4F, 7.5F, 5.9F, 2.5F, 24, true, 1F));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_4_PIDS, argument -> new RenderPIDS<>(argument, 0F, 15F, 7F, 6F, 32, true, 1F));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_5_PIDS, argument -> new RenderPIDS<>(argument, -4F, 15F, 7F, 6F, 40, true, 1F));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_6_PIDS, argument -> new RenderPIDS<>(argument, -8F, 15F, 7F, 6F, 48, true, 1F));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.YAMANOTE_7_PIDS, argument -> new RenderPIDS<>(argument, -12F, 15F, 7F, 6F, 56, true, 1F));

        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_LONG, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_LONG));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_LONG_TOP, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_LONG_TOP));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_SHORT, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_SHORT));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_SHORT_TOP, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_SHORT_TOP));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_LONG_COUNTERWEIGHT, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_LONG_COUNTERWEIGHT));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_LONG_COUNTERWEIGHT_MIRROR, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_LONG_COUNTERWEIGHT_MIRROR));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_SHORT_COUNTERWEIGHT, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_SHORT_COUNTERWEIGHT));
        REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.CATENARY_WITH_SHORT_COUNTERWEIGHT_MIRROR, argument -> new RenderCatenaryModel<>(argument, BlockCatenaryWithModel.CatenaryModel.CATENARY_SHORT_COUNTERWEIGHT_MIRROR));

        REGISTRY_CLIENT.setupPackets(new Identifier(Init.MOD_ID, "packet"));

        REGISTRY_CLIENT.eventRegistryClient.registerClientJoin(() -> {
            MSDMinecraftClientData.reset();
            lastMillis = System.currentTimeMillis();
            gameMillis = 0;
        });

        // Do not keep the previous world's MTR client data reachable through the station cache after leaving it
        REGISTRY_CLIENT.eventRegistryClient.registerClientDisconnect(InitClient::releaseStationCache);

        REGISTRY_CLIENT.eventRegistryClient.registerStartClientTick(() -> {
            incrementGameMillis();
            final ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().getPlayerMapped();
            if (clientPlayerEntity != null && lastUpdatePacketMillis >= 0 && getGameMillis() - lastUpdatePacketMillis > 500) {
                final MSDDataRequest dataRequest = new MSDDataRequest(clientPlayerEntity.getUuidAsString(), org.mtr.mod.Init.blockPosToPosition(MinecraftClient.getInstance().getGameRendererMapped().getCamera().getBlockPos()), MinecraftClientHelper.getRenderDistance() * 16L);
                dataRequest.writeExistingIds(MSDMinecraftClientData.getInstance());
                InitClient.REGISTRY_CLIENT.sendPacketToServer(new MSDPacketRequestData(dataRequest));
                lastUpdatePacketMillis = -1;
            }
        });

        REGISTRY_CLIENT.eventRegistryClient.registerChunkLoad((clientWorld, worldChunk) -> {
            if (lastUpdatePacketMillis < 0) {
                lastUpdatePacketMillis = getGameMillis();
            }
        });

        Config.refreshProperties();
        REGISTRY_CLIENT.init();
    }

    public static long getGameMillis() {
        return gameMillis;
    }

    public static void incrementGameMillis() {
        final long currentMillis = System.currentTimeMillis();
        final long millisElapsed = currentMillis - lastMillis;
        lastMillis = currentMillis;
        gameMillis += millisElapsed;
    }

    private static RegistryClient.ModelPredicateProvider checkItemPredicateTag() {
        return ((itemStack, clientWorld, livingEntity) -> itemStack.getOrCreateTag().contains(ItemBlockClickingBase.TAG_POS) ? 1 : 0);
    }

    private static RegistryClient.ModelPredicateProvider checkItemHoldTag() {
        return ((itemStack, clientWorld, livingEntity) -> itemStack.getOrCreateTag().contains(ItemHold.TAG_HOLD) ? itemStack.getOrCreateTag().getInt(ItemHold.TAG_HOLD) / 10F : 0);
    }

    /**
     * Results are cached per block position and cleared whenever the MTR client data is synced (see {@link top.mcmtr.mixin.MinecraftClientDataMixin}) or replaced.
     */
    public static Station findStation(BlockPos blockPos) {
        synchronized (STATION_CACHE) {
            final MinecraftClientData minecraftClientData = MinecraftClientData.getInstance();
            if (stationCacheData != minecraftClientData) {
                STATION_CACHE.clear();
                stationCacheData = minecraftClientData;
            }
            final long key = blockPos.asLong();
            final Station cachedStation = STATION_CACHE.get(key);
            if (cachedStation != null || STATION_CACHE.containsKey(key)) {
                return cachedStation;
            }
            final Station station = minecraftClientData.stations.stream().filter(stationToCheck -> stationToCheck.inArea(org.mtr.mod.Init.blockPosToPosition(blockPos))).findFirst().orElse(null);
            STATION_CACHE.put(key, station);
            return station;
        }
    }

    public static void clearStationCache() {
        synchronized (STATION_CACHE) {
            STATION_CACHE.clear();
        }
    }

    private static void releaseStationCache() {
        synchronized (STATION_CACHE) {
            STATION_CACHE.clear();
            stationCacheData = null;
        }
    }
}