package com.sampoom.backend.api.part.entity;

public enum ProcurementType {
    MANUFACTURE,   // 내부 생산
    PURCHASE,      // 외부 구매
    BOTH,
    EMERGENCY      // MRP 실패 시 긴급 조달
}
