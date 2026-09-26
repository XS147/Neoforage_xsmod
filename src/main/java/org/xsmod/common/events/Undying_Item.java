package org.xsmod.common.events;


import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.xsmod.Xsmod;
import org.xsmod.common.init.ModItems;


@EventBusSubscriber(modid = Xsmod.MODID)
public class Undying_Item {
	@SubscribeEvent
	public static void onPlayerTakeDamage(LivingDamageEvent.Pre event) {
		// 1. 获取受击实体并判断是否为玩家
		if (!(event.getEntity() instanceof Player player)) return;

		// 避免客户端重复触发
		if (player.level().isClientSide()) return;

		ItemStack mainHand = player.getMainHandItem();
		ItemStack offHand = player.getOffhandItem();

		// 2. 判断：不是空手 + 是你的自定义物品 + 数量达到了 1
		if (!(!mainHand.isEmpty() && mainHand.is(ModItems.RAW_XS_INGOT) && mainHand.getCount() >= 1)&&
				!(!offHand.isEmpty() && offHand.is(ModItems.RAW_XS_INGOT) && offHand.getCount() >= 1)) {
			return; // 没有金牌，正常受击
		}

		//获取伤害值并判断是否为致死伤害
		float incomingDamage = event.getNewDamage();
		float currentHealth = player.getHealth();

		if (currentHealth - incomingDamage <= 0.0F) {
			//取消伤害：将新伤害设为 0
			event.setNewDamage(0.0F);

			//锁血并给予 Buff
			player.setHealth(0.5F);
			// player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3));
			// player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));

			//消耗物品
			boolean mainhave = (!mainHand.isEmpty() && mainHand.is(ModItems.RAW_XS_INGOT) && mainHand.getCount() >= 1);
			boolean offhave = (!offHand.isEmpty() && offHand.is(ModItems.RAW_XS_INGOT) && offHand.getCount() >= 1);
			if(mainhave && !offhave){
				mainHand.shrink(1);
			} else if (!mainhave && offhave) {
				offHand.shrink(1);
			}else {
				offHand.shrink(1);
			}

			// 或让附近所有人都听到：
			player.level().playSound(
					null,
					player.getX(), player.getY(), player.getZ(),
					SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F
			);

			// 发送提示
			// 在 if 块内部使用模式匹配
			if (player instanceof ServerPlayer serverPlayer) {
				ServerLevel level = serverPlayer.level();
				level.sendParticles(
						new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()),
						player.getX(),
						player.getY() + 1.0,
						player.getZ(),
						10,       // 数量
						2,      // X 扩散
						2.2,      // Y 扩散
						2,      // Z 扩散
						0.02      // 速度倍率
				);

//				Component msg = Component.literal("§d[凛冬庇护] §f");

//				serverPlayer.connection.send(new ClientboundSystemChatPacket(msg, false));
				player.displayClientMessage(Component.literal("§d[你的锭已碎裂，保住了你一命！] §f"), true);
			}
		}
	}
}
