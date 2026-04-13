package com.aitakeaway.server.mapper;

import com.aitakeaway.server.entity.Merchant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家 Mapper 接口
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}