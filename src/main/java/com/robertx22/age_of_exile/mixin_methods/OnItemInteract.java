package com.robertx22.age_of_exile.mixin_methods;

import com.robertx22.age_of_exile.database.data.currency.IItemAsCurrency;
import com.robertx22.age_of_exile.database.data.currency.loc_reqs.LocReqContext;
import com.robertx22.age_of_exile.database.data.profession.items.CraftedSoulItem;
import com.robertx22.age_of_exile.mmorpg.ForgeEvents;
import com.robertx22.age_of_exile.mmorpg.registers.common.items.SlashItems;
import com.robertx22.age_of_exile.saveclasses.item_classes.GearItemData;
import com.robertx22.age_of_exile.saveclasses.stat_soul.StatSoulData;
import com.robertx22.age_of_exile.saveclasses.stat_soul.StatSoulItem;
import com.robertx22.age_of_exile.uncommon.datasaving.StackSaving;
import com.robertx22.age_of_exile.uncommon.interfaces.data_items.ISalvagable;
import com.robertx22.age_of_exile.uncommon.utilityclasses.PlayerUtils;
import com.robertx22.age_of_exile.vanilla_mc.items.SoulMakerItem;
import com.robertx22.age_of_exile.vanilla_mc.items.misc.RarityStoneItem;
import com.robertx22.library_of_exile.utils.SoundUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemStackedOnOtherEvent;

public class OnItemInteract {

    public static void register() {

        ForgeEvents.registerForgeEvent(ItemStackedOnOtherEvent.class, x -> {
            ItemStack cursor = x.getStackedOnItem();
            ItemStack stack = x.getCarriedItem();
            Player player = x.getPlayer();

            if (player.level().isClientSide) {
                return;
            }


            boolean success = false;


            if (StackSaving.JEWEL.has(stack)) {
                var data = StackSaving.JEWEL.loadFrom(stack);

                if (data.uniq.isCraftableUnique()) {
                    ItemStack cost = data.uniq.getStackNeededForUpgrade();

                    if (cost.getItem() == cursor.getItem()) {
                        if (cursor.getCount() >= cost.getCount()) {
                            if (data.uniq.getCraftedTier().canUpgradeMore()) {
                                data.uniq.upgradeUnique(data);

                                StackSaving.JEWEL.saveTo(stack, data);
                                cursor.shrink(cost.getCount());

                                SoundUtils.ding(player.level(), player.blockPosition());
                                SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.ANVIL_USE, 1, 1);
                                x.setCanceled(true);

                                return;
                            }
                        }
                    }
                }
            }

            if (stack.isDamaged() && cursor.getItem() instanceof RarityStoneItem) {

                GearItemData gear = StackSaving.GEARS.loadFrom(stack);

                if (gear == null) {
                    return;
                }

                RarityStoneItem essence = (RarityStoneItem) cursor.getItem();

                SoundUtils.playSound(player, SoundEvents.ANVIL_USE, 1, 1);

                int repair = essence.getTotalRepair();

                stack.setDamageValue(stack.getDamageValue() - repair);
                success = true;

            } else if (cursor.getItem() instanceof StatSoulItem || cursor.getItem() instanceof CraftedSoulItem) {
                StatSoulData data = StackSaving.STAT_SOULS.loadFrom(cursor);
                if (cursor.getItem() instanceof CraftedSoulItem cs) {
                    data = cs.getSoul(cursor);
                }

                if (data != null) {
                    if (data.canInsertIntoStack(stack)) {
                        data.insertAsUnidentifiedOn(stack);
                        success = true;
                    }
                }
            } else if (cursor.getItem() instanceof IItemAsCurrency) {
                LocReqContext ctx = new LocReqContext(player, stack, cursor);
                if (ctx.effect.canItemBeModified(ctx)) {
                    ItemStack result = ctx.effect.modifyItem(ctx).stack;
                    stack.shrink(1);

                    PlayerUtils.giveItem(result, player);
                    //slot.set(result);
                    success = true;
                }
            } else if (cursor.getItem() == SlashItems.SALVAGE_HAMMER.get()) {
                ISalvagable data = ISalvagable.load(stack);

                if (data == null && stack.getItem() instanceof ISalvagable) {
                    data = (ISalvagable) stack.getItem();
                }

                if (data != null) {
                    if (data.isSalvagable()) {
                        SoundUtils.playSound(player, SoundEvents.ANVIL_USE, 1, 1);

                        stack.shrink(1);
                        data.getSalvageResult(stack)
                                .forEach(e -> PlayerUtils.giveItem(e, player));
                        //ci.setReturnValue(ItemStack.EMPTY);
                        //stack.shrink(1000);
                        //ci.cancel();
                        x.setCanceled(true);
                        return;
                    }
                }
            } else if (cursor.getItem() == SlashItems.SOCKET_EXTRACTOR.get()) {

                GearItemData gear = StackSaving.GEARS.loadFrom(stack);

                if (gear != null) {
                    if (gear.sockets != null && gear.sockets.getSocketed().size() > 0) {
                        try {
                            ItemStack gem = new ItemStack(gear.sockets.getSocketed().get(0)
                                    .getGem()
                                    .getItem());
                            gear.sockets.getSocketed().remove(0);
                            StackSaving.GEARS.saveTo(stack, gear);
                            PlayerUtils.giveItem(gem, player);
                            x.setCanceled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }


                }
            } else if (cursor.getItem() instanceof SoulMakerItem se) {

                GearItemData gear = StackSaving.GEARS.loadFrom(stack);

                if (gear != null) {
                    try {

                        if (se.canExtract(gear)) {
                            StatSoulData soul = new StatSoulData();
                            soul.slot = gear.GetBaseGearType().getGearSlot().GUID();
                            soul.gear = gear;

                            ItemStack soulstack = soul.toStack();

                            SoundUtils.playSound(player, SoundEvents.EXPERIENCE_ORB_PICKUP);

                            stack.shrink(1);
                            cursor.shrink(1);
                            PlayerUtils.giveItem(soulstack, player);
                            x.setCanceled(true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;


                }
            } else if (cursor.is(SlashItems.SOUL_CLEANER.get())) {

                GearItemData gear = StackSaving.GEARS.loadFrom(stack);

                if (gear != null) {
                    try {
                        stack.getOrCreateTag().remove(StackSaving.GEARS.GUID());
                        cursor.shrink(1);
                        x.setCanceled(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;


                }
            }

            if (success) {
                SoundUtils.ding(player.level(), player.blockPosition());
                SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.ANVIL_USE, 1, 1);

                x.setCanceled(true);
                cursor.shrink(1);
                //stack.shrink(1000);
                //ci.setReturnValue(ItemStack.EMPTY);
                //ci.cancel();
            }


        });
    }


}
