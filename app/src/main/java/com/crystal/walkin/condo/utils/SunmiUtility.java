package com.crystal.walkin.condo.utils;

import android.os.Bundle;

import java.util.Set;

public final class SunmiUtility {
    private SunmiUtility() {
        throw new AssertionError("Create instance of Utility is forbidden.");
    }

    /** Bundle对象转换成字符串 */
    public static String bundle2String(Bundle bundle) {
        if (bundle == null || bundle.keySet().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Set<String> set = bundle.keySet();
        for (String key : set) {
            sb.append(key);
            sb.append(":");
            sb.append(bundle.get(key));
            sb.append("\n");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /** 将null转换成空串 */
    public static String null2String(String str) {
        return str == null ? "" : str;
    }
}
