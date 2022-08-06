package dev.jianmu.engine.register.util;

import dev.jianmu.engine.rpc.util.Assert;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

/**
 * cron 表达式解析工具
 * @apiNote Just as an example
 * */
@UtilityClass
public class CronParser {

    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    /**
     * @param cron 1h2m3s
     *  不限制顺序，不限制重复次数
     *  For example: 2s3s1m -> 1m5s后执行
     * */
    public static long parse(String cron) {
        final char[] chars = cron.toCharArray();
        int total = 0;
        int perCount = 0;
        for (final char aChar : chars) {
            int multiple;
            switch (aChar) {
                case 's': {
                    multiple = 1;
                    break;
                }
                case 'm': {
                    multiple = 60;
                    break;
                }
                case 'h': {
                    multiple = 3600;
                    break;
                }
                default: {
                    Assert.isTrue(48 <= aChar && aChar <= 57, "Bad number character range");
                    perCount *= 10;
                    perCount += aChar - 48;
                    continue;
                }
            }
            total += multiple * perCount;
            perCount = 0;
        }
        return total;
    }

    /**
     * @return 是否超时
     * */
    public static boolean timeout(String cron, long limitTime) {
        return limitTime <= parse(cron) * 1000L + System.currentTimeMillis();
    }

}
