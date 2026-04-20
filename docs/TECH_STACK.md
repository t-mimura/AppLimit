# 技術スタック

## 言語

**Kotlin** (100%)

- Googleが推奨するAndroid開発の第一言語
- Javaとの相互運用性あり（必要な場合のみ）

## UIフレームワーク

**Jetpack Compose**

- 宣言的UIで記述量が少なく、AIエージェントによるコード生成と相性が良い
- XMLレイアウト不要でファイル数が減り保守しやすい
- Google公式の推奨UIツールキット

## アーキテクチャ

**MVVM + Repository パターン**

```
UI (Compose) → ViewModel → Repository → DataSource (Room)
```

- Android公式アーキテクチャガイドラインに準拠
- テスタビリティが高い（各層を独立してテスト可能）

## 主要ライブラリ

| カテゴリ | ライブラリ | 用途 |
|---------|-----------|------|
| DI | Hilt | 依存性注入 |
| DB | Room | ローカルデータ永続化 |
| 非同期 | Kotlin Coroutines + Flow | 非同期処理・リアクティブデータ |
| ナビゲーション | Navigation Compose | 画面遷移 |
| ライフサイクル | Lifecycle ViewModel Compose | ViewModel統合 |

## ビルドツール

- **Gradle** (Kotlin DSL — `build.gradle.kts`)
- **Android Gradle Plugin** 8.x
- **Version Catalog** (`libs.versions.toml`) でバージョン一元管理

## 品質ツール

| ツール | 用途 | 適用タイミング |
|--------|------|--------------|
| ktlint | Kotlinコードフォーマット・リント | pre-commitフック |
| Android Lint | Android固有の問題検出 | ビルド時 |
| JUnit 5 + Turbine | 単体テスト（Flow含む） | pre-commitフック |
| Robolectric | Androidフレームワーク依存のテスト | CI / ローカル |
| pre-commit (git hook) | テスト・リント通過を強制 | コミット時 |

## 静的解析・フォーマット自動化の仕組み

### Git pre-commit フック

コミット時に以下を自動実行し、失敗した場合はコミットを拒否する:

1. **ktlint** — フォーマット違反の検出
2. **Android Lint** — 重大な問題(error)の検出
3. **単体テスト** — 変更に関連するテストの実行

### Gradle タスク統合

```
./gradlew check   # lint + test を一括実行
./gradlew ktlintCheck  # フォーマットチェック
./gradlew ktlintFormat  # 自動フォーマット
```

## ディレクトリ構成（予定）

```
app/src/main/java/com/example/applimit/
├── di/                  # Hiltモジュール
├── data/
│   ├── db/              # Room (Entity, Dao, Database)
│   └── repository/      # Repository実装
├── domain/
│   └── model/           # ドメインモデル
├── service/             # フォアグラウンドサービス（監視）
├── receiver/            # BroadcastReceiver（再起動時）
├── overlay/             # オーバーレイ表示
└── ui/
    ├── main/            # メイン画面
    ├── appselect/       # アプリ選択画面
    └── settings/        # アプリ個別設定画面
```

## 最低開発環境要件

- JDK 17
- Android SDK (API 35)
- Android Build Tools
- Gradle 8.x（Wrapper経由）
