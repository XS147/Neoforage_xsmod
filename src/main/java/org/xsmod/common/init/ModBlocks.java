package org.xsmod.common.init; // 👈 修改包名

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.xsmod.Xsmod;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Xsmod.MODID);

    public static final DeferredBlock<TransparentBlock> XS_BLOCK = BLOCKS.register(
            "xs_block",
            registryName -> new TransparentBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isValidSpawn((state, level, pos, entityType) -> false)
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
            )
    );

    // 👇 新增统一的注册方法
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}