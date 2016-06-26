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
package org.babyfishdemo.war3shop.web.captcha;

import java.io.IOException;
import java.io.OutputStream;

import com.github.bingoohuang.patchca.service.CaptchaService;
import com.github.bingoohuang.patchca.utils.encoder.EncoderHelper;

/**
 * @author Tao Chen
 */
public class CaptchaHolder {
    
    private CaptchaService captchaService;
    
    private String token;

    public CaptchaService getCaptchaService() {
        return captchaService;
    }

    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    public void generateImage(OutputStream os) throws IOException {
        this.token = EncoderHelper.getChallangeAndWriteImage(this.captchaService, "png", os);
    }

    public void validate(String token) {
        if (token == null) {
            throw new IllegalArgumentException("The captcha is null");
        }
        String oldToken = this.token;
        if (oldToken == null) {
            throw new IllegalStateException("The current captcha is not ready");
        }
        this.token = null;
        if (!oldToken.equals(token)) {
            throw new IllegalArgumentException("The captcha is wrong");
        }
    }
}
