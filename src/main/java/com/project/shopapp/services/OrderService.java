package com.project.shopapp.services;

import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.dtos.NotificationDTO;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CouponRepository couponRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) throws  Exception {
        //tìm user theo userid có tồn tại không
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(()->new DataNotFoundException("Cannot find user with id "+orderDTO.getUserId()));
        //convert oderDTO => Oder
        //tạo luồng bảng ánh xạ riêng để kiểm soát việc ánh xạ
        modelMapper.typeMap(OrderDTO.class,Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        //cập nhật các trường của đơn hàng từ orderDTO
        Order order = new Order();
        modelMapper.map(orderDTO,order);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        //kiểm tra shpping date phải >= ngay hôm nay
        //LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now(): orderDTO.getShippingDate();
        LocalDate shippingDate = LocalDate.now().plusDays(2); // Giao hàng sau 2 ngày
        if (shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Date must be at last today !!");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        //địa chỉ giao hàng
        order.setZipCode(orderDTO.getZipCode());
        order.setPrefecture(orderDTO.getPrefecture());
        order.setCity(orderDTO.getCity());
        order.setAddressLine1(orderDTO.getAddressLine1());
        order.setAddressLine2(orderDTO.getAddressLine2());
        //order.setTotalMoney(orderDTO.getTotalMoney());
        orderRepository.save(order);

        //tạo danh sách các đối tương orderDetail từ cartItems
        List<OrderDetail> orderDetails = new ArrayList<>();
        float totalMoney = 0;

        for (CartItemDTO cartItemDTO: orderDTO.getCartItems()){
            //tạo một đối tượng orderDetail từ CartItemDTO
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            //lấy thông tin sản phẩm từ cartItemDTO
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            //tìm thông tin sản phẩm từ database (hoặc từ cache nếu cần)
            Product product = productRepository.findById(productId)
                    .orElseThrow(()-> new DataNotFoundException("Product not found with  id : "+ productId));

            //Đặt thông tin cho orderDetail
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());

            //thêm orderdetail vào danh sách
            orderDetails.add(orderDetail);

            totalMoney += product.getPrice() * quantity;

        }
        if (orderDTO.getCouponCode() != null && !orderDTO.getCouponCode().isEmpty()) {
            try {
                double discount = applyCoupon(orderDTO);
                float total = (float) (orderDTO.getTotalMoney() - discount);
                order.setTotalMoney(total > 0 ? total : 0);
            } catch (Exception e) {
                throw new RuntimeException("Coupon không hợp lệ: " + e.getMessage());
            }
        } else {
            order.setTotalMoney(orderDTO.getTotalMoney());
        }

        orderDetailRepository.saveAll(orderDetails);

        // Gửi email thông báo
        String subject = "🛒 Có đơn hàng mới từ " + order.getFullName();
        String content = String.format("Khách hàng: %s\nTổng tiền: %.2fusd\nPhương thức: %s",
                order.getFullName(), order.getTotalMoney(), order.getPaymentMethod());

        emailService.sendOrderNotification("tranthaibinh1998@gmail.com", subject, content);


        notificationService.createAndSendNotification(NotificationDTO.builder()
                .userId(order.getUser().getId())
                .title("Đặt hàng thành công")
                .content("Cảm ơn bạn đã đặt hàng. Mã đơn hàng: #" + order.getId())
                .build());


        return order;
    }

    @Override
    public Order getOrder(Long id) throws DataNotFoundException {
        return orderRepository.findById(id).orElseThrow(
                ()->new DataNotFoundException("null @@ ")
        );
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Cannot find order with id: " + id));
        User existingUser = userRepository.findById(
                orderDTO.getUserId()).orElseThrow(() ->
                new DataNotFoundException("Cannot find user with id: " + id));
        // Tạo một luồng bảng ánh xạ riêng để kiểm soát việc ánh xạ
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        // Cập nhật các trường của đơn hàng từ orderDTO
        modelMapper.map(orderDTO, order);
        order.setUser(existingUser);
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) throws DataNotFoundException {
        // Tìm đơn hàng bằng id
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id " + orderId));

        // Cập nhật trạng thái mới
        order.setStatus(status);



        // Tạo thông báo cho người dùng
        NotificationDTO noti = NotificationDTO.builder()
                .userId(order.getUser().getId())
                .title("Cập nhật đơn hàng")
                .content("Đơn hàng #" + order.getId() + " đã chuyển sang trạng thái: " + status)
                .build();

        notificationService.createAndSendNotification(noti); // ✅ Gửi real-time


        return orderRepository.save(order);
    }


    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(null);
        //xoá mềm đơn hàng
        if (order!=null){
            order.setActive(false);
            orderRepository.save(order);
        }

    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Page<Order> getOrderByKeyword(String keyword, Pageable pageable){
        return orderRepository.findByKeyword(keyword,pageable);
    }

    private double applyCoupon(OrderDTO orderDTO) {
        String couponCode = orderDTO.getCouponCode();
        if (couponCode == null || couponCode.isEmpty()) {
            return 0;
        }

        Coupon coupon = couponRepository.findByCodeAndActiveTrue(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại hoặc không còn hiệu lực"));


        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
        }

        double discount = coupon.isPercentage()
                ? orderDTO.getTotalMoney() * (coupon.getDiscountValue() / 100)
                : coupon.getDiscountValue();

        return discount;
    }


}
