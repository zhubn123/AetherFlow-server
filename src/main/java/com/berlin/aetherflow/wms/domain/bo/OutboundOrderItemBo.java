package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 出库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrderItem.class, reverseConvertGenerate = false)
public class OutboundOrderItemBo extends BaseEntity {

    private Long id;

    /**
     * 出库单ID。
     */
    private Long orderId;

    /**
     * 行号。
     */
    @Positive(message = "行号必须大于0", groups = {Default.class, CreateGroup.class})
    private Integer lineNo;

    /**
     * 物料ID。
     */
    @NotNull(message = "出库单明细物料不能为空", groups = {Default.class, CreateGroup.class})
    private Long materialId;

    /**
     * 来源库位ID。
     */
    private Long locationId;

    /**
     * 计划出库数量。
     */
    @NotNull(message = "出库单明细计划数量不能为空", groups = {Default.class, CreateGroup.class})
    @DecimalMin(value = "0.01", message = "出库单明细计划数量必须大于0", groups = {Default.class, CreateGroup.class})
    private BigDecimal plannedQty;

    /**
     * 已出库数量。
     */
    @DecimalMin(value = "0", message = "出库单明细已出库数量不能小于0", groups = {Default.class, CreateGroup.class})
    private BigDecimal shippedQty;

    /**
     * 备注。
     */
    @Size(max = 255, message = "出库单明细备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;
}
