/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.radiolinedemo.utils;

import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

import java.io.IOException;

/**
 * 资源管理类
 *
 * @since 2020-11-17
 */
public class ResourceUtil {
    private static final String TAG = "ResourceUtil";

    private ResourceUtil() {
    }

    /**
     * the function of get drawable
     *
     * @param context the sample context
     * @param resId the resource ID
     * @return the result
     */
    public static String getDrawable(Context context, int resId) {
        if (context == null) {
            return null;
        }
        try {
            return context.getResourceManager().getMediaPath(resId);
        } catch (NotExistException | IOException | WrongTypeException e) {
            //HiWearKitLog.e(TAG,"Exception in getDrawable");
        }
        return null;
    }
}
