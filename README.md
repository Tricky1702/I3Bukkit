# I3Bukkit

Bukkit to Intermud3 bridge.

# Commands
```
commands:
  intermud3:
    aliases: [ i3 ]
    description: Regular Intermud3 commands
    usage: |
        /<command> emote <channel|.> <emote> - sends an emote to an I3 channel, use '.' for the last channel used
        /<command> msg <channel|.> <message> - sends a message to an I3 channel, use '.' for the last channel used
        /<command> tunein|tuneout <channel> - tunein or tuneout of an I3 channel
        /<command> alias <alias> <channel> - alias an I3 channel
        /<command> unalias <alias> - remove an I3 channel alias
        /<command> list - list I3 channels you are listening to
        /<command> available - list available I3 channels
        /<command> aliases - list your aliases
  i3admin:
    aliases: [ i3a ]
    description: Admin Intermud3 commands
    usage: |
        /<command> channels - list I3 channels being listened to
        /<command> channels aliases - list I3 channel aliases
        /<command> channels tunein|tuneout <channel> - set default I3 channels
        /<command> channels alias <alias> <channel> - set default aliases for I3 channels
        /<command> channels unalias <alias> - remove a default alias
        /<command> connect - connect to the preferred router
        /<command> debug - output debug information
        /<command> debug <on|off|switch> - turn on, off or switch debug state
        /<command> disconnect - disconnect from the router
        /<command> reload - reload all configuration files
```

# Permissions
```
permissions:
  intermud3.*:
    default: op
    description: Allows the use of all /intermud3 commands
    children:
      intermud3.admin.*: true
      intermud3.channel: true
      intermud3.help: true
      intermud3.use: true
  intermud3.admin.*:
    description: Allows the use of all admin /intermud3 commands
    children:
      intermud3.admin.channels: true
      intermud3.admin.channels.alias: true
      intermud3.admin.channels.add: true
      intermud3.admin.channels.modify: true
      intermud3.admin.channels.remove: true
      intermud3.admin.channels.tune: true
      intermud3.admin.connect: true
      intermud3.admin.debug: true
      intermud3.admin.disconnect: true
      intermud3.admin.reload: true
  intermud3.admin.channels:
    description: Allows the use of /intermud3 channels
  intermud3.admin.channels.alias:
    description: Allows the use of /intermud3 channels alias|unalias
  intermud3.admin.channels.add:
    description: Allows the use of /intermud3 channels add
  intermud3.admin.channels.modify:
    description: Allows the use of /intermud3 channels modify
  intermud3.admin.channels.remove:
    description: Allows the use of /intermud3 channels remove
  intermud3.admin.channels.tune:
    description: Allows the use of /intermud3 channels tunein|tuneout
  intermud3.admin.connect:
    description: Allows the use of /intermud3 connect
  intermud3.admin.debug:
    description: Allows the use of /intermud3 debug on|off|switch
  intermud3.admin.disconnect:
    description: Allows the use of /intermud3 disconnect
  intermud3.admin.reload:
    description: Allows the use of /intermud3 reload
  intermud3.channel:
    description: Allows the use of /intermud3 emote|msg
  intermud3.help:
    description: Allows the use of /intermud3 help
  intermud3.use:
    description: Allows the use of /intermud3
```

# config.yml
```
router:
  preferred: '*dalet, 97.107.133.86 8787'
  list:
  - '*dalet, 97.107.133.86 8787'
  password: 0
  mudlistID: 0
  chanlistID: 0
autoConnect: false
adminEmail: 'your@email.address.here'
hostName: 'minecraft.hostname.here:25565'
debug: false
```
