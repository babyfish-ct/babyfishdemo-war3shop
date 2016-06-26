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
import java.util.List;

import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;

/**
 * @author Tao Chen
 */
public interface ManufacturerRepository extends AbstractRepository<Manufacturer, Long> {

    Page<Manufacturer> getManufacturers(
            ManufacturerSpecification specification,
            int pageIndex,
            int pageSize,
            Manufacturer__ ... queryPaths);
    
    Manufacturer getManufacturerById(long id, Manufacturer__ ... queryPaths);
    
    List<Manufacturer> getManufacturerByIds(Collection<Long> ids, Manufacturer__ ... queryPaths);

    Manufacturer getManufacturerByNameInsensitively(String name, Manufacturer__ ... queryPaths);

    void persistManufacturer(Manufacturer manufacturer);
}
