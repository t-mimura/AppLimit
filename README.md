# AppLimit

特定アプリ（SNS など）の連続使用時間を制限する Android アプリ。
制限時間に達すると全画面オーバーレイで対象アプリを覆い、ユーザーに使用を中断させる。

## 主な機能

- 対象アプリごとに **制限時間 / クールダウン時間 / 延長時間** を個別に設定
- 制限時間到達時にフルスクリーンの警告オーバーレイを表示
- 「延長する」「閉じる（ホームへ戻る）」を選択可能
- 「閉じる」選択後はクールダウンに入り、対象アプリを再度開いてもオーバーレイで遮断
- 制限時間の数分前に通知でリマインド
- 対象アプリ以外を使っている間はサービスがフォアグラウンド通知（無音）で常駐し、再起動後も自動復帰

サーバー通信は一切なく、すべてのデータは端末内（Room/SQLite）に保存される。

## 技術スタック

| カテゴリ | 採用 |
|---|---|
| 言語 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| アーキテクチャ | MVVM + Repository |
| DI | Hilt |
| DB | Room |
| 非同期 | Coroutines + Flow |
| ナビゲーション | Navigation Compose |
| 品質 | ktlint / Android Lint / JUnit 5 + Turbine / Robolectric |

詳細は [docs/TECH_STACK.md](docs/TECH_STACK.md)。

## 動作要件

- minSdk 26 (Android 8.0)
- targetSdk / compileSdk 35

実機で動作させるには以下の権限が必要:

| 権限 | 用途 |
|---|---|
| 使用状況へのアクセス (`PACKAGE_USAGE_STATS`) | フォアグラウンドアプリの検出 |
| 他のアプリの上に表示 (`SYSTEM_ALERT_WINDOW`) | オーバーレイ表示 |
| 通知 (`POST_NOTIFICATIONS`, Android 13+) | 警告・サービス通知 |

加えて、Android 11+ の **App Hibernation**（使用していないアプリを管理する）をオフにすると、長期間アプリを開かない場合でも監視が止まりにくい。

## インストール

[GitHub Releases](https://github.com/t-mimura/AppLimit/releases) から `AppLimit-vX.Y.Z.apk` をダウンロードして、端末にコピーまたは直接DLしてインストールする。

### Android 14 以降にブラウザ等から直接インストールした場合の追加手順

Android 14 以降の **制限付き設定**（Restricted Settings）機能により、ブラウザ等から直接インストールしたアプリは `使用状況へのアクセス`・`他のアプリの上に表示` の権限を直接許可できないことがある。

その場合は：

1. 設定 → アプリ → AppLimit を開く
2. 画面右上の **⋮（メニュー）** → **「制限付きの設定を許可」**
3. 戻って再度権限を許可

## ビルドと開発

### 必要な環境

- JDK 17
- Android SDK (API 35)
- Gradle Wrapper（リポジトリに同梱）

### よく使うコマンド

```bash
./gradlew assembleDebug          # デバッグビルド
./gradlew check                  # lint + test 一括実行
./gradlew ktlintCheck            # フォーマットチェック
./gradlew ktlintFormat           # 自動フォーマット
./gradlew testDebugUnitTest      # 単体テストのみ
./gradlew lintDebug              # Android Lint のみ
```

### コミット前

```bash
./gradlew ktlintFormat && ./gradlew check
```

リポジトリには pre-commit フック（`.git/hooks/pre-commit`）が用意されており、ktlint と単体テストを自動実行する。

### リリース署名

`signing.properties.template` をコピーして `signing.properties` を作成し、keystore のパスとパスワードを記入する（`signing.properties` は gitignore 済み）。

```bash
cp signing.properties.template signing.properties
# signing.properties を編集
./gradlew assembleRelease
```

## ディレクトリ構成

```
app/src/main/java/studio/hazeray/applimit/
├── di/           # Hilt モジュール
├── data/         # DB / Repository
├── domain/       # ドメインモデル
├── service/      # 監視フォアグラウンドサービス
├── receiver/     # BootReceiver
├── overlay/      # オーバーレイ表示
├── debug/        # デバッグ用ログバッファ・設定
└── ui/           # Compose UI（main / appselect / settings / permission / debug）
```

## ドキュメント

- [docs/SPEC.md](docs/SPEC.md) — アプリ仕様
- [docs/TECH_STACK.md](docs/TECH_STACK.md) — 技術スタック詳細
- [docs/CODING_RULES.md](docs/CODING_RULES.md) — コーディング規約
- [docs/TEST_POLICY.md](docs/TEST_POLICY.md) — テスト方針
- [CLAUDE.md](CLAUDE.md) — Claude Code 向けプロジェクトガイド

## ライセンス

[MIT License](LICENSE)
