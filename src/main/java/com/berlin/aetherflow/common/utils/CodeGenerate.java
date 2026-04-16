package com.berlin.aetherflow.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CodeGenerate
 *
 * @author zhubn
 * @date 2026/4/15
 */

public class CodeGenerate {
    private static final String CK = "CK";

    // 1. 计数器：仅用于保证并发安全（作为锁或基础种子），不直接参与数字计算
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    // 2. 随机数生成器：负责生成核心数字
    private static final Random RANDOM = new Random();

    // 3. 年份格式化
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yy");

    /**
     * 生成编码
     * 逻辑：利用 AtomicInteger 保证线程安全，利用 Random 生成不可预测的数字
     */
    public static String generate() {
        // 1. 获取年份
        String year = LocalDate.now().format(YEAR_FORMATTER);

        // 2. 核心逻辑
        // 先让计数器自增（保证线程安全进入），然后直接调用 Random 生成 0-9999 的数
        ATOMIC_INTEGER.incrementAndGet();
        int randomNum = RANDOM.nextInt(10000); // 生成 0 到 9999 之间的随机数

        // 3. 格式化并拼接
        String number = String.format("%04d", randomNum);

        return CK + year + number;
    }

    // 测试方法
    public static void main(String[] args) {
        System.out.println(generate()); // 输出: CK26000001
        System.out.println(generate()); // 输出 : CK26000002
    }
}
