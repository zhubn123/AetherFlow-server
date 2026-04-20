package com.berlin.aetherflow.wms.constant;

/**
 * OrderStatusConst
 *
 * @author zhubn
 * @date 2026/4/20
 */
// TODO 升级成从数据库表中取
public interface OrderStatusConst {
    /**
     * 单据暂存状态 - ORDER DRAFT
     */
    Integer DRAFT = 0;

    /**
     * 单据完成状态 - OUTBOUND CONFIRMED
     */
    Integer CONFIRMED = 1;
}
