package com.android.jtwallet.qrCode;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import static android.graphics.Color.WHITE;

public class QrCodeGenerator {

    /**
     * 生成二维码
     *
     * @param content        字符串内容
     * @param widthAndHeight 二维码尺寸
     * @param color          二维码颜色
     * @return
     * @throws WriterException
     */
    public static Bitmap getQrCodeImage(String content, int widthAndHeight, int color) {
        return getQrCodeImage(content, widthAndHeight, Bitmap.Config.ARGB_8888, color);
    }


    /**
     * 生成二维码
     *
     * @param data           字符串内容
     * @param widthAndHeight 二维码图片尺寸
     * @param config         图片模式
     * @return
     * @throws WriterException
     */
    public static Bitmap getQrCodeImage(String data, int widthAndHeight, Bitmap.Config config, int color) {

        if (data == null || data.length() == 0) {
            return null;
        }
        Map<EncodeHintType, Object> hintsMap = new HashMap<>(3);
        hintsMap.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hintsMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hintsMap.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hintsMap);
            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] pixels = new int[w * h];
            //画黑点
            for (int y = 0; y < w; y++) {
                for (int x = 0; x < w; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * w + x] = color; //0xff000000
                    } else {
                        pixels[y * w + x] = WHITE;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析Bitmap中的二维码
     *
     * @param bitmap
     * @return 解析结果，null表示解析失败
     */
    public static String decodeQrImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        try {
            final Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result = new QRCodeReader().decode(binaryBitmap, hints);

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
