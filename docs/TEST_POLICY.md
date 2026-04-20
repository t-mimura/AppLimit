# テスト方針

## 基本方針

- **テストファースト**: 実装コードより先にテストを書く
- テストが通らないコードはコミットしない（pre-commitフックで強制）
- テストは実装の詳細ではなく振る舞いを検証する

## テストの種類と対象

### 単体テスト (Unit Test)

- **場所**: `app/src/test/`
- **フレームワーク**: JUnit 5 + Mockk + Turbine (Flow テスト用)
- **対象**:
  - ViewModel — 入力に対する状態遷移とUIステートの検証
  - Repository — データ取得・保存ロジックの検証（Dao はモック）
  - ドメインモデル — ビジネスロジックの検証
  - Service のロジック部分 — タイマー計算、状態遷移等

### Android テスト (Instrumented Test / Robolectric)

- **場所**: `app/src/test/` (Robolectric)
- **フレームワーク**: Robolectric + JUnit 5
- **対象**:
  - Room Database — Dao の実際のクエリ検証
  - Context 依存のロジック — パッケージ情報取得等

### UIテスト

- v1 では実施しない（個人利用のためコストに見合わない）
- 将来的に Compose UI Test で主要導線のみカバーを検討

## テストの書き方

### 命名規則

```kotlin
@Test
fun `制限時間を超過したらオーバーレイ状態になる`() { ... }
```

- 日本語でテストの意図を明確に記述する
- Given-When-Then 構造を意識する

### 構造

```kotlin
@Test
fun `テスト名`() {
    // Given: 前提条件のセットアップ
    val repository = FakeAppRepository()
    val viewModel = MainViewModel(repository)

    // When: テスト対象の操作
    viewModel.onTimerExpired(appId)

    // Then: 期待する結果の検証
    assertEquals(SessionState.OVERLAY, viewModel.state.value.sessionState)
}
```

### モックとフェイク

- 外部依存（DB、システムAPI）は **Fake** を優先する
  - `FakeAppRepository`, `FakeUsageStatsProvider` 等
- Fake の用意が過剰な場合のみ **Mockk** を使用する
- Fake クラスは `app/src/test/.../fake/` パッケージに配置する

## カバレッジ方針

- カバレッジ数値の目標は設けない
- 以下を必ずテストする:
  - タイマーの状態遷移（開始 → 制限発動 → 延長 → クールダウン → 再開）
  - 設定値の保存と読み出し
  - 境界値（制限時間0分、クールダウン0分等の異常系）
  - ViewModel の状態変化

## pre-commit フックで実行されるテスト

```bash
./gradlew testDebugUnitTest
```

- 全単体テストが対象
- 1つでも失敗したらコミットを拒否する
