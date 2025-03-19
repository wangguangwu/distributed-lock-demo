package com.wangguangwu.distributedlockredis.constants;

/**
 * @author wangguangwu
 */
public final class LockConstants {

    private LockConstants() {
    }

    public static final String LOCK_SUCCESS = "OK";

    public static final String LOCK_FAIL = "FAIL";

    public static final String UNLOCK_SUCCESS = "UNLOCK_SUCCESS";

    public static final String UNLOCK_FAIL = "UNLOCK_FAIL";

    public static final long DEFAULT_LOCK_TIME_PX = 10000;

    public static final long DEFAULT_WAIT_TIME_SEC = 5;

    public static final long DEFAULT_LEASE_TIME_SEC = 10;
}
