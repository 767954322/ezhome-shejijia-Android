package com.autodesk.shejijia.shared.components.common.utility;

/**
 * @author he.liu .
 * @version v1.0 .
 * @date 2016-6-8 .
 * @file RegexUtil.java .
 * @brief 项目中的正则工具类 .
 */
public class RegexUtil {
    private RegexUtil() {
    }

    /**
     * 小区地址校验：无特殊字符，2-32位
     */
    public static final String ADDRESS_REGEX = "^[\\u4e00-\\u9fa5_a-zA-Z0-9_]{2,32}$";

    public static final String ADDRESS_NUM = "^[1-9]+(.[0-9]{2})?$";

    /**
     * 手机号码校验
     */
    public static final String PHONE_REGEX = "^1[3|4|5|7|8]\\d{9}$";

    /**
     * 银行卡号验证
     */
    public static final String PHONE_BLANK = "^(\\d{16}|\\d{22})$";

    /**
     * 量房面积校验： 整数四位，小数2位
     */
    public static final String AREA_REGEX = "^[0-9]{1,4}?(\\.[0-9]{0,2})?$";

    /**
     * 量房面积校验： 整数四位,不能全部为0
     */

    public static final String AREA_REGEX_ZERO = "^(?!0{2,})(?:\\d{1,4}(\\.\\d+)?|10000)$";

    /**
     * 量房费验证：整数位可以有0
     */
    public static final String MEASURE_FEE_REGEX = "^[0-9]{0,4}?(\\.[0-9]{0,2})?$";

    /**
     * 验证姓名
     */
    public static final String NAME_REGEX = "^([\u4e00-\u9fa5]{2,10})$";
    public static final String NAME_REGEX1 = "^[A-Za-z\\u4e00-\\u9fa5]{2,12}+$";

    /**
     * 验证邮编
     */
    public static final String POST_NUMBER_REGEX = "^[0-9]{6}$";

    /**
     * 验证昵称
     */
    public static final String NICK_NAME_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\-]{2,10}$";

    /**
     * 验证邮箱
     */
    public static final String EMAIL_REGEX = "^[a-z0-9]+([._\\\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$";

    /**
     * 验证正整数
     */
    public static final String POSITIVE_INTEGER_REGEX = "^[1-9]\\d*|0";

    /**
     * 身份号码
     */
    public static final String ID_CARD_REGEX = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X|x)$";


}
