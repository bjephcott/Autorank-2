package me.armar.plugins.autorank.pathbuilder.requirement;

import me.armar.plugins.autorank.language.Lang;
import me.armar.plugins.autorank.statsmanager.StatsPlugin;
import me.armar.plugins.autorank.statsmanager.query.StatisticQuery;
import me.armar.plugins.autorank.statsmanager.query.parameter.ParameterType;
import me.staartvin.plugins.pluginlibrary.Library;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BlocksPlacedRequirement extends AbstractRequirement {

    BlocksPlacedWrapper wrapper = null;

    @Override
    public String getDescription() {
        final ItemStack item = wrapper.getItem();

        final StringBuilder arg = new StringBuilder("" + wrapper.getBlocksPlaced());

        // No material was given.
        if (item == null) {
            arg.append(" blocks");
        } else {
            // If we have a display name, use that instead.
            if (wrapper.getDisplayName() != null) {
                arg.append(" ").append(wrapper.getDisplayName());
            } else {
                arg.append(" ").append(item.getType().name().replace("_", " ").toLowerCase());
            }
        }

        String lang = Lang.PLACED_BLOCKS_REQUIREMENT.getConfigValue(arg.toString());

        // Check if this requirement is world-specific
        if (this.isWorldSpecific()) {
            lang = lang.concat(" (in world '" + this.getWorld() + "')");
        }

        return lang;
    }

    @Override
    public String getProgressString(UUID uuid) {

        int progress = 0;

        if (wrapper.getItem() == null) {
            // No material was given, so only check the number of blocks placed.
            progress = getStatsPlugin().getNormalStat(StatsPlugin.StatType.TOTAL_BLOCKS_PLACED, uuid,
                    StatisticQuery.makeStatisticQuery(ParameterType.WORLD.getKey(), this.getWorld()));
        } else {
            progress = getStatsPlugin().getNormalStat(StatsPlugin.StatType.BLOCKS_PLACED, uuid,
                    StatisticQuery.makeStatisticQuery(ParameterType.WORLD.getKey(), this.getWorld(),
                            ParameterType.BLOCK_TYPE.getKey(), wrapper.getItem().getType()
                                    .name()));
        }

        return progress + "/" + wrapper.getBlocksPlaced();
    }

    @Override
    protected boolean meetsRequirement(UUID uuid) {
        if (!getStatsPlugin().isEnabled())
            return false;

        int progress = 0;

        if (wrapper.getItem() == null) {
            // No material was given, so only check the number of blocks placed.
            progress = getStatsPlugin().getNormalStat(StatsPlugin.StatType.TOTAL_BLOCKS_PLACED, uuid,
                    StatisticQuery.makeStatisticQuery(ParameterType.WORLD.getKey(), this.getWorld()));
        } else {
            progress = getStatsPlugin().getNormalStat(StatsPlugin.StatType.BLOCKS_PLACED, uuid,
                    StatisticQuery.makeStatisticQuery(ParameterType.WORLD.getKey(), this.getWorld(),
                            ParameterType.BLOCK_TYPE.getKey(), wrapper.getItem().getType()
                                    .name()));
        }

        return progress >= wrapper.getBlocksPlaced();
    }

    @Override
    public boolean initRequirement(final String[] options) {

        // Add dependency
        addDependency(Library.STATZ);

        String materialName = null;
        int amount = 1;

        String displayName = null;
        boolean useDisplayName = false;

        if (options.length == 1) {
            amount = Integer.parseInt(options[0].trim());
        }
        if (options.length > 1) {
            materialName = options[0].trim().toUpperCase().replace(" ", "_");
            amount = Integer.parseInt(options[1].trim());
        }
        if (options.length > 2) {
            // Displayname
            displayName = options[2];
        }
        if (options.length > 3) {
            // use display name?
            useDisplayName = (options[3].equalsIgnoreCase("true"));
        }

        ItemStack itemStack = null;

        // If a material was given, check if it is valid and create an item stack
        if (materialName != null) {

            Material matchedMaterial = Material.matchMaterial(materialName);

            if (matchedMaterial == null) {
                this.registerWarningMessage("Material '" + materialName + "' is not a valid material.");
                return false;
            }

            itemStack = new ItemStack(matchedMaterial, amount);
        }

        // If no material is given, the item stack is null.

        wrapper = new BlocksPlacedWrapper(itemStack, displayName, false, useDisplayName);

        wrapper.setBlocksPlaced(amount);

        if (amount < 0) {
            this.registerWarningMessage("Amount is not provided or smaller than 0.");
            return false;
        }

        if (wrapper == null) {
            this.registerWarningMessage("No valid block provided.");
            return false;
        }

        return true;
    }
}

class BlocksPlacedWrapper extends ItemWrapper {

    private int blocksPlaced; // How many items does the player need to place?

    public BlocksPlacedWrapper(ItemStack item, String displayName, boolean showShortValue, boolean useDisplayName) {
        super(item, displayName, showShortValue, useDisplayName);
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(final int blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
    }
}
