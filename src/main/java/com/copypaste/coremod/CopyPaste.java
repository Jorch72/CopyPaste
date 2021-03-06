package com.copypaste.coremod;

import com.copypaste.api.util.constant.Constants;
import com.copypaste.coremod.event.EventHandler;
import com.copypaste.coremod.event.FMLEventHandler;
import com.copypaste.coremod.network.messages.*;
import com.copypaste.coremod.placementhandlers.CopyPastePlacementHandlers;
import com.copypaste.coremod.proxy.IProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION, dependencies="after:gbook",
  /*dependencies = Constants.FORGE_VERSION,*/ acceptedMinecraftVersions = Constants.MC_VERSION)
public class CopyPaste
{
    private static final Logger    logger = LogManager.getLogger(Constants.MOD_ID);
    /**
     * Forge created instance of the Mod.
     */
    @Mod.Instance(Constants.MOD_ID)
    public static        CopyPaste instance;
    /**
     * Access to the proxy associated with your current side. Variable updated
     * by forge.
     */
    @SidedProxy(clientSide = Constants.CLIENT_PROXY_LOCATION, serverSide = Constants.SERVER_PROXY_LOCATION)

    public static        IProxy    proxy;

    private static SimpleNetworkWrapper network;

    static
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
    }

    /**
     * Returns whether the side is client or not
     *
     * @return True when client, otherwise false
     */
    public static boolean isClient()
    {
        return proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    /**
     * Returns whether the side is server or not
     *
     * @return True when server, otherwise false
     */
    public static boolean isServer()
    {
        return !proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isServer();
    }

    /**
     * Getter for the copypaste Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @Mod.EventHandler
    public void preInit(@NotNull final FMLPreInitializationEvent event)
    {
        proxy.registerEntities();
        proxy.registerEntityRendering();
        proxy.registerEvents();

        @NotNull final Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();

        if (configuration.hasChanged())
        {
            configuration.save();
        }
    }

    /**
     * Event handler for forge init event.
     *
     * @param event the forge init event.
     */
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event)
    {
        initializeNetwork();

        proxy.registerTileEntities();

        proxy.registerEvents();

        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        CopyPastePlacementHandlers.initHandlers();

        //Register Vanilla items with tags
    }

    private static synchronized void initializeNetwork()
    {
        int id = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_NAME);

        getNetwork().registerMessage(ServerUUIDMessage.class, ServerUUIDMessage.class, ++id, Side.CLIENT);

        getNetwork().registerMessage(BuildToolPasteMessage.class, BuildToolPasteMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(MultiBlockChangeMessage.class, MultiBlockChangeMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ScanOnServerMessage.class, ScanOnServerMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RemoveBlockMessage.class, RemoveBlockMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(RemoveEntityMessage.class, RemoveEntityMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(ReplaceBlockMessage.class, ReplaceBlockMessage.class, ++id, Side.SERVER);

        // Schematic transfer messages
        getNetwork().registerMessage(SchematicRequestMessage.class, SchematicRequestMessage.class, ++id, Side.SERVER);
        getNetwork().registerMessage(SchematicSaveMessage.class, SchematicSaveMessage.class, ++id, Side.CLIENT);
        getNetwork().registerMessage(SchematicSaveMessage.class, SchematicSaveMessage.class, ++id, Side.SERVER);

        //Client side only
        getNetwork().registerMessage(SaveScanMessage.class, SaveScanMessage.class, ++id, Side.CLIENT);

        //JEI Messages
    }

    public static SimpleNetworkWrapper getNetwork()
    {
        return network;
    }
}