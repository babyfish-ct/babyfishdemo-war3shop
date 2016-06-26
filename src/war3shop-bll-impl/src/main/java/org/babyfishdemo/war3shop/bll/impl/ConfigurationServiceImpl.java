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
package org.babyfishdemo.war3shop.bll.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.babyfish.collection.LinkedHashMap;
import org.babyfishdemo.war3shop.bll.ConfigurationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.impl.aop.RequirePermission;
import org.babyfishdemo.war3shop.dal.VariableRepository;
import org.babyfishdemo.war3shop.entities.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {
    
    private static final Cipher RSA_PUBLIC_KEY_CIPHER;
    
    private static final Cipher RSA_PRIVATE_KEY_CIPHER;
    
    private static final String STAR = "*";
    
    @Resource
    private VariableRepository variableRepository;
    
    @Transactional(readOnly = true)
    @Override
    public String getVariable(String variableName) {
        Variable variable = this.variableRepository.getVariableByName(variableName);
        if (variable != null) {
            return getValue(variable);
        }
        return null;
    }
    
    @Transactional(readOnly = true)
    @Override
    public String getVariable(String variableName, String defaultValue) {
        String value = this.getVariable(variableName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    
    @Transactional(readOnly = true)
    @Override
    public Map<String, String> getVariables(String ... variableNames) {
        Map<String, String> map = new LinkedHashMap<>((variableNames.length * 4 + 2) / 3);
        for (String vairableName : variableNames) {
            map.put(vairableName, null);
        }
        for (Entry<String, String> entry : map.entrySet()) {
            entry.setValue(this.getVariable(entry.getKey()));
        }
        return map;
    }
    
    private static String getValue(Variable variable) {
        if (variable.getName().startsWith(STAR)) {
            if (variable.getEncryptedValue() == null) {
                throw new IllegalArgumentException(
                        "The encrypted value of \"" +
                        Variable.class.getName() +
                        "\" must NOT be null when its name starts with \"*\""
                );
            }
            try {
                return new String(RSA_PRIVATE_KEY_CIPHER.doFinal(variable.getEncryptedValue()));
            } catch (IllegalBlockSizeException | BadPaddingException ex) {
                throw new IllegalArgumentException(
                        "Can not decrypt the value" + 
                        variable.getEncryptedValue(), 
                        ex);
            }
        }
        return variable.getValue();
    }
    
    @Transactional(readOnly = true)
    @Override
    public int getVariableInt(String variableName, int defaultValue) {
        String value = this.getVariable(variableName);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    @Transactional
    @Override
    public void setVariable(String variableName, String variableValue) {
        Variable variable = new Variable();
        variable.setName(variableName);
        if (variableName.startsWith(STAR)) {
            byte[] encryptedValue;
            try {
                encryptedValue = RSA_PUBLIC_KEY_CIPHER.doFinal(variableValue.getBytes("utf-8"));
            } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException ex) {
                throw new IllegalArgumentException(
                        "Can not encrypt the value", 
                        ex
                );
            }
            variable.setEncryptedValue(encryptedValue);
        } else {
            variable.setValue(variableValue);
        }
        this.variableRepository.mergeEntity(variable);
    }

    @RequirePermission(Constants.ACCOUNT_MANAGER_PERMISSION)
    @Transactional
    @Override
    public void setSysMailConfiguration(Map<String, String> sysMailConfiguration, String password) {
        if (password != null) {
            this.setVariable(SYS_EMAIL_PASSWORD, password);
        }
        for (String variableName : sysMailConfiguration.keySet()) {
            if (variableName.startsWith(STAR)) {
                throw new IllegalArgumentException(
                        "Can not set  the encrypted variable \""
                        + variableName
                        + "\" directly");
            }
        }
        for (Entry<String, String> entry : sysMailConfiguration.entrySet()) {
            this.setVariable(entry.getKey(), entry.getValue());
        }
    }
    
    static {
        String publicKeyText = 
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnljdDcU8WCbjrcfU9sEZHnf" +
                "DZlMi5Hg987f7MmtwkLN53gWiMFLnH3Sj6lC1A2pvws+BrLXbyzAipn4vqve+lPcWg5P" +
                "TokswWTCR8lbpo1/8GKG9NcgnbR0Tn2ioNhlLrjYLSOr2eFf2KmgZqVRu0yGutAXBd6b" +
                "THbEFWvy8O+lgayAHI+RGobQUnRS/09D0PNDKEzcxq3XbT0uRsUo/ASMBXNGC9aexytz" +
                "qFkhcjmrjEwmHFmfxsqzSCJmTHaPdQJ24y8kVQ9qUYSs0mLKj99H7p7ZqHiQGUuI7J/U" +
                "p9bKbHzEuTc24bUdIpksLfSH9Zh1PeSVHIAD9UWZGlm/mwIDAQAB";
        byte[] publicKeyBytes = new Base64().decode(publicKeyText);  
        KeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
            Cipher cipher = Cipher.getInstance("RSA");  
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            RSA_PUBLIC_KEY_CIPHER = cipher;
        } catch (InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            throw new IllegalArgumentException("Can not create RSA public cipher", ex);
        }
        
        String privateKeyText =
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqeWN0NxTxYJuOtx9T" +
                "2wRked8NmUyLkeD3zt/sya3CQs3neBaIwUucfdKPqULUDam/Cz4GstdvLMCKmfi+q976" +
                "U9xaDk9OiSzBZMJHyVumjX/wYob01yCdtHROfaKg2GUuuNgtI6vZ4V/YqaBmpVG7TIa6" +
                "0BcF3ptMdsQVa/Lw76WBrIAcj5EahtBSdFL/T0PQ80MoTNzGrddtPS5GxSj8BIwFc0YL" +
                "1p7HK3OoWSFyOauMTCYcWZ/GyrNIImZMdo91AnbjLyRVD2pRhKzSYsqP30funtmoeJAZ" +
                "S4jsn9Sn1spsfMS5NzbhtR0imSwt9If1mHU95JUcgAP1RZkaWb+bAgMBAAECggEAQ2+T" +
                "hRSJB1clEhakoOJh4XFIaDO9UPTbHoxKjnvVbaRJmYgqudxlEbnAQq49Tm89N+K1naa/" +
                "jC4S2IFCIgMRSMtFvqYsfy/KcZJvPECOLduJwcjoBBDZySjqXWZbn+8aT5o1gd27jIwY" +
                "AzhOyGwbYdFK9rl8cgj2PBA7TxIN33r3iXU2tos20v82j5szzsv++cfhK1dCM4YdIDV0" +
                "GAz2uyFyqrh2+1Th3X4TOm/pBuUjoqH3oUSS5k4wjhOIyGwNCk5ioezgz77XZdj/coh8" +
                "FHz7lif27S7mM/aB2M23OOuz95knQjCFLWOJWBCoxOmS8D7W/iLyoJ/OZO68aqcQWQKB" +
                "gQDjXxKnlYtcc1UM20+mUpfyxi7Z2eZ+BDIkLZfj80tpDS6gAy1vz3Y4wQLJ+QTplRia" +
                "jiPXVDRM2QQZ6hHtPBwNGQtmIvX46smvgN+VMaImCVeKb1FAys2oSDGIqgQRy14AhomX" +
                "mn58w/pzC/gRwOIsNS93hgdAXnGfDGTa6DWBHwKBgQC/8FTmPuSNx42fLYpANi7P0yKL" +
                "+VNdsgTh0vg0Zj8n4eDa6goE7NZyqO7W9wR51pIdqgoRqlUOaO5vTIGgtgMaro/vcM5y" +
                "dP1jAMAOmOcgwjO53zXE0QKRmkpRysEgZbrj/nKcIJcPjWoFWAdtBZJ+QWAFxrJl/2R2" +
                "GqAC4UiGBQKBgQDDVddh8ADlMRQ9UmCQRF7ULFztvdzeIF6vZBgcttTvcGrA/1nWuAYZ" +
                "5f+uYuhMgFG93iV+hZHU/5Adb1FtXXm/6uc3HQvyiEszIuVyeEyUnuv9O3szzcd3LmaX" +
                "wXBzlSKyEuM8QncuWvPcSMka/3mPfzXDWnr6WukEOhKfWmU1AwKBgEYUAWtvWxNI4LRV" +
                "dDyti7nlZJ+mkIGNFas909GN41E0b1x+Rto1frauZ0WlOsUhSof6JWL9xcBVH3kTfeCn" +
                "k8qSIYtSrI2tmTYkd/rcaMbVIP2kOYV6qm1u3+OtG4YwnmPP6xiF7aGICEWbNlrfxfTC" +
                "+cQiH9CNxqg4YGOszUXhAoGAVMvoD0mt2lu6mwoqjDRO7YZacV6wTq4koHTwOaJct/zd" +
                "EynW0UovedXGtB2jkd+ExBw5/6WHugyKa/VGOVYrjG945eGGHMk1LlPaWTeF721ccZbV" +
                "q8F3U36vS4tuBZPrHwlOaAKj3Ih+XWL8/lLBckrwtB/eW9EBlidy9qAtpVg=";
        byte[] privateKeyBytes = new Base64().decode(privateKeyText);  
        KeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);  
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);
            Cipher cipher = Cipher.getInstance("RSA");  
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            RSA_PRIVATE_KEY_CIPHER = cipher;
        } catch (InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            throw new IllegalArgumentException("Can not create RSA private cipher", ex);
        }
    }
}
