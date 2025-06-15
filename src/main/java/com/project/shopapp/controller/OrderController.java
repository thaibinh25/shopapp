package com.project.shopapp.controller;


import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.Order;
import com.project.shopapp.response.OrderListResponse;
import com.project.shopapp.response.OrderResponse;
import com.project.shopapp.services.OrderService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    public ResponseEntity<?> creatOrder(@Valid @RequestBody OrderDTO orderDTO,
                                           BindingResult result) throws Exception {
        if (result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        Order order = orderService.createOrder(orderDTO);

        return ResponseEntity.ok(order);
    }


    @GetMapping("/users/{user_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getOrders(@Valid @PathVariable("user_id") Long userId){
        try{
            List<Order> orders =orderService.findByUserId(userId);
            List<OrderResponse> responseList = orders.stream()
                    .sorted(((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate())))
                    .map(OrderResponse::fromOrder)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") Long orderId){
        try{
            Order exiistingOrder = orderService.getOrder(orderId);
            OrderResponse orderResponse = OrderResponse.fromOrder(exiistingOrder);
            return ResponseEntity.ok(orderResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    //đây là công việc của admin
    public ResponseEntity<?> updateOrder(
            @Valid @PathVariable long id,
            @Valid @RequestBody OrderDTO orderDTO
    ){
        try{
            Order order = orderService.updateOrder(id,orderDTO);
            OrderResponse response = OrderResponse.fromOrder(order);
            return  ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    //@PatchMapping("/{id}/status"): Chúng ta sử dụng PATCH để cập nhật một phần dữ liệu của tài nguyên (trong trường hợp này là trạng thái).
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") Long orderId,
                                               @RequestParam("status") String status) {
        try {
            // Cập nhật trạng thái cho đơn hàng
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);

            // Chuyển đổi sang OrderResponse và trả về
            OrderResponse orderResponse = OrderResponse.fromOrder(updatedOrder);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@Valid @PathVariable long id){
        //xoá mềm => cập nhật trường active = false
        orderService.deleteOrder(id);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY));
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OrderListResponse> getOrdersByKeyword(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        PageRequest pageRequest = PageRequest.of(
                page,limit,
                Sort.by("id").ascending()
        );
        Page<OrderResponse> orderPage = orderService
                .getOrderByKeyword(keyword,pageRequest)
                .map(OrderResponse::fromOrder);

        int totalPage = orderPage.getTotalPages();
        List<OrderResponse> orderResponses = orderPage.getContent();
        return ResponseEntity.ok(OrderListResponse
                .builder()
                .orders(orderResponses)
                .totalPages(totalPage)
                .build());
    }

}
