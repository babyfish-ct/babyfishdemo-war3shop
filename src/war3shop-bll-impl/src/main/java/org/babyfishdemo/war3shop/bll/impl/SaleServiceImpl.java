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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;

import javax.annotation.Resource;
import javax.persistence.metamodel.Attribute;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfish.model.metadata.ModelClass;
import org.babyfishdemo.war3shop.bll.AlarmService;
import org.babyfishdemo.war3shop.bll.AuthorizationService;
import org.babyfishdemo.war3shop.bll.Constants;
import org.babyfishdemo.war3shop.bll.SaleService;
import org.babyfishdemo.war3shop.bll.impl.aop.RequirePermission;
import org.babyfishdemo.war3shop.bll.model.Sale;
import org.babyfishdemo.war3shop.dal.InventoryRepository;
import org.babyfishdemo.war3shop.dal.OrderRepository;
import org.babyfishdemo.war3shop.dal.PreferentialRepository;
import org.babyfishdemo.war3shop.dal.ProductRepository;
import org.babyfishdemo.war3shop.dal.UserRepository;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.GiftItem;
import org.babyfishdemo.war3shop.entities.Order;
import org.babyfishdemo.war3shop.entities.OrderItem;
import org.babyfishdemo.war3shop.entities.OrderItem_;
import org.babyfishdemo.war3shop.entities.Order__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.Preferential;
import org.babyfishdemo.war3shop.entities.PreferentialAction;
import org.babyfishdemo.war3shop.entities.PreferentialActionType;
import org.babyfishdemo.war3shop.entities.PreferentialItem;
import org.babyfishdemo.war3shop.entities.PreferentialItem_;
import org.babyfishdemo.war3shop.entities.PreferentialThresholdType;
import org.babyfishdemo.war3shop.entities.Preferential__;
import org.babyfishdemo.war3shop.entities.Product;
import org.babyfishdemo.war3shop.entities.Product_;
import org.babyfishdemo.war3shop.entities.Product__;
import org.babyfishdemo.war3shop.entities.TotalMoney;
import org.babyfishdemo.war3shop.entities.TotalPreferential;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.specification.AdministratorSpecification;
import org.babyfishdemo.war3shop.entities.specification.OrderSpecification;
import org.babyfishdemo.war3shop.entities.specification.PreferentialSpecification;
import org.babyfishdemo.war3shop.entities.specification.ProductSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
public class SaleServiceImpl implements SaleService {
    
    private static final int TEMPRARY_ORDER_GC_THRESHOLD_HOURS = 7 * 24;
    
    private static final MathContext DOWN_MATH_CONTEXT = new MathContext(0, RoundingMode.DOWN);

    @Resource
    private ProductRepository productRepository;
    
    @Resource
    private PreferentialRepository preferentialRepository;
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private AuthorizationService authorizationService;
    
    @Resource
    private UserRepository userRepository;
    
    @Resource
    private InventoryRepository inventoryRepository;
    
    @Resource
    private AlarmService alarmService;
    
    @Resource(name = "new-order-mail-component")
    private MailComponent newOrderMailComponent;
    
    @Resource(name = "assign-order-mail-component")
    private MailComponent assignOrderMailComponent;
    
    @Resource(name = "delivery-order-mail-component")
    private MailComponent deliveryOrderMailComponent;
    
    @Transactional(readOnly = true)
    @Override
    public Page<Sale> getSales(
            ProductSpecification specification,
            int pageIndex, 
            int pageSize, 
            Product__[] productQueryPaths,
            Preferential__[] preferentialQueryPaths) {
        
        final Page<Product> productPage = this.productRepository.getProducts(
                specification, 
                pageIndex, 
                pageSize,
                productQueryPaths
        );
        final List<Sale> sales = new ArrayList<>(productPage.getEntities().size());
        if (!productPage.getEntities().isEmpty()) {
            PreferentialSpecification preferentialSpecification = new PreferentialSpecification();
            Date currentDate = new Date();
            preferentialSpecification.setMinDate(currentDate);
            preferentialSpecification.setMaxDate(currentDate);
            preferentialSpecification.setActive(true);
            preferentialSpecification.setIncludedProductIds(
                    JPAEntities.extractAttribute(productPage.getEntities(), Product_.id)
            );
            List<Preferential> preferentials = this.preferentialRepository.getPreferentials(
                    preferentialSpecification, 
                    0, 
                    Integer.MAX_VALUE, 
                    preferentialQueryPaths
            )
            .getEntities();
            for (Product product : productPage.getEntities()) {
                Sale sale = new Sale();
                sale.setProduct(product);
                for (Preferential preferential : preferentials) {
                    if (preferential.getProduct().getId().equals(product.getId())) {
                        sale.setPreferential(preferential);
                        break;
                    }
                }
                sales.add(sale);
            }
        }
        return new Page<Sale>() {

            @Override
            public List<Sale> getEntities() {
                return sales;
            }

            @Override
            public int getExpectedPageIndex() {
                return productPage.getExpectedPageIndex();
            }

            @Override
            public int getActualPageIndex() {
                return productPage.getActualPageIndex();
            }

            @Override
            public int getPageSize() {
                return productPage.getPageSize();
            }

            @Override
            public int getTotalRowCount() {
                return productPage.getTotalRowCount();
            }

            @Override
            public int getTotalPageCount() {
                return productPage.getTotalPageCount();
            }
        };
    }

    @Transactional(readOnly = true)
    @Override
    public Order getTemporaryOrder(Order__ ... queryPaths) {
        User user = this.authorizationService.getCurrentUser();
        if (user instanceof Customer) {
            return this.orderRepository.getNewestTemporaryOrderByCustomerId(
                    user.getId(), 
                    queryPaths
            );
        }
        return null;
    }

    @RequirePermission(Constants.CUSTOMER_PERMISSION)
    @Transactional
    @Override
    public void addProductIntoCart(long productId) {
        
        Product product = this.requireProductWithPreferentials(productId);
        int inventoryQuantity = inventoryQuantity(product);
        
        Order order = this.prepareTemporaryOrder();
        OrderItem orderItem = findOrderItem(order, productId);
        if (orderItem != null) {
            orderItem.setQuantity(Math.min(orderItem.getQuantity() + 1, inventoryQuantity));
        } else {
            orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setInstantUnitPrice(product.getPrice());
            orderItem.setQuantity(Math.min(1, inventoryQuantity));
            orderItem.setOrder(order);
        }
        
        this.new PreferentialContext(order).apply();
        this.refreshTemporaryOrder(order);
    }
    
    @RequirePermission(Constants.CUSTOMER_PERMISSION)
    @Transactional
    @Override
    public void setProductQuantityOfCart(long productId, int quantity) {
        
        Product product = this.requireProductWithPreferentials(productId);
        int inventoryQuantity = inventoryQuantity(product);
        
        Order order = this.prepareTemporaryOrder();
        OrderItem orderItem = findOrderItem(order, productId);
        if (quantity > 0) {
            if (orderItem != null) {
                orderItem.setQuantity(Math.min(quantity, inventoryQuantity));
            } else {
                orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setInstantUnitPrice(product.getPrice());
                orderItem.setQuantity(Math.min(quantity, inventoryQuantity));
                orderItem.setOrder(order);
            }
        } else if (orderItem != null) {
            orderItem.setOrder(null);
        }
        
        this.new PreferentialContext(order).apply();
        this.refreshTemporaryOrder(order);
    }
    
    @RequirePermission(Constants.CUSTOMER_PERMISSION)
    @Transactional
    @Override
    public void removeProductFromCart(long productId) {
        
        Order order = this.prepareTemporaryOrder();
        OrderItem orderItem = findOrderItem(order, productId);
        if (orderItem != null) {
            orderItem.setOrder(null);
        }
        
        this.new PreferentialContext(order).apply();
        this.refreshTemporaryOrder(order);
    }
    
    @RequirePermission(Constants.CUSTOMER_PERMISSION)
    @Transactional
    @Override
    public void createOrder(String address, String phone) {
        Arguments.mustNotBeEmpty("address", Arguments.mustNotBeNull("address", address));
        Arguments.mustNotBeEmpty("phone", Arguments.mustNotBeNull("phone", phone));
        User currentUser = this.authorizationService.getCurrentUser();
        Order order = this.orderRepository.getNewestTemporaryOrderByCustomerId(
                currentUser.getId(),
                Order__.begin().orderItems().product().inventory().end(),
                Order__.begin().giftItems().product().inventory().end()
        );
        if (order == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("The order should not be null or empty");
        }
        for (OrderItem orderItem : order.getOrderItems()) {
            this.descreaseInventory(orderItem.getProduct(), orderItem.getQuantity());
        }
        for (GiftItem giftItem : order.getGiftItems()) {
            this.descreaseInventory(giftItem.getProduct(), giftItem.getQuantity());
        }
        order.setGcThreshold(null);
        order.setCreationTime(new Date());
        order.setAddress(address);
        order.setPhone(phone);
        
        order = this.orderRepository.mergeEntity(order);
        
        this.newOrderMailComponent.asyncSendAndIgnoreException(
                currentUser.getEmail(), 
                "userName",
                currentUser.getName(),
                "orderId",
                order.getId().toString()
        );
    }

    @RequirePermission(Constants.MANAGE_ORDERS)
    @Transactional(readOnly = true)
    @Override
    public Page<Order> getAssuredOrders(
            OrderSpecification specification,
            int pageIndex, 
            int pageSize, 
            Order__... queryPaths) {
        return this.orderRepository.getAssuredOrders(specification, pageIndex, pageSize, queryPaths);
    }

    @RequirePermission(Constants.CUSTOMER_PERMISSION)
    @Transactional(readOnly = true)
    @Override
    public Page<Order> getMyOrders(
            OrderSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Order__... queryPaths) {
        if (specification == null) {
            specification = new OrderSpecification();
        }
        specification.setIncludedCustomerIds(MACollections.wrap(this.authorizationService.getCurrentUserId()));
        specification.setExcludedCustomerIds(null);
        return this.orderRepository.getAssuredOrders(specification, pageIndex, pageSize, queryPaths); 
    }

    @RequirePermission(Constants.DELIVERY_ORDERS)
    @Transactional(readOnly = true)
    @Override
    public Page<Order> getMyRepsonsibleOrders(
            OrderSpecification specification,
            int pageIndex, 
            int pageSize, 
            Order__... queryPaths) {
        if (specification == null) {
            specification = new OrderSpecification();
        }
        specification.setIncludedDeliverymanIds(MACollections.wrap(this.authorizationService.getCurrentUserId()));
        specification.setExcludedDeliverymanIds(null);
        return this.orderRepository.getAssuredOrders(specification, pageIndex, pageSize, queryPaths);
    }

    @RequirePermission(Constants.MANAGE_ORDERS)
    @Transactional
    @Override
    public void assignDeliveryman(long orderId, long deliverymanId) {
        Order order = this.orderRepository.getOrderById(
                orderId, 
                Order__.begin().deliveryman().end(),
                Order__.begin().customer().end()
        );
        if (order == null) {
            throw new IllegalArgumentException("No such order");
        }
        if (order.getDeliveredTime() != null) {
            throw new IllegalArgumentException("The specified order has already been delivered");
        }
        AdministratorSpecification specification = new AdministratorSpecification();
        specification.setIds(MACollections.wrapLong(deliverymanId));
        specification.setIncludedPrivilegeNames(MACollections.wrap("delivery-orders"));
        Page<Administrator> page = this.authorizationService.getAdministrators(
                specification, 
                0, 
                Integer.MAX_VALUE);
        if (page.getEntities().isEmpty()) {
            throw new IllegalArgumentException("No such deliveryman");
        }
        boolean isNotAssigned = order.getDeliveryman() == null;
        
        order.setDeliveryman(page.getEntities().get(0));
        this.orderRepository.mergeEntity(order);
        
        if (isNotAssigned) {
            this.assignOrderMailComponent.asyncSendAndIgnoreException(
                    order.getCustomer().getEmail(), 
                    "userName",
                    order.getCustomer().getName(),
                    "orderId",
                    order.getId().toString()
            );
        }
    }

    @RequirePermission(Constants.DELIVERY_ORDERS)
    @Transactional
    @Override
    public void deliveryOrder(long orderId) {
        Order order = this.orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("No such order");
        }
        if (order.getDeliveryman() == null ||
                !this.authorizationService.getCurrentUserId().equals(order.getDeliveryman().getId())) {
            throw new IllegalArgumentException(
                    "The specified order can not be delivered by you");
        }
        if (order.getDeliveredTime() != null) {
            throw new IllegalArgumentException(
                    "The specified order has already been delivered");
        }
        order.setDeliveredTime(new Date());
        
        this.orderRepository.mergeEntity(order);
        
        this.deliveryOrderMailComponent.asyncSendAndIgnoreException(
                order.getCustomer().getEmail(), 
                "userName",
                order.getCustomer().getName(),
                "orderId",
                order.getId().toString()
        );
    }

    @RequirePermission(Constants.CUSTOMER_PERMISSION)
    @Transactional
    @Override
    public void changeOrderAddress(long orderId, String address, String phone) {
        Order order = this.orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("No such order");
        }
        if (order.getCustomer() == null || 
                !this.authorizationService.getCurrentUserId().equals(order.getCustomer().getId())) {
            throw new IllegalArgumentException(
                    "The specified order can not be changed by you");
        }
        if (order.getDeliveredTime() != null) {
            throw new IllegalArgumentException(
                    "The specified order has already been delivered");
        }
        if (address.equals(order.getAddress()) && phone.equals(order.getPhone())) {
            return;
        }
        if (order.getDeliveryman() != null) {
            String message = 
                    "The order whose id is \"" +
                    orderId +
                    "\" is changed";
            if (!address.equals(order.getAddress())) {
                message += 
                        ", its address is changed to be \"" +
                        address + 
                        "\"";
            }
            if (!phone.equals(order.getPhone())) {
                message += 
                        ", its phone is changed to be \"" +
                        phone +   
                        "\"";
            }
            this.alarmService.sendAlarm(order.getDeliveryman(), message);
        }
        order.setAddress(address);
        order.setPhone(phone);
        this.orderRepository.mergeEntity(order);
    }

    private Order prepareTemporaryOrder() {
        Long currentUserId = this.authorizationService.getCurrentUserId();
        Order order = this.orderRepository.getNewestTemporaryOrderByCustomerId(
                currentUserId, 
                Order__.begin().customer().end(),
                Order__.begin().orderItems().product().inventory().end(),
                Order__.begin().orderItems().preferentialActions().giftProduct().end(),
                Order__.begin().giftItems().end()
        );
        if (order == null) {
            Customer customer = (Customer)this.authorizationService.getCurrentUser();
            order = new Order();
            order.setTotalMoney(new TotalMoney());
            order.setCustomer(customer);
            order.setGcThreshold(gcThredshold());
            order.setTotalMoney(new TotalMoney());
            order.setAddress(customer.getAddress());
            order.setPhone(customer.getPhone());
            return this.orderRepository.mergeEntity(order);
        }
        return order;
    }
    
    private Product requireProductWithPreferentials(long productId) {
        Product product = this.productRepository.getProductById(productId, Product__.begin().preferentials().end());
        if (product == null) {
            throw new IllegalArgumentException("There is no product whose id is " + productId);
        }
        return product;
    }

    private void refreshTemporaryOrder(Order order) {
        if (order.getGcThreshold() == null) {
            throw new IllegalArgumentException("The order must be temporary");
        }
        order.setGcThreshold(gcThredshold());
        this.orderRepository.mergeEntity(order);
    }

    private static OrderItem findOrderItem(Order order, long productId) {
        if (order == null) {
            return null;
        }
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getProduct().getId().longValue() == productId) {
                return orderItem;
            }
        }
        return null;
    }
    
    private static Date gcThredshold() {
        return new Date(System.currentTimeMillis() + TEMPRARY_ORDER_GC_THRESHOLD_HOURS * 60 * 60 * 1000);
    }
    
    private static int inventoryQuantity(Product product) {
        if (product.getInventory() == null) {
            return 0;
        }
        return product.getInventory().getQuantity();
    }
    
    private void descreaseInventory(Product product, int quantity) {
        if (quantity <= 0) {
            return;
        }
        int originalInventoryQuantity = product.getInventory() != null ? product.getInventory().getQuantity() : 0;
        if (product.getInventory() == null || originalInventoryQuantity < quantity) {
            this.sendAlarmToPurcharsers(
                    true,
                    product.getId(), 
                    "Not enough \"" + product.getName() + "\"");
            throw new IllegalStateException("Not enough \"" + product.getName() + "\"");
        }
        int newInventoryQuantity;
        if (originalInventoryQuantity == quantity) {
            this.inventoryRepository.removeEntity(product.getInventory());
            newInventoryQuantity = 0;
        } else {
            newInventoryQuantity = originalInventoryQuantity - quantity;
            product.getInventory().setQuantity(newInventoryQuantity);
            this.inventoryRepository.mergeEntity(product.getInventory());
        }
        if (originalInventoryQuantity >= 10 && newInventoryQuantity < 10) {
            this.sendAlarmToPurcharsers(
                    false,
                    product.getId(), 
                    "The inventory of \"" + product.getName() + "\" is too low");
        }
    }
    
    private void sendAlarmToPurcharsers(boolean forcibly, long productId, String message) {
        AdministratorSpecification specification = new AdministratorSpecification();
        specification.setIncludedPurchasedProductIds(MACollections.wrapLong(productId));
        List<Administrator> purchasers = 
                this
                .userRepository //Use userRepository, not authorizationService, to avoid the permission checking.
                .getAdministrators(
                        specification, 
                        0, 
                        Integer.MAX_VALUE
                )
                .getEntities();
        if (forcibly) {
            for (Administrator purchaser : purchasers) {
                this.alarmService.sendAlarmForcibly(purchaser, message);
            }
        } else {
            for (Administrator purchaser : purchasers) {
                this.alarmService.sendAlarm(purchaser, message);
            }
        }
    }
    
    private class PreferentialContext {
        
        private Order order;
        
        private Map<Product, Integer> giftProductMap;
        
        private Date now;
        
        private Map<Long, Preferential> preferentialMap;
        
        private OrderItem currentOrderItem;
        
        private Preferential currentPreferential;
        
        private PreferentialItem currentPreferentialItem;
        
        private int currentPreferentItemMatchedCount;
        
        public PreferentialContext(Order order) {
            this.order = order;
        }
        
        public void apply() {
            this.giftProductMap = new LinkedHashMap<>(
                    ModelClass.of(Product.class).getDefaultEqualityComparator(),
                    (EqualityComparator<Integer>)null
            );
            this.now = new Date();
            
            this.clearPreferentialActions();
            this.initPreferentials();
            this.scanOrder();
        }
        
        private void clearPreferentialActions() {
            TotalMoney zeroTotalMoney = new TotalMoney();
            this.order.setTotalMoney(zeroTotalMoney);
            this.order.getGiftItems().clear();
            for (OrderItem orderItem : this.order.getOrderItems()) {
                orderItem.setTotalMoney(zeroTotalMoney);
                orderItem.getPreferentialActions().clear();
            }
        }
        
        private void initPreferentials() {
            PreferentialSpecification specification = new PreferentialSpecification();
            specification.setActive(true);
            XOrderedSet<Long> productIds = JPAEntities.extractAttribute(
                    JPAEntities.extractAttribute(this.order.getOrderItems(), OrderItem_.product), 
                    Product_.id
            );
            specification.setIncludedProductIds(productIds);
            specification.setMinDate(this.now);
            specification.setMaxDate(this.now);
            Page<Preferential> page = SaleServiceImpl.this.preferentialRepository.getPreferentials(
                    specification, 
                    0, 
                    Integer.MAX_VALUE, 
                    Preferential__.begin().items().giftProduct().end()
            );
            Map<Long, Preferential> preferentialMap = new HashMap<>();
            for (Preferential preferential : page.getEntities()) {
                if (preferentialMap.put(preferential.getProduct().getId(), preferential) != null) {
                    throw new IllegalStateException(
                            "Too many active preferentials for the product whose id is " + 
                            preferential.getProduct().getId());
                }
            }
            this.preferentialMap = preferentialMap;
        }
        
        private void scanOrder() {
            TotalMoney totalMoney = new TotalMoney();
            for (OrderItem orderItem : this.order.getOrderItems()) {
                this.currentOrderItem = orderItem;
                this.currentPreferential = this.preferentialMap.get(this.currentOrderItem.getProduct().getId());
                this.scanCurrentOrderItem();
                totalMoney.setReducedMoney(totalMoney.getReducedMoney().add(orderItem.getTotalMoney().getReducedMoney()));
                totalMoney.setGiftMoney(totalMoney.getGiftMoney().add(orderItem.getTotalMoney().getGiftMoney()));
                totalMoney.setExpectedMoney(totalMoney.getExpectedMoney().add(orderItem.getTotalMoney().getExpectedMoney()));
                totalMoney.setActualMoney(totalMoney.getActualMoney().add(orderItem.getTotalMoney().getActualMoney()));
                if (this.currentPreferential != null) {
                    for (PreferentialAction preferentialAction : orderItem.getPreferentialActions()) {
                        if (preferentialAction.getActionType() == PreferentialActionType.SEND_GIFT) {
                            Integer value = this.giftProductMap.get(preferentialAction.getGiftProduct());
                            int giftQuantityDelta = 
                                    preferentialAction.getGiftQuantity().intValue() * 
                                    preferentialAction.getMatchedCount();
                            if (value == null) {
                                value = giftQuantityDelta;
                            } else {
                                value = value.intValue() + giftQuantityDelta;
                            }
                            this.giftProductMap.put(preferentialAction.getGiftProduct(), value);
                        }
                    }
                }
            }
            for (Entry<Product, Integer> e : this.giftProductMap.entrySet()) {
                GiftItem giftItem = new GiftItem();
                giftItem.setProduct(e.getKey());
                giftItem.setInstantUnitPrice(e.getKey().getPrice());
                giftItem.setQuantity(e.getValue());
                giftItem.setOrder(this.order);
            }
            this.order.setTotalMoney(totalMoney);
        }

        private void scanCurrentOrderItem() {
            TotalMoney totalMoney = new TotalMoney();
            if (this.currentPreferential != null) {
                if (this.currentPreferential.getThresholdType() == PreferentialThresholdType.QUANTITY) {
                    this.determineMatchedRuleByThresholdQuantity();
                } else {
                    this.determineMatchedRuleByThresholdMoney();
                }
                for (PreferentialAction preferentialAction : this.currentOrderItem.getPreferentialActions()) {
                    totalMoney.setReducedMoney(
                            totalMoney.getReducedMoney()
                            .add(preferentialAction.getTotalPreferential().getReducedMoney())
                    );
                    totalMoney.setGiftMoney(
                            totalMoney.getGiftMoney()
                            .add(preferentialAction.getTotalPreferential().getGiftMoney())
                    );
                }
            }
            totalMoney.setExpectedMoney(
                    this
                    .currentOrderItem
                    .getInstantUnitPrice()
                    .multiply(new BigDecimal(this.currentOrderItem.getQuantity()))
            );
            totalMoney.setActualMoney(
                    totalMoney.getExpectedMoney()
                    .subtract(totalMoney.getReducedMoney())
            );
            this.currentOrderItem.setTotalMoney(totalMoney);
        }

        private void determineMatchedRuleByThresholdQuantity() {
            int quantity = this.currentOrderItem.getQuantity();
            if (this.currentPreferential.getActionType() == PreferentialActionType.MULTIPLIED_BY_PERCENTAGE) {
                for (PreferentialItem preferentialItem : this.descendingCurrentPreferentialItems()) {
                    this.currentPreferentialItem = preferentialItem;
                    if (quantity >= preferentialItem.getThresholdQuantity()) {
                        this.currentPreferentItemMatchedCount = 1;
                        this.doPreferentialActions();
                        break; //MULTIPLIED_BY_PERCENTAGE can only be matched once
                    }
                }
            } else {
                for (PreferentialItem preferentialItem : this.descendingCurrentPreferentialItems()) {
                    this.currentPreferentialItem = preferentialItem;
                    this.currentPreferentItemMatchedCount = quantity / preferentialItem.getThresholdQuantity();
                    if (this.currentPreferentItemMatchedCount != 0) {
                        this.doPreferentialActions();
                        quantity -= this.currentPreferentItemMatchedCount * preferentialItem.getThresholdQuantity();
                    }
                }
            }
        }
        
        private void determineMatchedRuleByThresholdMoney() {
            BigDecimal money = this.currentOrderItem.getProduct().getPrice().multiply(new BigDecimal(this.currentOrderItem.getQuantity()));
            if (this.currentPreferential.getActionType() == PreferentialActionType.MULTIPLIED_BY_PERCENTAGE) {
                for (PreferentialItem preferentialItem : this.descendingCurrentPreferentialItems()) {
                    this.currentPreferentialItem = preferentialItem;
                    if (money.compareTo(preferentialItem.getThresholdMoney()) >= 0) {
                        this.currentPreferentItemMatchedCount = 1;
                        this.doPreferentialActions();
                        break; //MULTIPLIED_BY_PERCENTAGE can only be matched once
                    }
                }
            } else {
                for (PreferentialItem preferentialItem : this.descendingCurrentPreferentialItems()) {
                    this.currentPreferentialItem = preferentialItem;
                    BigDecimal[] arr = money.divideAndRemainder(preferentialItem.getThresholdMoney(), DOWN_MATH_CONTEXT);
                    this.currentPreferentItemMatchedCount = arr[0].intValue();
                    if (this.currentPreferentItemMatchedCount != 0) {
                        this.doPreferentialActions();
                        money = arr[1];
                    }
                }
            }
        }
        
        private void doPreferentialActions() {
            PreferentialAction preferentialAction = new PreferentialAction();
            TotalPreferential totalPreferential = new TotalPreferential();
            preferentialAction.setThresholdType(this.currentPreferential.getThresholdType());
            preferentialAction.setActionType(this.currentPreferential.getActionType());
            preferentialAction.setMatchedCount(this.currentPreferentItemMatchedCount);
            switch (this.currentPreferential.getThresholdType()) {
            case QUANTITY:
                preferentialAction.setThresholdQuantity(this.currentPreferentialItem.getThresholdQuantity());
                break;
            default:
                preferentialAction.setThresholdMoney(this.currentPreferentialItem.getThresholdMoney());
                break;
            }
            switch (this.currentPreferential.getActionType()) {
            case MULTIPLIED_BY_PERCENTAGE:
                preferentialAction.setPercentageFactor(this.currentPreferentialItem.getPercentageFactor());
                totalPreferential.setReducedMoney(
                        this.currentOrderItem.getInstantUnitPrice()
                        .multiply(new BigDecimal(this.currentOrderItem.getQuantity()))
                        .multiply(
                                BigDecimal.ONE
                                .subtract(
                                        new BigDecimal(
                                                "0." + 
                                                this.currentPreferentialItem.getPercentageFactor()
                                        )
                                )
                        )
                );
                break;
            case REDUCE_MONEY:
                preferentialAction.setReducedMoney(this.currentPreferentialItem.getReducedMoney());
                totalPreferential.setReducedMoney(
                        this.currentPreferentialItem.getReducedMoney()
                        .multiply(new BigDecimal(currentPreferentItemMatchedCount))
                );
                break;
            default:
                preferentialAction.setGiftProduct(this.currentPreferentialItem.getGiftProduct());
                preferentialAction.setGiftQuantity(this.currentPreferentialItem.getGiftQuantity());
                totalPreferential.setGiftMoney(
                        this.currentPreferentialItem.getGiftProduct().getPrice()
                        .multiply(new BigDecimal(this.currentPreferentialItem.getGiftQuantity()))
                        .multiply(new BigDecimal(currentPreferentItemMatchedCount))
                );
                break;
            }
            preferentialAction.setTotalPreferential(totalPreferential);
            preferentialAction.setOrderItem(this.currentOrderItem);
        }
        
        private NavigableSet<PreferentialItem> descendingCurrentPreferentialItems() {
            Attribute<PreferentialItem, ?> attribute;
            if (this.currentPreferential.getThresholdType() == PreferentialThresholdType.QUANTITY) {
                attribute = PreferentialItem_.thresholdQuantity;
            } else {
                attribute = PreferentialItem_.thresholdMoney;
            }
            Comparator<PreferentialItem> comparator = 
                    MACollections.reverseOrder(
                            JPAEntities.comparator(PreferentialItem.class, attribute)
                    );
            NavigableSet<PreferentialItem> preferentialItems = new TreeSet<>(comparator);
            preferentialItems.addAll(this.currentPreferential.getItems());
            return preferentialItems;
        }
    }
}
