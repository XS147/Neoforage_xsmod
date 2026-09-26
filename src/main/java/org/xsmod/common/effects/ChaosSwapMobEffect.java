package org.xsmod.common.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ChaosSwapMobEffect extends MobEffect {

	// 🌟 新增：自定义参数，在构造函数中赋值，并且是 final 的（不可变）
	private final double swapRadius;
	private final boolean includePlayers;
	private final int swapInterval;

	// 🌟 全参构造函数：用于注册不同的变种
	public ChaosSwapMobEffect(MobEffectCategory category, int color, double swapRadius, boolean includePlayers, int swapInterval) {
		super(category, color);
		this.swapRadius = swapRadius;
		this.includePlayers = includePlayers;
		this.swapInterval = swapInterval;
	}

	// 默认构造函数：保持向后兼容（如果你其他地方用到了无参构造）
	public ChaosSwapMobEffect(MobEffectCategory category, int color) {
		this(category, color, 16.0D, true, 20); // 默认 16格，包含玩家，20tick(1秒)
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {

		// 1. 搜索范围依然以当前实体为中心（这是 Buff 的基本逻辑）
		AABB searchArea = entity.getBoundingBox().inflate(this.swapRadius);

		// 2. 获取参与者
		List<LivingEntity> participants = new ArrayList<>(level.getEntitiesOfClass(
				LivingEntity.class,
				searchArea,
				target -> {
					if (!target.isAlive()) return false;
					if (!this.includePlayers && target instanceof Player) return false;
					return !(target instanceof Player p) || !p.isSpectator();
				}
		));

		// 至少需要 2 个实体才能互换
		if (participants.size() < 2) {
			return true;
		}

		// 🌟 核心修改 1：按距离中心的远近进行排序（制造空间层次感）
		// 我们将参与者按照距离 entity 的距离从近到远排序
		Vec3 center = entity.position();
		participants.sort((e1, e2) -> {
			double dist1 = e1.distanceToSqr(center);
			double dist2 = e2.distanceToSqr(center);
			return Double.compare(dist1, dist2);
		});

		// 计算需要互换的对数
		int swapPairsCount = 1 + (amplifier * 2);
		int maxPossiblePairs = participants.size() / 2;
		if (swapPairsCount > maxPossiblePairs) {
			swapPairsCount = maxPossiblePairs;
		}

		RandomSource random = level.getRandom();

		//优先让“近处”和“远处”的实体互换
		// 我们将列表分为“内圈”和“外圈”。让内圈的实体和外圈的实体互换。
		// 这样可以最大程度地把中心的实体“甩”到边缘，把边缘的实体“拉”到中心，防止中心聚团。
		// 创建一个副本用于配对，避免破坏原列表结构
		List<LivingEntity> pool = new ArrayList<>(participants);

		for (int i = 0; i < swapPairsCount; i++) {
			if (pool.size() < 2) break; // 池子里不够配对了

			// 从池子前半部分（靠近中心）随机选一个
			int innerHalf = Math.max(1, pool.size() / 2);
			int index1 = random.nextInt(innerHalf);
			LivingEntity entity1 = pool.remove(index1);

			// 从池子后半部分（远离中心）随机选一个
			// 注意：因为上面 remove 了一个，现在的 pool.size() 变了
			if (pool.isEmpty()) break;
			int index2 = random.nextInt(pool.size()); // 从剩下的里面随机（偏向外部）
			LivingEntity entity2 = pool.remove(index2);

			// 执行位置互换
			Vec3 pos1 = entity1.position();
			Vec3 pos2 = entity2.position();

			// 互换位置 (Y轴稍微加一点，防止卡进地里)
			entity1.teleportTo(pos2.x, pos2.y + 0.1, pos2.z);
			entity2.teleportTo(pos1.x, pos1.y + 0.1, pos1.z);

			// 播放音效
			level.playSound(null, pos1.x, pos1.y, pos1.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
			level.playSound(null, pos2.x, pos2.y, pos2.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

			// 防止摔死
			entity1.resetFallDistance();
			entity2.resetFallDistance();
			if (entity1 instanceof Player p) p.resetCurrentImpulseContext();
			if (entity2 instanceof Player p) p.resetCurrentImpulseContext();

			//施加随机排斥力（打乱 AI 归巢本能）
			//互换后，给它们一个随机的水平初速度
			double pushStrength = 0.3D; // 排斥力强度

			double angle1 = random.nextDouble() * Math.PI * 2;
			entity1.push(Math.cos(angle1) * pushStrength, 0.1D, Math.sin(angle1) * pushStrength);

			double angle2 = random.nextDouble() * Math.PI * 2;
			entity2.push(Math.cos(angle2) * pushStrength, 0.1D, Math.sin(angle2) * pushStrength);

			// 随机改变它们的朝向，让 AI 重新评估目标
			entity1.setYRot(random.nextFloat() * 360.0F);
			entity2.setYRot(random.nextFloat() * 360.0F);
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
		return tickCount % this.swapInterval == 0;
	}
}