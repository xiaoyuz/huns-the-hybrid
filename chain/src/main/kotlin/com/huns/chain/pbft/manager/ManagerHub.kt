package com.huns.chain.pbft.manager

class ManagerHub(
    private var prePrepareManager: PrePrepareManager? = null,
    private var prepareManager: PrepareManager? = null,
    private var commitManager: CommitManager? = null
) {

    fun prePrepareConfirmed(hash: String, number: Int) = prePrepareManager?.hasConfirmed(hash, number) ?: false

    fun prepareConfirmed(hash: String, number: Int) = prepareManager?.hasConfirmed(hash, number) ?: false

    fun commitConfirmed(hash: String, number: Int) = commitManager?.hasConfirmed(hash, number) ?: false

    fun getBlockByHash(hash: String) = prePrepareManager?.getBlockByHash(hash)
}