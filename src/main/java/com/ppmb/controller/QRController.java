package com.ppmb.controller;

import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeGenWrapper;
import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@RestController
public class QRController {
    @GetMapping("/qrcode")
    public void qrcode(@RequestParam("url") String url, HttpServletResponse resp) {
        HashMap map = new HashMap();
        map.put(EncodeHintType.CHARACTER_SET, "utf-8");
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        map.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300, map);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0XFFFFFFFF);
            }
        }
        try {
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logo")
    public void logo(@RequestParam("url") String url, HttpServletResponse resp, HttpServletRequest req) {
        HashMap map = new HashMap();
        map.put(EncodeHintType.CHARACTER_SET, "utf-8");
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        map.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = null;
        BufferedImage image = null;
        Part logo = null;
        try {
            matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300, map);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0XFFFFFFFF);
            }
        }
        try {
            ;
            try {
                logo = req.getPart("logo");
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
            // 通过Part对象获取输入流
            InputStream inputStream = logo.getInputStream();
            // 通过ImageIO的read方法，从输入流中读取，从而获得logo图片
            Image logoImage = ImageIO.read(inputStream);
            // 获取logo图片的宽度
            int logoWidth = logoImage.getWidth(null);
            // 获取logo图片的高度
            int logoHeight = logoImage.getHeight(null);
            // 如果logo的宽度或者高度大于100，则重新赋值100
            if (logoWidth > 60) {
                logoWidth = 60;
            }
            if (logoHeight > 60) {
                logoHeight = 60;
            }
            // 使用平滑缩放算法对原logo图像进行缩放得到一个全新的图像。
            Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);

            // 第二部分：将缩放后的logo画到黑白二维码上
            // 获取2D画笔
            Graphics2D graphics2D = image.createGraphics();
            // 开始画的x和y坐标
            int x = (300 - logoWidth) / 2;
            int y = (300 - logoHeight) / 2;
            // 将缩放后的logo画上去
            graphics2D.drawImage(scaledLogo, x, y, null);
            // 创建一个具有指定位置、宽度、高度和圆角半径的圆角矩形。这个圆角矩形是用来绘制边框的。
            Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoHeight, 10, 10);
            // 使用一个宽度为4像素的基本笔触
            graphics2D.setStroke(new BasicStroke(4f));
            // 给logo画圆角矩形
            graphics2D.draw(shape);
            // 释放画笔
            graphics2D.dispose();

            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/github")
    public void githubQR(HttpServletRequest req, HttpServletResponse resp) {
        String url = req.getParameter("url");
        BufferedImage image = null;
        try {
            image = QrCodeGenWrapper.of(url).asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/test01")
    public void test(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String url = req.getParameter("url");
        try {
            BufferedImage image = QrCodeGenWrapper.of(url)
                    .setLogo(req.getPart("logo").getInputStream())
                    .setLogoRate(7) // 设置 logo 图片与二维码之间的比例。在这个例子中，它设置为 7，表示 logo 的宽度等于二维码的 1/7。
                    .setLogoStyle(QrCodeOptions.LogoStyle.ROUND) // 设置 logo 图片的样式。设置为 ROUND，表示将 logo 的边框形状设置为圆形。
                    .asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/test02")
    public void test02(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String url = req.getParameter("url");
        try {
            BufferedImage image = QrCodeGenWrapper.of(url)
                    .setDrawPreColor(Color.BLUE)
                    .asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/test03")
    public void test03(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String url = req.getParameter("url");
        try {
            BufferedImage image = QrCodeGenWrapper.of(url)
                    .setBgImg(req.getPart("logo").getInputStream())
                    .setBgOpacity(0.7F)
                    .asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/test04")
    public void test04(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String url = req.getParameter("url");
        try {
            BufferedImage image = QrCodeGenWrapper.of(url)
                    .setDrawEnableScale(true) // 启用二维码绘制时的缩放功能
                    .setDrawStyle(QrCodeOptions.DrawStyle.DIAMOND) // 指定绘制样式
                    .asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/test05")
    public void test05(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String url = req.getParameter("url");
        try {
            BufferedImage image = QrCodeGenWrapper.of(url)
                    .setErrorCorrection(ErrorCorrectionLevel.H) // 设置二维码的错误纠正级别
                    .setDrawStyle(QrCodeOptions.DrawStyle.IMAGE) // 绘制样式采用图片填充
                    .addImg(1, 1, req.getPart("logo").getInputStream()) // 添加图片
                    .asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/test06")
    public void test06(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String url = req.getParameter("url");
        try {
            BufferedImage image = QrCodeGenWrapper.of(url)
                    .setW(500)
                    .setH(500)
                    .setBgImg(req
                            .getPart("logo").getInputStream())
                    .setBgOpacity(0.6f)
                    .setPicType("gif")
                    .asBufferedImage();
            ImageIO.write(image, "png", resp.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
}
