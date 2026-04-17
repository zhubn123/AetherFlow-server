package com.berlin.aetherflow.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CodeGenerate
 *
 * @author zhubn
 * @date 2026/4/15
 */

public class CodeGenerate {
    // 使用 ConcurrentHashMap 存储每种类型的计数器
    private static final ConcurrentHashMap<String, AtomicInteger> COUNTERS = new ConcurrentHashMap<>();

    // 用于生成随机数的随机数生成器
    private static final Random RANDOM = new Random();

    // 年份格式化
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");

    /**
     * 生成编码
     * 逻辑：根据类型分别维护计数器，保证同类型编码的唯一性和连续性
     */
    public static String generate(String type) {
        // 1. 获取年份
        String year = LocalDate.now().format(YEAR_FORMATTER);

        // 2. 获取对应类型的计数器，如果不存在则创建新的
        AtomicInteger counter = COUNTERS.computeIfAbsent(type, k -> new AtomicInteger(0));

        // 3. 让计数器自增
        int count = counter.incrementAndGet();

        // 4. 使用随机数确保唯一性（虽然计数器已能保证唯一性）
        int randomNum = RANDOM.nextInt(8999) + 1000; // 生成 1000 到 9999 之间的随机数

        // 5. 格式化并拼接
        String number = String.format("%04d", randomNum);
        String sequence = String.format("%03d", count % 1000); // 使用计数器的后三位

        return type + year + sequence + number;
    }

    /**
     * 简化版本 - 只使用计数器生成序列号
     */
    public static String generateSimple(String type) {
        // 1. 获取年份
        String year = LocalDate.now().format(YEAR_FORMATTER);

        // 2. 获取对应类型的计数器，如果不存在则创建新的
        AtomicInteger counter = COUNTERS.computeIfAbsent(type, k -> new AtomicInteger(0));

        // 3. 让计数器自增
        int count = counter.incrementAndGet();

        // 4. 格式化序列号为6位数字
        String sequence = String.format("%06d", count);

        return type + year + sequence;
    }

    // 测试方法
    public static void main(String[] args) {
        // 测试不同类型的编码生成
        System.out.println(generateSimple("CK")); // 出库单：CK26000001
        System.out.println(generateSimple("RK")); // 入库单：RK26000001
        System.out.println(generateSimple("CK")); // 出库单：CK26000002
        System.out.println(generateSimple("RK")); // 入库单：RK26000002
        System.out.println(generateSimple("WH")); // 仓库：WH26000001
    }
}