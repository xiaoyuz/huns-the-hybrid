package com.huns.common

import com.huns.chain.common.checkPairKey
import com.huns.chain.common.sign
import com.huns.chain.common.verify
import com.huns.common.model.Transaction
import com.huns.common.model.TransactionReverse
import com.huns.common.model.Operation
import com.huns.common.crypto.SHA256
import com.huns.common.crypto.toHex
import com.huns.common.bean.GenTransactionBody
import com.huns.common.model.TransactionBody

object TransactionHelper {

    fun checkKeyPair(genTransactionBody: GenTransactionBody) =
        checkPairKey(genTransactionBody.privateKey, genTransactionBody.publicKey)

    fun computeTransactionBody(genTransactionBody: GenTransactionBody) = Transaction(genTransactionBody).apply {
        if (base.operation == Operation.ADD.value) {
            base.txId = UUID()
        }
        sign = sign(genTransactionBody.privateKey, genInfoString())
    }.let { TransactionBody(it) }

    fun buildTransaction(transactionBody: TransactionBody) = Transaction(transactionBody).apply {
        timeStamp = System.currentTimeMillis()
        hash = computeHash(genInfoString(), sign)
    }

    fun buildTransactionReverse(transaction: Transaction) = TransactionReverse(transaction).apply {
        if (transaction.base.operation == Operation.ADD.value) {
            base.operation = Operation.DELETE.value
        } else if(transaction.base.operation == Operation.DELETE.value) {
            base.operation = Operation.ADD.value
        }
    }

    fun checkContent(transaction: Transaction): Boolean {
        val operation = transaction.base.operation
        if (operation !in Operation.values().map { it.value }) return false
        return operation != Operation.UPDATE.value && operation != Operation.DELETE.value ||
                transaction.base.txId != null && transaction.base.oldJson != null && transaction.json != null
    }

    fun checkSign(transaction: Transaction) =
        verify(transaction.genInfoString(), transaction.sign, transaction.publicKey)

    fun checkHash(transaction: Transaction) =
        computeHash(transaction.genInfoString(), transaction.sign) == transaction.hash

    private fun computeHash(infoString: String, sign: String) = SHA256.sha256("$infoString>>$sign").toHex()

}