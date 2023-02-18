package net.brdle.collectorsreap.common;

import net.brdle.collectorsreap.CollectorsReap;
import net.brdle.collectorsreap.Util;
import net.brdle.collectorsreap.common.block.CRBlocks;
import net.brdle.collectorsreap.common.config.CRConfig;
import net.brdle.collectorsreap.common.effect.CREffects;
import net.brdle.collectorsreap.common.item.CRItems;
import net.brdle.collectorsreap.data.CREntityTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid=CollectorsReap.MODID)
public class ForgeEvents {

	@SubscribeEvent
	public static void onMissingMappings(MissingMappingsEvent e) {
		if (e.getRegistry() == ForgeRegistries.ITEMS) {
			for (var map : e.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), CollectorsReap.MODID)) {
				var remap = Util.cr(map.getKey().getPath().replace("lemon", "lime"));
				if (ForgeRegistries.ITEMS.containsKey(remap)) {
					map.remap(ForgeRegistries.ITEMS.getValue(remap));
				} else {
					map.warn();
				}
			}
		} else if (e.getRegistry() == ForgeRegistries.BLOCKS) {
			for (var map : e.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), CollectorsReap.MODID)) {
				String path = map.getKey().getPath();
				if (path.equalsIgnoreCase("medium_lime_bush") || path.equalsIgnoreCase("medium_lemon_bush")) {
					map.remap(CRBlocks.LIME_BUSH.get());
				} else {
					ResourceLocation remap = Util.cr(path.replace("lemon", "lime"));
					if (ForgeRegistries.BLOCKS.containsKey(remap)) {
						map.remap(ForgeRegistries.BLOCKS.getValue(remap));
					} else {
						map.warn();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onWanderingTrader(WandererTradesEvent e) {
		if (CRConfig.verify(CRItems.LIME) && CRConfig.verify(CRItems.LIME_SEEDS)) {
			e.getGenericTrades().add((ent, r) -> {
				return new MerchantOffer(new ItemStack(Items.EMERALD, 1), Util.gs(CRItems.LIME_SEEDS), 5, 1, 1);
			});
		}
	}

	@SubscribeEvent
	public static void onCorrode(LivingAttackEvent e) {
		if (e.getEntity().hasEffect(CREffects.CORROSION.get()) && e.getSource().getEntity() instanceof Player p) {
			InteractionHand hand = p.getUsedItemHand();
			ItemStack stack = p.getItemInHand(hand);
			if (stack.isDamageableItem()) {
				int damage = Objects.requireNonNull(e.getEntity().getEffect(CREffects.CORROSION.get())).getAmplifier();
				stack.hurtAndBreak(damage, p, en -> en.broadcastBreakEvent(hand));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onCorrodeProjectile(ProjectileImpactEvent e) {
		if (e.getRayTraceResult().getType() == HitResult.Type.ENTITY &&
			((EntityHitResult) e.getRayTraceResult()).getEntity() instanceof LivingEntity victim &&
			victim.hasEffect(CREffects.CORROSION.get())) {
			Projectile proj = e.getProjectile();
			if (proj.getType().is(CREntityTags.CORROSION_IMMUNE)) {
				return;
			}
			e.setCanceled(true);
			if (!proj.getLevel().isClientSide() && proj.getLevel() instanceof ServerLevel server) {
				for (int i = 0; i < 3; i++) {
					server.sendParticles(CRParticleTypes.ACID.get(), proj.getRandomX(0.3D), proj.getRandomY(), proj.getRandomZ(0.3D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
				}
				victim.playSound(SoundEvents.REDSTONE_TORCH_BURNOUT, 0.4F, 1.1F);
				if (proj instanceof ThrownTrident trident) {
					trident.tridentItem.hurt(5 * Objects.requireNonNull(victim.getEffect(CREffects.CORROSION.get())).getAmplifier(), trident.getLevel().getRandom(), null);
				} else {
					proj.discard();
					proj.gameEvent(GameEvent.ENTITY_DIE);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onVolatile(LivingAttackEvent e) {
		LivingEntity victim = e.getEntity();
		if (victim != null &&
			!victim.getLevel().isClientSide() &&
			victim.getLevel() instanceof ServerLevel server &&
			e.getSource().getEntity() != null &&
			e.getSource().getEntity() instanceof LivingEntity attacker &&
			attacker.hasEffect(CREffects.VOLATILITY.get()) &&
			!(attacker instanceof Player p && p.getCooldowns().isOnCooldown(p.getMainHandItem().getItem()))) {
			List<LivingEntity> nearby = server.getNearbyEntities(LivingEntity.class,
				TargetingConditions.DEFAULT.selector((living) -> (living != attacker && living != victim)),
				victim, victim.getBoundingBox().inflate(5.0D, 2.0D, 5.0D)).stream().limit(4).toList();
			if (nearby.size() == 0) {
				return;
			} else if (attacker instanceof Player p) {
				p.getCooldowns().addCooldown(p.getMainHandItem().getItem(), 40);
			}
			server.sendParticles(CRParticleTypes.SHOCKWAVE.get(), victim.getX(), victim.getY(), victim.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
			for (LivingEntity ent : nearby) {
				Vec3 vec32 = ent.getEyePosition().subtract(victim.position().add(0.0D, 1.0F, 0.0D)).normalize();
				ent.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.15F, 1.75F);
				ent.hurt(e.getSource(), (e.getAmount() * 0.65F) / nearby.size());
				double d1 = (1.0D - ent.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) * 0.75D;
				ent.push(vec32.x() * d1, vec32.y() * d1 * 0.35D, vec32.z() * d1);
			}
		}
	}
}