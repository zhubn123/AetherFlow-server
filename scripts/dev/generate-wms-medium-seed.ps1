[CmdletBinding()]
param(
    [string]$OutputPath = (Join-Path $PSScriptRoot "..\..\docs\local\generated-wms-medium-seed.sql"),
    [int]$WarehouseCount = 6,
    [int]$AreasPerWarehouse = 5,
    [int]$LocationsPerArea = 36,
    [int]$MaterialCount = 1200,
    [int]$InventoryPerLocation = 3,
    [int]$DisabledWarehouseCount = 1,
    [string]$CodePrefix = "A003M"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ($WarehouseCount -lt 1) {
    throw "WarehouseCount must be greater than 0."
}
if ($AreasPerWarehouse -lt 1) {
    throw "AreasPerWarehouse must be greater than 0."
}
if ($LocationsPerArea -lt 1) {
    throw "LocationsPerArea must be greater than 0."
}
if ($MaterialCount -lt 1) {
    throw "MaterialCount must be greater than 0."
}
if ($InventoryPerLocation -lt 0) {
    throw "InventoryPerLocation cannot be negative."
}
if ($DisabledWarehouseCount -lt 0 -or $DisabledWarehouseCount -gt $WarehouseCount) {
    throw "DisabledWarehouseCount must be between 0 and WarehouseCount."
}
if ([string]::IsNullOrWhiteSpace($CodePrefix)) {
    throw "CodePrefix cannot be empty."
}

function Resolve-FullPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PathValue
    )

    if ([System.IO.Path]::IsPathRooted($PathValue)) {
        return [System.IO.Path]::GetFullPath($PathValue)
    }

    return [System.IO.Path]::GetFullPath((Join-Path (Get-Location).Path $PathValue))
}

function Escape-SqlString {
    param(
        [AllowNull()]
        [string]$Value
    )

    if ($null -eq $Value) {
        return "null"
    }

    return "'" + $Value.Replace("'", "''") + "'"
}

function Format-Decimal {
    param(
        [decimal]$Value
    )

    return $Value.ToString("0.00", [System.Globalization.CultureInfo]::InvariantCulture)
}

function Add-BatchInsert {
    param(
        [Parameter(Mandatory = $true)]
        [System.Text.StringBuilder]$Builder,
        [Parameter(Mandatory = $true)]
        [string]$TableName,
        [Parameter(Mandatory = $true)]
        [string]$Columns,
        [Parameter(Mandatory = $true)]
        [System.Collections.Generic.List[string]]$Values,
        [int]$BatchSize = 200
    )

    if ($Values.Count -eq 0) {
        return
    }

    for ($start = 0; $start -lt $Values.Count; $start += $BatchSize) {
        $end = [Math]::Min($start + $BatchSize - 1, $Values.Count - 1)
        [void]$Builder.AppendLine("insert into $TableName ($Columns)")
        [void]$Builder.AppendLine("values")
        for ($index = $start; $index -le $end; $index++) {
            $suffix = if ($index -eq $end) { ";" } else { "," }
            [void]$Builder.AppendLine("    " + $Values[$index] + $suffix)
        }
        [void]$Builder.AppendLine()
    }
}

function New-SeedRow {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Properties
    )

    return [pscustomobject]$Properties
}

$resolvedOutputPath = Resolve-FullPath -PathValue $OutputPath
$outputDirectory = Split-Path -Parent $resolvedOutputPath
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$warehouseBaseId = 2100000000000000001L
$areaBaseId = 2100000000000100001L
$locationBaseId = 2100000000000200001L
$materialBaseId = 2100000000000300001L
$inventoryBaseId = 2100000000000400001L

$warehouseRangeMax = 2100000000000009999L
$areaRangeMax = 2100000000000199999L
$locationRangeMax = 2100000000000299999L
$materialRangeMax = 2100000000000399999L
$inventoryRangeMax = 2100000000000499999L

$areaTypes = @("RECEIVING", "STORAGE", "PICKING", "QUALITY", "BUFFER")
$materialUnits = @("EA", "BOX", "SET", "ROLL")
$materialSpecs = @("M8", "M10", "SUS304", "ALU", "STD")

$warehouses = New-Object System.Collections.Generic.List[object]
$areas = New-Object System.Collections.Generic.List[object]
$locations = New-Object System.Collections.Generic.List[object]
$materials = New-Object System.Collections.Generic.List[object]
$inventoryRows = New-Object System.Collections.Generic.List[object]

$disabledWarehouseStart = $WarehouseCount - $DisabledWarehouseCount + 1

for ($warehouseIndex = 1; $warehouseIndex -le $WarehouseCount; $warehouseIndex++) {
    $warehouseId = $warehouseBaseId + $warehouseIndex - 1
    $warehouseStatus = if ($warehouseIndex -ge $disabledWarehouseStart) { 1 } else { 0 }
    $warehouseCode = "WH-$CodePrefix-{0:D3}" -f $warehouseIndex
    $warehouseName = "$CodePrefix Warehouse {0:D3}" -f $warehouseIndex

    $warehouse = New-SeedRow @{
        Id = $warehouseId
        Ordinal = $warehouseIndex
        Code = $warehouseCode
        Name = $warehouseName
        Status = $warehouseStatus
        Remark = "generated-medium-seed"
    }
    $warehouses.Add($warehouse)

    for ($areaIndex = 1; $areaIndex -le $AreasPerWarehouse; $areaIndex++) {
        $areaId = $areaBaseId + $areas.Count
        $areaType = $areaTypes[($areaIndex - 1) % $areaTypes.Count]
        $areaStatus = if ($warehouseStatus -eq 1 -or $areaIndex -eq $AreasPerWarehouse) { 1 } else { 0 }
        $areaCode = "AR-$CodePrefix-W{0:D3}-A{1:D2}" -f $warehouseIndex, $areaIndex
        $areaName = "$CodePrefix Area W{0:D3} A{1:D2}" -f $warehouseIndex, $areaIndex

        $area = New-SeedRow @{
            Id = $areaId
            WarehouseId = $warehouseId
            WarehouseOrdinal = $warehouseIndex
            Ordinal = $areaIndex
            Code = $areaCode
            Name = $areaName
            AreaType = $areaType
            Status = $areaStatus
            Remark = "generated-medium-seed"
        }
        $areas.Add($area)

        for ($locationIndex = 1; $locationIndex -le $LocationsPerArea; $locationIndex++) {
            $locationId = $locationBaseId + $locations.Count
            $locationStatus = if ($areaStatus -eq 1 -or ($locationIndex % 17) -eq 0) { 1 } else { 0 }
            $locationCode = "LOC-$CodePrefix-W{0:D3}-A{1:D2}-L{2:D3}" -f $warehouseIndex, $areaIndex, $locationIndex
            $locationName = "$CodePrefix Location W{0:D3} A{1:D2} L{2:D3}" -f $warehouseIndex, $areaIndex, $locationIndex

            $location = New-SeedRow @{
                Id = $locationId
                WarehouseId = $warehouseId
                AreaId = $areaId
                WarehouseOrdinal = $warehouseIndex
                AreaOrdinal = $areaIndex
                Ordinal = $locationIndex
                Code = $locationCode
                Name = $locationName
                Status = $locationStatus
                Remark = "generated-medium-seed"
            }
            $locations.Add($location)
        }
    }
}

for ($materialIndex = 1; $materialIndex -le $MaterialCount; $materialIndex++) {
    $materialId = $materialBaseId + $materialIndex - 1
    $materialStatus = if (($materialIndex % 19) -eq 0) { 1 } else { 0 }
    $materialCode = "MAT-$CodePrefix-{0:D5}" -f $materialIndex
    $materialName = "$CodePrefix Material {0:D5}" -f $materialIndex
    $materialSpec = $materialSpecs[($materialIndex - 1) % $materialSpecs.Count] + "-{0:D2}" -f ((($materialIndex - 1) % 24) + 1)
    $materialUnit = $materialUnits[($materialIndex - 1) % $materialUnits.Count]

    $material = New-SeedRow @{
        Id = $materialId
        Ordinal = $materialIndex
        Code = $materialCode
        Name = $materialName
        Specification = $materialSpec
        Unit = $materialUnit
        Status = $materialStatus
        Remark = "generated-medium-seed"
    }
    $materials.Add($material)
}

$activeLocations = @($locations | Where-Object { $_.Status -eq 0 })
$activeMaterials = @($materials | Where-Object { $_.Status -eq 0 })

if ($InventoryPerLocation -gt 0 -and $activeMaterials.Count -eq 0) {
    throw "No active materials were generated, cannot build inventory rows."
}

$inventoryIndex = 0
foreach ($location in $activeLocations) {
    for ($slot = 0; $slot -lt $InventoryPerLocation; $slot++) {
        $materialIndex = (($inventoryIndex + $slot) % $activeMaterials.Count)
        $material = $activeMaterials[$materialIndex]
        $inventoryId = $inventoryBaseId + $inventoryRows.Count
        $patternIndex = $inventoryRows.Count + 1

        if (($patternIndex % 11) -eq 0) {
            $quantity = [decimal]0
            $lockedQuantity = [decimal]0
        } else {
            $quantity = [decimal](20 + (($patternIndex % 24) * 5))
            if (($patternIndex % 7) -eq 0) {
                $lockedQuantity = [decimal][Math]::Min([double]($quantity / 3), 20.0)
            } else {
                $lockedQuantity = [decimal]0
            }
        }

        $inventoryRows.Add((New-SeedRow @{
            Id = $inventoryId
            WarehouseId = $location.WarehouseId
            LocationId = $location.Id
            MaterialId = $material.Id
            Quantity = $quantity
            LockedQuantity = $lockedQuantity
        }))
    }

    $inventoryIndex++
}

$sql = New-Object System.Text.StringBuilder
[void]$sql.AppendLine("-- Generated by scripts/dev/generate-wms-medium-seed.ps1")
[void]$sql.AppendLine("-- Purpose: local-only medium seed for A003/A005 option, search, and cache verification")
[void]$sql.AppendLine("-- Safe usage: run after Liquibase baseline, do not include in db.changelog-master.yaml")
[void]$sql.AppendLine("-- Reserved ID ranges:")
[void]$sql.AppendLine("--   warehouse: $warehouseBaseId ~ $warehouseRangeMax")
[void]$sql.AppendLine("--   area:      $areaBaseId ~ $areaRangeMax")
[void]$sql.AppendLine("--   location:  $locationBaseId ~ $locationRangeMax")
[void]$sql.AppendLine("--   material:  $materialBaseId ~ $materialRangeMax")
[void]$sql.AppendLine("--   inventory: $inventoryBaseId ~ $inventoryRangeMax")
[void]$sql.AppendLine()
[void]$sql.AppendLine("delete from inventory where id between $inventoryBaseId and $inventoryRangeMax;")
[void]$sql.AppendLine("delete from location where id between $locationBaseId and $locationRangeMax;")
[void]$sql.AppendLine("delete from area where id between $areaBaseId and $areaRangeMax;")
[void]$sql.AppendLine("delete from material where id between $materialBaseId and $materialRangeMax;")
[void]$sql.AppendLine("delete from warehouse where id between $warehouseBaseId and $warehouseRangeMax;")
[void]$sql.AppendLine()

$warehouseValues = New-Object System.Collections.Generic.List[string]
foreach ($warehouse in $warehouses) {
    $warehouseValues.Add(
        "($($warehouse.Id), $(Escape-SqlString $warehouse.Code), $(Escape-SqlString $warehouse.Name), $($warehouse.Status), $(Escape-SqlString $warehouse.Remark), 'seed-script', now(), 'seed-script', now())"
    )
}
Add-BatchInsert -Builder $sql `
    -TableName "warehouse" `
    -Columns "id, warehouse_code, warehouse_name, status, remark, create_by, create_time, update_by, update_time" `
    -Values $warehouseValues

$areaValues = New-Object System.Collections.Generic.List[string]
foreach ($area in $areas) {
    $areaValues.Add(
        "($($area.Id), $($area.WarehouseId), $(Escape-SqlString $area.Code), $(Escape-SqlString $area.Name), $(Escape-SqlString $area.AreaType), $($area.Status), $(Escape-SqlString $area.Remark), 'seed-script', now(), 'seed-script', now())"
    )
}
Add-BatchInsert -Builder $sql `
    -TableName "area" `
    -Columns "id, warehouse_id, area_code, area_name, area_type, status, remark, create_by, create_time, update_by, update_time" `
    -Values $areaValues

$locationValues = New-Object System.Collections.Generic.List[string]
foreach ($location in $locations) {
    $locationValues.Add(
        "($($location.Id), $($location.WarehouseId), $($location.AreaId), $(Escape-SqlString $location.Code), $(Escape-SqlString $location.Name), $($location.Status), $(Escape-SqlString $location.Remark), 'seed-script', now(), 'seed-script', now())"
    )
}
Add-BatchInsert -Builder $sql `
    -TableName "location" `
    -Columns "id, warehouse_id, area_id, location_code, location_name, status, remark, create_by, create_time, update_by, update_time" `
    -Values $locationValues

$materialValues = New-Object System.Collections.Generic.List[string]
foreach ($material in $materials) {
    $materialValues.Add(
        "($($material.Id), $(Escape-SqlString $material.Code), $(Escape-SqlString $material.Name), $(Escape-SqlString $material.Specification), $(Escape-SqlString $material.Unit), $($material.Status), $(Escape-SqlString $material.Remark), 'seed-script', now(), 'seed-script', now())"
    )
}
Add-BatchInsert -Builder $sql `
    -TableName "material" `
    -Columns "id, material_code, material_name, specification, unit, status, remark, create_by, create_time, update_by, update_time" `
    -Values $materialValues

$inventoryValues = New-Object System.Collections.Generic.List[string]
foreach ($inventory in $inventoryRows) {
    $inventoryValues.Add(
        "($($inventory.Id), $($inventory.WarehouseId), $($inventory.LocationId), $($inventory.MaterialId), $(Format-Decimal $inventory.Quantity), $(Format-Decimal $inventory.LockedQuantity), 'seed-script', now(), 'seed-script', now())"
    )
}
Add-BatchInsert -Builder $sql `
    -TableName "inventory" `
    -Columns "id, warehouse_id, location_id, material_id, quantity, locked_quantity, create_by, create_time, update_by, update_time" `
    -Values $inventoryValues

[System.IO.File]::WriteAllText($resolvedOutputPath, $sql.ToString(), [System.Text.UTF8Encoding]::new($false))

$summary = @(
    "Generated local medium seed SQL:",
    "  OutputPath: $resolvedOutputPath",
    "  Warehouses: $($warehouses.Count)",
    "  Areas: $($areas.Count)",
    "  Locations: $($locations.Count)",
    "  Materials: $($materials.Count)",
    "  InventoryRows: $($inventoryRows.Count)",
    "  ActiveLocations: $($activeLocations.Count)",
    "  ActiveMaterials: $($activeMaterials.Count)"
)

$summary -join [Environment]::NewLine | Write-Host
