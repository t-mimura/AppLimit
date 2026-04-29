# R8 (minify) rules for release builds.
#
# Hilt / Room / Coroutines / Compose はそれぞれ consumer-rules を同梱しているので
# 通常は追加不要。壊れた場合だけ該当セクションを有効化する。

# --- よくある犯人テンプレ（必要になったらコメント解除） ---

# Enum.valueOf() / name() を文字列で扱っている場合
#-keepclassmembers enum studio.hazeray.applimit.** {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}

# リフレクションでアクセスされるモデル
#-keep class studio.hazeray.applimit.domain.model.** { *; }

# Room エンティティ（Room の consumer-rules でカバーされるはずだが念のため）
#-keep @androidx.room.Entity class * { *; }
#-keep @androidx.room.Dao class * { *; }

# kotlinx.serialization を導入した場合のみ
#-keepattributes *Annotation*, InnerClasses
#-keepnames class kotlinx.serialization.Serializable
