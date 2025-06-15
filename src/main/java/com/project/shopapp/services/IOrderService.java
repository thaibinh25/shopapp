package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.response.OrderResponse;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    Order createOrder(OrderDTO orderDTO) throws DataNotFoundException, Exception;
    Order getOrder(Long id) throws DataNotFoundException;
    Order updateOrder (Long id, OrderDTO orderDTO) throws DataNotFoundException;

    Order updateOrderStatus(Long orderId, String status) throws DataNotFoundException;

    void deleteOrder(Long id);

    List<Order> findByUserId(Long userId);

    Page<Order> getOrderByKeyword(String keyword, Pageable pageable);
}
