package com.berlin.aetherflow.wms.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 物料实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("material")
public class MaterialVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料编码。
     */
    private String materialCode;

    /**
     * 物料名称。
     */
    private String materialName;

    /**
     * 规格型号。
     */
    private String specification;

    /**
     * 计量单位。
     */
    private String unit;

    /**
     * 物料状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}
