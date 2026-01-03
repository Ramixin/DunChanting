package net.ramixin.dunchanting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.ModUtil;
import net.ramixin.dunchanting.util.PlayerDuck;

public class ModCommands {

    private static final SimpleCommandExceptionType NEGATIVE_POINTS = new SimpleCommandExceptionType(
            Component.translatable("commands.player.enchantment_points.negative")
    );

    private static final SimpleCommandExceptionType PLAYER_NOT_FOUND = new SimpleCommandExceptionType(
            Component.translatable("commands.player.enchantment_points.player_not_found")
    );

    public static void register(CommandNode<CommandSourceStack> rootNode, CommandBuildContext registryAccess) {
        CommandNode<CommandSourceStack> enchanting = Commands.literal("enchanting").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN)).build();

        enchanting.addChild(createPointsCommand());
        enchanting.addChild(createItemCommand(registryAccess));

        rootNode.addChild(enchanting);
    }

    private static CommandNode<CommandSourceStack> createItemCommand(CommandBuildContext registryAccess) {
        CommandNode<CommandSourceStack> item = Commands.literal("item").build();

        item.addChild(createReRoll());
        item.addChild(createModify(registryAccess));
        item.addChild(createGildingCommand(registryAccess));

        return item;
    }

    private static CommandNode<CommandSourceStack> createGildingCommand(CommandBuildContext registryAccess) {
        CommandNode<CommandSourceStack> gilding = Commands.literal("gilding").build();

        gilding.addChild(createSetGilding(registryAccess));
        gilding.addChild(createRemoveGilding());

        return gilding;
    }

    private static LiteralCommandNode<CommandSourceStack> createRemoveGilding() {
        return Commands.literal("remove")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if(player == null) throw PLAYER_NOT_FOUND.create();
                    ItemStack stack = player.getMainHandItem();
                    stack.remove(ModDataComponents.GILDED);
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.player.enchanting.gilding.remove.success"), false);
                    return 1;
                }).build();
    }

    private static LiteralCommandNode<CommandSourceStack> createSetGilding(CommandBuildContext registryAccess) {
        return Commands.literal("set")
                .then(Commands.argument("enchantment", ResourceArgument.resource(registryAccess, Registries.ENCHANTMENT))
                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    Holder<Enchantment> entry = ResourceArgument.getEnchantment(ctx, "enchantment");
                                    int targetLevel = IntegerArgumentType.getInteger(ctx, "level");
                                    int levelToSet = Math.min(entry.value().getMaxLevel(), targetLevel);
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    if(player == null) throw PLAYER_NOT_FOUND.create();
                                    ItemStack stack = player.getMainHandItem();

                                    ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                                    ItemEnchantments.Mutable builder;
                                    if(enchantments == null || enchantments == ItemEnchantments.EMPTY) {
                                        builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                                    } else {
                                        builder = new ItemEnchantments.Mutable(enchantments);

                                        Gilded gilded = stack.get(ModDataComponents.GILDED);
                                        if(gilded != null)
                                            builder.removeIf(holder -> holder.value().equals(gilded.enchantmentEntry().value()));

                                    }
                                    builder.set(entry, levelToSet);
                                    stack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
                                    stack.set(ModDataComponents.GILDED, new Gilded(entry));
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.player.enchanting.gilding.set.success"), false);
                                    return 1;
                                })
                        )).build();
    }

    private static LiteralCommandNode<CommandSourceStack> createModify(CommandBuildContext registryAccess) {
        return Commands.literal("modify")
                .then(Commands.argument("slotId", IntegerArgumentType.integer(0, 2))
                        .then(Commands.argument("optionId", IntegerArgumentType.integer(0, 2))
                                .then(Commands.argument("enchantment", ResourceArgument.resource(registryAccess, Registries.ENCHANTMENT))
                                        .executes(ctx -> {
                                            Holder<Enchantment> entry = ResourceArgument.getEnchantment(ctx, "enchantment");
                                            int slotId = IntegerArgumentType.getInteger(ctx, "slotId");
                                            int optionId = IntegerArgumentType.getInteger(ctx, "optionId");
                                            ServerPlayer player = ctx.getSource().getPlayer();
                                            if(player == null) throw PLAYER_NOT_FOUND.create();
                                            ItemStack stack = player.getMainHandItem();
                                            EnchantmentOptions options = stack.getOrDefault(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
                                            EnchantmentOptions newOptions = options.withEnchantment(entry, slotId, optionId);
                                            stack.set(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, newOptions);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.player.enchanting.modify.success", slotId, optionId), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> createReRoll() {
        return Commands.literal("reroll")
                .executes(ModCommands::runReroll)
                .then(Commands.argument("level", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                        .executes(ctx -> runReroll(ctx, IntegerArgumentType.getInteger(ctx, "level")))
                ).build();
    }

    private static int runReroll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        if(player == null) throw PLAYER_NOT_FOUND.create();
        return runReroll(ctx, player.experienceLevel);
    }

    private static int runReroll(CommandContext<CommandSourceStack> ctx, int level) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        if(player == null) throw PLAYER_NOT_FOUND.create();
        ItemStack stack = player.getMainHandItem();
        stack.remove(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS);
        stack.remove(ModDataComponents.LOCKED_ENCHANTMENT_OPTIONS);
        stack.remove(ModDataComponents.SELECTED_ENCHANTMENTS);
        ModUtil.generateComponents(stack, ctx.getSource().getLevel(), level);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.player.enchantment_points.reroll.success"), false);
        return 1;
    }

    private static CommandNode<CommandSourceStack> createPointsCommand() {
        CommandNode<CommandSourceStack> points = Commands.literal("points").build();

        points.addChild(createGetPoints());
        points.addChild(createSetPoints());
        points.addChild(createAddPoints());

        return points;
    }

    private static LiteralCommandNode<CommandSourceStack> createGetPoints() {
        return Commands.literal("get")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            EntitySelector selector = context.getArgument("player", EntitySelector.class);
                            ServerPlayer player = selector.findSinglePlayer(context.getSource());
                            PlayerDuck duck = (PlayerDuck) player;
                            int points = duck.dungeonEnchants$getEnchantmentPoints();
                            context.getSource().sendSuccess(
                                    () -> Component.translatable(
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

    private static LiteralCommandNode<CommandSourceStack> createSetPoints() {
        return Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("points", IntegerArgumentType.integer())
                                .executes(
                                        context -> {
                                            EntitySelector selector = context.getArgument("player", EntitySelector.class);
                                            ServerPlayer player = selector.findSinglePlayer(context.getSource());
                                            PlayerDuck duck = (PlayerDuck) player;
                                            int requiredPoints = IntegerArgumentType.getInteger(context, "points");
                                            if(requiredPoints < 0) throw NEGATIVE_POINTS.create();
                                            duck.dungeonEnchants$setEnchantmentPoints(requiredPoints);
                                            int points = duck.dungeonEnchants$getEnchantmentPoints();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable(
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

    private static LiteralCommandNode<CommandSourceStack> createAddPoints() {
        return Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("points", IntegerArgumentType.integer())
                                .executes(
                                        context -> {
                                            EntitySelector selector = context.getArgument("player", EntitySelector.class);
                                            ServerPlayer player = selector.findSinglePlayer(context.getSource());
                                            PlayerDuck duck = (PlayerDuck) player;
                                            int deltaPoints = IntegerArgumentType.getInteger(context, "points");
                                            if(duck.dungeonEnchants$getEnchantmentPoints() + deltaPoints < 0) throw NEGATIVE_POINTS.create();
                                            duck.dungeonEnchants$changeEnchantmentPoints(deltaPoints);
                                            int points = duck.dungeonEnchants$getEnchantmentPoints();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable(
                                                            "commands.player.enchantment_points.add",
                                                            deltaPoints,
                                                            deltaPoints == 1 ? "" : "s",
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
