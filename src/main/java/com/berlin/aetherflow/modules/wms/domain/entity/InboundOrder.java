package com.berlin.aetherflow.modules.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_order")
public class InboundOrder extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 入库单号。
     */
    private String orderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 状态（0草稿 1已确认）。
     */
    private Integer status;

    /**
     * 总数量。
     */
    private BigDecimal totalQty;

    /**
     * 入库时间。
     */
    private LocalDateTime inboundTime;

    /**
     * 备注。
     */
    private String remark;
}
