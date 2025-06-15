package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.response.OrderDetailResponse;
import com.project.shopapp.services.OrderDetailService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
            ) throws DataNotFoundException {
        try{
            OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
            return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(newOrderDetail));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        try {
            OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
            return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //lấy danh sách orderdetail của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public  ResponseEntity<?> getOrderDetails(
            @Valid @PathVariable("orderId") Long orderId
    ){
        try {
            List<OrderDetail> orderDetails =orderDetailService.findByOrderId(orderId);
            List<OrderDetailResponse> orderDetailResponses =orderDetails
                    .stream()
                    .map(OrderDetailResponse::fromOrderDetail)
                    .toList();
            return ResponseEntity.ok(orderDetailResponses);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO
    ){
        try {
            OrderDetail orderDetail =  orderDetailService.updateOrderDetail(id, orderDetailDTO);
            return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY));
    }
}
