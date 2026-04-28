package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
* @author berlin
* @description 针对表【stock(库存表)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.wms.domain.entity.Inventory
*/
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("""
            update inventory
            set quantity = quantity - #{deductQty},
                update_by = #{updateBy},
                update_time = now()
            where id = #{id}
              and quantity - locked_quantity >= #{deductQty}
            """)
    int deductAvailableQuantity(@Param("id") Long id,
                                @Param("deductQty") BigDecimal deductQty,
                                @Param("updateBy") String updateBy);
}




