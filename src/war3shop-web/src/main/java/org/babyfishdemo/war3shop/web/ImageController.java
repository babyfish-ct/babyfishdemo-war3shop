/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfishdemo.war3shop.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.ProductService;
import org.babyfishdemo.war3shop.bll.UploadService;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.User__;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/image")
public class ImageController {
    
    private static final int UNKNOWN_USER = 1 << 0;
    
    private static final int UNKNOWN_GRAY = 1 << 1;

    @Resource
    private AuthorizationService authorizationService;
    
    @Resource
    private ProductService productService;
    
    @Resource
    private UploadService uploadService;
    
    // The parameter "boolean gray" is used to gernate gray image.
    //
    // Ideally, CSS3 supports grayscale for image, like this:
    // 
    // img.grayscale {
    //          -webkit-filter: grayscale(100%); /* For Chrome */
    //          -moz-filter: grayscale(100%); /* Not work, hope for Firefox, but not implemented */
    //          -ms-filter: grayscale(100%); /* Not work, Hope for IE, but not implemented */
    //          -o-filter: grayscale(100%); /* For Opera */
    //          filter: grayscale(100%); /* W3C */
    //          filter: gray; /* For IE6-9 */
    //          filter:url('grayscale.svg#grayscale'); /* For Firefox */
    // }
    //
    // Specially, for Firefox, it needs another file "grayscale.svg", like this
    //
    // <svg xmlns="http://www.w3.org/2000/svg">  
    //      <filter id="grayscale">  
    //          <feColorMatrix 
    //                  type="matrix" 
    //                  values="
    //                      0.3333 0.3333 0.3333 0      0
    //                      0.3333 0.3333 0.3333 0      0
    //                      0.3333 0.3333 0.3333 0      0 
    //                      0      0      0      1      0"/>  
    //      </filter>  
    // </svg>
    //
    // Unfortunately, this powerful solution still can not run all browsers, such as
    // IE10, IE11, Safri(version < 7). 
    // 
    // So, I had to give up CSS3 and implement this work in server-side
    // (Another choice is to create the gray image by javascript via canvas/Context2D API, 
    // but I think do it by Java is more simple for browser cache mechanism)
    // Angry!!!
    @RequestMapping("/user-image")
    public void userImage(
            @RequestParam("id") long id,
            @RequestParam(value = "gray", defaultValue = "false") boolean gray,
            HttpServletResponse response) throws IOException {
        User user = this.authorizationService.getUserById(id, User__.begin().image().end());
        if (user == null || user.getImageMimeType() == null || user.getImage() == null) {
            renderUnknownImage(response, UNKNOWN_USER | (gray ? UNKNOWN_GRAY : 0));
        } else {
            if (gray) {
                renderGrayImage(user.getImageMimeType(), new ByteArrayInputStream(user.getImage()), response);
            } else {
                renderImage(user.getImageMimeType(), user.getImage(), response);
            }
        }
    }
    
    @RequestMapping("/product-image")
    public void productImage(
            @RequestParam("id") long id,
            HttpServletResponse response) throws IOException {
        Product product = this.productService.getProductById(id, Product__.begin().image().end());  
        if (product == null) {
            renderUnknownImage(response, 0);
        } else {
            renderImage(product.getImageMimeType(), product.getImage(), response);
        }
    }
    
    @RequestMapping("/manufacturer-image")
    public void manufacturerImage(
            @RequestParam("id") long id,
            HttpServletResponse response) throws IOException {
        Manufacturer manufacturer = this.productService.getManufacturerById(id, Manufacturer__.begin().image().end());  
        if (manufacturer == null) {
            renderUnknownImage(response, 0);
        } else {
            renderImage(manufacturer.getImageMimeType(), manufacturer.getImage(), response);
        }
    }
    
    @RequestMapping("/uploaded-image")
    public void uploadImage(HttpServletResponse response) throws IOException {
        TemporaryUploadedFile temporaryUploadedFile = 
                this.uploadService.getUploadedImage(TemporaryUploadedFile__.begin().content().end());
        if (temporaryUploadedFile == null) {
            renderUnknownImage(response, 0);
        } else if (!temporaryUploadedFile.getMimeType().startsWith("image/")) {
            renderUnknownImage(response, 0);
        } else {
            renderImage(temporaryUploadedFile.getMimeType(), temporaryUploadedFile.getContent(), response);
        }
    }
    
    @RequestMapping("/current-user-image")
    public void currentUserImage(HttpServletResponse response) throws IOException {
        User user = this.authorizationService.getCurrentUser(User__.begin().image().end());
        if (user == null || user.getImageMimeType() == null || user.getImage() == null) {
            renderUnknownImage(response, 0);
        } else {
            renderImage(user.getImageMimeType(), user.getImage(), response);
        }
    }
    
    private static void renderImage(
            String mimeType, 
            byte[] image, 
            HttpServletResponse response) throws IOException {
        response.setContentType(mimeType);
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(image);
        }
    }
    
    private static void renderGrayImage(
            String mimeType, 
            InputStream inputStream, 
            HttpServletResponse response) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        int[] rgbs = bufferedImage.getRGB(
                0, 
                0, 
                bufferedImage.getWidth(), 
                bufferedImage.getHeight(), 
                null, 
                0, 
                bufferedImage.getWidth());
        for (int i = rgbs.length - 1; i >= 0; i--) {
            int rgb = rgbs[i];
            int r = rgb & 0xFF;
            int g = (rgb >>> 8) & 0xFF;
            int b = (rgb >>> 16) & 0xFF;
            int alphaMask = rgb & 0xFF000000;
            int avg = (r + g + b) / 3;
            rgbs[i] = alphaMask | (avg << 16) | (avg << 8) | avg;
        }
        bufferedImage.setRGB(
                0, 
                0, 
                bufferedImage.getWidth(), 
                bufferedImage.getHeight(), 
                rgbs, 
                0, 
                bufferedImage.getWidth());
        try (OutputStream outputStream = response.getOutputStream()) {
            int slashIndex = mimeType.lastIndexOf('/');
            ImageIO.write(bufferedImage, slashIndex != -1 ? mimeType.substring(slashIndex + 1) : mimeType, outputStream);
        }
    }
    
    private static void renderUnknownImage(HttpServletResponse response, int unknownFlags) throws IOException {
        response.setContentType("image/png");
        byte[] buf = new byte[1024];
        try (OutputStream outputStream = response.getOutputStream()) {
            String name = (unknownFlags & UNKNOWN_USER) != 0 ? "unknown-user.png" : "unknown.png";
            try (InputStream inputStream = ImageController.class.getClassLoader().getResourceAsStream(name)) {
                if ((unknownFlags & UNKNOWN_GRAY) != 0) {
                    renderGrayImage("png", inputStream, response);
                } else {
                    while (true) {
                        int len = inputStream.read(buf);
                        if (len == -1) {
                            break;
                        }
                        outputStream.write(buf, 0, len);
                    }
                }
            }
        }
    }
}
