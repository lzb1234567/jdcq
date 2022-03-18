package fsnl.common.util.str;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：字符串处理工具类
 * @ClassName：StringUtil.java
 * @Date 2022/3/1 15:18
 */
public class StringUtil {

    /**
     * 去除字符串的最后一位字符。
     * @param sb
     * @return
     */
    public static String removeTail(StringBuffer sb){
        String str = sb.toString();
        return str.substring(0, str.length() - 1);
    }
}
