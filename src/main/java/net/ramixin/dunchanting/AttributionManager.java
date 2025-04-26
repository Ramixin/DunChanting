package net.ramixin.dunchanting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.ramixin.dunchanting.items.ModItemComponents;
import net.ramixin.dunchanting.items.components.AttributionEntry;
import net.ramixin.dunchanting.items.components.Attributions;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerEntityDuck;

import java.util.*;

public class AttributionManager {

    private static final Map<UUID, Integer> pointsToDistribute = Collections.synchronizedMap(new HashMap<>());

    public static void redistribute(ItemStack stack, ServerWorld world) {
        for(int i = 0; i < 3; i++)
            redistribute(stack, world, i);
    }

    public static void redistribute(ItemStack stack, ServerWorld world, int slotId) {
        Attributions attributions = stack.get(ModItemComponents.ATTRIBUTIONS);
        if (attributions == null) return;
        List<AttributionEntry> entries = attributions.get(slotId);
        for(AttributionEntry entry : entries) {
            UUID uuid = entry.playerUUID();
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if((player instanceof ServerPlayerEntity serverPlayer)) {
                PlayerEntityDuck duck = (PlayerEntityDuck) serverPlayer;
                duck.dungeonEnchants$changeEnchantmentPoints(entry.points());
                int points = duck.dungeonEnchants$getEnchantmentPoints();
                ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(points));
                continue;
            }
            if(pointsToDistribute.containsKey(uuid)) pointsToDistribute.put(uuid, pointsToDistribute.get(uuid) + entry.points());
            else pointsToDistribute.put(uuid, entry.points());
        }
        entries.removeIf(unused -> true);
    }

    public static int popAttributions(UUID uuid) {
        if(!pointsToDistribute.containsKey(uuid)) return 0;
        return pointsToDistribute.remove(uuid);
    }

    public static void save(NbtCompound root) {
        NbtList entries = new NbtList();
        for(Map.Entry<UUID, Integer> entry : pointsToDistribute.entrySet()) {
            NbtCompound tag = new NbtCompound();
            tag.putUuid("uuid", entry.getKey());
            tag.putInt("points", entry.getValue());
            entries.add(tag);
        }
        root.put("attributions", entries);
    }

    public static void load(NbtCompound root) {
        NbtList entries = root.getList("attributions", 10);
        for(NbtElement entry : entries) {
            NbtCompound tag = (NbtCompound) entry;
            pointsToDistribute.put(tag.getUuid("uuid"), tag.getInt("points"));
        }
    }
}
