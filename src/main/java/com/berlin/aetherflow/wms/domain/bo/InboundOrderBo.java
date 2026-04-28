package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
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
 * 入库单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrder.class, reverseConvertGenerate = false)
public class InboundOrderBo extends BaseEntity {

    @Null(message = "创建入库单时ID必须为空", groups = CreateGroup.class)
    private Long id;

    /**
     * 入库单号。
     */
    private String orderNo;

    /**
     * 仓库ID。
     */
    @NotNull(message = "仓库不能为空", groups = CreateGroup.class)
    private Long warehouseId;

    /**
     * 状态（0草稿 1已确认）。
     */
    private Integer status;

    /**
     * 实际入库时间。
     */
    private LocalDateTime inboundTime;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过255个字符", groups = {Default.class, CreateGroup.class})
    private String remark;

    /**
     * 入库单明细
     */
    @NotNull(message = "入库单明细不能为空", groups = CreateGroup.class)
    @Size(min = 1, message = "入库单明细不能为空", groups = {Default.class, CreateGroup.class})
    private List<@NotNull(message = "入库单明细项不能为空", groups = {Default.class, CreateGroup.class}) @Valid InboundOrderItemBo> orderItemsBo;
}
