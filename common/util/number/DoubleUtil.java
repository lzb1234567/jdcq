package fsnl.common.util.number;

import java.math.BigDecimal;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：double类似工具类
 * @ClassName：DoubleUtil.java
 * @Date 2022/5/19 16:56
 */
public class DoubleUtil {
    /**
     * 针对double类型保留i位小数
     * @param number 需要处理的数值
     * @param i 保留多少位
     * @return 返回处理结果
     */
    public static double getValueByPlace(double number , int i){
        BigDecimal valueDecimal = new BigDecimal(number);
        return valueDecimal.setScale(i, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
