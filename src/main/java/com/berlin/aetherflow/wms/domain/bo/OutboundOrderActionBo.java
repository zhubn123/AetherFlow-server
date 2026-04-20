package com.berlin.aetherflow.wms.domain.bo;

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
    String action;
}
