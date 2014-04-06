I3Bukkit
========

Bukkit to Intermud3 bridge.

Intermud3 commands
==================
```
commands:
  intermud3:
    aliases: [ i3 ]
    description: A bridge connecting Minecraft to Intermud3
    usage: |
        /<command> connect - connect to the preferred router.
        /<command> disconnect - disconnect from the router.
        /<command> channels - list I3 channels being listened to.
        /<command> channels available - list available I3 channels.
        /<command> channels aliases - list I3 channel aliases.
        /<command> channels tunein|tuneout <channel> - tune into or out of an I3 channel.
        /<command> channels alias <alias> <channel> - add an alias to an I3 channel.
        /<command> channels unalias <alias> - remove the alias.
        /<command> msg <channel> <message> - sends a message to an I3 channel.
        /<command> emote <channel> <emote> - sends an emote to an I3 channel.
```

Intermud3 permissions
=====================
```
permissions:
  intermud3.*:
    default: op
    description: Allows the use of all /intermud3 commands.
    children:
      intermud3.connect: true
      intermud3.disconnect: true
      intermud3.channels: true
      intermud3.tune: true
      intermud3.alias: true
      intermud3.msg: true
      intermud3.emote: true
  intermud3.connect:
    description: Allows the use of /intermud3 connect.
  intermud3.disconnect:
    description: Allows the use of /intermud3 disconnect.
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
