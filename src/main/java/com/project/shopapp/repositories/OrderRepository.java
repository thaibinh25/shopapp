package com.project.shopapp.repositories;

import com.project.shopapp.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    //tìm đơn hàng của 1 user nào đó
    List<Order> findByUserId(long id);

    /*@Query("SELECT o FROM Order o WHERE" +
            "(:keyword IS NULL OR :keyword = '' OR o.fullName LIKE %:keyword% OR o.address LIKE %:keyword%" +
            "OR o.note LIKE %:keyword%)")*/
    @Query("SELECT o FROM Order o WHERE o.active = true AND" +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(o.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Order> findByKeyword(String keyword, Pageable pageable);
}

