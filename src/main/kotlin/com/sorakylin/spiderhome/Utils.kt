package com.sorakylin.spiderhome

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object Utils {
    // 需要排除监控的ip范围
    private val ipRange = arrayOf(
        intArrayOf(607649792, 608174079),
        intArrayOf(1038614528, 1039007743),
        intArrayOf(1783627776, 1784676351),
        intArrayOf(2035023872, 2035154943),
        intArrayOf(2078801920, 2079064063),
        intArrayOf(-1950089216, -1948778497),
        intArrayOf(-1425539072, -1425014785),
        intArrayOf(-1236271104, -1235419137),
        intArrayOf(-770113536, -768606209),
        intArrayOf(-569376768, -564133889)
    )

    /**
     * 获取随机IP
     */
    fun getRandomIp(): String {
        val rdint = Random()
        val index = rdint.nextInt(10)
        return num2ip(ipRange[index][0] + Random().nextInt(ipRange[index][1] - ipRange[index][0]))
    }

    /*
    * 将十进制转换成IP地址
    */
    fun num2ip(ip: Int): String {
        val b = IntArray(4)

        b[0] = (ip shr 24 and 0xff)
        b[1] = (ip shr 16 and 0xff)
        b[2] = (ip shr 8 and 0xff)
        b[3] = (ip and 0xff)

        return "${b[0]}.${b[1]}.${b[2]}.${b[3]}"
    }

    /**
     * 填充时间字符串
     */
    fun fillDateStr(str: String): String {
        return if (str == "TBA") "3000-01-01"
        else if (str.length == 4) "$str-01-01"
        else if (str.length == 7) "$str-01"
        else str
    }

    //local data to string
    fun formatLocalDate(localDate: LocalDate) = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
}