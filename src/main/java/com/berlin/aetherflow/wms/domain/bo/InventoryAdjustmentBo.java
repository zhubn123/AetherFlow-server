package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存调整单参数。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InventoryAdjustment.class, reverseConvertGenerate = false)
public class InventoryAdjustmentBo extends BaseEntity {

    @Null(message = "创建库存调整单时ID必须为空", groups = CreateGroup.class)
    private Long id;

    /**
     * 调整单号。
     */
    private String orderNo;

    /**
     * 仓库ID。
     */
    @NotNull(message = "仓库不能为空", groups = {Default.class, CreateGroup.class})
    private Long warehouseId;

    /**
     * 区域ID。
     */
    @NotNull(message = "区域不能为空", groups = {Default.class, CreateGroup.class})
    private Long areaId;

    /**
     * 调整方向。
     */
    @NotBlank(message = "调整方向不能为空", groups = {Default.class, CreateGroup.class})
    @Size(max = 16, message = "调整方向长度不能超过16个字符", groups = {Default.class, CreateGroup.class})
    private String adjustType;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 实际调整时间。
     */
    private LocalDateTime adjustTime;

    /**
     * 调整原因。
     */
    @NotBlank(message = "调整原因不能为空", groups = {Default.class, CreateGroup.class})
    @Size(max = 128, message = "调整原因长度不能超过128个字符", groups = {Default.class, CreateGroup.class})
    private String adjustReason;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;

    /**
     * 调整明细。
     */
    @NotNull(message = "库存调整明细不能为空", groups = CreateGroup.class)
    @Size(min = 1, message = "库存调整明细不能为空", groups = {Default.class, CreateGroup.class})
    private List<@NotNull(message = "库存调整明细项不能为空", groups = {Default.class, CreateGroup.class}) @Valid InventoryAdjustmentItemBo> adjustmentItemsBo;
}
