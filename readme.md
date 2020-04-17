A simple plugin that can take actions on specific villager professions

Permissions:
 - villagerkiller.reload\
 Permission to reload the config

Commands:
 - /villagerkiller or /vk\
   Reloads the config

Config:
```yaml
# Whether the plugin should take action when enabled
action-on-startup: true

# Whether the plugin should take action on villagers in a chunk when it is loaded
action-on-chunk-load: true

# Whether the plugin should take action when a villager spawns
action-on-spawn: true

# The action to take when a villager changes to a profession that has an action set for it
# Valid options are "default" to run the regular action, "cancel" to cancel the change, or "none" to do nothing
change-action: default

# Whether to enable the action timer
# The other events (chunk-load, spawn, and change) *should* cover the bases but this is an assurance
# Every interval, it will check all loaded chunks for villagers and run the actions on them (if any)
action-timer-enabled: true

# The timing interval for the action timer in ticks (20 ticks = 1 second)
action-timer-interval: 20

# Actions for a particular village profession
# Valid professions can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Profession.html
# Valid options are:
# - "random" to change the villager to a different profession that doesn't have an action for it
# - "kill" to instantly kill the villager
# - <profession> (the name of another profession) to change the villager to that profession
actions:
  armorer: random
  fisherman: kill
```