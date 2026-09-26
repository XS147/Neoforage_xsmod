package org.xsmod.common.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason; // ✅ 1.21.x 新类名
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.xsmod.Xsmod;

@EventBusSubscriber(modid = Xsmod.MODID)
public class Creeper_Changes {

	@SubscribeEvent
	public static void onMobDrop(LivingDropsEvent event) {
		// 仅服务端执行
		if (event.getEntity().level().isClientSide()) return;

		// ✅ 修复1: 提前返回，避免模式变量作用域问题
		if (!(event.getEntity() instanceof Creeper creeper)) return;
		if (!(event.getSource().getEntity() instanceof Player)) return;

		ServerLevel serverLevel = (ServerLevel) creeper.level();

		// ✅ 防止无限递归：跳过由本事件生成的分裂苦力怕
//		if (creeper.getPersistentData().getBoolean("xsmod:split_creeper").orElse(false)) return;

		// 替换掉落物为 3 个 TNT
		event.getDrops().clear();
		event.getDrops().add(new ItemEntity(
				serverLevel,
				creeper.getX(), creeper.getY(), creeper.getZ(),
				new ItemStack(Items.TNT, 3)
		));

		// ✅ 修复2 & 3: 使用 EntitySpawnReason + setPos 替代 moveTo
		for (int i = 0; i < 2; i++) {
			Creeper newCreeper = EntityType.CREEPER.create(serverLevel, EntitySpawnReason.TRIGGERED);
			if (newCreeper != null) {
//				double offsetX = (serverLevel.random.nextFloat() - 0.5) * 1.5;
//				double offsetZ = (serverLevel.random.nextFloat() - 0.5) * 1.5;

				// ✅ 1.21.x 正确的位置设置方式
				newCreeper.setPos(
						creeper.getX(), //+ offsetX,
						creeper.getY(),
						creeper.getZ() //+ offsetZ
				);
				newCreeper.setYRot(serverLevel.random.nextFloat() * 360F);
				newCreeper.setXRot(0F);

				// 标记为分裂体，防止递归生成
//				newCreeper.getPersistentData().putBoolean("xsmod:split_creeper", true);
//				newCreeper.setPersistenceRequired();
				serverLevel.addFreshEntity(newCreeper);
			}
		}

		// 播放雷击音效
		serverLevel.playSound(
				null,
				creeper.getX(), creeper.getY(), creeper.getZ(),
				SoundEvents.LIGHTNING_BOLT_THUNDER,
				SoundSource.HOSTILE,
				1.0F, 1.5F
		);
	}
}