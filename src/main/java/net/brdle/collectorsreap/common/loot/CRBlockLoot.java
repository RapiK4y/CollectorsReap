package net.brdle.collectorsreap.common.loot;

import net.brdle.collectorsreap.common.block.PortobelloColonyBlock;
import net.brdle.collectorsreap.common.block.CRBlocks;
import net.brdle.collectorsreap.common.item.CRItems;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class CRBlockLoot extends BlockLoot {

    @Override
    protected void addTables() {
        this.dropSelf(CRBlocks.PORTOBELLO.get());
        this.empty(CRBlocks.PORTOBELLO_QUICHE);
        this.empty(CRBlocks.LEMON_PIE);
        this.dropSelf(CRBlocks.SMALL_LEMON_BUSH.get());
        this.dropOther(CRBlocks.MEDIUM_LEMON_BUSH.get(), CRItems.LEMON_SEEDS.get());
        this.dropOther(CRBlocks.LEMON_BUSH.get(), CRItems.LEMON_SEEDS.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return CRBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)
            .filter(b -> !(b instanceof PortobelloColonyBlock))
            ::iterator;
    }

    public void empty(RegistryObject<Block> block) {
        this.add(block.get(), LootTable.lootTable());
    }
}
