/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.radiolinedemo.utils;

import ohos.agp.colors.RgbColor;
import ohos.agp.components.ComponentState;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.components.element.StateElement;
import ohos.app.Context;
import ohos.global.resource.Resource;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

import java.io.IOException;

/**
 * 图片工具类
 *
 * @since 2020-11-17
 */
public class ImageTools {
    private ImageTools() {
    }

    /**
     * 返回一个矩形
     *
     * @param cornerRadius 矩形的圆角半径
     * @param rgbColor     矩形的颜色
     * @return 矩形
     */
    public static ShapeElement getRectangleShape(float cornerRadius, RgbColor rgbColor) {
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setShape(ShapeElement.RECTANGLE);
        shapeElement.setCornerRadius(cornerRadius);
        shapeElement.setRgbColor(rgbColor);
        return shapeElement;
    }

    /**
     * 返回一个矩形
     *
     * @param cornerRadius 矩形的圆角半径
     * @return 矩形
     */
    public static ShapeElement getRectangleShape(float cornerRadius) {
        ShapeElement shapeElement = new ShapeElement();
        shapeElement.setShape(ShapeElement.RECTANGLE);
        shapeElement.setCornerRadius(cornerRadius);
        return shapeElement;
    }

    /**
     * 返回根据不同的状态显示不同的图片的Element
     *
     * @param context     上下文
     * @param normalPicId 平常状态的时候的图片
     * @param pressPicId  选中状态的时候的图片
     * @return 状态 Element
     */
    public static StateElement getSelectedElement(Context context, int normalPicId, int pressPicId) {
        try {
            PixelMapElement normalPixelMapElement = new PixelMapElement(getPixelMap(context, normalPicId));
            PixelMapElement selectedPixelMapElement = new PixelMapElement(getPixelMap(context, pressPicId));

            // 状态Element
            StateElement stateElement = new StateElement();
            stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_PRESSED}, selectedPixelMapElement);
            stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, normalPixelMapElement);
            return stateElement;
        } catch (Exception e) {
            //HiWearKitLog.e("Exception--->" + e.toString());
        }
        return null;
    }

    /**
     * 返回根据不同的状态显示不同的图片的Element
     *
     * @param context 上下文
     * @return 状态 Element
     */
    public static StateElement getSelectedElement(Context context, RgbColor normal, RgbColor press) {
        try {
            // 状态Element
            StateElement stateElement = new StateElement();
            stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_PRESSED}, getRectangleShape(0, press));
            stateElement.addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, getRectangleShape(0, normal));
            return stateElement;
        } catch (Exception e) {
            //HiWearKitLog.e("Exception--->" + e.toString());
        }
        return null;
    }

    /**
     * 根据传入的图片id返回图片背景
     *
     * @param context    上下文
     * @param drawableId 图片id
     * @return 图片背景
     */
    public static Element getPixelMapElement(Context context, int drawableId) {
        PixelMapElement drawable = new PixelMapElement(getPixelMap(context, drawableId));
        return drawable;
    }

    public static PixelMap getPixelMap(Context context, int drawableId) {
        String drawingPath = ResourceUtil.getDrawable(context, drawableId);
        ImageSource.SourceOptions srcOpts = new ImageSource.SourceOptions();
        srcOpts.formatHint = "image/jpg";
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.ARGB_8888;
        Resource assrt = null;
        try {
            assrt = context.getResourceManager().getRawFileEntry(drawingPath).openRawFile();
        } catch (IOException e) {
        }
        ImageSource source = ImageSource.create(assrt, srcOpts);
        PixelMap pixelMap = source.createPixelmap(decodingOptions);
        return pixelMap;
    }

    public static Size getPixelMapSize(Context context, int drawableId) {
        String drawingPath = ResourceUtil.getDrawable(context, drawableId);
        ImageSource.SourceOptions srcOpts = new ImageSource.SourceOptions();
        srcOpts.formatHint = "image/jpg";
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.ARGB_8888;
        Resource assrt = null;
        try {
            assrt = context.getResourceManager().getRawFileEntry(drawingPath).openRawFile();
        } catch (IOException e) {
        }
        ImageSource source = ImageSource.create(assrt, srcOpts);
        PixelMap pixelMap = source.createPixelmap(decodingOptions);
        return pixelMap.getImageInfo().size;
    }
}
