package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
* @author berlin
* @description 针对表【outbound_order(出库单)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.wms.domain.entity.OutboundOrder
*/
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {

    @Update("""
            update outbound_order
            set status = #{confirmedStatus},
                outbound_time = case when outbound_time is null then #{outboundTime} else outbound_time end,
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and status = #{draftStatus}
            """)
    int confirmDraftOrder(@Param("id") Long id,
                          @Param("draftStatus") Integer draftStatus,
                          @Param("confirmedStatus") Integer confirmedStatus,
                          @Param("outboundTime") LocalDateTime outboundTime,
                          @Param("updateBy") String updateBy);
}




