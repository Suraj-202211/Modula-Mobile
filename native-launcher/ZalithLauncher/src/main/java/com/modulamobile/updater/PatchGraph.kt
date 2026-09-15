package com.modulamobile.updater

import android.util.Log
import java.util.PriorityQueue

object PatchGraph {
    private const val TAG = "OTA-CHAIN"

    data class PathResult(
        val path: List<PatchEntry>,
        val totalBytes: Long
    )

    private data class SearchNode(
        val versionCode: Int,
        val currentSha: String,
        val totalCost: Long,
        val hopCount: Int,
        val path: List<PatchEntry>
    ) : Comparable<SearchNode> {
        override fun compareTo(other: SearchNode): Int {
            val costCmp = this.totalCost.compareTo(other.totalCost)
            if (costCmp != 0) return costCmp
            return this.hopCount.compareTo(other.hopCount)
        }
    }

    /**
     * Finds the shortest (cheapest) cryptographic patch path from [installedVersionCode] to [targetVersionCode].
     *
     * @return PathResult if a valid cryptographic path exists, null otherwise.
     */
    fun findBestPath(
        installedVersionCode: Int,
        targetVersionCode: Int,
        installedApkSha: String?,
        targetApkSha: String?,
        patches: List<PatchEntry>
    ): PathResult? {
        Log.d(TAG, "[$TAG] Starting path search: Installed $installedVersionCode -> Target $targetVersionCode")
        Log.d(TAG, "[$TAG] Available patch edges count: ${patches.size}")
        patches.forEach { p ->
            Log.d(TAG, "[$TAG] Available edge: ${p.fromVersionCode} -> ${p.toVersionCode} (${p.patchSizeBytes} bytes, src=${p.sourceSha256.take(8)}..., tgt=${p.targetSha256.take(8)}...)")
        }

        if (installedVersionCode >= targetVersionCode) {
            Log.d(TAG, "[$TAG] Installed version ($installedVersionCode) >= target ($targetVersionCode), no patch path needed")
            return null
        }

        // Build adjacency list indexed by fromVersionCode
        val validPatches = patches.filter { p ->
            p.patchSizeBytes > 0 &&
                    p.patchUrl.isNotBlank() &&
                    p.patchSha256.isNotBlank() &&
                    p.sourceSha256.isNotBlank() &&
                    p.targetSha256.isNotBlank()
        }

        val adj = mutableMapOf<Int, MutableList<PatchEntry>>()
        for (p in validPatches) {
            adj.getOrPut(p.fromVersionCode) { mutableListOf() }.add(p)
        }

        // Dijkstra PriorityQueue
        val pq = PriorityQueue<SearchNode>()
        // Track best cost to reach (versionCode, targetSha256)
        val bestCost = mutableMapOf<Pair<Int, String>, Long>()

        // Initial outgoing edges from installedVersionCode
        val initialEdges = adj[installedVersionCode] ?: emptyList()
        for (edge in initialEdges) {
            if (!installedApkSha.isNullOrBlank() && !edge.sourceSha256.equals(installedApkSha, ignoreCase = true)) {
                Log.d(TAG, "[$TAG] Edge ${edge.fromVersionCode} -> ${edge.toVersionCode} rejected: source SHA mismatch (apk=$installedApkSha, patch=${edge.sourceSha256})")
                continue
            }
            val key = Pair(edge.toVersionCode, edge.targetSha256.lowercase())
            bestCost[key] = edge.patchSizeBytes
            pq.add(
                SearchNode(
                    versionCode = edge.toVersionCode,
                    currentSha = edge.targetSha256,
                    totalCost = edge.patchSizeBytes,
                    hopCount = 1,
                    path = listOf(edge)
                )
            )
            Log.d(TAG, "[$TAG] Initial candidate edge: ${edge.fromVersionCode} -> ${edge.toVersionCode} (${edge.patchSizeBytes} bytes)")
        }

        var optimalResult: PathResult? = null

        while (!pq.isEmpty()) {
            val current = pq.poll() ?: break

            if (current.versionCode == targetVersionCode) {
                // Check target SHA match if targetApkSha is provided
                if (!targetApkSha.isNullOrBlank() && !current.currentSha.equals(targetApkSha, ignoreCase = true)) {
                    Log.d(TAG, "[$TAG] Reached target version $targetVersionCode but target SHA mismatch ($targetApkSha vs ${current.currentSha})")
                    continue
                }
                optimalResult = PathResult(current.path, current.totalCost)
                break
            }

            val recordedBest = bestCost[Pair(current.versionCode, current.currentSha.lowercase())]
            if (recordedBest != null && current.totalCost > recordedBest) {
                continue
            }

            val nextEdges = adj[current.versionCode] ?: emptyList()
            for (nextEdge in nextEdges) {
                // Cryptographic continuity check: nextEdge sourceSha must equal current intermediate targetSha
                if (!nextEdge.sourceSha256.equals(current.currentSha, ignoreCase = true)) {
                    continue
                }

                val newCost = current.totalCost + nextEdge.patchSizeBytes
                val nextKey = Pair(nextEdge.toVersionCode, nextEdge.targetSha256.lowercase())
                val prevBest = bestCost[nextKey]

                if (prevBest == null || newCost < prevBest) {
                    bestCost[nextKey] = newCost
                    val newPath = current.path + nextEdge
                    pq.add(
                        SearchNode(
                            versionCode = nextEdge.toVersionCode,
                            currentSha = nextEdge.targetSha256,
                            totalCost = newCost,
                            hopCount = current.hopCount + 1,
                            path = newPath
                        )
                    )
                    Log.d(TAG, "[$TAG] Explored path: ${newPath.joinToString(" -> ") { "${it.fromVersionCode}->${it.toVersionCode}" }} (cost: $newCost bytes)")
                }
            }
        }

        if (optimalResult != null) {
            val chainStr = optimalResult.path.joinToString(" -> ") { "${it.fromVersionCode} -> ${it.toVersionCode}" }
            Log.d(TAG, "[$TAG] Selected chain: $chainStr")
            Log.d(TAG, "[$TAG] Total patch size: ${optimalResult.totalBytes} bytes (${optimalResult.totalBytes / 1024 / 1024} MB)")
        } else {
            Log.d(TAG, "[$TAG] No valid patch chain found from $installedVersionCode to $targetVersionCode")
        }

        return optimalResult
    }

    /**
     * Resolves the best DownloadPayload (Patch, PatchChain, or FullApk) by checking the patch graph.
     */
    fun selectPayload(
        installedVersionCode: Int,
        targetVersionCode: Int,
        installedApkSha: String?,
        targetApkSha: String?,
        patches: List<PatchEntry>,
        fullApkUrl: String,
        fullApkSizeBytes: Long,
        fullApkSha256: String
    ): DownloadPayload {
        Log.d(TAG, "[$TAG] Installed: $installedVersionCode")
        Log.d(TAG, "[$TAG] Latest: $targetVersionCode")
        Log.d(TAG, "[$TAG] Full APK size: $fullApkSizeBytes bytes (${fullApkSizeBytes / 1024 / 1024} MB)")

        val bestPath = findBestPath(
            installedVersionCode = installedVersionCode,
            targetVersionCode = targetVersionCode,
            installedApkSha = installedApkSha,
            targetApkSha = targetApkSha,
            patches = patches
        )

        if (bestPath != null && bestPath.totalBytes < fullApkSizeBytes) {
            if (bestPath.path.size == 1) {
                val direct = bestPath.path[0]
                Log.d(TAG, "[$TAG] Selected payload: PATCH (direct single patch)")
                return DownloadPayload.Patch(
                    url = direct.patchUrl,
                    sizeBytes = direct.patchSizeBytes,
                    sha256 = direct.patchSha256,
                    sourceVersionCode = direct.fromVersionCode,
                    targetVersionCode = direct.toVersionCode,
                    sourceSha256 = direct.sourceSha256,
                    targetSha256 = direct.targetSha256
                )
            } else {
                Log.d(TAG, "[$TAG] Selected payload: PATCH_CHAIN (${bestPath.path.size} patches)")
                return DownloadPayload.PatchChain(
                    patches = bestPath.path,
                    sizeBytes = bestPath.totalBytes,
                    targetVersionCode = targetVersionCode
                )
            }
        }

        if (bestPath != null && bestPath.totalBytes >= fullApkSizeBytes) {
            Log.d(TAG, "[$TAG] Patch chain total size (${bestPath.totalBytes}) >= full APK size ($fullApkSizeBytes), selecting FULL_APK")
        } else {
            Log.d(TAG, "[$TAG] No valid patch chain, selecting FULL_APK")
        }

        Log.d(TAG, "[$TAG] Selected payload: FULL_APK")
        return DownloadPayload.FullApk(
            url = fullApkUrl,
            sizeBytes = fullApkSizeBytes,
            sha256 = fullApkSha256,
            targetVersionCode = targetVersionCode
        )
    }
}
