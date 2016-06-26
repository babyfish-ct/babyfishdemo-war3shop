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
package org.babyfishdemo.war3shop.dal;

import java.util.Collection;
import java.util.Date;

import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile;
import org.babyfishdemo.war3shop.entities.TemporaryUploadedFile__;

/**
 * @author Tao Chen
 */
public interface TemporaryUploadedFileRepository extends AbstractRepository<TemporaryUploadedFile, Long> {
    
    TemporaryUploadedFile getTemporaryUploadedFileById(Long id, TemporaryUploadedFile__ ... queryPaths);

    void removeTemporaryUploadedFilesByIds(Collection<Long> ids);
    
    void removeTemporaryUploadedFilesByGcThreshold(Date gcThreshold);
}
