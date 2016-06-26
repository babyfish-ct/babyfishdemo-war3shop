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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.babyfish.collection.HashMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.UploadService;
import org.babyfishdemo.war3shop.dal.TemporaryUploadedFileRepository;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class UploadServiceImpl implements UploadService {
    
    private static final int GC_DELAY_MINTUES = 60;
    
    private Map<String, Long> uploadFileIds;
    
    @Resource
    private TemporaryUploadedFileRepository temporaryUploadedFileRepository;
    
    @Transactional
    @Override
    public TemporaryUploadedFile getUploadedImage(TemporaryUploadedFile__ ... queryPaths) {
        return this.getAndDeleteUploadedFileByKey(Constants.UPLOADED_KEY_IMAGE, false, queryPaths);
    }
    
    @Transactional
    @Override
    public TemporaryUploadedFile getAndDeleteUploadedImage(TemporaryUploadedFile__ ... queryPaths) {
        return this.getAndDeleteUploadedFileByKey(Constants.UPLOADED_KEY_IMAGE, true, queryPaths);
    }

    @Transactional
    @Override
    public void prepareToUpload() {
        if (Nulls.isNullOrEmpty(uploadFileIds)) {
            return;
        }
        Collection<Long> ids = this.uploadFileIds.values();
        this.uploadFileIds = null;
        this.temporaryUploadedFileRepository.removeTemporaryUploadedFilesByIds(ids);
    }

    @Transactional
    @Override
    public void uploadImage(String mimeType, byte[] image) {
        TemporaryUploadedFile temporaryUploadedFile = new TemporaryUploadedFile();
        JPAEntities.disableAll(temporaryUploadedFile);
        temporaryUploadedFile.setKey(Constants.UPLOADED_KEY_IMAGE);
        temporaryUploadedFile.setMimeType(mimeType);
        temporaryUploadedFile.setContent(image);
        this.uploadFile(temporaryUploadedFile);
    }

    @Transactional
    @Override
    public void cancelUpload(String key) {
        if (Nulls.isNullOrEmpty(this.uploadFileIds)) {
            return;
        }
        Long id = this.uploadFileIds.get(key);
        if (id == null) {
            return;
        }
        this.uploadFileIds.remove(key);
        this.temporaryUploadedFileRepository.removeEntityById(id);
    }
    
    private TemporaryUploadedFile getAndDeleteUploadedFileByKey(String key, boolean delete, TemporaryUploadedFile__ ... queryPaths) {
        if (Nulls.isNullOrEmpty(this.uploadFileIds)) {
            return null;
        }
        Long id = this.uploadFileIds.get(key);
        if (id == null) {
            return null;
        }
        TemporaryUploadedFile temporaryUploadedFile = 
                this
                .temporaryUploadedFileRepository
                .getTemporaryUploadedFileById(id, queryPaths);
        if (delete) {
            this.uploadFileIds.remove(key);
            if (temporaryUploadedFile != null) {
                this.temporaryUploadedFileRepository.removeEntity(temporaryUploadedFile);
            }
        }
        return temporaryUploadedFile;
    }
    
    private void uploadFile(TemporaryUploadedFile temporaryUploadedFile) {
        Arguments.mustBeNull("temporaryUploadedFile.getId()", temporaryUploadedFile.getId());
        Map<String, Long> map = this.uploadFileIds;
        if (map == null) {
            this.uploadFileIds = map = new HashMap<>();
        }
        String key = temporaryUploadedFile.getKey();
        temporaryUploadedFile.setGcThreshold(new Date(System.currentTimeMillis() + GC_DELAY_MINTUES * 60 * 1000));
        long newId = this.temporaryUploadedFileRepository.mergeEntity(temporaryUploadedFile).getId();
        
        Long originalId = map.get(key);
        if (originalId != null) {
            this.temporaryUploadedFileRepository.removeEntityById(originalId);
        }

        map.put(key, newId);
    }
}
