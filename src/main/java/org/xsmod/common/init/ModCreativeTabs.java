package org.xsmod.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {

	// 1. 创建 DeferredRegister，目标是创造模式物品栏注册表
	// 注意：请将 "xsmod" 替换为你实际的 Mod ID (通常在主类中定义为常量)
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "xsmod");

	// 2. 注册你的专属物品栏
	// 这里我们注册一个名为 "main" 的 Tab
	public static final Supplier<CreativeModeTab> XS_MAIN_TAB = CREATIVE_MODE_TABS.register("main",
			() -> CreativeModeTab.builder()
					// 设置 Tab 的标题（必须使用语言键，见第三步）
					.title(Component.translatable("itemGroup.xsmod.main"))
					// 设置 Tab 的图标（这里用你的不义游戏饼干作为图标）
					.icon(() -> new ItemStack(ModItems.CHORUS_COOKIE.get()))
					// 设置 Tab 里显示哪些物品
					.displayItems((parameters, output) -> {
						// 🌟 在这里把你的物品一个个加进去！
						output.accept(ModItems.CHORUS_COOKIE.get());
						output.accept(ModItems.XS_BLOCK_ITEM.get()); // 如果有方块物品也加进来
						output.accept(ModItems.XS_POTION_DRINK.get());
						output.accept(ModItems.XS_APPLE.get());
						output.accept(ModItems.MAGIC_FRUIT.get());
						output.accept(ModItems.RAW_XS_INGOT.get());
						output.accept(ModItems.TRACKING_BOW.get());
						// 💡 进阶：如果你想把 ModItems 里所有的物品都自动加进来，可以使用循环（见下方提示）
					})
					.build()
	);

	// 3. 提供给主类调用的注册方法
	public static void register(IEventBus eventBus) {
		CREATIVE_MODE_TABS.register(eventBus);
	}
}