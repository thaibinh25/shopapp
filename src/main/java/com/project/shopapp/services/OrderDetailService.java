package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService{
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        //tìm xem orderid có tồn tại k
        Order order = orderRepository.findById(orderDetailDTO.getOrderId()).orElseThrow(()->
                new DataNotFoundException("cannot find order with id: " +orderDetailDTO.getOrderId()));
        //tìm xem product có tồn tại hay không
        Product product = productRepository.findById(orderDetailDTO.getProductId()).orElseThrow(()->
                new DataNotFoundException("cannot"));
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .price(orderDetailDTO.getPrice())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .color(orderDetailDTO.getColor()).build();
        return orderDetailRepository.save(orderDetail);

    }

    @Override
    public OrderDetail getOrderDetail(Long id) throws DataNotFoundException {
        return orderDetailRepository.findById(id).orElseThrow(()->
                new DataNotFoundException("cannot find orderDetail with id: " +id));
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        //tìm xem order detail có tồn tại hay không
        OrderDetail existingOrderdetail = orderDetailRepository.findById(id)
                .orElseThrow(()-> new DataNotFoundException("cannot find order detail with id: "+id));
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find order with id:" +orderDetailDTO.getOrderId()));
        Product existingProduct = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find product with id:"+orderDetailDTO.getProductId()));
        existingOrderdetail.setPrice(orderDetailDTO.getPrice());
        existingOrderdetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderdetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderdetail.setColor(orderDetailDTO.getColor());
        existingOrderdetail.setOrder(existingOrder);
        existingOrderdetail.setProduct(existingProduct);
        return orderDetailRepository.save(existingOrderdetail);
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long id) {

        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
