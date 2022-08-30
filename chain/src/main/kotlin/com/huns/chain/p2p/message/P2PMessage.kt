package com.huns.chain.p2p.message

const val PING: Byte = 0
const val GEN_BLOCK_COMPLETE_REQ: Byte = 1
const val GENERATE_COMPLETE_RESPONSE: Byte = -1
const val GEN_BLOCK_REQ: Byte = 2
const val GENERATE_BLOCK_RESPONSE: Byte = -2
const val TOTAL_BLOCK_REQ: Byte = 3
const val TOTAL_BLOCK_RESP: Byte = -3
const val BLOCK_REQ: Byte = 4
const val BLOCK_RESP: Byte = -4
const val NEXT_BLOCK_REQ: Byte = 5
const val NEXT_BLOCK_RESP: Byte = -5

const val PBFT_VOTE: Byte = 10

data class P2PMessage(
    var type: Byte = 0,
    var data: String = ""
) : java.io.Serializable