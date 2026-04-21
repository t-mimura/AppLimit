# CLAUDE.md — AppLimit プロジェクト

## プロジェクト概要

特定アプリ（SNS等）の連続使用時間を制限するAndroidアプリ。
詳細仕様は [docs/SPEC.md](docs/SPEC.md) を参照。

## ドキュメント構成

- [docs/SPEC.md](docs/SPEC.md) — アプリケーション仕様
- [docs/TECH_STACK.md](docs/TECH_STACK.md) — 技術スタック
- [docs/CODING_RULES.md](docs/CODING_RULES.md) — コーディングルール
- [docs/TEST_POLICY.md](docs/TEST_POLICY.md) — テスト方針
- [AGENTS.md](AGENTS.md) — AIエージェント向けガイド

## クイックリファレンス

### ビルド・チェックコマンド

```bash
./gradlew assembleDebug          # デバッグビルド
./gradlew check                  # lint + test 一括実行
./gradlew ktlintCheck            # フォーマットチェック
./gradlew ktlintFormat           # 自動フォーマット
./gradlew testDebugUnitTest      # 単体テストのみ
./gradlew lintDebug              # Android Lintのみ
```

### コミット前に必ず実行

```bash
./gradlew ktlintFormat && ./gradlew check
```

pre-commitフックが設定済みであれば自動実行される。

### パッケージ構成

```
studio.hazeray.applimit
├── di/           # Hiltモジュール
├── data/         # DB, Repository
├── domain/       # ドメインモデル
├── service/      # 監視フォアグラウンドサービス
├── receiver/     # BootReceiver
├── overlay/      # オーバーレイ画面
└── ui/           # Compose UI (main, appselect, settings)
```

### 重要な制約

- minSdk 26 / targetSdk 35 / compileSdk 35
- サーバー通信なし。全データはRoom (SQLite) にローカル保存
- オーバーレイには `SYSTEM_ALERT_WINDOW` 権限が必要
- アプリ使用状況の取得には `PACKAGE_USAGE_STATS` 権限が必要
