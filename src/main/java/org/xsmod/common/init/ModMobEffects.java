package org.xsmod.common.init; // 👈 修改包名

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.xsmod.common.effects.ChaosSwapMobEffect; // 👈 确保引用正确
import org.xsmod.Xsmod;

public class ModMobEffects {
	public static final DeferredRegister<MobEffect> MOB_EFFECTS =
			DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT, Xsmod.MODID);

	public static final DeferredHolder<MobEffect, MobEffect> CHAOS_SWAP_NORMAL = MOB_EFFECTS.register(
			"chaos_swap_normal",
			() -> new ChaosSwapMobEffect(MobEffectCategory.NEUTRAL, 0xFF8A2BE2, 160D, false, 1)
	);

	public static final DeferredHolder<MobEffect, MobEffect> CHAOS_SWAP_PARTY = MOB_EFFECTS.register(
			"chaos_swap_party",
			() -> new ChaosSwapMobEffect(MobEffectCategory.NEUTRAL, 0xFFFF00FF, 160D, true, 1)
	);

	//统一注册方法
	public static void register(IEventBus eventBus) {
		MOB_EFFECTS.register(eventBus);
	}
}