package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;
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
 * 入库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrderItem.class, reverseConvertGenerate = false)
public class InboundOrderItemBo extends BaseEntity {

    private Long id;

    /**
     * 入库单ID。
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
    @NotNull(message = "入库单明细物料不能为空", groups = {Default.class, CreateGroup.class})
    private Long materialId;

    /**
     * 目标库位ID。
     */
    private Long locationId;

    /**
     * 计划入库数量。
     */
    @NotNull(message = "入库单明细计划数量不能为空", groups = {Default.class, CreateGroup.class})
    @DecimalMin(value = "0.01", message = "入库单明细计划数量必须大于0", groups = {Default.class, CreateGroup.class})
    private BigDecimal plannedQty;

    /**
     * 已入库数量。
     */
    @DecimalMin(value = "0", message = "入库单明细已入库数量不能小于0", groups = {Default.class, CreateGroup.class})
    private BigDecimal receivedQty;

    /**
     * 备注。
     */
    @Size(max = 255, message = "入库单明细备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;
}
