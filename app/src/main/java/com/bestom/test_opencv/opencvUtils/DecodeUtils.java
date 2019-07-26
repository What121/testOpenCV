package com.bestom.test_opencv.opencvUtils;

import com.dtr.zbar.build.ZBarDecoder;

public class DecodeUtils {
    private ZBarDecoder zBarDecoder;

    public DecodeUtils() {
        zBarDecoder=new ZBarDecoder();
    }

    public synchronized String decode(byte[] data,int width,int height) {
        // long start = System.currentTimeMillis();
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
//        byte[] rotatedData = data;
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
//
//        // 宽高也要调整
        int tmp = width;
        width = height;
        height = tmp;

        String result =null;
        if (zBarDecoder != null) {
            try {
                //result = zBarDecoder.decodeCrop(rotatedData, width, height, mCropRect.left, mCropRect.top, mCropRect.width(), mCropRect.height());
                result = zBarDecoder.decodeCrop(rotatedData, width, height, 0, 0, width, height);
//                result = zBarDecoder.decodeRaw(rotatedData, width, height);
            }catch (Exception ex){
                ex.printStackTrace();
                zBarDecoder=null;
            }
        }

        return result;
    }


}
