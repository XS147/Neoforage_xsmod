package org.xsmod.common.init;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import org.xsmod.common.items.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import org.xsmod.Xsmod;

import static net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Xsmod.MODID);

    //方块物品
    public static final DeferredItem<BlockItem> XS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("xs_block", ModBlocks.XS_BLOCK);

    // ====== 1. 普通物品 ======
    public static final DeferredItem<Item> RAW_XS_INGOT = ITEMS.registerSimpleItem("raw_xs_ingot");

        // ====== 2. 简单食物（使用默认 Consumable）======

    public static final DeferredItem<Item> XS_APPLE = ITEMS.registerSimpleItem(
            "xs_apple",
            props -> props.food(
                    new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.3F)
                            .build()
                )
        );

        // ====== 3. 高级食物（自定义 Consumable + 状态效果）======
    public static final DeferredItem<Item> MAGIC_FRUIT = ITEMS.registerSimpleItem(
                "magic_fruit",
                props -> props.food(
                        new FoodProperties.Builder()
                                .nutrition(6)
                                .saturationModifier(0.6F)
                                .alwaysEdible()
                                .build()
                        )
                        .component(
                                DataComponents.CONSUMABLE,
                                Consumable.builder()
                                        .consumeSeconds(2.0F)
                                        .animation(ItemUseAnimation.EAT)
                                        .sound(SoundEvents.GENERIC_EAT)
                                        .soundAfterConsume(SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP))
                                        .hasConsumeParticles(true)
                                        .onConsume(
                                                new ApplyStatusEffectsConsumeEffect(
                                                        new MobEffectInstance(MobEffects.REGENERATION, 600, 0),
                                                        0.8F
                                                )
                                        )
                                        .onConsume(
                                                new ApplyStatusEffectsConsumeEffect(
                                                        new MobEffectInstance(MobEffects.STRENGTH, 400, 0),
                                                        0.3F
                                                )
                                        )
                                        .build()
                        )
        );

        // ====== 4. 饮料类物品（使用 DRINK 动画）======
        public static final DeferredItem<Item> XS_POTION_DRINK = ITEMS.registerSimpleItem(
                "xs_potion_drink",
                props -> props.component(
                        DataComponents.CONSUMABLE,
                        Consumable.builder()
                                .consumeSeconds(1.0F)
                                .animation(ItemUseAnimation.DRINK)
                                .sound(SoundEvents.GENERIC_DRINK)
                                .soundAfterConsume(SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP))
                                .hasConsumeParticles(false)
                                .onConsume(
                                        new ApplyStatusEffectsConsumeEffect(
                                                new MobEffectInstance(MobEffects.SPEED, 1200, 1),
                                                1.0F
                                        )
                                )
                                .build()
                )
        );

    // ====== 5. 趣味物品（不义游戏饼干）======
    public static final DeferredItem<Item> CHORUS_COOKIE = ITEMS.registerSimpleItem(
            "chorus_cookie",
            props -> props.food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.1F)
                            .alwaysEdible()
                            .build()
                    )
                    .component(
                            DataComponents.CONSUMABLE,
                            Consumable.builder()
                                    .consumeSeconds(0.8F)
                                    .animation(ItemUseAnimation.EAT)
                                    .sound(SoundEvents.GENERIC_EAT)
                                    .onConsume(
                                            new ApplyStatusEffectsConsumeEffect(
                                                    new MobEffectInstance(ModMobEffects.CHAOS_SWAP_NORMAL, 200, 3),
                                                    1.0F
                                            )
                                    )
                                    .build()
                    )
    );
    public static final DeferredItem<TrackingBow> TRACKING_BOW = ITEMS.registerItem(
            "tracking_bow",
            props -> new TrackingBow(props.durability(384).stacksTo(1))
    );

    //统一注册方法
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}