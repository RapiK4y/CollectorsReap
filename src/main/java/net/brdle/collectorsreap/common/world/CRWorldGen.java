package net.brdle.collectorsreap.common.world;

import net.brdle.collectorsreap.Util;
import net.brdle.collectorsreap.CollectorsReap;
import net.brdle.collectorsreap.common.block.CRBlocks;
import net.brdle.collectorsreap.common.block.PomegranateBushBlock;
import net.brdle.collectorsreap.common.config.CRConfig;
import net.brdle.collectorsreap.data.CRBlockTags;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import vectorwing.farmersdelight.common.registry.ModBiomeFeatures;
import vectorwing.farmersdelight.common.world.WildCropGeneration;
import vectorwing.farmersdelight.common.world.configuration.WildCropConfiguration;
import java.util.List;

public class CRWorldGen extends WildCropGeneration {
	public static Holder<ConfiguredFeature<WildCropConfiguration, ?>> FEATURE_PATCH_PORTOBELLO;
	public static Holder<PlacedFeature> PATCH_PORTOBELLO;
	public static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> FEATURE_PATCH_LIME_BUSH;
	public static Holder<PlacedFeature> PATCH_LIME_BUSH;
	public static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> FEATURE_PATCH_WILD_POMEGRANATE;
	public static Holder<PlacedFeature> PATCH_WILD_POMEGRANATE;


	private static Holder<PlacedFeature> registerPlacement(ResourceLocation id, Holder<? extends ConfiguredFeature<?, ?>> feature, PlacementModifier... modifiers) {
		return BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id, new PlacedFeature(Holder.hackyErase(feature), List.of(modifiers)));
	}

	private static Holder<PlacedFeature> registerConditionalPlacement(ResourceLocation id, Holder<? extends ConfiguredFeature<?, ?>> feature, boolean condition, PlacementModifier... modifiers) {
		return condition ? registerPlacement(id, feature, modifiers) : registerPlacement(id, feature, BlockPredicateFilter.forPredicate(BlockPredicate.not(BlockPredicate.alwaysTrue())));
	}

	public static void registerGeneration() {
		FEATURE_PATCH_PORTOBELLO = register(Util.rl(CollectorsReap.MODID, "patch_portobello"),
			ModBiomeFeatures.WILD_CROP.get(),
			portobelloColonyConfig(CRBlocks.PORTOBELLO_COLONY.get(), CRBlocks.PORTOBELLO.get(), BlockPredicate.matchesTag(BLOCK_BELOW, CRBlockTags.PORTOBELLO_SPAWNS)));

		PATCH_PORTOBELLO = registerConditionalPlacement(Util.rl(CollectorsReap.MODID, "patch_portobello"),
			FEATURE_PATCH_PORTOBELLO,
			CRConfig.verify("portobello") && CRConfig.CHANCE_PORTOBELLO.get() > 0, RarityFilter.onAverageOnceEvery(CRConfig.CHANCE_PORTOBELLO.get()), CountPlacement.of(1), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

		FEATURE_PATCH_LIME_BUSH = register(Util.rl(CollectorsReap.MODID, "patch_lime_bush"),
			RandomPatchFeature.RANDOM_PATCH,
			limeBushConfig(CRBlocks.LIME_BUSH.get(), 10, 5, BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.DIRT))
		);

		PATCH_LIME_BUSH = registerConditionalPlacement(Util.rl(CollectorsReap.MODID, "patch_lime_bush"),
			FEATURE_PATCH_LIME_BUSH,
			CRConfig.verify("lime") && CRConfig.CHANCE_LIME_BUSH.get() > 0, RarityFilter.onAverageOnceEvery(CRConfig.CHANCE_LIME_BUSH.get()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

		FEATURE_PATCH_WILD_POMEGRANATE = register(Util.rl(CollectorsReap.MODID, "patch_wild_pomegranate"),
			RandomPatchFeature.RANDOM_PATCH,
			pomegranateBushConfig(CRBlocks.POMEGRANATE_BUSH.get()
				.defaultBlockState().setValue(PomegranateBushBlock.AGE, PomegranateBushBlock.MAX_AGE),
				10, 10, BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.NYLIUM))
		);

		PATCH_WILD_POMEGRANATE = registerConditionalPlacement(Util.rl(CollectorsReap.MODID, "patch_wild_pomegranate"),
			FEATURE_PATCH_WILD_POMEGRANATE,
			CRConfig.verify("pomegranate") && CRConfig.CHANCE_WILD_POMEGRANATE.get() > 0, RarityFilter.onAverageOnceEvery(CRConfig.CHANCE_WILD_POMEGRANATE.get()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
	}

	public static RandomPatchConfiguration pomegranateBushConfig(BlockState block, int tries, int xzSpread, BlockPredicate plantedOn) {
		return new RandomPatchConfiguration(tries, xzSpread, 3, PlacementUtils.filtered(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block)), BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, plantedOn)));
	}

	public static RandomPatchConfiguration limeBushConfig(Block block, int tries, int xzSpread, BlockPredicate plantedOn) {
		return new RandomPatchConfiguration(tries, xzSpread, 3, PlacementUtils.filtered(CRFeatures.BUSH_BLOCK.get(),
			new SimpleBlockConfiguration(BlockStateProvider.simple(block)), BlockPredicate.allOf(plantedOn, BlockPredicate.ONLY_IN_AIR_PREDICATE)));
	}

	public static WildCropConfiguration portobelloColonyConfig(Block colonyBlock, Block secondaryBlock, BlockPredicate plantedOn) {
		return new WildCropConfiguration(32, 3, 3, colonyBlockConfig(colonyBlock, plantedOn), plantBlockConfig(secondaryBlock, plantedOn), null);
	}
}