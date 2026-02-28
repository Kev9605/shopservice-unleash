package com.example.order_service.order;

import com.example.order_service.product.ProductClient;
import com.example.order_service.product.ProductDto;
import com.example.order_service.service.FeatureFlagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderLogicService {

    private static final Logger log = LoggerFactory.getLogger(OrderLogicService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final FeatureFlagService featureFlagService;


    public OrderLogicService(OrderRepository orderRepository,
                             ProductClient productClient,
                             FeatureFlagService featureFlagService) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.featureFlagService = featureFlagService;
    }

    public ResponseEntity<Order> create(CreateOrderRequest orderRequest) {
        var productOpt = productClient.getProductById(orderRequest.getProductId());
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ProductDto productDto = productOpt.get();

        Order order = new Order();
        order.setProductId(orderRequest.getProductId());
        order.setQuantity(orderRequest.getQuantity());

        if (orderRequest.getQuantity() > productDto.getQuantity()) {
            order.setStatus("REJECTED_OUT_OF_STOCK");
            order.setTotalPrice(0);
            Order saved = orderRepository.save(order);

            // Optional: notify rejected
            if (featureFlagService.orderNotificationsEnabled()) {
                log.info("ORDER NOTIFICATION (REJECTED): orderId={}, productId={}, qty={}",
                        saved.getId(), saved.getProductId(), saved.getQuantity());
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(orderRepository.save(order));
        }

        // Base total
        double total = productDto.getPrice() * orderRequest.getQuantity();

        boolean discountFlag = featureFlagService.bulkOrderDiscountEnabled();
        boolean qualifiesByQty = orderRequest.getQuantity() > 5;

        // Bulk discount (15% off if qty > 5 AND flag ON)
        if (discountFlag && qualifiesByQty) {
            double before = total;
            total = total * 0.85;

            log.info("BULK_DISCOUNT_APPLIED flag={}, qty={}, before={}, after={}, discountPercent=15",
                    discountFlag, orderRequest.getQuantity(), before, total);
        } else {
            log.info("BULK_DISCOUNT_NOT_APPLIED flag={}, qty={}, qualifiesByQty={}",
                    discountFlag, orderRequest.getQuantity(), qualifiesByQty);
        }

        // Round to 2 decimals
        total = Math.round(total * 100.0) / 100.0;

        order.setTotalPrice(total);
        order.setStatus("CREATED");

        Order saved = orderRepository.save(order);

        // Order notifications (log only when flag ON)
        if (featureFlagService.orderNotificationsEnabled()) {
            log.info("ORDER NOTIFICATION: orderId={}, productId={}, qty={}, total={}",
                    saved.getId(), saved.getProductId(), saved.getQuantity(), saved.getTotalPrice());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
