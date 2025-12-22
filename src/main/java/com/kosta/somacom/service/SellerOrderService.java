package com.kosta.somacom.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.order.OrderItemStatus;
import com.kosta.somacom.domain.order.OrderStatus;
import com.kosta.somacom.repository.OrderItemRepository;
import com.kosta.somacom.repository.OrderRepository;
import com.kosta.somacom.seller.dto.SellerOrderItemUpdateRequest;
import com.kosta.somacom.seller.dto.SellerOrderResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public Page<SellerOrderResponseDto> getOrdersForSeller(Long sellerId, Pageable pageable) {
        return orderItemRepository.findBySellerIdDesc(sellerId, pageable)
                .map(SellerOrderResponseDto::new);
    }
    
    @Transactional
    public void updateOrderItemStatus(Long orderItemId, Long sellerId, SellerOrderItemUpdateRequest request) {
        OrderItem orderItem = findOrderItemAndCheckOwnership(orderItemId, sellerId);
        orderItem.updateShippingInfo(request.getStatus(), request.getTrackingNumber());
        Order order = orderItem.getOrder();
        if(order == null || order.getStatus() == OrderStatus.PENDING) return;
        order.setStatus(OrderStatus.DELIVERED);
        
        for(OrderItem oItem : order.getOrderItems()) {
        	if(oItem.getStatus() == OrderItemStatus.PAID || oItem.getStatus() == OrderItemStatus.PREPARING) {
        		order.setStatus(OrderStatus.PAID);
        		break;
        	} else if(oItem.getStatus() == OrderItemStatus.SHIPPED) {
        		order.setStatus(OrderStatus.SHIPPED);
        	}
        }
    }

    private OrderItem findOrderItemAndCheckOwnership(Long orderItemId, Long sellerId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + orderItemId));

        if (!orderItem.getProduct().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("You do not have permission to modify this order item.");
        }
        return orderItem;
    }
}