package n.e.k.o.menus;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod("nekomenus")
public class NekoMenus {

    private static final Logger logger = LogManager.getLogger();

    private static final Queue<Runnable> runLaterQueue = new ConcurrentLinkedQueue<>();

    public NekoMenus() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        logger.info("Hello from NekoMenus :)");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        while (!runLaterQueue.isEmpty()) {
            runLaterQueue.poll().run();
        }
    }

    public static void runLater(Runnable run) {
        runLaterQueue.offer(run);
    }

}
