package com.aitakeaway.server.controller;

import com.aitakeaway.server.common.Result;
import com.aitakeaway.server.dto.ReviewVO;
import com.aitakeaway.server.entity.Review;
import com.aitakeaway.server.service.ReviewService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new RuntimeException("请先登录");
    }

    /** 提交评价 */
    @PostMapping
    public Result<Void> submitReview(@RequestBody ReviewRequest request) {
        reviewService.submitReview(getCurrentUserId(), request.getOrderId(),
                request.getRating(), request.getContent());
        return Result.success();
    }

    /** 获取商家评价列表 */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<ReviewVO>> getMerchantReviews(@PathVariable Long merchantId) {
        return Result.success(reviewService.getMerchantReviews(merchantId));
    }

    /** 获取某订单的评价（用于判断是否已评价） */
    @GetMapping("/order/{orderId}")
    public Result<Review> getOrderReview(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReview(orderId));
    }

    /** 获取商家平均评分 */
    @GetMapping("/merchant/{merchantId}/rating")
    public Result<RatingVO> getMerchantRating(@PathVariable Long merchantId) {
        Double avg = reviewService.getAvgRating(merchantId);
        long count = reviewService.getReviewCount(merchantId);
        return Result.success(new RatingVO(avg, count));
    }

    @Data
    static class ReviewRequest {
        private Long orderId;
        private Integer rating;
        private String content;
    }

    @Data
    static class RatingVO {
        private final Double avgRating;
        private final long reviewCount;
    }
}
