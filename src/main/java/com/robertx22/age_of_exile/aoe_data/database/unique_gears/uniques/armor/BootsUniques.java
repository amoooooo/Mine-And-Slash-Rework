package com.robertx22.age_of_exile.aoe_data.database.unique_gears.uniques.armor;

import com.robertx22.age_of_exile.aoe_data.database.ailments.Ailments;
import com.robertx22.age_of_exile.aoe_data.database.base_gear_types.BaseGearTypes;
import com.robertx22.age_of_exile.aoe_data.database.stats.Stats;
import com.robertx22.age_of_exile.aoe_data.database.unique_gears.UniqueGearBuilder;
import com.robertx22.age_of_exile.database.data.StatMod;
import com.robertx22.age_of_exile.database.data.stats.effects.defense.MaxElementalResist;
import com.robertx22.age_of_exile.database.data.stats.types.ailment.AilmentChance;
import com.robertx22.age_of_exile.database.data.stats.types.gear_base.GearDefense;
import com.robertx22.age_of_exile.database.data.stats.types.generated.ElementalResist;
import com.robertx22.age_of_exile.uncommon.enumclasses.Elements;
import com.robertx22.age_of_exile.uncommon.enumclasses.ModType;
import com.robertx22.library_of_exile.registry.ExileRegistryInit;

import java.util.Arrays;

public class BootsUniques implements ExileRegistryInit {
    @Override
    public void registerAll() {

        UniqueGearBuilder.of("madness_pursuit", "Pursuit of Madness", BaseGearTypes.CLOTH_BOOTS)
                .setReplacesName()
                .stats(Arrays.asList(
                        new StatMod(25, 100, GearDefense.getInstance(), ModType.PERCENT),
                        new StatMod(50, 100, new ElementalResist(Elements.Chaos), ModType.FLAT),
                        new StatMod(-25, -25, new ElementalResist(Elements.Cold), ModType.FLAT),
                        new StatMod(-25, -25, new ElementalResist(Elements.Lightning), ModType.FLAT),
                        new StatMod(-25, -25, new ElementalResist(Elements.Fire), ModType.FLAT)
                ))
                .build();

        UniqueGearBuilder.of("fire_step", "Flaming Steps", BaseGearTypes.LEATHER_BOOTS)
                .keepsBaseName()
                .stat(GearDefense.getInstance().mod(50, 100).percent())
                .stat(new AilmentChance(Ailments.BURN).mod(5, 10))
                .stat(Stats.ELEMENTAL_DAMAGE.get(Elements.Fire).mod(20, 20))
                .stat(new MaxElementalResist(Elements.Fire).mod(5, 5))
                .build();

    }
}
