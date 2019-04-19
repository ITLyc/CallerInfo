package com.lyc.callerinfo.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)) {
            return true;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取assets中json文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String getJson(Context context, String fileName) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 手机号中间四位由****代替
     *
     * @param phone
     * @return
     */
    public static String formatPhoneNo(String phone) {
        if (phone.length() == 11) {
            StringBuilder sb = new StringBuilder(phone);
            sb.replace(3, 7, "****");
            return sb.toString();
        } else {
            return phone;
        }
    }

    /**
     * 手机号中间四位由****代替
     *
     * @param idCard
     * @return
     */
    public static String formatIdCard(String idCard) {
        if (idCard.length() == 18) {
            StringBuilder sb = new StringBuilder(idCard);
            sb.replace(1, 17, "****************");
            return sb.toString();
        } else {
            return idCard;
        }
    }


    /**
     * 保留两位小数
     *
     * @param money
     * @return
     */
    public static String formatMoney(String money) {

        if (money != null && !"".equals(money)) {
            BigDecimal b = new BigDecimal(money);
            b = b.setScale(2, BigDecimal.ROUND_DOWN); //小数位 直接舍去
            return b.toString();
        }

        return "0.00";
    }

    /**
     * 保留两位小数
     *
     * @param money
     * @return
     */
    public static String formatMoney(double money) {
        BigDecimal b = new BigDecimal(money);
        b = b.setScale(2, BigDecimal.ROUND_HALF_UP); //小数位 四舍五入
        return b.toString();
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回
     */
    public static int getInt(Object obj, int defValue) {
        if (obj == null)
            return defValue;
        return toInt(obj.toString(), defValue);
    }

    /**
     * 对象转long
     *
     * @param obj
     * @return 转换异常返回
     */
    public static long getLong(Object obj, long defValue) {
        if (obj == null)
            return defValue;
        return toLong(obj.toString(), defValue);
    }

    /**
     * 对象转long
     *
     * @param obj
     * @return 转换异常返回
     */
    public static long toLong(String obj, long defValue) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return defValue;
    }


    /**
     * 判断参数字符串是否为电话格式
     *
     * @param phoneStr
     * @return
     */
    public static boolean isPhoneNumber(String phoneStr) {
        if (TextUtils.isEmpty(phoneStr)) {
            return false;
        }
        if (phoneStr.length() == 11) {
            for (int i = 0; i < 11; i++) {
                boolean b = PhoneNumberUtils.isISODigit(phoneStr.charAt(i));
                if (!b) {
                    return false;
                }
            }
            //定义电话格式的正则表达式
            String regex = "[1][3456789]\\d{9}";
            return phoneStr.matches(regex);
        } else {
            return false;
        }
    }

    public static boolean isPasswordCorrect(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        if (password.length() > 5 && password.length() < 21) {
            //定义电话格式的正则表达式
            String regex = "^(?![\\d]+$)(?![a-zA-Z]+$)(?![^\\da-zA-Z]+$).{6,20}$";
            return password.matches(regex);
        } else {
            return false;
        }
    }

    // 判断email格式是否正确
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    // 判断是否全是数字
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    // 判断时间格式是否正确 格式 yyyy-mm 或 yyyy-m
    public static boolean isTime(String mobiles) {
        Pattern p = Pattern.compile("^\\d{4}-\\d{1,2}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 随机字符串用做临时图片名字
     *
     * @return
     */
    public static String Number() {
        String str = "";
        for (int i = 1; i <= 9; i++) {
            int ma = (int) (Math.random() * 25 + 65);
            char ca = (char) ma;
            str += ca + "";
        }
        str = str + ".jpg";
        return str;
    }

    /**
     * 18位或者15位身份证验证 18位的最后一位可以是字母x
     *
     * @param text
     * @return
     */
    public static boolean personIdValidation(String text) {
        String regx = "[0-9]{17}x";
        String reg1 = "[0-9]{15}";
        String regex = "[0-9]{18}";
        boolean flag = text.matches(regx) || text.matches(reg1) || text.matches(regex);
        return flag;
    }

    /**
     * ArrayList<String> 转换为 1，2，3
     *
     * @param list
     * @return
     */
    public static String ListToStringOne(List<String> list) {
        String str = "";
        if (list == null || list.size() == 0) {
            return str;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            str = str + list.get(i) + ",";
        }
        return str.substring(0, str.length() - 1);
    }

    /**
     * @return
     */
    public static String getString(String str) {
        return str == null ? "" : str;
    }

    /**
     * @return
     */
    public static String getString(String str, String showText) {
        return str == null ? showText : str;
    }

    /**
     * 获取String 中的数组
     *
     * @param context
     * @param resid
     * @return
     */
    public static String[] getStringArray(Context context, int resid) {
        return context.getResources().getStringArray(resid);
    }

    /**
     * 获取换行字符串
     *
     * @return
     */
    public static String getNString(String str) {
        str = getString(str);
        if (str.isEmpty()) {
            return str;
        }
        return "\n" + str;
    }

    /**
     * 获取换行字符串
     *
     * @return
     */
    public static String getTString(String str) {
        str = getString(str);
        if (str.isEmpty()) {
            return str;
        }
        return "\t\t" + str;
    }

    /**
     * list转化为String
     *
     * @param stringList
     * @return
     */
    public static String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            } else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }

    /***
     * String 转化为List<String>
     *
     * @param string
     * @return
     */
    public static List<String> StringToList(String string) {
        List<String> list = new ArrayList<String>();
        String d[] = string.split(",");
        for (int i = 0; i < d.length; i++) {
            list.add(d[i]);
        }
        return list;
    }

    /***
     * 时间弹出框，选择时间为整点的时候，补0 的方法
     *
     * @param x
     * @return
     */
    public static String format(int x) {
        String s = "" + x;
        if (s.length() == 1)
            s = "0" + s;
        return s;
    }

    /**
     * 复制内容到剪切板
     *
     * @param copyStr
     * @return
     */
    public static boolean copy(Context context,String copyStr) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
