package org.xsmod.common.events; // 根据你的实际包名调整

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.xsmod.Xsmod;
import org.xsmod.common.init.ModEntities;
import net.minecraft.world.entity.boss.wither.WitherBoss;

// 核心注意：不要加 value = Dist.CLIENT！属性注册是双端（服务端+客户端）必须的！
// 如果加了 Dist.CLIENT，在独立服务端或多人联机时，服务端会因为找不到属性而崩溃！
@EventBusSubscriber(modid = Xsmod.MODID)
public class CommonModEvents {

	@SubscribeEvent
	public static void onAttributeCreate(EntityAttributeCreationEvent event) {
		//  核心：为你的自定义实体注册默认属性！
		// 这里直接“白嫖”原版凋灵（WitherBoss）的属性配置

		 // 进阶：如果你想自定义血量或攻击力，可以这样写：
		 event.put(ModEntities.END_WINTER.get(), WitherBoss.createAttributes()
		         .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 500.0) // 设置 500 血
		         .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 20.0) // 设置 20 攻击力
		         .build());
	}




}