/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.radiolinedemo.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.Locale;

/**
 * Log utils
 *
 * @since 2020-03-01
 */
public class LogUtils {
    private static final String TAG_LOG = "Idream";

    private static final int DOMAIN_ID = 0xD000F00;

    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, DOMAIN_ID, LogUtils.TAG_LOG);

    private static final String LOG_FORMAT = "%{public}s: %{public}s";
    private static final String DEFAULT_STR = "null";

    private LogUtils() {
        /* Do nothing */
    }

    /**
     * Print debug log
     *
     * @param tag log tag
     * @param msg log message
     */
    public static void fatal(String tag, String msg) {
        HiLog.fatal(LABEL_LOG, LOG_FORMAT, tag, msg);
    }

    /**
     * Print debug log
     *
     * @param tag log tag
     * @param msg log message
     */
    public static void debug(String tag, String msg) {
        HiLog.debug(LABEL_LOG, LOG_FORMAT, tag, msg);
    }

    /**
     * Print debug log
     *
     * @param classType class name
     * @param format    format
     * @param args      args
     */
    public static void debug(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.debug(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print info log
     *
     * @param tag log tag
     * @param msg log message
     */
    public static void info(String tag, String msg) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, tag, msg);
    }

    /**
     * Print info log
     *
     * @param className class name
     * @param format    format
     * @param args      args
     */
    public static void info(String className, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.info(LABEL_LOG, LOG_FORMAT, className, buffMsg);
    }

    /**
     * Print info log
     *
     * @param classType class name
     * @param format    format
     * @param args      args
     */
    public static void info(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.info(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print warn log
     *
     * @param tag log tag
     * @param msg log message
     */
    public static void warn(String tag, String msg) {
        HiLog.warn(LABEL_LOG, LOG_FORMAT, tag, msg);
    }

    /**
     * Print warn log
     *
     * @param classType class name
     * @param format    format
     * @param args      args
     */
    public static void warn(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.warn(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print error log
     *  @param tag log tag
     * @param msg log message
     */
    public static void error(HiLogLabel tag, String msg) {
        HiLog.error(LABEL_LOG, LOG_FORMAT, tag, msg);
    }

    /**
     * Print error log
     *
     * @param classType class name
     * @param format    format
     * @param args      args
     */
    public static void error(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.error(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print error log
     *
     * @param tag    log tag
     * @param format format
     * @param args   args
     */
    public static void error(String tag, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.error(LABEL_LOG, LOG_FORMAT, tag, buffMsg);
    }

    public static void warn(HiLogLabel tag, String volumn) {
    }
}
