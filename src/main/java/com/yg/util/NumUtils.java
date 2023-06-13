package com.yg.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumUtils.class);
	private static Pattern pattern1 = Pattern.compile("^\\d+$|-\\d+$");
	private static Pattern pattern2 = Pattern.compile("\\d+\\.\\d+$|-\\d+\\.\\d+$");
	private static Pattern pattern3 = Pattern.compile("^((-?\\d+.?\\d*)[Ee]{1}([+-]?\\d+))$");

	private static Pattern patternDate = Pattern.compile("^[12]\\d{3}(0\\d|1[0-2])([0-2]\\d|3[01])$");
	private static Pattern patternKxjs = Pattern.compile("^((-?\\d+.?\\d*)[Ee]{1}([+-]?\\d+))$");
	private static final Pattern PATTERN_IDCARD = Pattern.compile("^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");

	private NumUtils() {
	}

	public static boolean isDate(String str) {
		Matcher isNum1 = patternDate.matcher(str);
		return isNum1.matches();
	}

	public static boolean isNumeric(String str) {
		Matcher isNum1 = pattern1.matcher(str);
		Matcher isNum2 = pattern2.matcher(str);
		Matcher isNum3 = pattern3.matcher(str);
		boolean isNum4 = isKxjsf(str);
		return isNum1.matches() || isNum2.matches() || isNum3.matches() || isNum4;
	}

	public static String formatDecimal(String value) {
		// 判断是否为空
		if (StringUtils.isBlank(value))
			return "";
		try {
			return new BigDecimal(value, new MathContext(15))
					.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
		}catch (Exception e){
			return value;
		}
	}

	public static String formatKxjsf(String value) {
		BigDecimal bd = new BigDecimal(value);
		String res = bd.toPlainString();
		if (res.endsWith("0")) {
			res = res.substring(0, res.length() - 1);
		}
		return res;
	}

	public static String formatKxjsf2(String value) {
		BigDecimal bd = new BigDecimal(value);
		String res = bd.toPlainString();
		return res;
	}

	public static boolean isKxjsf(String value) {
		Matcher isNum = patternKxjs.matcher(value);
		return isNum.matches();
	}

	public static boolean lengthLeft4(String value) {
		if (value.contains(".") && !value.contains("E")) {
			if (value.endsWith(".0")) {
				return false;
			}
			String[] a = value.split("\\.");
			return a[1].length() <= 4;
		} else if (value.contains("E") && value.contains(".")) {
			return value.contains("-4") || value.contains("-3") || value.contains("-2") || value.contains("-1");
		} else {
			return false;
		}
	}

	public static boolean isIdcard(String str) {
		Matcher matcher = PATTERN_IDCARD.matcher(str);
		return matcher.matches();
	}

	// 特殊数字校验正则
	static final String regMoneyWithPercent = "(?:-?)(?:\\d{1,3}(?:[\\s,]\\d{3})+|\\d+)(?:\\.\\d{1,15})?(?:\\%?)";

	/***
	 * 将需要保存的单元格值
	 * 千分符、百分号替换成正常的数字
	 * @return
	 */
	public static String cellMoneyFormat(String val) {
		if (StringUtils.isBlank(val)) {
			return val;
		}
		String newVal = val;
		if (val.matches(regMoneyWithPercent)) {
            newVal = newVal.replace(",", "");
			if (newVal.endsWith("%")) {
                newVal = newVal.replace("%", "");
                newVal = String.valueOf(Double.valueOf(newVal) / 100);
			}
		}
		return newVal;
	}

	/***
	 * 将需要保存的单元格值
	 * 千元符 替换成正常的数字
	 * @return
	 */
	public static String cellThousandFormat(String val) {
		if (StringUtils.isBlank(val)) {
			return val;
		}
		String newVal = val;
		if (val.matches(regMoneyWithPercent)) {
			newVal = newVal.replace(",", "");
		}
		return newVal;
	}
}
