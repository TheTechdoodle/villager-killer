package com.darkender.plugins.villagerkiller;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class VillagerKiller extends JavaPlugin implements Listener
{
    private boolean actionOnStartup;
    private boolean actionOnChunkLoad;
    private boolean actionOnSpawn;
    private String changeAction;
    private BukkitTask actionTimer = null;
    private Map<Villager.Profession, String> actions;
    
    private List<Villager.Profession> randomSelection;
    private final Random randomGenerator = new Random();
    
    @Override
    public void onEnable()
    {
        reload(getServer().getConsoleSender());
        getServer().getPluginManager().registerEvents(this, this);
        
        VillagerKillerCommand command = new VillagerKillerCommand(this);
        getCommand("villagerkiller").setTabCompleter(command);
        getCommand("villagerkiller").setExecutor(command);
        
        if(actionOnStartup)
        {
            cleanAllVillagers();
        }
    }
    
    public void reload(CommandSender sender)
    {
        saveDefaultConfig();
        reloadConfig();
        
        actionOnStartup = getConfig().getBoolean("action-on-startup");
        actionOnChunkLoad = getConfig().getBoolean("action-on-chunk-load");
        actionOnSpawn = getConfig().getBoolean("action-on-spawn");
        changeAction = getConfig().getString("change-action");
        
        if(getConfig().getBoolean("action-timer-enabled") && actionTimer == null)
        {
            BukkitRunnable timer = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    cleanAllVillagers();
                }
            };
            actionTimer = timer.runTaskTimer(this, 0L, getConfig().getLong("action-timer-interval"));
        }
        else if(!getConfig().getBoolean("action-timer-enabled") && actionTimer != null)
        {
            actionTimer.cancel();
            actionTimer = null;
        }
        
        actions = new HashMap<>();
        Map<String, Object> actionValues = getConfig().getConfigurationSection("actions").getValues(false);
        for(Map.Entry<String, Object> entry : actionValues.entrySet())
        {
            Villager.Profession prof;
            String value = (String) entry.getValue();
            try
            {
                // Ensure both the key and the value (if the value isn't "kill" or "random") are valid professions
                prof = Villager.Profession.valueOf(entry.getKey().toUpperCase());
            }
            catch(Exception e)
            {
                sender.sendMessage(ChatColor.RED + "\"" + entry.getKey() + "\" is not a valid profession");
                continue;
            }
    
            if(!value.equals("kill") && !value.equals("random"))
            {
                try
                {
                    Villager.Profession.valueOf(value.toUpperCase());
                }
                catch(Exception e)
                {
                    sender.sendMessage(ChatColor.RED + "\"" + value + "\" is not a valid profession");
                    continue;
                }
            }
            
            actions.put(prof, (String) entry.getValue());
        }
        randomSelection = new ArrayList<>();
        for(Villager.Profession profession : Villager.Profession.values())
        {
            if(!actions.containsKey(profession))
            {
                randomSelection.add(profession);
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "Reloaded!");
    }
    
    private Villager.Profession getRandomProfession()
    {
        return randomSelection.get(randomGenerator.nextInt(randomSelection.size()));
    }
    
    private void checkVillager(Villager villager)
    {
        if(actions.containsKey(villager.getProfession()))
        {
            String action = actions.get(villager.getProfession());
            if(action.equals("kill"))
            {
                villager.remove();
            }
            else
            {
                Villager.Profession profession;
                if(action.equals("random"))
                {
                    profession = getRandomProfession();
                }
                else
                {
                    profession = Villager.Profession.valueOf(action.toUpperCase());
                }
                villager.setProfession(profession);
                villager.setVillagerLevel(1);
                villager.setVillagerExperience(0);
            }
        }
    }
    
    public void cleanVillagers(Chunk chunk)
    {
        for(Entity e : chunk.getEntities())
        {
            if(e.getType() != EntityType.VILLAGER)
            {
                continue;
            }
            checkVillager((Villager) e);
        }
    }
    
    public void cleanAllVillagers()
    {
        for(World world : getServer().getWorlds())
        {
            for(Entity e : world.getEntitiesByClasses(Villager.class))
            {
                checkVillager((Villager) e);
            }
        }
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        if(actionOnChunkLoad)
        {
            cleanVillagers(event.getChunk());
        }
    }
    
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        if(actionOnSpawn && event.getEntityType() == EntityType.VILLAGER)
        {
            Villager villager = (Villager) event.getEntity();
            if(actions.containsKey(villager.getProfession()))
            {
                checkVillager(villager);
            }
        }
    }
    
    @EventHandler
    public void onVillagerCareerChange(VillagerCareerChangeEvent event)
    {
        if(!changeAction.equals("none") && actions.containsKey(event.getProfession()))
        {
            if(changeAction.equals("cancel"))
            {
                event.setCancelled(true);
            }
            else if(changeAction.equals("default"))
            {
                getServer().getScheduler().runTaskLater(this, () -> checkVillager(event.getEntity()), 1L);
            }
        }
    }
}
