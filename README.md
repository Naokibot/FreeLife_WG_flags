# FreeLifeWGFlags

Spigot 1.21.1 / Java 21 / WorldGuard 7.0.12 向けのカスタムフラグ拡張です。
WorldGuard / WorldEdit 本体は同梱せず、通常の WorldGuard リージョンへ FreeLife 用フラグだけを追加します。

## 必要環境

- Java 21
- Spigot 1.21.1
- WorldEdit 7.3.8
- WorldGuard 7.0.12

カスタムフラグは `onLoad()` で登録します。サーバーを完全停止して JAR を入れ替え、通常起動してください。

## 追加フラグ

| フラグ | 型 | 設定例 | 動作 |
|---|---|---|---|
| `fl-villager-trade` | State | `deny` | 村人・行商人との取引を禁止 |
| `fl-only-wheat-seeds` | State | `allow` | 農作物の植え付けを小麦の種だけに制限 |
| `fl-wind-charge` | State | `deny` / `allow` | ウィンドチャージの使用を禁止 / 許可 |
| `fl-ender-pearl` | State | `deny` | エンダーパールを禁止 |
| `fl-chorus-fruit` | State | `deny` | コーラスフルーツを禁止 |
| `fl-invincible` | State | `allow` | リージョン内プレイヤーを無敵化 |
| `fl-entry-message` | String | `&6Welcome` | リージョンへ入ったとき表示 |
| `fl-animal-damage` | State | `deny` | 動物への全ダメージを無効化 |
| `fl-named-animal-damage` | State | `deny` | 名前付き動物への全ダメージを無効化 |
| `fl-effects` | String | `speed:1,night_vision:1` | リージョン内のプレイヤーへ効果を付与 |
| `fl-remove-effects-on-exit` | State | `allow` | FreeLifeWGFlags が付与した効果を退出時に除去 |
| `fl-time-switch` | String | 下記参照 | Minecraft内時間でFreeLifeのStateフラグを切替 |
| `fl-stay-seconds` | Integer | `300` | 滞在TPまでの秒数 |
| `fl-stay-tp` | String | `hub;0.5;64;0.5;0;0` | 滞在時間到達後のTP先 |
| `fl-afk-seconds` | Integer | `300` | AFK判定秒数 |
| `fl-afk-tp` | String | `hub;0.5;64;0.5;0;0` | AFK時のTP先 |
| `fl-item-entry` | State | `deny` | アイテムを持った状態でリージョンへ入ることを禁止 |
| `fl-item-exit` | State | `deny` | アイテムを持った状態でリージョンから出ることを禁止 |
| `fl-place-blocks` | String | `stone,cobblestone` | 設置できるブロックだけを指定 |
| `fl-break-blocks` | String | `stone,cobblestone` | 破壊できるブロックだけを指定 |
| `fl-block-rollback-seconds` | Integer | `30` | 設置・破壊を指定秒数後に自動復元 |
| `fl-chat-allowed` | String | `hello,trade *` | 許可するチャットだけを指定 |
| `fl-command-allowed` | String | `spawn,hub,warp shop*` | 許可するコマンドだけを指定 |
| `fl-storage-protection` | State | `allow` | ドア/ボタン/レバー/ベッドは公共利用、収納だけ禁止 |

Stateフラグは、特に記載がない限り `deny` で禁止、`allow` で許可です。`fl-only-wheat-seeds`、`fl-invincible`、`fl-remove-effects-on-exit`、`fl-storage-protection` は `allow` でそのモードを有効化します。

## 基本設定例

```text
/rg flag farm fl-villager-trade deny
/rg flag farm fl-only-wheat-seeds allow
/rg flag farm fl-wind-charge allow
/rg flag farm fl-ender-pearl deny
/rg flag farm fl-chorus-fruit deny
/rg flag farm fl-invincible allow
/rg flag farm fl-entry-message &6農場エリアへ入りました
/rg flag farm fl-animal-damage deny
/rg flag farm fl-named-animal-damage deny
```

## エフェクト

`fl-effects` は `効果名:レベル` をカンマ区切りで設定します。レベルは1始まりです。

```text
/rg flag arena fl-effects speed:2,night_vision:1
/rg flag arena fl-remove-effects-on-exit allow
```

退出時に削除するのはこのプラグインが付けた効果だけです。入場前に同種の効果が存在していた場合は記録し、プラグインの効果を除去できた場合に元の効果へ戻します。

## Minecraft内時間での切替

形式:

```text
開始tick-終了tick:flag=allow,flag=deny|開始tick-終了tick:flag=allow
```

例:

```text
/rg flag arena fl-time-switch 0-11999:fl-wind-charge=allow|12000-23999:fl-wind-charge=deny
```

0〜11999ではウィンドチャージを許可し、12000〜23999では禁止します。`18000-1000` のような日付境界をまたぐ範囲にも対応します。対象はこのプラグインが追加したStateフラグです。

## 滞在時間TP / AFK TP

TP先は `world;x;y;z` または `world;x;y;z;yaw;pitch` です。

```text
/rg flag queue fl-stay-seconds 600
/rg flag queue fl-stay-tp world;0.5;70;0.5;90;0

/rg flag lobby fl-afk-seconds 300
/rg flag lobby fl-afk-tp afk;0.5;64;0.5
```

アイテム持込/持出禁止とTPが競合する場合、アイテム制限を優先してTPを行いません。アイテムが消える方式にはしていません。

## アイテム持込・持出

```text
/rg flag minigame fl-item-entry deny
/rg flag minigame fl-item-exit deny
```

インベントリ、装備、オフハンドのいずれかにアイテムがある場合、境界を越えられません。アイテムを自動削除・複製・一時保管しないため、クラッシュ時のアイテム消失や複製経路を作りません。

## 特定ブロックだけ許可

```text
/rg flag build fl-place-blocks stone,cobblestone,oak_planks
/rg flag build fl-break-blocks stone,cobblestone,oak_planks
```

`*` は全ブロック、`none` は許可ブロックなしです。このフラグはWorldGuard本来のBUILD拒否を解除しません。WorldGuardが既に許可した設置・破壊をさらに絞り込みます。

## ブロック自動修復

```text
/rg flag arena fl-block-rollback-seconds 20
```

設置・破壊前のBlockStateを保持し、20秒後に復元します。復元予定位置が別のブロックへ変更されている場合は上書きしません。

## チャット / コマンド

```text
/rg flag lobby fl-chat-allowed hello,trade *
/rg flag lobby fl-command-allowed spawn,hub,msg *
```

- `*` はすべて許可
- `none` はすべて拒否
- 末尾 `*` は前方一致
- コマンドで引数なしのルール (`spawn`) はコマンド名に一致

チャットイベントは非同期ですが、WorldGuard APIを非同期スレッドから直接呼びません。同期タスクで更新したリージョンポリシーのキャッシュだけを読みます。

## 公共操作 + 収納保護

```text
/rg flag spawn fl-storage-protection allow
```

このモードでは、ドア、ボタン、レバー、ベッドのブロック使用を明示的に許可し、次の収納は拒否します。

- チェスト / トラップチェスト
- 樽
- ホッパー
- 全色シュルカーボックス

PlayerInteractEventだけでなくInventoryOpenEventでも収納を再確認します。

## ビルド

```bash
mvn -B verify
```

生成物:

```text
target/FreeLifeWGFlags-1.0.0-Spigot-1.21.1.jar
```

### 運用上の補足

`fl-block-rollback-seconds` が有効なブロックを破壊した場合、ブロックが後で復元されるため、アイテム複製を防ぐ目的でその破壊のブロックドロップとブロック経験値は発生させません。

AFK判定では移動だけでなく、インベントリのクリック/ドラッグ、ホットバー切替、アイテムドロップ、メイン/オフハンド交換も活動として扱います。`fl-item-exit deny` のリージョン内で `keepInventory` のまま死亡すると持出し扱いになるため、その死亡についてはインベントリ保持を解除し、アイテムはリージョン内へドロップさせます。
