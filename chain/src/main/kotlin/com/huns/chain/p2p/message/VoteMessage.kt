package com.huns.chain.p2p.message

import com.huns.chain.pbft.model.VoteData

data class VoteMessage(
    var common: CommonInfo = CommonInfo(),
    var voteData: VoteData = VoteData()
) : java.io.Serializable
