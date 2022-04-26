package leeks.utils;

import java.util.HashMap;
import java.util.List;

/**
 * @Created by DAIE
 * @Date 2021/3/8 20:26
 * @Description leek面板TABLE工具类
 */
public class WindowUtils {

    private static HashMap<String,String> remapPinYinMap = new HashMap<>();

    public static void reg(List<String> names) {
        names.forEach(n -> remapPinYinMap.put(PinYinUtils.toPinYin(n), n));
    }

    public static String remapPinYin(String pinyin) {
        return remapPinYinMap.getOrDefault(pinyin, pinyin);
    }


}
