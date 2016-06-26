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

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.babyfishdemo.war3shop.bll.UploadService;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/upload")
public class UploadController {
    
    @Resource
    private UploadService uploadService;
    
    @RequestMapping("/prepare-to-upload")
    public JsonpModelAndView prepareToUnpload() {
        this.uploadService.prepareToUpload();
        return new JsonpModelAndView(null);
    }
    
    @RequestMapping("/upload-image")
    public void uploadImage(
            @RequestParam("key") String key,
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
        Collection<MultipartFile> files = multipartRequest.getFileMap().values();
        if (!files.isEmpty()) {
            MultipartFile file = files.iterator().next();
            String mimeType = file.getOriginalFilename();
            int dotIndex = mimeType.lastIndexOf('.');
            if (dotIndex == -1) {
                throw new IllegalArgumentException(
                        "The can not determine the mime type from the file name \"" +
                        mimeType +
                        "\"");
            }
            mimeType = mimeType.substring(dotIndex + 1).toLowerCase();
            switch (mimeType) {
            case "png":
                mimeType = "image/png";
                break;
            case "jpg":
                mimeType = "image/jpg";
                break;
            case "jpeg":
                mimeType = "image/jpeg";
                break;
            case "gif":
                mimeType = "image/gif";
                break;
            case "bmp":
                mimeType = "image/bmp";
                break;
            default:
                throw new IllegalArgumentException(
                        "The can not determine the mime type from the file name \"" +
                        mimeType +
                        "\"");
            }
            this.uploadService.uploadImage(mimeType, file.getBytes());
        }
    }
    
    @RequestMapping("/cancel-upload")
    public void cancelUpload(@RequestParam("key") String key) {
        this.uploadService.cancelUpload(key);
    }
}
