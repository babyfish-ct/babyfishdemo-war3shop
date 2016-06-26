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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.annotation.Resource;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.ProductService;
import org.babyfishdemo.war3shop.bll.impl.aop.RequirePermission;
import org.babyfishdemo.war3shop.dal.ManufacturerRepository;
import org.babyfishdemo.war3shop.dal.PreferentialItemRepository;
import org.babyfishdemo.war3shop.dal.PreferentialRepository;
import org.babyfishdemo.war3shop.dal.ProductRepository;
import org.babyfishdemo.war3shop.dal.UserRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator_;
import org.babyfishdemo.war3shop.entities.Administrator__;
import org.babyfishdemo.war3shop.entities.Manufacturer;
import org.babyfishdemo.war3shop.entities.Manufacturer_;
import org.babyfishdemo.war3shop.entities.Manufacturer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.PreferentialActionType;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem__;
import org.babyfishdemo.war3shop.entities.PreferentialThresholdType;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.specification.ManufacturerSpecification;
import org.babyfishdemo.war3shop.entities.specification.PreferentialSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    @Resource
    private ProductRepository productRepository;
    
    @Resource
    private ManufacturerRepository manufacturerRepository;
    
    @Resource
    private PreferentialRepository preferentialRepository;
    
    @Resource
    private PreferentialItemRepository preferentialItemRepository;
    
    @Resource
    private UserRepository userRepository;
    
    @Transactional(readOnly = true)
    @Override
    public Manufacturer getManufacturerById(long id, Manufacturer__... queryPaths) {
        return this.manufacturerRepository.getManufacturerById(id, queryPaths);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Manufacturer> getManufacturers(
            ManufacturerSpecification specification, 
            int pageIndex,
            int pageSize, 
            Manufacturer__... queryPaths) {
        return this.manufacturerRepository.getManufacturers(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }

    @RequirePermission(Constants.MANAGE_MANUFACTURERS)
    @Transactional
    @Override
    public void submitManufacturer(Manufacturer manufacturer) {
        JPAEntities.validateMaxEnabledRange(
                manufacturer,
                JPAEntities.required(Manufacturer_.version),
                Manufacturer_.name,
                Manufacturer_.race,
                Manufacturer_.email,
                Manufacturer_.phone,
                Manufacturer_.purchasers,
                Manufacturer_.description,
                Manufacturer_.imageMimeType,
                Manufacturer_.image
        );
        
        if (JPAEntities.isEnabled(manufacturer, Manufacturer_.name)) {
            Manufacturer conflict = 
                    this
                    .manufacturerRepository
                    .getManufacturerByNameInsensitively(manufacturer.getName());
            if (conflict != null && !JPAEntities.isIdEquals(manufacturer, conflict)) {
                throw new IllegalArgumentException(
                        "The manufacturer whoes name is \"" +
                        manufacturer.getName() +
                        "\" is already existing");
            }
        }
        
        if (JPAEntities.isEnabled(manufacturer, Manufacturer_.purchasers) &&
                !Nulls.isNullOrEmpty(manufacturer.getPurchasers())) {
            List<Administrator> administrators = this.userRepository.getAdministratorsByIds(
                    JPAEntities.extractAttribute(manufacturer.getPurchasers(), Administrator_.id), 
                    Administrator__.begin().roles().privileges().end(),
                    Administrator__.begin().privileges().end());
            for (Administrator administrator : administrators) {
                if (!hasPrivilege(administrator, Constants.PURCHASE_PRODUCTS)) {
                    throw new IllegalArgumentException(
                            "The administrator whose id is \"" +
                            administrator.getId() +
                            "\" and name is \"" +
                            administrator.getName() +
                            "\" has no privilege \"" +
                            Constants.PURCHASE_PRODUCTS +
                            "\" so that it can not be speicified to be a purchaser of manufacturer"
                    );
                }
            }
        }
        if (manufacturer.getId() == null) {
            if (JPAEntities.isDisabled(manufacturer, Manufacturer_.image) || manufacturer.getImage() == null) {
                throw new IllegalArgumentException("Please specify the image for the new manufacturer");
            }
        }
        this.manufacturerRepository.mergeEntity(manufacturer);
    }

    @RequirePermission(Constants.MANAGE_MANUFACTURERS)
    @Transactional
    @Override
    public void deleteManufacturer(long id) {
        this.manufacturerRepository.removeEntityById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Product getProductById(long id, Product__... queryPaths) {
        return this.productRepository.getProductById(id, queryPaths);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Product> getProducts(
            ProductSpecification specification,
            int pageIndex, 
            int pageSize, 
            Product__... queryPaths) {
        return this.productRepository.getProducts(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }
    
    @RequirePermission(Constants.MANAGE_PRODUCTS)
    @Transactional
    @Override
    public void submitProduct(Product product) {
        
        JPAEntities.validateMaxEnabledRange(
                product,
                JPAEntities.required(Product_.version),
                Product_.name,
                Product_.active,
                Product_.type,
                Product_.race,
                Product_.price,
                Product_.manufacturers,
                Product_.purchasers,
                Product_.description,
                Product_.imageMimeType,
                Product_.image
        );
        
        if (JPAEntities.isEnabled(product, Product_.name)) {
            Product conflict = this.productRepository.getProductLikeNameInsensitively(product.getName());
            if (conflict != null && !JPAEntities.isIdEquals(product, conflict)) {
                throw new IllegalArgumentException(
                        "The product whoes name is \"" +
                        product.getName() +
                        "\" is already existing");
            }
        }
        
        BigDecimal oldPrice = null;
        if (product.getId() == null) {
            product.setActive(true);
            product.setCreationTime(new Date());
        } else if (JPAEntities.isEnabled(product, Product_.price)) {
            oldPrice = this.productRepository.getProductById(product.getId()).getPrice();
        }
        if (JPAEntities.isEnabled(product, Product_.purchasers) && !Nulls.isNullOrEmpty(product.getPurchasers())) {
            List<Administrator> administrators = this.userRepository.getAdministratorsByIds(
                    JPAEntities.extractAttribute(product.getPurchasers(), Administrator_.id),
                    Administrator__.begin().roles().privileges().end(),
                    Administrator__.begin().privileges().end());
            for (Administrator administrator : administrators) {
                if (!hasPrivilege(administrator, Constants.PURCHASE_PRODUCTS)) {
                    throw new IllegalArgumentException(
                            "The administrator whose id is \"" +
                            administrator.getId() +
                            "\" and name is \"" +
                            administrator.getName() +
                            "\" has no privilege \"" +
                            Constants.PURCHASE_PRODUCTS +
                            "\" so that it can not be speicified to be a purchaser of product");
                }
            }
        }
        if (product.getId() == null) {
            if (JPAEntities.isDisabled(product, Product_.image) || product.getImage() == null) {
                throw new IllegalArgumentException("Please specify the image for the new product");
            }
        }
        
        this.productRepository.mergeEntity(product);
        
        if (oldPrice != null && !product.getPrice().equals(oldPrice)) {
            List<Preferential> affectedPreferentials = this.preferentialRepository.getPreferentialsThatCanBeAffectedByProduct(
                    product.getId(), 
                    Preferential__.begin().product().end(),
                    Preferential__.begin().items().giftProduct().end());
            for (Preferential affectedPreferential : affectedPreferentials) {
                try {
                    this.validatePreferential(affectedPreferential);
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException(
                            "Can not change the price of the current product whose id is "
                            + product.getId()
                            + "to be "
                            + product.getPrice() +
                            ", because the preferential whose id is "
                            + affectedPreferential.getId()
                            + " will be broken, " +
                            ex.getMessage(),
                            ex);
                }
            }
        }
    }

    @RequirePermission(Constants.MANAGE_PRODUCTS)
    @Transactional
    @Override
    public void deleteProduct(long id) {
        this.productRepository.removeEntityById(id);
    }
    
    @Transactional(readOnly = true)
    @Override
    public Preferential getPreferentialById(long id, Preferential__... queryPaths) {
        return this.preferentialRepository.getPreferentialById(id, queryPaths);
    }

    @RequirePermission(Constants.MANAGE_PREFERENTIALS)
    @Transactional
    @Override
    public Page<Preferential> getPreferentials(
            PreferentialSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Preferential__ ... queryPaths) {
        return this.preferentialRepository.getPreferentials(
                specification, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }
    
    @RequirePermission(Constants.MANAGE_PREFERENTIALS)
    @Transactional
    @Override
    public void submitPreferential(Preferential preferential) {
        Arguments.mustNotBeNull("preferential", preferential);
        Arguments.mustNotBeNull("preferential.getThresholdType()", preferential.getThresholdType());
        Arguments.mustNotBeNull("preferential.getActionType()", preferential.getActionType());
        Arguments.mustNotBeNull("preferential.getProduct()", preferential.getProduct());
        Arguments.mustNotBeNull("preferential.getProduct().getId()", preferential.getProduct().getId());
        Arguments.mustNotBeNull("preferential.getStartDate()", preferential.getStartDate());
        Arguments.mustNotBeNull("preferential.getEndDate()", preferential.getEndDate());
        Arguments.mustBeLessThanOrEqualToOther(
                "preferential.getStartDate()", 
                preferential.getStartDate(), 
                "preferential.getEndDate()", 
                preferential.getEndDate()
        );
        if (preferential.getId() == null) {
            Calendar currentCalendar = new GregorianCalendar();
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
            currentCalendar.set(Calendar.MINUTE, 0);
            currentCalendar.set(Calendar.SECOND, 0);
            currentCalendar.set(Calendar.MILLISECOND, 0);
            if (preferential.getStartDate().compareTo(currentCalendar.getTime()) < 0) {
                throw new IllegalArgumentException(
                        "The start date of new preferential must be greator than or equal to today");
            }
        }
        PreferentialSpecification specification = new PreferentialSpecification();
        specification.setIncludedProductIds(MACollections.wrap(preferential.getProduct().getId()));
        specification.setMinDate(preferential.getStartDate());
        specification.setMaxDate(preferential.getEndDate());
        List<Preferential> conflictPreferentials = 
                this
                .preferentialRepository
                .getPreferentials(specification, 0, Integer.MAX_VALUE).getEntities();
        if (!conflictPreferentials.isEmpty()) {
            for (Preferential conflictPreferential : conflictPreferentials) {
                if (preferential.getId() == null || !conflictPreferential.getId().equals(preferential.getId())) {
                    throw new IllegalArgumentException(
                            "There is another preferential for the same product in the same date range.");
                }
            }
        }
        if (preferential.getItems().isEmpty()) {
            throw new IllegalArgumentException("No preferential items");
        }
        if (JPAEntities.isDisabled(preferential.getProduct(), Product_.price)) {
            preferential.setProduct(this.productRepository.getProductById(preferential.getProduct().getId()));
        }
        for (PreferentialItem item : preferential.getItems()) {
            switch (preferential.getThresholdType()) {
            case QUANTITY:
                if (item.getThresholdQuantity() == null) {
                    throw new IllegalArgumentException(
                            "The threshold quantity of any preferential item should not be null " +
                            "when the preferential's threshold type is quantity");
                }
                if (item.getThresholdQuantity().intValue() <= 0) {
                    throw new IllegalArgumentException("The threshold quantity of any preferential item must > 0 ");
                }
                break;
            case MONEY:
                if (item.getThresholdMoney() == null) {
                    throw new IllegalArgumentException(
                            "The threshold money of any preferential item should not be null " +
                            "when the preferential's threshold type is money");
                }
                if (item.getThresholdMoney().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("The threshold mony of any preferential item must > 0 ");
                }
                break;
            }
            switch (preferential.getActionType()) {
            case MULTIPLIED_BY_PERCENTAGE:
                if (item.getPercentageFactor() == null) {
                    throw new IllegalArgumentException(
                            "The percentage factory of any preferential item should not be null " +
                            "when the preferential's action type is multiplied by percentage");
                }
                if (item.getPercentageFactor().intValue() <= 0 || item.getPercentageFactor().intValue() >= 100) {
                    throw new IllegalArgumentException("The percentage factor of any preferential item must between 1 and 99");
                }
                break;
            case REDUCE_MONEY:
                if (item.getReducedMoney() == null) {
                    throw new IllegalArgumentException(
                            "The reduced money of any preferential item should not be null " +
                            "when the preferential's action type is reduce money");
                }
                if (item.getReducedMoney().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("The reduced money of any preferential item must be greator than zero");
                }
                break;
            case SEND_GIFT:
                if (item.getGiftProduct() == null || item.getGiftProduct().getId() == null) {
                    throw new IllegalArgumentException(
                            "The gift product and its id of any preferential item should not be null " +
                            "when the preferential's action type is reduce money");
                }
                if (item.getGiftQuantity().intValue() <= 0) {
                    throw new IllegalArgumentException("The gift quantity of any preferential item must greator than zero");
                }
                break;
            }
        }
        this.validatePreferential(preferential);
        PreferentialItem[] items = preferential.getItems().toArray(new PreferentialItem[preferential.getItems().size()]);
        for (int i = items.length - 1; i >= 0; i--) {
            for (int ii = items.length - 1; ii > i; ii--) {
                if (preferential.getThresholdType() == PreferentialThresholdType.QUANTITY) {
                    int cmp = items[i].getThresholdQuantity().compareTo(items[ii].getThresholdQuantity());
                    if (cmp == 0) {
                        throw new IllegalArgumentException("The threshold quantity of two preferential items can not be same");
                    }
                } else {
                    int cmp = items[i].getThresholdMoney().compareTo(items[ii].getThresholdMoney());
                    if (cmp == 0) {
                        throw new IllegalArgumentException("The threshold money of two preferential items can not be same");
                    }
                }
            }
        }
        this.preferentialRepository.mergePreferential(preferential);
    }

    @Override
    public void validatePreferential(Preferential preferential) {
        
        PreferentialThresholdType thresholdType = preferential.getThresholdType();
        PreferentialActionType actionType = preferential.getActionType();
        NavigableMap<BigDecimal, BigDecimal> sortedMap = new TreeMap<>();
        Map<BigDecimal, Product> giftMap = new HashMap<>();
        
        for (PreferentialItem preferentialItem : preferential.getItems()) {
            BigDecimal thresholdValue;
            if (thresholdType == PreferentialThresholdType.QUANTITY) {
                thresholdValue = new BigDecimal(preferentialItem.getThresholdQuantity());
            } else {
                thresholdValue = preferentialItem.getThresholdMoney();
            }
        
            BigDecimal preferentialValue;
            if (actionType == PreferentialActionType.MULTIPLIED_BY_PERCENTAGE) {
                preferentialValue = new BigDecimal(100 - preferentialItem.getPercentageFactor());
            } else if (actionType == PreferentialActionType.REDUCE_MONEY) {
                preferentialValue = preferentialItem.getReducedMoney();
            } else {
                if (preferentialItem.getGiftProduct() != null && JPAEntities.isDisabled(preferentialItem.getGiftProduct(), Product_.price)) {
                    preferentialItem.setGiftProduct(this.productRepository.getProductById(preferentialItem.getGiftProduct().getId()));
                }
                preferentialValue = preferentialItem.getGiftProduct().getPrice().multiply(new BigDecimal(preferentialItem.getGiftQuantity()));
                giftMap.put(thresholdValue, preferentialItem.getGiftProduct());
            }
            
            sortedMap.put(thresholdValue, preferentialValue);
        }
        
        if (sortedMap.isEmpty()) {
            return;
        }
        
        Iterator<Entry<BigDecimal, BigDecimal>> itr = sortedMap.entrySet().iterator();
        Entry<BigDecimal, BigDecimal> prevEntry = itr.next();
        while (itr.hasNext()) {
            Entry<BigDecimal, BigDecimal> entry = itr.next();
            BigDecimal preferentialMoneyLowerBound = 
                    prevEntry
                    .getValue()
                    .multiply(entry.getKey())
                    .divide(prevEntry.getKey(), 2, BigDecimal.ROUND_HALF_UP);
            if (entry.getValue().compareTo(preferentialMoneyLowerBound) <= 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("The ");
                if (actionType == PreferentialActionType.MULTIPLIED_BY_PERCENTAGE) {
                    builder.append("percentage factor");
                } else if (actionType == PreferentialActionType.REDUCE_MONEY) {
                    builder.append("reduced money");
                } else {
                    builder.append("gift quantity");
                }
                builder
                .append(" of the preferential item whoes threshold ")
                .append(thresholdType.name().toLowerCase())
                .append(" is \"")
                .append(entry.getKey())
                .append("\" must be");
                if (actionType == PreferentialActionType.MULTIPLIED_BY_PERCENTAGE) {
                    builder
                    .append(" less than ")
                    .append(new BigDecimal(100).subtract(preferentialMoneyLowerBound));
                } else if (actionType == PreferentialActionType.REDUCE_MONEY) {
                    builder
                    .append(" greator than ")
                    .append(preferentialMoneyLowerBound);
                } else {
                    Product giftProduct = giftMap.get(entry.getKey());
                    builder
                    .append(" greator than ")
                    .append(preferentialMoneyLowerBound.divide(giftProduct.getPrice(), 0, BigDecimal.ROUND_DOWN));
                }
                throw new IllegalArgumentException(builder.toString());
            }
            prevEntry = entry;
        }
    }

    private static boolean hasPrivilege(Administrator administrator, String privilegeName) {
        for (Privilege privilege : administrator.getPrivileges()) {
            if (privilege.getName().equals(privilegeName)) {
                return true;
            }
        }
        for (Role role : administrator.getRoles()) {
            for (Privilege privilege : role.getPrivileges()) {
                if (privilege.getName().equals(privilegeName)) {
                    return true;
                }
            }       
        }
        return false;
    }

    @RequirePermission(Constants.MANAGE_PREFERENTIALS)
    @Transactional(readOnly = true)
    @Override
    public Page<PreferentialItem> getPreferentialItemsByParent(
            long preferentialId,
            int pageIndex, 
            int pageSize, 
            PreferentialItem__... queryPaths) {
        return this.preferentialItemRepository.getPreferentialItemsByParent(
                preferentialId, 
                pageIndex, 
                pageSize, 
                queryPaths);
    }

    @RequirePermission(Constants.MANAGE_PREFERENTIALS)
    @Transactional
    @Override
    public void deletePreferential(long id) {
        this.preferentialRepository.removeEntityById(id);
    }
}
