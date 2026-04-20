package com.aitakeaway.server.service;

import com.aitakeaway.server.dto.ReviewVO;
import com.aitakeaway.server.entity.Review;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ReviewService extends IService<Review> {

    /** 提交评价：只有 COMPLETED 订单、且该订单未评过才能提交 */
    void submitReview(Long userId, Long orderId, Integer rating, String content);

    /** 获取某商家的所有评价列表（带用户名，按时间倒序） */
    List<ReviewVO> getMerchantReviews(Long merchantId);

    /** 获取某订单的评价（若未评过返回 null） */
    Review getOrderReview(Long orderId);

    /**
     * 获取商家平均评分，无评价时返回 null
     * 返回格式：保留一位小数的 Double
     */
    Double getAvgRating(Long merchantId);

    /** 获取商家评价数量 */
    long getReviewCount(Long merchantId);
}
