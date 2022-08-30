package com.huns.chain.pbft.model

import com.huns.chain.block.model.Block

const val VOTE_PREPREPARE: Byte = 1
const val VOTE_PREFARE: Byte = 2
const val VOTE_COMMIT: Byte = 3

data class VoteData(
    var voteType: Byte = 0,
    var hash: String = "",
    var number: Int = 0,
    var appId: String = "",
    var agree: Boolean = false,
    var block: Block? = null
) : java.io.Serializable
