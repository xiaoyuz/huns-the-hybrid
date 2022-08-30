package com.huns.common.exception

enum class Errors(
    var code: String,
    var message: String
) {
    INVALID_PARAM_ERROR("101", "invalid param error"),
    AES_ENCRYPT_ERROR("103", "AES encrypt error"),
    ECDSA_ENCRYPT_ERROR("104", "ECDSA encrypt error"),
    SIGN_ERROR("105", "sign error"),
    GENERATE_SIGN_ERROR("106", "generate sign error"),
    GENERATE_SQL_ERROR("107", "generate sql error"),
    VERIFY_SIGN_ERROR("108", "verify sign error"),
    VERIFY_HASH_ERROR("109", "verify hash error"),
    DUPLICATE_TRANSACTION_ERROR("110", "duplicate transaction error"),
    VERIFY_MERKLE_ROOT_ERROR("109", "merkle root error"),

    BLOCK_BODY_MISSING_ERROR("201", "block body missing"),
    EMPTY_TRANSACTIONS_ERROR("202", "empty transactions"),
    DIFFERENT_PUBLIC_KEY_ERROR("203", "different public key error"),
    PERMISSION_CHECK_ERROR("204", "permission check error"),
    NO_PREV_BLOCK_ERROR("205", "no previous block"),
    BLOCK_NUM_MISMATCH_ERROR("206", "block number mismatch"),
    BLOCK_TIME_MISMATCH_ERROR("206", "block time mismatch"),

    INVALID_KEY_PAIR("301", "invalid key pair"),
    INVALID_CONTENT("302", "invalid content"),
}