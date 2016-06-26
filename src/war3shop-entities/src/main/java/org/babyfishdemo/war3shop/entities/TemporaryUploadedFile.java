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
package org.babyfishdemo.war3shop.entities;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "TEMPORARY_UPLOADED_FILE")
@SequenceGenerator(
        name = "temporaryUploadedFileSequence",
        sequenceName = "TEMPORARY_UPLOADED_FILE_ID_SEQ", //Oh, reach the max identifier length or Oracle.
        initialValue = 1,
        allocationSize = 1
)
public class TemporaryUploadedFile {

    @Id
    @Column(name = "TEMPORARY_UPLOADED_FILE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "temporaryUploadedFileSequence")
    private Long id;
    
    @Column(name = "GC_THRESHOLD")
    @Temporal(TemporalType.TIMESTAMP)
    private Date gcThreshold;
    
    @Column(name = "MIME_TYPE", length = 20, nullable = false)
    private String mimeType;
    
    @Column(name = "KEY", length = 20, nullable = false)
    private String key;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "CONTENT", nullable = false)
    private byte[] content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGcThreshold() {
        return gcThreshold;
    }

    public void setGcThreshold(Date gcThreshold) {
        this.gcThreshold = gcThreshold;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
