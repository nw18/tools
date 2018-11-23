package com.newind.util;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

public class TextUtil {
	public static boolean equal(String a,String b){
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}

	public static boolean isEmpty(String s){
		return s == null || s.length() == 0;
	}

	/**
	 * 生成包含字符串信息的二维码图片
	 * @param content 二维码携带信息
	 * @param qrCodeSize 二维码图片大小
	 * @throws WriterException
	 * @throws IOException
	 */
	public static BufferedImage createQrCode(String content, int qrCodeSize) throws WriterException {
		//设置二维码纠错级别ＭＡＰ
		Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  // 矫错级别
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		//创建比特矩阵(位矩阵)的QR码编码的字符串
		BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
		// 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
		int matrixWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, matrixWidth, matrixWidth);
		// 使用比特矩阵画并保存图像
		graphics.setColor(Color.BLACK);
		for (int i = 0; i < matrixWidth; i++){
			for (int j = 0; j < matrixWidth; j++){
				if (byteMatrix.get(i, j)){
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		return image;
	}
}
