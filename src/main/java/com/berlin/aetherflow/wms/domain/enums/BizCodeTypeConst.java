package com.berlin.aetherflow.wms.domain.enums;

/**
 * BizCodeTypeConst
 *
 * @author zhubn
 * @date 2026/4/17
 */
public interface BizCodeTypeConst {
    /**
     * 入库单编码 - INBOUND ORDER
     * 用于标识货物进入仓库的业务单据
     */
    String INBOUND_ORDER = "IO";

    /**
     * 出库单编码 - OUTBOUND ORDER
     * 用于标识货物离开仓库的业务单据
     */
    String OUTBOUND_ORDER = "OO";

    /**
     * 仓库编码 - WAREHOUSE
     * 用于标识具体的仓库或存储区域
     */
    String WAREHOUSE = "WH";

    /**
     * 采购入库单 - PURCHASE INBOUND
     * 专门用于采购业务的入库单据
     */
    String PURCHASE_INBOUND = "PI";

    /**
     * 销售出库单 - SALES OUTBOUND
     * 专门用于销售业务的出库单据
     */
    String SALES_OUTBOUND = "SO";

    /**
     * 调拨单编码 - TRANSFER ORDER
     * 用于仓库之间或库位之间的货物转移
     */
    String TRANSFER_ORDER = "TO";

    /**
     * 盘点单编码 - INVENTORY COUNT
     * 用于库存盘点业务的单据
     */
    String INVENTORY_COUNT = "IC";

    /**
     * 退货入库单 - RETURN INBOUND
     * 客户退货或不合格品返库的单据
     */
    String RETURN_INBOUND = "RI";
}
