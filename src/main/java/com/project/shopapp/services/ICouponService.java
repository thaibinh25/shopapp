package com.project.shopapp.services;


import java.util.Map;

public interface ICouponService {

    Map<String, Object> applyCoupon(String code, double totalAmount);
}
