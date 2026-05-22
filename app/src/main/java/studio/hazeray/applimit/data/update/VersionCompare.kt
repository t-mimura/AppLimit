package studio.hazeray.applimit.data.update

fun isNewerVersion(candidate: String, current: String): Boolean {
    val a = parseSemver(candidate) ?: return false
    val b = parseSemver(current) ?: return false
    return compareSemver(a, b) > 0
}

private fun parseSemver(version: String): IntArray? {
    val parts = version.removePrefix("v").split(".")
    if (parts.size != 3) return null
    return try {
        intArrayOf(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (_: NumberFormatException) {
        null
    }
}

private fun compareSemver(a: IntArray, b: IntArray): Int {
    for (i in 0..2) {
        val cmp = a[i].compareTo(b[i])
        if (cmp != 0) return cmp
    }
    return 0
}
