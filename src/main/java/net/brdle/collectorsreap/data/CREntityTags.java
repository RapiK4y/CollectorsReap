package net.brdle.collectorsreap.data;

import net.brdle.collectorsreap.CollectorsReap;
import net.brdle.collectorsreap.Util;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class CREntityTags {

	// Forge

	// VD
	public static final TagKey<EntityType<?>> CORROSION_IMMUNE = vd("corrosion_immune");

	private static TagKey<EntityType<?>> vd(String name) {
		return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, Util.rl(CollectorsReap.MODID, name));
	}
}