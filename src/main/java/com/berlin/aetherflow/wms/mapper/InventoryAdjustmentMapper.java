package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 库存调整单 Mapper。
 */
@Mapper
public interface InventoryAdjustmentMapper extends BaseMapper<InventoryAdjustment> {

    @Update("""
            update inventory_adjustment
            set status = #{confirmedStatus},
                adjust_time = case when adjust_time is null then #{adjustTime} else adjust_time end,
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and status = #{draftStatus}
            """)
    int confirmDraftOrder(@Param("id") Long id,
                          @Param("draftStatus") Integer draftStatus,
                          @Param("confirmedStatus") Integer confirmedStatus,
                          @Param("adjustTime") LocalDateTime adjustTime,
                          @Param("updateBy") String updateBy);
}
