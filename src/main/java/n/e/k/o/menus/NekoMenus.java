package n.e.k.o.menus;

import n.e.k.o.menus.utils.GlobalConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod("nekomenus")
public class NekoMenus {

    private static final Logger logger = LogManager.getLogger();

    private static final Queue<Runnable> runLaterQueue = new ConcurrentLinkedQueue<>();

    public static boolean isEnabled = false;

    public NekoMenus() {
        if (!GlobalConfig.checkEnabled("NekoMenus", logger)) {
            logger.warn("Disabling NekoMenus.");
            return;
        }
        isEnabled = true;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        logger.info("Hello from NekoMenus :)");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (!runLaterQueue.isEmpty()) {
            runLaterQueue.poll().run();
        }
    }

    public static void runLater(Runnable run) {
        runLaterQueue.offer(run);
    }

}
