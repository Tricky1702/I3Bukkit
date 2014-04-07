I3Bukkit
========

Bukkit to Intermud3 bridge.

Commands
========
```
commands:
  intermud3:
    aliases: [ i3 ]
    description: A bridge connecting Minecraft to Intermud3
    usage: |
        /<command> connect - connect to the preferred router.
        /<command> disconnect - disconnect from the router.
        /<command> reload - reload all configuration files.
        /<command> channels - list I3 channels being listened to.
        /<command> channels available - list available I3 channels.
        /<command> channels aliases - list I3 channel aliases.
        /<command> channels tunein|tuneout <channel> - tune into or out of an I3 channel.
        /<command> channels alias <alias> <channel> - add an alias to an I3 channel.
        /<command> channels unalias <alias> - remove the alias.
        /<command> msg <channel|.> <message> - sends a message to an I3 channel. Use '.' for the last channel used.
        /<command> emote <channel|.> <emote> - sends an emote to an I3 channel. Use '.' for the last channel used.
```

Permissions
===========
```
permissions:
  intermud3.*:
    default: op
    description: Allows the use of all /intermud3 commands.
    children:
      intermud3.connect: true
      intermud3.disconnect: true
      intermud3.reload: true
      intermud3.channels: true
      intermud3.tune: true
      intermud3.alias: true
      intermud3.msg: true
      intermud3.emote: true
  intermud3.connect:
    description: Allows the use of /intermud3 connect.
  intermud3.disconnect:
    description: Allows the use of /intermud3 disconnect.
  intermud3.reload:
    description: Allows the use of /intermud3 reload.
  intermud3.channels:
    description: Allows the use of /intermud3 channels.
  intermud3.tune:
    description: Allows the use of /intermud3 channels tunein|tuneout.
  intermud3.alias:
    description: Allows the use of /intermud3 channels alias|unalias.
  intermud3.msg:
    description: Allows the use of /intermud3 msg.
  intermud3.emote:
    description: Allows the use of /intermud3 emote.
```

config.yml
==========
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
