package studio.hazeray.applimit.data.update

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class GitHubReleaseApi @Inject constructor() {

    suspend fun fetchLatestRelease(): GitHubRelease = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000

            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("GitHub API responded with HTTP $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseRelease(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRelease(body: String): GitHubRelease {
        val json = JSONObject(body)
        val tagName = json.getString("tag_name")
        val versionName = tagName.removePrefix("v")
        val assets = json.optJSONArray("assets")
        var apkAsset: GitHubRelease.ApkAsset? = null
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkAsset = GitHubRelease.ApkAsset(
                        name = name,
                        downloadUrl = asset.getString("browser_download_url"),
                        sizeBytes = asset.optLong("size", 0L)
                    )
                    break
                }
            }
        }
        return GitHubRelease(tagName = tagName, versionName = versionName, apkAsset = apkAsset)
    }

    companion object {
        private const val GITHUB_OWNER = "t-mimura"
        private const val GITHUB_REPO = "AppLimit"
    }
}
