package com.Lino.globalBoosters.listeners;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameEventListener implements Listener {

    private final GlobalBoosters plugin;
    private final Random random = new Random();
    private final Map<Location, Long> recentlyPlacedBlocks = new HashMap<>();

    public GameEventListener(GlobalBoosters plugin) {
        this.plugin = plugin;
        // Clean up old entries every minute
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            recentlyPlacedBlocks.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() > 3000); // Remove after 3 seconds
        }, 1200L, 1200L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockGrow(BlockGrowEvent event) {
        if (plugin.getBoosterManager().isBoosterActive(BoosterType.PLANT_GROWTH) && !event.isCancelled()) {
            Block block = event.getBlock();

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (block.getBlockData() instanceof Ageable) {
                    Ageable ageable = (Ageable) block.getBlockData();
                    if (ageable.getAge() < ageable.getMaximumAge()) {
                        ageable.setAge(ageable.getMaximumAge());
                        block.setBlockData(ageable);
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.SPAWNER_RATE)) {
            double multiplier = plugin.getConfigManager().getBoosterMultiplier(BoosterType.SPAWNER_RATE);
            int extraMobs = (int) (multiplier - 1);

            if (extraMobs > 0) {
                Entity spawnedEntity = event.getEntity();
                EntityType entityType = spawnedEntity.getType();
                Location spawnLocation = spawnedEntity.getLocation();

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    for (int i = 0; i < extraMobs; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * 2;
                        double offsetZ = (random.nextDouble() - 0.5) * 2;
                        Location newLocation = spawnLocation.clone().add(offsetX, 0, offsetZ);

                        Entity newEntity = spawnLocation.getWorld().spawnEntity(newLocation, entityType);
                        if (newEntity instanceof LivingEntity) {
                            LivingEntity newLiving = (LivingEntity) newEntity;
                            newLiving.setMetadata("spawner_boosted", new FixedMetadataValue(plugin, true));
                        }
                    }
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        double multiplier = plugin.getBoosterManager().getMultiplier(BoosterType.EXP_MULTIPLIER);
        if (multiplier > 1.0) {
            event.setAmount((int) (event.getAmount() * multiplier));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (plugin.getBoosterManager().isBoosterActive(BoosterType.MOB_DROP)) {
            double multiplier = plugin.getConfigManager().getBoosterMultiplier(BoosterType.MOB_DROP);

            List<ItemStack> drops = event.getDrops();
            for (ItemStack drop : drops) {
                if (drop != null && drop.getType() != Material.AIR) {
                    drop.setAmount((int) (drop.getAmount() * multiplier));
                }
            }

            event.setDroppedExp((int) (event.getDroppedExp() * multiplier));
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH &&
                plugin.getBoosterManager().isBoosterActive(BoosterType.FISHING_LUCK)) {

            double multiplier = plugin.getConfigManager().getBoosterMultiplier(BoosterType.FISHING_LUCK);

            Entity caught = event.getCaught();
            if (caught instanceof org.bukkit.entity.Item) {
                org.bukkit.entity.Item item = (org.bukkit.entity.Item) caught;
                ItemStack stack = item.getItemStack();
                int newAmount = (int) (stack.getAmount() * multiplier);
                stack.setAmount(newAmount);
                item.setItemStack(stack);
            }

            int newExp = (int) (event.getExpToDrop() * multiplier);
            event.setExpToDrop(newExp);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Mining Speed - separate and independent
        if (plugin.getBoosterManager().isBoosterActive(BoosterType.MINING_SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        // Track placement of farmable blocks
        if (type == Material.SUGAR_CANE || type == Material.BAMBOO ||
                type == Material.CACTUS || type == Material.KELP ||
                type == Material.KELP_PLANT) {
            recentlyPlacedBlocks.put(block.getLocation(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFarmingFortuneCheck(BlockBreakEvent event) {
        // Completely separate handler for farming fortune
        if (!plugin.getBoosterManager().isBoosterActive(BoosterType.FARMING_FORTUNE)) {
            return; // Do absolutely nothing if not active
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Check if this block was recently placed
        if (recentlyPlacedBlocks.containsKey(block.getLocation())) {
            return; // Block was just placed, no bonus
        }

        // Check for mature crops only
        if (blockType == Material.WHEAT ||
                blockType == Material.CARROTS ||
                blockType == Material.POTATOES ||
                blockType == Material.BEETROOTS) {

            BlockData data = block.getBlockData();
            if (data instanceof Ageable) {
                Ageable ageable = (Ageable) data;
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    // Spawn bonus drops
                    spawnBonusCrop(player, blockType);
                }
            }
        } else if (blockType == Material.SUGAR_CANE ||
                blockType == Material.BAMBOO ||
                blockType == Material.CACTUS) {
            // Check all blocks in the column for recently placed
            boolean isNatural = true;

            // Check current block and blocks below
            Block checkBlock = block;
            while (checkBlock.getType() == blockType) {
                if (recentlyPlacedBlocks.containsKey(checkBlock.getLocation())) {
                    isNatural = false;
                    break;
                }
                checkBlock = checkBlock.getRelative(0, -1, 0);
            }

            // Check blocks above
            checkBlock = block.getRelative(0, 1, 0);
            while (checkBlock.getType() == blockType) {
                if (recentlyPlacedBlocks.containsKey(checkBlock.getLocation())) {
                    isNatural = false;
                    break;
                }
                checkBlock = checkBlock.getRelative(0, 1, 0);
            }

            if (isNatural) {
                spawnBonusCrop(player, blockType);
            }
        } else if (blockType == Material.MELON || blockType == Material.PUMPKIN) {
            // These don't need checks
            spawnBonusCrop(player, blockType);
        }
    }

    private void spawnBonusCrop(Player player, Material cropType) {
        double multiplier = plugin.getConfigManager().getBoosterMultiplier(BoosterType.FARMING_FORTUNE);
        if (multiplier <= 1.0) return;

        Material dropMaterial = null;
        int baseAmount = 1;

        switch (cropType) {
            case WHEAT:
                dropMaterial = Material.WHEAT;
                break;
            case CARROTS:
                dropMaterial = Material.CARROT;
                baseAmount = 2;
                break;
            case POTATOES:
                dropMaterial = Material.POTATO;
                baseAmount = 2;
                break;
            case BEETROOTS:
                dropMaterial = Material.BEETROOT;
                break;
            case SUGAR_CANE:
                dropMaterial = Material.SUGAR_CANE;
                break;
            case BAMBOO:
                dropMaterial = Material.BAMBOO;
                break;
            case CACTUS:
                dropMaterial = Material.CACTUS;
                break;
            case MELON:
                dropMaterial = Material.MELON_SLICE;
                baseAmount = 5;
                break;
            case PUMPKIN:
                dropMaterial = Material.PUMPKIN;
                break;
        }

        if (dropMaterial != null) {
            int extraAmount = (int) (baseAmount * (multiplier - 1));
            if (extraAmount > 0) {
                ItemStack bonus = new ItemStack(dropMaterial, extraAmount);
                player.getWorld().dropItemNaturally(player.getLocation(), bonus);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.COMBAT_DAMAGE)) {
            double damage = event.getDamage();
            event.setDamage(damage * plugin.getBoosterManager().getMultiplier(BoosterType.COMBAT_DAMAGE));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                plugin.getBoosterManager().isBoosterActive(BoosterType.NO_FALL_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.HUNGER_SAVER)) {
            if (event.getFoodLevel() < player.getFoodLevel()) {
                if (random.nextDouble() < plugin.getBoosterManager().getMultiplier(BoosterType.HUNGER_SAVER)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        if (!isArmor(item.getType())) {
            return;
        }

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.ARMOR_DURABILITY)) {
            if (random.nextDouble() < plugin.getBoosterManager().getMultiplier(BoosterType.ARMOR_DURABILITY)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.KEEP_INVENTORY)) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
                name.equals("ELYTRA") || name.equals("SHIELD");
    }
}