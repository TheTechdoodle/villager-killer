package com.darkender.plugins.villagerkiller;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class VillagerKillerCommand implements CommandExecutor, TabCompleter
{
    private final VillagerKiller villagerKiller;
    private final List<String> empty = new ArrayList<>();
    
    public VillagerKillerCommand(VillagerKiller villagerKiller)
    {
        this.villagerKiller = villagerKiller;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        villagerKiller.reload(sender);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return empty;
    }
}
