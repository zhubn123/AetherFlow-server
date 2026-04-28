package com.berlin.aetherflow.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("stock_transaction")
public class StockTransaction extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务类型。
     */
    private String bizType;

    /**
     * 业务单据ID。
     */
    private Long bizId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 变动数量。
     */
    private BigDecimal changeQty;

    /**
     * 变动前库存。
     */
    private BigDecimal beforeQty;

    /**
     * 变动后库存。
     */
    private BigDecimal afterQty;

    /**
     * 操作人ID。
     */
    private Long operatorId;

    /**
     * 操作时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;

    /**
     * 备注。
     */
    private String remark;
}
