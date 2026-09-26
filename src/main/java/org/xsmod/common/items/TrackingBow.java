package org.xsmod.common.items;

import org.xsmod.common.entities.TrackingArrow;
import org.xsmod.common.init.ModEntities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;

import org.jetbrains.annotations.Nullable;

public class TrackingBow extends BowItem {

	public TrackingBow(Properties properties) {
		super(properties);
	}

	@Override
	protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
	                               float velocity, float inaccuracy, float angle,
	                               @Nullable LivingEntity target) {
		// 移除原版箭矢
		projectile.discard();

		// 创建追踪箭
		TrackingArrow trackingArrow = new TrackingArrow(ModEntities.TRACKING_ARROW.get(), shooter.level());

		// 设置出生位置（从玩家眼睛高度偏下一点）
		trackingArrow.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());

		// 使用固定速度 5.0F（与 TrackingArrow 中的 INITIAL_SPEED 一致）
		trackingArrow.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle,
				0.0F, 5.0F, inaccuracy);

		// 设置拥有者
		trackingArrow.setOwner(shooter);

		// 加入世界
		shooter.level().addFreshEntity(trackingArrow);
	}
}