package com.Lino.globalBoosters.listeners;

import com.Lino.globalBoosters.GlobalBoosters;
import com.Lino.globalBoosters.boosters.BoosterType;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
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
import org.bukkit.inventory.meta.Damageable;
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
    private final Map<Player, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<Player, ItemStack[]> savedArmor = new HashMap<>();

    public GameEventListener(GlobalBoosters plugin) {
        this.plugin = plugin;
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

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.MINING_SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false));
        }

        if (plugin.getBoosterManager().isBoosterActive(BoosterType.FARMING_FORTUNE)) {
            if (isCrop(block.getType())) {
                event.setDropItems(false);
                double multiplier = plugin.getConfigManager().getBoosterMultiplier(BoosterType.FARMING_FORTUNE);

                for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                    if (drop != null && drop.getType() != Material.AIR) {
                        ItemStack multipliedDrop = drop.clone();
                        multipliedDrop.setAmount((int) (drop.getAmount() * multiplier));
                        block.getWorld().dropItemNaturally(block.getLocation(), multipliedDrop);
                    }
                }
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

    private boolean isCrop(Material material) {
        switch (material) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case NETHER_WART:
            case SWEET_BERRY_BUSH:
            case COCOA:
            case SUGAR_CANE:
            case BAMBOO:
            case CACTUS:
            case MELON:
            case PUMPKIN:
                return true;
            default:
                return false;
        }
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
                name.equals("ELYTRA") || name.equals("SHIELD");
    }
}