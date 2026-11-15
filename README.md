# Remote Bot Fetch

A Carpet extension: the server lets fake players fetch items out of storage, the client plugs that
into the player inventory and Litematica's material list. One source tree, three jar flavours.

| Jar | Mod id | Environment | Dependencies | Use |
| --- | --- | --- | --- | --- |
| `*-universal.jar` | `remote-bot-fetch` | `*` | Fabric Loader, Carpet (MaLiLib, Fabric API, Litematica, Mod Menu optional) | one file for both client and server |
| `*-server.jar` | `remote-bot-fetch-server` | `*` | Fabric Loader, Carpet | server only |
| `*-client.jar` | `remote-bot-fetch-client` | `client` | MaLiLib, Fabric API, Carpet (server mod, Litematica and Mod Menu optional) | client only |

**`*-universal.jar` is the recommended one**: drop the same file into `mods/` on the client and on the
server. The server half only initialises in a server environment and the client half only in a client
environment; when MaLiLib or Fabric API is missing on the client it logs an error and disables the
client features instead of crashing.

## Client fetch panel

- The inventory's **recipe book button** toggles the fetch panel on the left; while it is enabled the
  vanilla recipe book no longer opens.
- Reuses the vanilla creative category tabs and layout: search box, refresh button, 9x5 item grid,
  scrollbar.
- Picking matches the creative inventory: click an entry to put one item on the cursor, left click
  adds one, right click removes one, `Shift` + left click takes a full stack, middle click clones a
  full stack. Clicking outside the panel fetches one stack with the left button or one shulker box
  (27 stacks) with the right button, multiplied by the held count; `ESC` clears the cursor.
- Hovering shows the storage tooltip; fetching still uses the server `getItem` / `getStorageData`
  commands.
- The panel can be toggled in the config screen via `Inventory Fetch Panel` (`inventoryFetchMenu`);
  the default key to open the config screen is `R` + `B`.
- With Mod Menu installed the config screen can also be opened from the mod list (optional dependency).

## Building

Requires JDK 25.

```bash
./gradlew :26.1.2:build              # every jar for one version
./gradlew buildAndGather -x check    # every version, collected into build/libs
```

Output: `versions/<version>/build/libs/remote-bot-fetch-v<version>-mc<MC>-SNAPSHOT-{universal,client,server}.jar`.
`local-repo/` ships the Parchment mappings and `org.gradle.configureondemand=true` is already set.

## Development runs

```bash
./gradlew :26.1.2:runClientMod              # client sources only, mirrors the split client jar
./gradlew :26.1.2:runUniversalClient        # client + server sources, mirrors the universal jar
./gradlew :26.1.2:runClientModMixinAudit
./gradlew :26.1.2:runServerMixinAudit
```

## Versions

`settings.json` lists 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1.2, 26.2 and 26.3;
every version builds all three jars, and 1.21.0, 1.21.2, 1.21.6, 1.21.7, 1.21.9, 26.1 and 26.1.1 are
covered by the dependency ranges of the neighbouring projects.

API differences are handled by preprocessor branches: `gui/RbfGraphics` adapts
`GuiGraphicsExtractor` (26.x) and `GuiGraphics` (1.21.x), input callbacks split at `MC>=12110`.

Known trade-offs:

- 1.21.1 uses the 1.21 builds of malilib `0.21.10` and litematica `0.19.61` (no 1.21.1 artifact exists).
- 1.21.x has no `PreeditEvent`, so the search box cannot show IME composition text.
- On 1.21.1 the config screen has to be opened with the hotkey.

## References

- [Carpet-AMS-Addition](https://github.com/Minecraft-AMS/Carpet-AMS-Addition) ([license](third-party/Carpet-AMS-Addition/LICENSE))
- [Carpet-LMS-Addition](https://github.com/Citrus-Union/Carpet-LMS-Addition) ([license](third-party/Carpet-LMS-Addition/LICENSE))
- [Carpet-Org-Addition](https://github.com/fcsailboat/Carpet-Org-Addition) ([license](third-party/Carpet-Org-Addition/LICENSE))
- [Carpet-TIS-Addition](https://github.com/TISUnion/Carpet-TIS-Addition) ([license](third-party/Carpet-TIS-Addition/LICENSE))
