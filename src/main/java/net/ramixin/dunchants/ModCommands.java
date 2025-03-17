package net.ramixin.dunchants;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchants.util.ModUtils;
import net.ramixin.dunchants.util.PlayerEntityDuck;

public class ModCommands {

    private static final SimpleCommandExceptionType NEGATIVE_POINTS = new SimpleCommandExceptionType(
            Text.translatable("commands.player.enchantment_points.negative")
    );

    private static final SimpleCommandExceptionType PLAYER_NOT_FOUND = new SimpleCommandExceptionType(
            Text.translatable("commands.player.enchantment_points.player_not_found")
    );

    public static void register(CommandNode<ServerCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<ServerCommandSource> enchanting = CommandManager.literal("enchanting").requires(source -> source.hasPermissionLevel(2)).build();

        enchanting.addChild(createPointsCommand());
        enchanting.addChild(createItemCommand(registryAccess));

        rootNode.addChild(enchanting);
    }

    private static CommandNode<ServerCommandSource> createItemCommand(CommandRegistryAccess registryAccess) {
        CommandNode<ServerCommandSource> item = CommandManager.literal("item").build();

        item.addChild(createReRoll());
        item.addChild(createModify(registryAccess));

        return item;
    }

    private static LiteralCommandNode<ServerCommandSource> createModify(CommandRegistryAccess registryAccess) {
        return CommandManager.literal("modify")
                .then(CommandManager.argument("slotId", IntegerArgumentType.integer(0, 2))
                        .then(CommandManager.argument("optionId", IntegerArgumentType.integer(0, 2))
                                .then(CommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                                        .executes(ctx -> {
                                            RegistryEntry<Enchantment> entry = RegistryEntryReferenceArgumentType.getEnchantment(ctx, "enchantment");
                                            int slotId = IntegerArgumentType.getInteger(ctx, "slotId");
                                            int optionId = IntegerArgumentType.getInteger(ctx, "optionId");
                                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                                            if(player == null) throw PLAYER_NOT_FOUND.create();
                                            ItemStack stack = player.getMainHandStack();
                                            EnchantmentOptions options = stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
                                            EnchantmentOptions newOptions = options.modify(entry.getIdAsString(), slotId, optionId);
                                            stack.set(ModItemComponents.ENCHANTMENT_OPTIONS, newOptions);
                                            ctx.getSource().sendFeedback(() -> Text.translatable("commands.player.enchanting.modify.success", slotId, optionId), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .build();
    }

    private static LiteralCommandNode<ServerCommandSource> createReRoll() {
        return CommandManager.literal("reroll")
                .executes(ModCommands::runReroll)
                .then(CommandManager.argument("level", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                        .executes(ctx -> runReroll(ctx, IntegerArgumentType.getInteger(ctx, "level")))
                ).build();
    }

    private static int runReroll(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if(player == null) throw PLAYER_NOT_FOUND.create();
        return runReroll(ctx, player.experienceLevel);
    }

    private static int runReroll(CommandContext<ServerCommandSource> ctx, int level) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if(player == null) throw PLAYER_NOT_FOUND.create();
        ItemStack stack = player.getMainHandStack();
        ModUtils.generateEnchantmentOptions(stack, ctx.getSource().getWorld(), level);
        ctx.getSource().sendFeedback(() -> Text.translatable("commands.player.enchantment_points.reroll.success"), false);
        return 1;
    }

    private static CommandNode<ServerCommandSource> createPointsCommand() {
        CommandNode<ServerCommandSource> points = CommandManager.literal("points").build();

        points.addChild(createGet());
        points.addChild(createSet());
        points.addChild(createAdd());

        return points;
    }

    private static LiteralCommandNode<ServerCommandSource> createGet() {
        return CommandManager.literal("get")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            EntitySelector selector = context.getArgument("player", EntitySelector.class);
                            ServerPlayerEntity player = selector.getPlayer(context.getSource());
                            PlayerEntityDuck duck = (PlayerEntityDuck) player;
                            int points = duck.dungeonEnchants$getEnchantmentPoints();
                            context.getSource().sendFeedback(
                                    () -> Text.translatable(
                                            "commands.player.enchantment_points.get",
                                            player.getName(),
                                            points,
                                            points == 1 ? "" : "s"
                                    ),
                                    false
                            );
                            return points;
                        })
                ).build();
    }

    private static LiteralCommandNode<ServerCommandSource> createSet() {
        return CommandManager.literal("set")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("points", IntegerArgumentType.integer())
                                .executes(
                                        context -> {
                                            EntitySelector selector = context.getArgument("player", EntitySelector.class);
                                            ServerPlayerEntity player = selector.getPlayer(context.getSource());
                                            PlayerEntityDuck duck = (PlayerEntityDuck) player;
                                            int requiredPoints = IntegerArgumentType.getInteger(context, "points");
                                            if(requiredPoints < 0) throw NEGATIVE_POINTS.create();
                                            duck.dungeonEnchants$setEnchantmentPoints(requiredPoints);
                                            int points = duck.dungeonEnchants$getEnchantmentPoints();
                                            context.getSource().sendFeedback(
                                                    () -> Text.translatable(
                                                            "commands.player.enchantment_points.set",
                                                            player.getName(),
                                                            points
                                                    ),
                                                    false
                                            );
                                            ServerPlayNetworking.send(player, new EnchantmentPointsUpdateS2CPayload(points));
                                            return points;
                                        }
                                )
                        )
                ).build();
    }

    private static LiteralCommandNode<ServerCommandSource> createAdd() {
        return CommandManager.literal("add")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("points", IntegerArgumentType.integer())
                                .executes(
                                        context -> {
                                            EntitySelector selector = context.getArgument("player", EntitySelector.class);
                                            ServerPlayerEntity player = selector.getPlayer(context.getSource());
                                            PlayerEntityDuck duck = (PlayerEntityDuck) player;
                                            int deltaPoints = IntegerArgumentType.getInteger(context, "points");
                                            if(duck.dungeonEnchants$getEnchantmentPoints() + deltaPoints < 0) throw NEGATIVE_POINTS.create();
                                            duck.dungeonEnchants$changeEnchantmentPoints(deltaPoints);
                                            int points = duck.dungeonEnchants$getEnchantmentPoints();
                                            context.getSource().sendFeedback(
                                                    () -> Text.translatable(
                                                            "commands.player.enchantment_points.add",
                                                            points,
                                                            points == 1 ? "" : "s",
                                                            player.getName()
                                                    ),
                                                    false
                                            );
                                            ServerPlayNetworking.send(player, new EnchantmentPointsUpdateS2CPayload(points));
                                            return points;
                                        }
                                )
                        )
                ).build();
    }
}
