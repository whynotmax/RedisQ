# RedisQ - the new way to queue in minecraft
It is a simple and easy to use plugin that allows you to queue up for a server.

## Features
- Queue up for a server
- Customizable messages

## Commands
- `/queue <name>` - Queue up for a server
- `/queue leave` - Leave the queue

## Permissions
- `redisq.queue.<name>` - Allows you to queue up for a server with the name <name>
- `redisq.leave` - Allows you to leave the queue

## Configuration
```yaml
redis:
    url: "redis://127.0.0.1:6379"
    password: "password"
general:
    prefix: "<dark_gray>[<red>RedisQ<dark_gray>]<gray>"
    no-permission: "%prefix% <red>You do not have permission to do that. (%permission%)"
    no-console: "%prefix% <red>This command can only be executed by players."
    no-args: "%prefix% <red>Usage: %usage%"
    no-queue: "%prefix% <red>Queue with the name %name% not found."
    sending-to-server: "%prefix% <gray>Sending you to %server%..."
    queue-leave: "%prefix% <gray>You have left the queue."
```

## Installation
1. Download the plugin from the [releases page](https://github.com/whynotmax/RedisQ/releases/latest)
2. Put the plugin in your plugins folder
3. Start your server
4. Edit the configuration file to your liking
5. Reload the server or restart it
6. You're done!

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits
- [Max](github.com/whynotmax) - Developer
- [All Contributors](github.com/whynotmax/RedisQ/graphs/contributors)