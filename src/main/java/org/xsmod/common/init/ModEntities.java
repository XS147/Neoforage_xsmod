package org.xsmod.common.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.xsmod.Xsmod;
import org.xsmod.common.entities.*;

import java.util.function.Supplier;

public class ModEntities {

	// 🌟 1. 使用 DeferredRegister.Entities 而不是普通的 DeferredRegister
	public static final DeferredRegister.Entities ENTITY_TYPES =
			DeferredRegister.createEntities(Xsmod.MODID);

	// 🌟 2. 使用 registerEntityType 极简方法！
	// 它会自动帮你处理 ResourceKey 和 build()，你只需要传名字、工厂和分类！
	// 如果需要设置大小等属性，传入一个 builder -> builder.sized(...) 的 Lambda 即可。
	public static final Supplier<EntityType<End_Winter>> END_WINTER = ENTITY_TYPES.registerEntityType(
			"end_winter",
			End_Winter::new,
			MobCategory.MONSTER,
			builder -> builder
					.sized(0.9F, 3.5F)       // 碰撞箱大小
					.clientTrackingRange(10) // 追踪范围
	);
	public static final Supplier<EntityType<TrackingArrow>> TRACKING_ARROW =
			ENTITY_TYPES.registerEntityType(
					"tracking_arrow",          // 不要大写
					TrackingArrow::new,
					MobCategory.MISC,
					builder -> builder
							.sized(0.1F,0.1F)
			);

	public static void register(IEventBus eventBus) {
		ENTITY_TYPES.register(eventBus);
	}
}