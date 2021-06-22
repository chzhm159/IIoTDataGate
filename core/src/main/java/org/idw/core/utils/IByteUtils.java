package org.idw.core.utils;

public class IByteUtils {

    public static Short asciiString2Short(String vs){
        return Short.parseShort(vs);
    }
    /// <summary>
    /// 16进制字符串转换为16位无符号整数.例如:
    /// 0BB8 => 3000
    /// </summary>
    /// <param name="hexstr"></param>
    /// <returns></returns>
    public static Short HexString2UShort(String hexstr) {
        return Short.decode(hexstr);
    }

    /// <summary>
    /// 16进制字符串转换为32位无符号整数.例如:
    /// 0000A000 => 40960
    /// </summary>
    /// <param name="hexstr"></param>
    /// <returns></returns>
    public static int HexString2UInt32(String hexstr)
    {
        return Integer.decode(hexstr);
    }

    /// <summary>
    /// 16进制字符串转换为2进制字符串,转换结果长度为16,高位以0补齐. 例如:
    /// 000A => 0000000000001010
    /// </summary>
    /// <param name="hexstr"></param>
    /// <returns></returns>
//    public static string HexString2BinaryStr(string hexstr)
//    {
//        return Convert.ToString(Convert.ToUInt16(hexstr, 16),2).PadLeft(16, '0');
//    }
    /// <summary>
    /// 16进制字符串转换为Byte 数组(未测试)
    /// </summary>
    /// <param name="hex"></param>
    /// <returns></returns>
//    public static byte[] HexString2ByteArray(String hex)
//    {
//        int NumberChars = hex.Length;
//        byte[] bytes = new byte[NumberChars / 2];
//        for (int i = 0; i < NumberChars; i += 2)
//            bytes[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
//        return bytes;
//    }
    /// <summary>
    /// 16进制字符串转换为ASCII码字符串.例如:
    /// 6130303032007E21402324255E262A2D5F3C3E3F2F00 => a0002~!@#$%^...
    ///
    /// </summary>
    /// <param name="hex"></param>
    /// <returns></returns>
//    public static string HexString2ASCII(string hex)
//    {
//        string ascii = string.Empty;
//
//        for (int i = 0; i < hex.Length; i += 2)
//        {
//            int num = Convert.ToInt32(hex.Substring(i, 2),16);
//            // 只处理 ASCII 中的可显示字符,控制字符忽略
//            if (num > 31 && num != 127) {
//                ascii += (char)num;
//            }
//        }
//        return ascii;
//    }
}
