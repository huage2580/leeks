package utils;

import com.github.promeg.pinyinhelper.Pinyin;

public class PinYinUtils {
    static String toPinYin(String input){
        return Pinyin.toPinyin(input,"_").toLowerCase();
    }
}
