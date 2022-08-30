package com.huns.common.model

data class TransactionReverse(
    var base: TransactionBase = TransactionBase()
) : java.io.Serializable {
    constructor(transaction: Transaction) : this(
        base = transaction.base
    )
}