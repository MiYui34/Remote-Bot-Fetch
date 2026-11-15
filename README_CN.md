# Remote Bot Fetch

Carpet 扩展：服务端让假人从仓库取货，客户端把取货接入玩家物品栏与 Litematica 材料列表。同一份源码产出两种形态的 jar。

| Jar | 模组 id | 环境 | 依赖 | 用途 |
| --- | --- | --- | --- | --- |
| `*-universal.jar` | `remote-bot-fetch` | `*` | Fabric Loader、Carpet（MaLiLib、Fabric API、Litematica、Mod Menu 可选） | 一个文件同时装客户端和服务端 |
| `*-server.jar` | `remote-bot-fetch-server` | `*` | Fabric Loader、Carpet | 只装服务端 |
| `*-client.jar` | `remote-bot-fetch-client` | `client` | MaLiLib、Fabric API、Carpet（服务端模组、Litematica、Mod Menu 可选） | 只装客户端 |

**推荐用 `*-universal.jar`**：客户端和服务端都放这一个文件即可——服务端部分只在服务端环境初始化，客户端部分只在客户端环境初始化；客户端缺少 MaLiLib 或 Fabric API 时会打印错误并禁用客户端功能，不会崩溃。

## 客户端取货面板

- 玩家物品栏的**配方书按钮**开关左侧取货面板；面板开启时原版配方书不再打开。
- 复用原版创造模式分类标签与布局：搜索框、刷新按钮、9×5 物品网格、滚动条。
- 取货与创造模式一致：点一下物品悬浮在鼠标上（1 个），左键 +1、右键 -1、`Shift`+左键整组、中键克隆；移出面板后左键取 1 组、右键取 1 潜影盒（27 组），都乘手上数量；`ESC` 取消。
- 悬停显示库存 tooltip，取货仍走服务端 `getItem` / `getStorageData`。
- 配置界面可用 `背包内取货`（`inventoryFetchMenu`）开关，默认打开配置的按键为 `R` + `B`。
- 安装 Mod Menu 后可直接在模组列表里打开配置界面（可选依赖）。

## 构建

需要 JDK 25。

```bash
./gradlew :26.1.2:build              # 单版本的全部 jar
./gradlew buildAndGather -x check    # 全部版本，收集到 build/libs
```

产物：`versions/<版本>/build/libs/remote-bot-fetch-v<版本>-mc<MC>-SNAPSHOT-{universal,client,server}.jar`。
`local-repo/` 预置了 Parchment 映射，`org.gradle.configureondemand=true` 已开启。

## 开发运行

```bash
./gradlew :26.1.2:runClientMod              # 只有客户端源码，等同分体 client jar
./gradlew :26.1.2:runUniversalClient        # 客户端 + 服务端源码，等同 universal jar
./gradlew :26.1.2:runClientModMixinAudit
./gradlew :26.1.2:runServerMixinAudit
```

## 版本

`settings.json` 列出 1.21.1、1.21.3、1.21.4、1.21.5、1.21.8、1.21.10、1.21.11、26.1.2、26.2、26.3，每个版本都产出客户端与服务端 jar；1.21.0、1.21.2、1.21.6、1.21.7、1.21.9、26.1、26.1.1 由相邻工程的依赖范围覆盖。

API 差异由 preprocessor 分支处理：`gui/RbfGraphics` 适配 `GuiGraphicsExtractor`（26.x）与 `GuiGraphics`（1.21.x），输入回调在 `MC>=12110` 分叉。

已知取舍：

- 1.21.1 使用 1.21 的 malilib `0.21.10` 与 litematica `0.19.61`（没有 1.21.1 制品）。
- 1.21.x 没有 `PreeditEvent`，搜索框不支持输入法预编辑。
- 1.21.1 的配置界面需用热键打开。

## 参考

- [Carpet-AMS-Addition](https://github.com/Minecraft-AMS/Carpet-AMS-Addition) ([license](third-party/Carpet-AMS-Addition/LICENSE))
- [Carpet-LMS-Addition](https://github.com/Citrus-Union/Carpet-LMS-Addition) ([license](third-party/Carpet-LMS-Addition/LICENSE))
- [Carpet-Org-Addition](https://github.com/fcsailboat/Carpet-Org-Addition) ([license](third-party/Carpet-Org-Addition/LICENSE))
- [Carpet-TIS-Addition](https://github.com/TISUnion/Carpet-TIS-Addition) ([license](third-party/Carpet-TIS-Addition/LICENSE))
