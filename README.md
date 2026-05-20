# BoneCane

A lightweight Bukkit/Paper plugin that lets you bonemeal **Sugar Cane** and **Cactus** — both by right-clicking with bonemeal in hand and via dispensers.

## Features

- Right-click sugar cane or cactus with bone meal to grow it instantly.
- Dispensers can automate bonemeal on sugar cane and cactus columns.
- Green particles and the bonemeal sound play on every use (even when the plant doesn't grow).
- Two configurable growth modes: **legacy** and **chance**.
- Configurable max column height.

## Configuration

Edit `plugins/BoneCane/config.yml` after first launch:

```yaml
# Growth mode: "legacy" or "chance"
# legacy: bonemeal always grows the plant (1 block, small chance for 2)
# chance: each bonemeal use has a percentage chance to grow the plant by 1 block
mode: legacy

# Maximum height a sugar cane or cactus column may reach (vanilla default: 3)
max-height: 3

legacy:
  # Probability (0.0 - 1.0) to grow by 2 blocks instead of 1
  double-grow-chance: 0.10

chance:
  # Probability (0.0 - 1.0) that the plant actually grows on a bonemeal use
  grow-chance: 0.10
```

### Growth modes

| Mode | Behaviour |
|------|-----------|
| `legacy` | Bonemeal always grows the plant by 1 block. Has a `double-grow-chance` probability of growing 2 blocks at once. Bonemeal is only consumed (and particles shown) when growth actually happens. |
| `chance` | Bonemeal is consumed on every use. Particles are always shown. The plant grows only if the random roll beats `grow-chance`. |

### Options

| Key | Default | Description |
|-----|---------|-------------|
| `mode` | `legacy` | Growth mode (`legacy` or `chance`). |
| `max-height` | `3` | Maximum column height for sugar cane and cactus (vanilla cap is 3). |
| `legacy.double-grow-chance` | `0.10` | Chance (0.0 - 1.0) to grow 2 blocks instead of 1 in legacy mode. |
| `chance.grow-chance` | `0.10` | Chance (0.0 - 1.0) that bonemealing actually grows the plant in chance mode. |

## Installation

1. Drop the `BoneCane.jar` into your server's `plugins/` folder.
2. Restart or reload the server.
3. Adjust `plugins/BoneCane/config.yml` to your liking and reload/restart again.

## Compatibility

Requires a Paper-compatible server running **Minecraft 1.21+**.
