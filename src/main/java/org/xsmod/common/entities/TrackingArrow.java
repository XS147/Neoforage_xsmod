package org.xsmod.common.entities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class TrackingArrow extends AbstractArrow {
	private LivingEntity target;
	private int hitCount = 0;
	private Vec3 initialDirection = Vec3.ZERO;
	private Vec3 spawnPosition = Vec3.ZERO;

	// 新增：目标选择状态
	private boolean firstTarget = true;
	private int pauseTicks = 0;

	// 速度常量
	private static final double HIGH_SPEED = 5.0D;
	private static final double LOW_SPEED = 1.0D;

	// 检测距离
	private static final double OBSTACLE_CHECK_DISTANCE = 3.0D;
	private static final double PENETRATION_CHECK_DISTANCE = 2.0D;

	// 目标选择参数
	private static final double SEARCH_RADIUS = 50.0D;
	private static final double ANGLE_THRESHOLD = 0.866D;
	private static final double ANGLE_WEIGHT = 100.0D;
	private static final double MAX_VERTICAL_DISTANCE = 15.0D;

	// 存续性参数
	private static final int MAX_LIFE_TICKS = 200;
	private static final double MAX_FLIGHT_DISTANCE = 100.0D;
	private int lifeTicks = 0;

	// 穿透计时
	private int noPhysicsTicks = 0;

	public TrackingArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
		this.spawnPosition = this.position();
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity, inaccuracy);
		this.initialDirection = new Vec3(x, y, z).normalize();
		this.spawnPosition = this.position();
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) return;

		// 暂停处理：击杀后暂停1 tick
		if (pauseTicks > 0) {
			pauseTicks--;
			this.setDeltaMovement(Vec3.ZERO); // 停止运动
			return;
		}

		// 存续性检查
		if (++lifeTicks > MAX_LIFE_TICKS) {
			this.discard();
			return;
		}
		if (this.spawnPosition != Vec3.ZERO && this.position().distanceTo(spawnPosition) > MAX_FLIGHT_DISTANCE) {
			this.discard();
			return;
		}

		// 处理临时穿透计时
		if (noPhysicsTicks > 0) {
			noPhysicsTicks--;
			this.setNoPhysics(true);
		} else {
			this.setNoPhysics(false);
		}

		// 目标失效时重新搜索
		if (this.target == null || this.target.isRemoved() || !this.target.isAlive()) {
			this.target = findTarget();
		}

		if (this.target != null) {
			Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() / 2, 0);
			Vec3 desiredDirection = targetPos.subtract(this.position()).normalize();

			if (noPhysicsTicks == 0 && shouldPenetrateThinWall(desiredDirection)) {
				noPhysicsTicks = 3;
				this.setDeltaMovement(desiredDirection.scale(HIGH_SPEED));
			} else if (noPhysicsTicks > 0) {
				this.setDeltaMovement(desiredDirection.scale(HIGH_SPEED));
			} else {
				Vec3 adjustedDirection = enhancedAvoidObstacles(desiredDirection);
				boolean isAvoiding = adjustedDirection.dot(desiredDirection) < 0.98;
				double currentSpeed = isAvoiding ? LOW_SPEED : HIGH_SPEED;
				this.setDeltaMovement(adjustedDirection.scale(currentSpeed));
			}
		}
	}

	private Vec3 enhancedAvoidObstacles(Vec3 desiredDirection) {
		if (isPathBlocked(desiredDirection, OBSTACLE_CHECK_DISTANCE)) {
			return desiredDirection;
		}

		Vec3[] candidates = {
				rotateHorizontal(desiredDirection, 30),
				rotateHorizontal(desiredDirection, -30),
				rotateHorizontal(desiredDirection, 60),
				rotateHorizontal(desiredDirection, -60),
				rotateVertical(desiredDirection, 30),
				rotateVertical(desiredDirection, 60),
				rotateHorizontal(rotateVertical(desiredDirection, 30), 30),
				rotateHorizontal(rotateVertical(desiredDirection, 30), -30),
				rotateHorizontal(rotateVertical(desiredDirection, 60), 60),
				rotateHorizontal(rotateVertical(desiredDirection, 60), -60),
				new Vec3(0, 1, 0)
		};

		Vec3 bestDir = null;
		double bestDot = -1.0;
		for (Vec3 dir : candidates) {
			if (isPathBlocked(dir, OBSTACLE_CHECK_DISTANCE)) {
				double dot = dir.dot(desiredDirection);
				if (dot > bestDot) {
					bestDot = dot;
					bestDir = dir;
				}
				if (dot > 0.99) break;
			}
		}
		return bestDir != null ? bestDir : new Vec3(0, 1, 0);
	}

	private boolean shouldPenetrateThinWall(Vec3 desiredDirection) {
		Vec3 arrowPos = this.position();
		Vec3 checkEnd = arrowPos.add(desiredDirection.scale(OBSTACLE_CHECK_DISTANCE));
		ClipContext context = new ClipContext(arrowPos, checkEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
		BlockHitResult hitResult = this.level().clip(context);

		if (hitResult.getType() != HitResult.Type.BLOCK) return false;

		Vec3 normal = new Vec3(
				hitResult.getDirection().getStepX(),
				hitResult.getDirection().getStepY(),
				hitResult.getDirection().getStepZ()
		);
		if (desiredDirection.dot(normal) > -0.9) return false;

		Vec3 start = hitResult.getLocation().add(desiredDirection.scale(0.6));
		Vec3 end = start.add(desiredDirection.scale(PENETRATION_CHECK_DISTANCE));
		ClipContext thicknessContext = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
		BlockHitResult thicknessHit = this.level().clip(thicknessContext);
		return thicknessHit.getType() != HitResult.Type.BLOCK;
	}

	private boolean isPathBlocked(Vec3 direction, double distance) {
		Vec3 start = this.position();
		Vec3 end = start.add(direction.scale(distance));
		ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
		BlockHitResult hit = this.level().clip(context);
		return hit.getType() != HitResult.Type.BLOCK;
	}

	private Vec3 rotateHorizontal(Vec3 dir, double degrees) {
		double rad = Math.toRadians(degrees);
		double cos = Math.cos(rad);
		double sin = Math.sin(rad);
		return new Vec3(dir.x * cos - dir.z * sin, dir.y, dir.x * sin + dir.z * cos).normalize();
	}

	private Vec3 rotateVertical(Vec3 dir, double degrees) {
		double rad = Math.toRadians(degrees);
		double cos = Math.cos(rad);
		double sin = Math.sin(rad);
		Vec3 horizontal = new Vec3(dir.x, 0, dir.z).normalize();
		if (horizontal.lengthSqr() < 0.001) return dir;
		return horizontal.scale(cos).add(0, sin, 0).normalize();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (this.level().isClientSide()) return;
		var entity = result.getEntity();
		if (entity instanceof LivingEntity living && living.isAlive()) {
			living.setHealth(0.0F);
			playHitSound();
			hitCount++;
			if (hitCount >= 50) {
				this.discard();
			} else {
				// 标记已经不是第一个目标
				firstTarget = false;
				// 设置暂停，让箭稳定后重新索敌
				pauseTicks = 1;
				this.target = null;
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		if (!this.level().isClientSide()) {
			this.discard();
		}
	}

	private void playHitSound() {
		var shooter = this.getOwner();
		if (shooter instanceof Player player) {
			this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
		} else {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 2.0F, 1.0F);
		}
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return Items.ARROW.getDefaultInstance();
	}

	/**
	 * 索敌逻辑：./
	 * - 第一个目标：前方30° + 垂直限制 + 综合评分（角度优先）
	 *
	 * - 后续目标：仅按距离优先（保留垂直限制，不限制角度）
	 */
	@Nullable
	private LivingEntity findTarget() {
		AABB aabb = this.getBoundingBox().inflate(SEARCH_RADIUS, MAX_VERTICAL_DISTANCE, SEARCH_RADIUS);
		Vec3 arrowPos = this.position();
		Vec3 aimDir = this.initialDirection;

		return this.level().getEntitiesOfClass(LivingEntity.class, aabb,
						entity -> entity != this.getOwner() && entity.isAlive()
				).stream()
				.filter(entity -> {
					Vec3 toEntity = entity.position().subtract(arrowPos);
					double verticalDist = Math.abs(toEntity.y);
					if (verticalDist > MAX_VERTICAL_DISTANCE) return false; // 垂直限制始终生效

					if (firstTarget) {
						// 仅第一个目标检查前方角度
						Vec3 dir = toEntity.normalize();
						return dir.dot(aimDir) > ANGLE_THRESHOLD;
					}
					return true; // 后续目标不受角度限制
				})
				.min((e1, e2) -> {
					Vec3 to1 = e1.position().subtract(arrowPos);
					Vec3 to2 = e2.position().subtract(arrowPos);
					double dist1 = to1.lengthSqr();
					double dist2 = to2.lengthSqr();

					if (firstTarget) {
						// 第一个目标：距离 + 角度惩罚 + 垂直惩罚
						double angle1 = to1.normalize().dot(aimDir);
						double angle2 = to2.normalize().dot(aimDir);
						double verticalPenalty1 = Math.abs(to1.y) * 10.0;
						double verticalPenalty2 = Math.abs(to2.y) * 10.0;
						double score1 = dist1 + (1 - angle1) * ANGLE_WEIGHT + verticalPenalty1;
						double score2 = dist2 + (1 - angle2) * ANGLE_WEIGHT + verticalPenalty2;
						return Double.compare(score1, score2);
					} else {
						// 后续目标：仅按距离（可加轻微垂直惩罚保持水平）
						double verticalPenalty1 = Math.abs(to1.y) * 5.0; // 权重较低
						double verticalPenalty2 = Math.abs(to2.y) * 5.0;
						return Double.compare(dist1 + verticalPenalty1, dist2 + verticalPenalty2);
					}
				})
				.orElse(null);
	}
}