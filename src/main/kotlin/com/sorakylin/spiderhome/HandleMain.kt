package com.sorakylin.spiderhome

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy
import com.sorakylin.spiderhome.context.DLsiteSpiderContext
import com.sorakylin.spiderhome.context.FanzaSpiderContext
import com.sorakylin.spiderhome.context.GetchuSpiderContext
import com.sorakylin.spiderhome.context.VndbSpiderContext
import com.sorakylin.spiderhome.model.GameCountExcelModel
import com.sorakylin.spiderhome.model.OriginConst
import com.sorakylin.spiderhome.model.ReleaseGame
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.LocalDate
import java.util.*


val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 7890))

/**
 * 爬vndb的数据
 * 需要反复多运行几次，幂等。
 */
object VndbHandle {

    @JvmStatic
    fun main(args: Array<String>) {

        //开场删掉那些未成功的数据
        val removeCount = GameRepository.removeNotCompleteData(OriginConst.vndb)
        println("删掉了垃圾数据 $removeCount 条")

        //设置已存在的数据
        val existGames = GameRepository.findExistedCompleteGames(OriginConst.vndb)

        val context = VndbSpiderContext()
            .useProxy(Proxy.NO_PROXY)
            .useProxy(proxy)
            .appendFilteredGame(existGames)

        //开爬
        for (ctx in context) GameRepository.insert(ctx.handle())
    }
}

object GetchuHandle {

    @JvmStatic
    fun main(args: Array<String>) {
        val existGames = GameRepository.findExistedCompleteGames(OriginConst.getchu)

        val context = GetchuSpiderContext()
            .useProxy(Proxy.NO_PROXY)
            .useProxy(proxy)
            .appendFilteredGame(existGames)

        for (ctx in context) GameRepository.insert(ctx.handle())
    }
}

object DLHandle {

    @JvmStatic
    fun main(args: Array<String>) {
        val existGames = GameRepository.findExistedCompleteGames(OriginConst.dl)

        val context = DLsiteSpiderContext()
            .useProxy(proxy)
            .appendFilteredGame(existGames)

        for (ctx in context) GameRepository.insert(ctx.handle())
    }
}

object FanzaHandle {

    @JvmStatic
    fun main(args: Array<String>) {
        val existGames = GameRepository.findExistedCompleteGames(OriginConst.fanza, OriginConst.fanza3d)

        //fanza的先运行一遍3D （OriginConst.fanza3d）， 再运行普通 （OriginConst.fanza）
        val context = FanzaSpiderContext(OriginConst.fanza)
            .useProxy(proxy)
            .appendFilteredGame(existGames)

        for (ctx in context) GameRepository.insert(ctx.handle())
    }
}

/**
 * 生成vndb 的统计Excel的
 */
object VndbExcelGenerator {

    //输出的Excel地址
    private val excelFile = File("E:\\JAVA\\sorakylin_workspace\\spider-home\\vndb2021.xlsx")

    private val dashboardHeader = listOf(
        listOf("vndb符合条件的游戏数(2016-2021日本发售并拥有中文/英文补丁)"),
        listOf("其中拥有中文补丁的数量"),
        listOf("其中拥有英文补丁的数量")
    )

    private val zhGamesHeader = listOf(
        listOf("游戏名"),
        listOf("发售时间"),
        listOf("最新中文补丁时间，3000-01-01为待定（TBA）"),
        listOf("链接")
    )

    private val enGamesHeader = listOf(
        listOf("游戏名"),
        listOf("发售时间"),
        listOf("最新英文补丁时间，3000-01-01为待定（TBA）"),
        listOf("链接")
    )


    @JvmStatic
    fun main(args: Array<String>) {
        val excelWriter = EasyExcel.write(excelFile)
            .registerWriteHandler(SimpleColumnWidthStyleStrategy(50))
            .build()

        try {
            val dashboardSheet = EasyExcel.writerSheet("统计").build()
            val zhGamesSheet = EasyExcel.writerSheet("有中文补丁的游戏").build()
            val enGamesSheet = EasyExcel.writerSheet("有英文补丁的游戏").build()

            dashboardSheet.head = dashboardHeader
            zhGamesSheet.head = zhGamesHeader
            enGamesSheet.head = enGamesHeader

            val games = GameRepository.findExistedCompleteGames(OriginConst.vndb)
            val zhGames = LinkedList<ReleaseGame>();
            val enGames = LinkedList<ReleaseGame>();

            games.forEach {
                if (it.patchZhDate != null) zhGames.add(it)
                if (it.patchEnDate != null) enGames.add(it)
            }

            // 总览sheet
            val yearGroups: MutableList<List<Any>> = listOf(2016, 2017, 2018, 2019, 2020, 2021)
                .map { year ->
                    listOf(
                        "年份($year):  ${games.filter { it.releaseDate?.year == year }.size}",
                        zhGames.filter { it.releaseDate?.year == year }.size,
                        enGames.filter { it.releaseDate?.year == year }.size
                    )
                }
                .toMutableList()

            yearGroups.add(0, listOf("年份(所有，2016-2021):  ${games.size}", zhGames.size, enGames.size));
            excelWriter.write(yearGroups, dashboardSheet)

            // zh sheet
            val zhGameColumns = zhGames.map {
                listOf(
                    it.name,
                    Utils.formatLocalDate(it.releaseDate!!),
                    Utils.formatLocalDate(it.patchZhDate!!),
                    it.itemLink
                )
            }.toList()
            excelWriter.write(zhGameColumns, zhGamesSheet)

            // en sheet
            val enGameColumns = enGames.map {
                listOf(
                    it.name,
                    Utils.formatLocalDate(it.releaseDate!!),
                    Utils.formatLocalDate(it.patchEnDate!!),
                    it.itemLink
                )
            }.toList()
            excelWriter.write(enGameColumns, enGamesSheet)
        } finally {
            excelWriter.finish()
        }
    }


}

/**
 * 统计Galgame2021年信息的， 导出Excel
 * 运行前设置好 vm options：
 * --illegal-access=warn --add-opens java.base/java.lang=ALL-UNNAMED
 */
object GameCountExcelGenerator {

    private val excelFile = File("E:\\JAVA\\sorakylin_workspace\\spider-home\\galgame2021_count.xlsx")

    @JvmStatic
    fun main(args: Array<String>) {

        val games = GameRepository.findExistedCompleteGames(OriginConst.dl, OriginConst.getchu, OriginConst.fanza)
            .asSequence()
            .filter { it.price != 0 }
            .filter { it.name!!.contains("CeVIO AI").not() }
            .filter { it.name!!.contains("【まとめ買い】").not() }
            .onEach { it.name = it.name?.trim() }
            .sortedByDescending { it.releaseDate }
            .map {
                GameCountExcelModel(
                    name = it.name,
                    price = it.price,
                    date = it.releaseDate,
                    origin = it.origin,
                    link = it.itemLink,
                )
            }
            .toList()

        //未知的集合、参与计算的集合、贵的统计（全价作）、便宜的统计（非全价作）
        val unknownList = mutableListOf<GameCountExcelModel>();
        val calcList = mutableListOf<GameCountExcelModel>();
        var dearNum = 0;
        var cheapNum = 0;

        //根据名字的前六个字分组
        val nameGroup = games.groupBy { if (it.name!!.length < 7) it.name!! else it.name!!.substring(0, 6) }

        nameGroup.forEach { (k, v) ->
            val minPrice = v.minOf { it.price!! }
            val maxPrice = v.maxOf { it.price!! }

            if (minPrice < 8000 && maxPrice >= 8000) {
                unknownList.addAll(v)
            } else {
                if (maxPrice < 8000) cheapNum++ else dearNum++
                calcList.addAll(v)
                calcList.add(GameCountExcelModel("▲▲▲在这之间的视为一个游戏▲▲▲", 0, LocalDate.MIN, "", "▲▲▲▲▲▲"))
            }
        }
        val dashboardHeader = listOf(listOf(">= 8000"), listOf("< 8000"))

        val excelWriter = EasyExcel.write(excelFile)
            .build()

        try {
            // 导出统计
            val dashboardSheet = EasyExcel.writerSheet("统计").build()
            dashboardSheet.head = dashboardHeader
            excelWriter.write(listOf(listOf(dearNum, cheapNum)), dashboardSheet)

            //导出未知列表
            val unknownSheet = EasyExcel.writerSheet("未计算数据").head(GameCountExcelModel::class.java).build()
            excelWriter.write(unknownList, unknownSheet)

            //导出经过计算的列表
            val calcSheet = EasyExcel.writerSheet("参与计算的数据").head(GameCountExcelModel::class.java).build()
            excelWriter.write(calcList, calcSheet)

            // 导出原始数据
            games.groupBy { it.origin }.forEach { (k, v) ->
                val writeSheet = EasyExcel.writerSheet("原始数据-$k").head(GameCountExcelModel::class.java).build()
                excelWriter.write(v, writeSheet);
            }
        } finally {
            excelWriter.finish()
        }
    }
}
