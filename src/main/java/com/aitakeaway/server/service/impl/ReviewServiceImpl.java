package com.aitakeaway.server.service.impl;

import com.aitakeaway.server.dto.ReviewVO;
import com.aitakeaway.server.entity.Order;
import com.aitakeaway.server.entity.Review;
import com.aitakeaway.server.entity.User;
import com.aitakeaway.server.mapper.ReviewMapper;
import com.aitakeaway.server.service.OrderService;
import com.aitakeaway.server.service.ReviewService;
import com.aitakeaway.server.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final OrderService orderService;
    private final UserService userService;

    @Override
    public void submitReview(Long userId, Long orderId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权评价");
        }
        if (order.getStatus() != Order.STATUS_COMPLETED) {
            throw new RuntimeException("只有已完成的订单才能评价");
        }
        boolean alreadyReviewed = count(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getDeleted, 0)) > 0;
        if (alreadyReviewed) {
            throw new RuntimeException("该订单已经评价过了");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setMerchantId(order.getMerchantId());
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        save(review);
    }

    @Override
    public List<ReviewVO> getMerchantReviews(Long merchantId) {
        List<Review> reviews = list(new LambdaQueryWrapper<Review>()
                .eq(Review::getMerchantId, merchantId)
                .eq(Review::getDeleted, 0)
                .orderByDesc(Review::getCreateTime));
        return reviews.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Review getOrderReview(Long orderId) {
        return getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getDeleted, 0)
                .last("LIMIT 1"));
    }

    @Override
    public Double getAvgRating(Long merchantId) {
        List<Review> reviews = list(new LambdaQueryWrapper<Review>()
                .eq(Review::getMerchantId, merchantId)
                .eq(Review::getDeleted, 0)
                .select(Review::getRating));
        if (reviews.isEmpty()) return null;
        OptionalDouble avg = reviews.stream().mapToInt(Review::getRating).average();
        if (avg.isEmpty()) return null;
        return BigDecimal.valueOf(avg.getAsDouble())
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public long getReviewCount(Long merchantId) {
        return count(new LambdaQueryWrapper<Review>()
                .eq(Review::getMerchantId, merchantId)
                .eq(Review::getDeleted, 0));
    }

    private ReviewVO toVO(Review r) {
        ReviewVO vo = new ReviewVO();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        vo.setRating(r.getRating());
        vo.setContent(r.getContent());
        vo.setCreateTime(r.getCreateTime());
        User user = userService.getById(r.getUserId());
        vo.setUsername(maskUsername(user != null ? user.getUsername() : "匿名用户"));
        return vo;
    }

    private String maskUsername(String name) {
        if (name == null || name.length() <= 1) return "***";
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "**" + name.charAt(name.length() - 1);
    }
}
