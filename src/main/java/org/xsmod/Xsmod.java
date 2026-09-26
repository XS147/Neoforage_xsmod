package org.xsmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import org.xsmod.common.init.*;

@Mod(Xsmod.MODID)
public class Xsmod {
    public static final String MODID = "xsmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Xsmod(IEventBus modEventBus, ModContainer modContainer) {
        // 🌟 优雅！主类只负责调用各个 init 类的注册方法
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
    }
}