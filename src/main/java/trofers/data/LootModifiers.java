package trofers.data;

import io.github.fabricators_of_create.porting_lib.loot.GlobalLootModifierProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import trofers.Trofers;
import trofers.common.init.ModItems;
import trofers.common.loot.AddEntityTrophy;
import trofers.common.loot.RandomTrophyChanceCondition;
import trofers.common.trophy.Trophy;
import trofers.data.trophies.TinkersConstructTrophies;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LootModifiers extends GlobalLootModifierProvider {

    private final Trophies trophies;

    public LootModifiers(DataGenerator generator, Trophies trophies) {
        super(generator, Trofers.MODID);
        this.trophies = trophies;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void start() {
        Map<String, Map<EntityType<?>, Trophy>> trophies = new HashMap<>();
        for (Trophy trophy : this.trophies.trophies) {
            EntityType<?> entityType = trophy.entity().getType();
            String modid = Registry.ENTITY_TYPE.getKey(trophy.entity().getType()).getNamespace();

            if (!trophies.containsKey(modid)) {
                trophies.put(modid, new HashMap<>());
            }
            trophies.get(modid).put(entityType, trophy);
        }

        if (FabricLoader.getInstance().isModLoaded("tinkers_construct"))
            TinkersConstructTrophies.addExtraTrophies(trophies);

        for (String modId : trophies.keySet()) {
            LootItemCondition[] conditions = new LootItemCondition[]{
                    LootItemKilledByPlayerCondition.killedByPlayer().build(),
                    RandomTrophyChanceCondition.randomTrophyChance().build()
            };

            Map<ResourceLocation, ResourceLocation> trophyIds = trophies.get(modId).entrySet().stream().collect(Collectors.toMap(entry -> Registry.ENTITY_TYPE.getKey(entry.getKey()), entry -> entry.getValue().id()));
            AddEntityTrophy modifier = new AddEntityTrophy(conditions, ModItems.SMALL_PLATE.get(), trophyIds);

            String name = modId.equals("minecraft") ? "vanilla" : modId;
            name = name + "_trophies";
            add(name, modifier);
        }
    }
}
