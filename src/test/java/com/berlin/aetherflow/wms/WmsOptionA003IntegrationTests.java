package com.berlin.aetherflow.wms;

import com.berlin.aetherflow.wms.domain.bo.MaterialBo;
import com.berlin.aetherflow.wms.domain.bo.WarehouseBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.WmsOptionQuery;
import com.berlin.aetherflow.wms.domain.vo.WmsOptionVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.MaterialService;
import com.berlin.aetherflow.wms.service.WarehouseService;
import com.berlin.aetherflow.wms.service.WmsOptionService;
import com.berlin.aetherflow.wms.support.WmsOptionCacheSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class WmsOptionA003IntegrationTests {

    @Autowired
    private WmsOptionService wmsOptionService;

    @Autowired
    private WmsOptionCacheSupport wmsOptionCacheSupport;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private AreaMapper areaMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private MaterialMapper materialMapper;

    private final List<Long> warehouseIds = new ArrayList<>();
    private final List<Long> areaIds = new ArrayList<>();
    private final List<Long> locationIds = new ArrayList<>();
    private final List<Long> materialIds = new ArrayList<>();

    @AfterEach
    void cleanupFixture() {
        if (!locationIds.isEmpty()) {
            locationMapper.deleteByIds(locationIds);
        }
        if (!areaIds.isEmpty()) {
            areaMapper.deleteByIds(areaIds);
        }
        if (!materialIds.isEmpty()) {
            materialMapper.deleteByIds(materialIds);
        }
        if (!warehouseIds.isEmpty()) {
            warehouseMapper.deleteByIds(warehouseIds);
        }
        wmsOptionCacheSupport.evictWarehouseRelatedOptions();
        wmsOptionCacheSupport.evictMaterialOptions();
        warehouseIds.clear();
        areaIds.clear();
        locationIds.clear();
        materialIds.clear();
    }

    @Test
    void queryOptionsShouldRespectStatusAndCascadeFilters() {
        String suffix = uniqueSuffix();
        Warehouse activeWarehouse = createWarehouse("WHA-" + suffix, "A003启用仓库-" + suffix, 0);
        Warehouse disabledWarehouse = createWarehouse("WHD-" + suffix, "A003停用仓库-" + suffix, 1);

        Area activeArea = createArea(activeWarehouse.getId(), "ARA-" + suffix, "A003启用区域-" + suffix, 0);
        createArea(activeWarehouse.getId(), "ARD-" + suffix, "A003停用区域-" + suffix, 1);
        Area otherArea = createArea(disabledWarehouse.getId(), "ARO-" + suffix, "A003其他区域-" + suffix, 0);

        createLocation(activeWarehouse.getId(), activeArea.getId(), "LCA-" + suffix, "A003启用库位-" + suffix, 0);
        createLocation(activeWarehouse.getId(), activeArea.getId(), "LCD-" + suffix, "A003停用库位-" + suffix, 1);
        createLocation(disabledWarehouse.getId(), otherArea.getId(), "LCO-" + suffix, "A003其他库位-" + suffix, 0);

        createMaterial("MTA-" + suffix, "A003启用物料-" + suffix, 0);
        createMaterial("MTD-" + suffix, "A003停用物料-" + suffix, 1);

        List<WmsOptionVo> warehouseOptions = wmsOptionService.queryWarehouseOptions(new WmsOptionQuery());
        assertTrue(warehouseOptions.stream()
                .anyMatch(option -> option.getId().equals(activeWarehouse.getId()) && option.getStatus() == 0));
        assertTrue(warehouseOptions.stream()
                .noneMatch(option -> option.getId().equals(disabledWarehouse.getId())));

        WmsOptionQuery areaQuery = new WmsOptionQuery();
        areaQuery.setWarehouseId(activeWarehouse.getId());
        List<WmsOptionVo> areaOptions = wmsOptionService.queryAreaOptions(areaQuery);
        assertEquals(1, areaOptions.size());
        assertEquals(activeArea.getId(), areaOptions.getFirst().getId());

        WmsOptionQuery locationQuery = new WmsOptionQuery();
        locationQuery.setWarehouseId(activeWarehouse.getId());
        locationQuery.setAreaId(activeArea.getId());
        List<WmsOptionVo> locationOptions = wmsOptionService.queryLocationOptions(locationQuery);
        assertEquals(1, locationOptions.size());
        assertEquals("LCA-" + suffix, locationOptions.getFirst().getCode());

        WmsOptionQuery materialQuery = new WmsOptionQuery();
        materialQuery.setKeyword(suffix);
        List<WmsOptionVo> materialOptions = wmsOptionService.queryMaterialOptions(materialQuery);
        assertEquals(1, materialOptions.size());
        assertEquals("MTA-" + suffix, materialOptions.getFirst().getCode());
    }

    @Test
    void updateWarehouseShouldEvictCachedOptions() {
        String suffix = uniqueSuffix();
        Warehouse warehouse = createWarehouse("WHC-" + suffix, "A003缓存仓库-" + suffix, 0);

        WmsOptionQuery query = new WmsOptionQuery();
        query.setKeyword("缓存仓库-" + suffix);
        List<WmsOptionVo> initialOptions = wmsOptionService.queryWarehouseOptions(query);
        assertEquals(1, initialOptions.size());
        assertEquals(warehouse.getId(), initialOptions.getFirst().getId());

        WarehouseBo updateBo = new WarehouseBo();
        updateBo.setId(warehouse.getId());
        updateBo.setWarehouseName("A003仓库已更新");
        updateBo.setStatus(0);
        updateBo.setRemark("updated");
        warehouseService.updateWarehouse(updateBo);

        List<WmsOptionVo> refreshedOptions = wmsOptionService.queryWarehouseOptions(query);
        assertTrue(refreshedOptions.isEmpty());
    }

    @Test
    void updateMaterialShouldEvictCachedOptions() {
        String suffix = uniqueSuffix();
        Material material = createMaterial("MTC-" + suffix, "A003缓存物料-" + suffix, 0);

        WmsOptionQuery query = new WmsOptionQuery();
        query.setKeyword(suffix);
        List<WmsOptionVo> initialOptions = wmsOptionService.queryMaterialOptions(query);
        assertEquals(1, initialOptions.size());
        assertEquals(material.getId(), initialOptions.getFirst().getId());

        MaterialBo updateBo = new MaterialBo();
        updateBo.setId(material.getId());
        updateBo.setMaterialName("A003物料已更新");
        updateBo.setSpecification(material.getSpecification());
        updateBo.setUnit(material.getUnit());
        updateBo.setStatus(1);
        updateBo.setRemark("updated");
        materialService.updateMaterial(updateBo);

        List<WmsOptionVo> refreshedOptions = wmsOptionService.queryMaterialOptions(query);
        assertTrue(refreshedOptions.isEmpty());
    }

    @Test
    void queryLocationOptionsShouldThrowWhenWarehouseMissing() {
        String suffix = uniqueSuffix();
        Warehouse warehouse = createWarehouse("WHL-" + suffix, "A003库位仓库-" + suffix, 0);
        Area area = createArea(warehouse.getId(), "ARL-" + suffix, "A003库位区域-" + suffix, 0);
        createLocation(warehouse.getId(), area.getId(), "LCL-" + suffix, "A003库位样例-" + suffix, 0);

        WmsOptionQuery query = new WmsOptionQuery();
        query.setAreaId(area.getId());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wmsOptionService.queryLocationOptions(query)
        );
        assertEquals("查询库位选项时必须传 warehouseId", exception.getMessage());
    }

    @Test
    void queryMaterialOptionsShouldThrowWhenKeywordMissing() {
        String suffix = uniqueSuffix();
        createMaterial("MTN-" + suffix, "A003无关键字物料-" + suffix, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wmsOptionService.queryMaterialOptions(new WmsOptionQuery())
        );
        assertEquals("查询物料选项时必须传 keyword", exception.getMessage());
    }

    @Test
    void queryMaterialOptionsShouldLimitKeywordResults() {
        String suffix = uniqueSuffix();
        for (int index = 1; index <= 130; index++) {
            createMaterial("MTL-" + suffix + "-" + index, "A003批量物料-" + suffix + "-" + index, 0);
        }

        WmsOptionQuery query = new WmsOptionQuery();
        query.setKeyword(suffix);
        List<WmsOptionVo> options = wmsOptionService.queryMaterialOptions(query);

        assertFalse(options.isEmpty());
        assertEquals(100, options.size());
    }

    private Warehouse createWarehouse(String code, String name, int status) {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(code);
        warehouse.setWarehouseName(name);
        warehouse.setStatus(status);
        warehouse.setRemark("A003 test");
        warehouseMapper.insert(warehouse);
        warehouseIds.add(warehouse.getId());
        assertNotNull(warehouse.getId());
        return warehouse;
    }

    private Area createArea(Long warehouseId, String code, String name, int status) {
        Area area = new Area();
        area.setWarehouseId(warehouseId);
        area.setAreaCode(code);
        area.setAreaName(name);
        area.setAreaType("STORAGE");
        area.setStatus(status);
        area.setRemark("A003 test");
        areaMapper.insert(area);
        areaIds.add(area.getId());
        assertNotNull(area.getId());
        return area;
    }

    private Location createLocation(Long warehouseId, Long areaId, String code, String name, int status) {
        Location location = new Location();
        location.setWarehouseId(warehouseId);
        location.setAreaId(areaId);
        location.setLocationCode(code);
        location.setLocationName(name);
        location.setStatus(status);
        location.setRemark("A003 test");
        locationMapper.insert(location);
        locationIds.add(location.getId());
        assertNotNull(location.getId());
        return location;
    }

    private Material createMaterial(String code, String name, int status) {
        Material material = new Material();
        material.setMaterialCode(code);
        material.setMaterialName(name);
        material.setSpecification("SPEC-" + code);
        material.setUnit("EA");
        material.setStatus(status);
        material.setRemark("A003 test");
        materialMapper.insert(material);
        materialIds.add(material.getId());
        assertNotNull(material.getId());
        return material;
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
