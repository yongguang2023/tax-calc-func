package com.yg.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class LocationUtil {

    private static Logger logger = LoggerFactory.getLogger(LocationUtil.class);

    /**
     * 数字坐标转化成字符位置，如 {0, 0} -> A1
     *
     * @param row 行号
     * @param col 列号
     * @return
     */
    public static String coordinateToPosition(int row, int col) {
        if (row < 0) {
            logger.warn("行号为负数，将自动置0！");
            row = 0;
        }

        if (col < 0) {
            logger.warn("列号为负数，将自动置0！");
            col = 0;
        }

        String rowstr = row + 1 + "";
        String position = colIndexToStr(col) + rowstr;
        return position;
    }

    /**
     * 字符位置转化成数字坐标，如 A1 -> {0, 0}
     *
     * @param position 字符串位置
     * @return
     */
    public static int[] positiontoCoordinate(String position) {
        position = position.toUpperCase();
        boolean isMatch = Pattern.matches("[A-Z]+\\d+", position);
        if (isMatch) {
            String str = position.replaceAll("\\d+", "");
            String num = position.replaceAll("\\D+", "");

            if (Integer.parseInt(num) <= 0) {
                logger.error("数字必须大于0！{}", num);
                return null;
            }

            int col = colStrToNum(str);
            int row = Integer.parseInt(num) - 1;

            int[] coordinateArr = new int[2];
            coordinateArr[0] = row;
            coordinateArr[1] = col;
            return coordinateArr;
        } else {
            logger.error("位置格式错误！{}", position);
            return null;
        }
    }

    /**
     * 字符列标转化成数值，如A -> 0
     *
     * @param colStr
     * @return
     */
    public static int colStrToNum(String colStr) {
        colStr = colStr.toUpperCase();
        int length = colStr.length();
        int num = 0;
        int result = 0;
        for (int i = 0; i < length; i++) {
            char ch = colStr.charAt(length - i - 1);
            num = ch - 'A' + 1;
            num *= Math.pow(26, i);
            result += num;
        }
        return result - 1;
    }

    /**
     * 数值列标转化成字符，如 0 -> A
     *
     * @param columnIndex
     * @return
     */
    public static String colIndexToStr(int columnIndex) {
        if (columnIndex < 0) {
            return null;
        }
        String columnStr = "";
        do {
            if (columnStr.length() > 0) {
                columnIndex--;
            }
            columnStr = ((char) (columnIndex % 26 + (int) 'A')) + columnStr;
            columnIndex = (columnIndex - columnIndex % 26) / 26;
        } while (columnIndex > 0);
        return columnStr;
    }
}
