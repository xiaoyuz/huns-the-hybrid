package com.huns.chain.common.merkle

import com.huns.common.crypto.SHA256
import com.huns.common.crypto.toHex
import com.huns.common.exception.Errors
import com.huns.common.exception.KeyException

data class MerkleTree(
    var transactionHashes: List<String>
) {

    data class Pos(var level: Int, var offset: Int)
    
    private val treeData = mutableListOf<List<String>>()
    private val treePos = mutableMapOf<String, Pos>()
    private var root: String = ""

    private fun addDatas(level: Int, hashes: List<String>) {
        treeData.add(hashes)
        hashes.forEachIndexed { index, s ->
            treePos[s] = Pos(level, index)
        }
    }

    fun build() {
        var level = 0
        addDatas(level, transactionHashes.toMutableList())
        var lastLevelData = treeData.last()
        do {
            level++
            var index = 0
            val tempData = mutableListOf<String>()
            while (index < lastLevelData.size) {
                val left = lastLevelData[index]
                index++
                var right = left
                if (index < lastLevelData.size) {
                    right = lastLevelData[index]
                    index++
                }
                val data = SHA256.sha256(left + right).toHex()
                tempData.add(data)
            }
            addDatas(level, tempData)
            lastLevelData = treeData.last()
        } while (lastLevelData.size > 1)
        if (lastLevelData.size == 1) root = lastLevelData.first()
    }

    fun treeData() = treeData

    fun root() = root

    fun hash(level: Int, offset: Int): String {
        if (level >= treeData.size) throw KeyException(Errors.INVALID_PARAM_ERROR)
        if (offset >= treeData[level].size) throw KeyException(Errors.INVALID_PARAM_ERROR)
        return treeData[level][offset]
    }

    fun genMerklePath(hash: String): List<String> {
        // Result is like "$hash-$relative", split by '-'
        // relative: 0 means the returned hash is left node, 1 means right
        var pos = treePos[hash] ?: return emptyList()
        val result = mutableListOf<String>()
        while (pos.level < treeData.size - 1) {
            val levelDatas = treeData[pos.level]
            val tempHash = levelDatas[pos.offset]
            val (brotherOffset, related) = if (pos.offset % 2 == 0) (pos.offset + 1) to 1 else (pos.offset - 1) to 0
            val res = if (brotherOffset >= levelDatas.size) "$tempHash-$related" else "${levelDatas[brotherOffset]}-$related"
            result.add(res)
            pos = Pos(pos.level + 1, pos.offset / 2)
        }
        return result
    }
}


fun main() {
    val txes = listOf(
        "abc",
        "sgre",
        "retwer",
        "cvxbf",
        "jhj",
    )
    val merkleTree = MerkleTree(txes)
    merkleTree.build()
    println(merkleTree.treeData())
    println(merkleTree.root())

    val testedHash = "jhj"

    val merklePath = merkleTree.genMerklePath(testedHash)
    println(merklePath)
    var computeHash = testedHash
    merklePath.forEach {
        val (brotherHash, relative) = it.split("-")
        val sumStr = if (relative.toInt() == 0) brotherHash + computeHash else computeHash + brotherHash
        computeHash = SHA256.sha256(sumStr).toHex()
    }
    println(computeHash)
}