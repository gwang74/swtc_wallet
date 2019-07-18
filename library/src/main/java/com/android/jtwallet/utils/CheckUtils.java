package com.android.jtwallet.utils;

import android.text.TextUtils;

import com.android.jtwallet.client.Wallet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtils {
    /**
     * 校验钱包地址是否有效
     *
     * @param address
     * @return
     */
    public static boolean isValidAddress(String address) {
        if (!TextUtils.isEmpty(address)) {
            return Wallet.isValidAddress(address);
        }
        return false;
    }

    /**
     * 判断string是否为数字
     *
     * @param ledger_index
     * @return
     */
    public static boolean isNumeric(String ledger_index) {
        if (!TextUtils.isEmpty(ledger_index)) {
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(ledger_index);
            if (!isNum.matches()) {
                return false;
            }
        }
        return true;
    }
}
