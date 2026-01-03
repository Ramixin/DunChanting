package net.ramixin.dunchanting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ramixin.dunchanting.items.components.AttributionEntry;
import net.ramixin.dunchanting.items.components.Attributions;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerDuck;

import java.util.*;

public class AttributionManager {

    private static final Map<UUID, Integer> pointsToDistribute = Collections.synchronizedMap(new HashMap<>());

    public static void redistribute(ItemStack stack, ServerLevel world) {
        for(int i = 0; i < 4; i++)
            redistribute(stack, world, i);
    }

    public static void redistribute(ItemStack stack, ServerLevel world, int slotId) {
        Attributions attributions = stack.get(ModDataComponents.ATTRIBUTIONS);
        if (attributions == null) return;
        List<AttributionEntry> entries;
        if(slotId == 3)
            entries = attributions.persistent();
        else
            entries = attributions.get(slotId);
        for(AttributionEntry entry : entries) {
            UUID uuid = entry.playerUUID();
            Player player = world.getPlayerByUUID(uuid);
            if((player instanceof ServerPlayer serverPlayer)) {
                PlayerDuck duck = (PlayerDuck) serverPlayer;
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

    public static void save(CompoundTag root) {
        ListTag entries = new ListTag();
        for(Map.Entry<UUID, Integer> entry : pointsToDistribute.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.store("uuid", UUIDUtil.CODEC, entry.getKey());
            tag.putInt("points", entry.getValue());
            entries.add(tag);
        }
        root.put("attributions", entries);
    }

    public static void load(CompoundTag root) {
        ListTag entries = root.getList("attributions").orElseThrow() ;
        for(Tag entry : entries) {
            CompoundTag tag = (CompoundTag) entry;
            UUID uuid = tag.read("uuid", UUIDUtil.CODEC).orElseThrow();
            pointsToDistribute.put(uuid, tag.getInt("points").orElseThrow() );
        }
    }
}