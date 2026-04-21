# コーディングルール

## 言語・スタイル

### Kotlin スタイル

- [Kotlin公式コーディング規約](https://kotlinlang.org/docs/coding-conventions.html) に準拠
- ktlint による自動フォーマットを適用する
- ktlint が検出しない事項について以下に定める

### 命名規則

| 対象 | 規則 | 例 |
|------|------|-----|
| クラス・インターフェース | UpperCamelCase | `AppRepository`, `MonitorService` |
| 関数・プロパティ | lowerCamelCase | `getTargetApps()`, `isRunning` |
| 定数 (const val, companion) | SCREAMING_SNAKE_CASE | `DEFAULT_LIMIT_MINUTES` |
| パッケージ | すべて小文字、区切りなし | `studio.hazeray.applimit.data.repository` |
| Compose関数 | UpperCamelCase | `MainScreen()`, `AppListItem()` |

### Nullable の扱い

- `!!` (non-null assertion) は原則禁止。使用する場合はコメントで理由を記載する
- `?.let {}` や `?:` (Elvis演算子) を優先する

### Coroutines

- ViewModelからのCoroutine起動は `viewModelScope` を使用する
- ServiceからのCoroutine起動は `lifecycleScope` またはカスタムスコープを使用する
- Dispatcher の指定は DI 経由で注入する（テスタビリティのため）

## アーキテクチャルール

### レイヤー間の依存方向

```
UI → ViewModel → Repository → DataSource (Room)
```

- 上位レイヤーが下位レイヤーに依存する（逆方向の依存は禁止）
- ViewModel は Android フレームワーク (Context等) に直接依存しない
- Repository はインターフェースで定義し、実装クラスは Hilt で注入する

### Compose UI

- State は ViewModel が保持し、Compose は stateless に保つ
- 画面単位の Composable は `〇〇Screen` と命名する
- 再利用可能な部品は `〇〇Component` や内容を表す名前にする
- Preview 用の関数を各画面に用意する

### Room (DB)

- Entity クラスは `data class` で定義する
- Dao はインターフェースで定義する
- マイグレーションは破壊的変更を避ける（データ保持）

## 禁止事項

- ハードコードされた文字列リソース（UI表示文字列は `strings.xml` に定義する）
- `Thread.sleep()` の使用（`delay()` を使う）
- `GlobalScope` の使用
- `var` の多用（`val` を優先する）
- 未使用の import 文（ktlint が検出）
- wildcard import `*`（ktlint が検出）

## コメント

- コードで意図が明確な場合はコメント不要
- 「なぜ」そうしているかが自明でない場合のみコメントを書く
- TODO コメントは `// TODO: <説明>` の形式にする
