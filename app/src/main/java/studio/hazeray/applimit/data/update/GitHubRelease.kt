package studio.hazeray.applimit.data.update

data class GitHubRelease(
    val tagName: String,
    val versionName: String,
    val apkAsset: ApkAsset?
) {
    data class ApkAsset(
        val name: String,
        val downloadUrl: String,
        val sizeBytes: Long
    )
}
