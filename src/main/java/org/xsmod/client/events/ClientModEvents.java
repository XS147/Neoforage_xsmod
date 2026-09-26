package org.xsmod.client.events;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState; // 新增
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.xsmod.Xsmod;
import org.xsmod.common.entities.TrackingArrow;
import org.xsmod.common.init.ModEntities;

@EventBusSubscriber(modid = Xsmod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

	@SubscribeEvent
	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// 注册 END_WINTER
		event.registerEntityRenderer(ModEntities.END_WINTER.get(), WitherBossRenderer::new);

		// 注册 TrackingArrow（使用匿名子类，实现抽象方法）
		event.registerEntityRenderer(
				ModEntities.TRACKING_ARROW.get(),
				ctx -> new ArrowRenderer<>(ctx) {
					@Override
					public Identifier getTextureLocation(ArrowRenderState state) {
						// 使用原版箭纹理
						return Identifier.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png");
						// 若自定义：return Identifier.fromNamespaceAndPath(Xsmod.MODID, "textures/entity/tracking_arrow.png");
					}

					@Override
					public ArrowRenderState createRenderState() {
						return new ArrowRenderState();
					}
				}
		);
	}
}