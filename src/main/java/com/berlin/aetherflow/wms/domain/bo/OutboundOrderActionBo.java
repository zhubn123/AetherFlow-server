package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * OutboundOrderActionBo
 *
 * @author zhubn
 * @date 2026/4/20
 */
// action（如 SUBMIT / CONFIRM / CANCEL）
// remark（可选）
// version（可选，做并发控制）
@Data
public class OutboundOrderActionBo {

    @NotBlank(message = "动作不能为空")
    @Size(max = 32, message = "动作长度不能超过32个字符")
    private String action;
}
