package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustmentItem;
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
 * 库存调整单明细参数。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InventoryAdjustmentItem.class, reverseConvertGenerate = false)
public class InventoryAdjustmentItemBo extends BaseEntity {

    private Long id;

    /**
     * 调整单ID。
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
    @NotNull(message = "库存调整明细物料不能为空", groups = {Default.class, CreateGroup.class})
    private Long materialId;

    /**
     * 库位ID。
     */
    @NotNull(message = "库存调整明细库位不能为空", groups = {Default.class, CreateGroup.class})
    private Long locationId;

    /**
     * 调整数量。
     */
    @NotNull(message = "库存调整明细调整数量不能为空", groups = {Default.class, CreateGroup.class})
    @DecimalMin(value = "0.01", message = "库存调整明细调整数量必须大于0", groups = {Default.class, CreateGroup.class})
    private BigDecimal adjustQty;

    /**
     * 备注。
     */
    @Size(max = 255, message = "库存调整明细备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;
}
