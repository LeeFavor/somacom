package com.kosta.somacom.service;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.repository.OrderItemRepository;
import com.kosta.somacom.seller.dto.SellerOrderItemUpdateRequest;
import com.kosta.somacom.seller.dto.SellerOrderResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderItemRepository orderItemRepository;

    public Page<SellerOrderResponseDto> getOrdersForSeller(Long sellerId, Pageable pageable) {
        return orderItemRepository.findBySellerId(sellerId, pageable)
                .map(SellerOrderResponseDto::new);
    }
    
    @Transactional
    public void updateOrderItemStatus(Long orderItemId, Long sellerId, SellerOrderItemUpdateRequest request) {
        OrderItem orderItem = findOrderItemAndCheckOwnership(orderItemId, sellerId);
        orderItem.updateShippingInfo(request.getStatus(), request.getTrackingNumber());
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