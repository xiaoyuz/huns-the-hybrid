package com.huns.chain.core

const val STORAGE_PUT = "storage.put.block"
const val STORAGE_GET = "storage.get.block"
const val STORAGE_REMOVE = "storage.remove.block"

const val BLOCK_INSERT = "block.insert"
const val BLOCK_GENERATE = "block.generate"
const val BLOCK_LAST_BLOCK = "block.last.block"
const val BLOCK_QUERY_BLOCK = "block.query.block"
const val BLOCK_QUERY_NEXT_BLOCK = "block.query.next.block"

const val TRANSACTION_QUERY_BLOCK = "transaction.query.block"
const val TRANSACTION_SUBMIT_POOL = "transaction.submit.pool"

const val FETCHER_CHECK_TRANSACTIONS_PERMISSION = "fetcher.check.instructions.permission"
const val FETCHER_CHECK_BLOCK_PERMISSION = "fetcher.check.block.permission"

const val P2P_CONNECT_NODES = "p2p.connect.nodes"
const val P2P_BROADCAST = "p2p.broadcast"
const val P2P_BROADCAST_INCLUDE_SELF = "p2p.broadcast.include.self"
const val P2P_PING = "p2p.ping"
const val P2P_SEND = "p2p.send"

const val PBFT_PUSH_VOTE = "pbft.push.vote"
const val PBFT_PUSH_CACHE = "pbft.push.cache"
const val PBFT_POP_CACHE = "pbft.pop.cache"

const val STORAGE_CODE_SUCCESS = 0
const val STORAGE_CODE_KEY_NOT_EXIST = -1

const val STORAGE_CODE_DB_ERROR = -100
const val BLOCK_CODE_ERROR = -200
const val P2P_CODE_ERROR = -300
const val FETCH_CODE_ERROR = -400
const val PBFT_CODE_ERROR = -500
const val TRANSACTION_CODE_ERROR = -600