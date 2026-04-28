package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 库存调整单动作参数。
 */
@Data
public class InventoryAdjustmentActionBo {

    @NotBlank(message = "动作不能为空")
    @Size(max = 32, message = "动作长度不能超过32个字符")
    private String action;
}
