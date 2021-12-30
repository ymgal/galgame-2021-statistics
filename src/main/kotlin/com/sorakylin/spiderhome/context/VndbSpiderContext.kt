package com.sorakylin.spiderhome.context

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.sorakylin.spiderhome.model.OriginConst
import com.sorakylin.spiderhome.model.ReleaseGame
import com.sorakylin.spiderhome.Utils
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 2016-2021 的有中文补丁/英文补丁的游戏
class VndbSpiderContext : AbstractSpiderContext() {

    // vndb 的请求路径模板， %s 是占位符，用来替换页数
    private val vndbUrlTemplate = "https://vndb.org/v?f=03122gen2gzh3gja4owin&p=%s&s=32w"

    override fun doGetPageUrl(currentPage: Int): String {
        return vndbUrlTemplate.format(currentPage);
    }

    //解析Html + 进入详情页面获取具体的ReleaseDate 信息
    override fun parseSearchResultHtml(html: String): List<ReleaseGame> {
        val trList: Elements = Jsoup.parse(html)
            .getElementById("maincontent")
            .getElementsByClass("stripe")
            .first()
            .getElementsByTag("tbody").first().children()

        val resultList = mutableListOf<ReleaseGame>();

        //基本解析
        for (element in trList) {
            val game = ReleaseGame()
            game.origin = OriginConst.vndb

            val title = element.select(".tc_title a")
            game.name = title.attr("title")
            game.itemLink = "https://vndb.org${title.attr("href")}"

            val dateText = element.getElementsByClass("tc_rel").text()
            game.releaseDate = LocalDate.parse(Utils.fillDateStr(dateText), DateTimeFormatter.ISO_LOCAL_DATE)

            // check year
            if (game.releaseDate?.year == 2015) {
                setDone(true)
                continue
            }

            // check existed
            if (getFilteredGame().any { it.itemLink == game.itemLink }) continue

            resultList.add(game)

            println("【基本解析】游戏：${game.name}, 地址：${game.itemLink}")
        }

        //补充解析
        resultList.forEachIndexed { index, game ->
            val (success, zhPatch, enPatch) = parseVndbPatchData(game.itemLink!!)
            game.complete = success
            game.patchZhDate = zhPatch
            game.patchEnDate = enPatch

            println("【补充解析-${index + 1}】游戏地址：${game.itemLink}, success: $success, zhPatch: $zhPatch, enPatch: $enPatch")
        }

        return resultList
    }

    /**
     * 解析VN详情页面的补丁日期， 拿中文&英文的补丁日期 （最新的）
     * @return success, zhPatch, enPatch
     */
    private fun parseVndbPatchData(vnLink: String): Triple<Boolean, LocalDate?, LocalDate?> {
        val (_, _, result) = vnLink.httpGet().apply {
            //装成透明代理
            val randomIp = Utils.getRandomIp()
            this.header("x-forwarded-for", randomIp).header("x-real-ip", randomIp)
        }.responseString()

        //vn item detail.
        val html = when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                println(ex)
                return Triple(false, null, null)
            }
            is Result.Success -> result.get()
        }

        var zhPatchDate: LocalDate? = null
        var enPatchDate: LocalDate? = null

        // vndb 的补丁信息全部是用 <details></details> 包装的
        val details = Jsoup.parse(html).getElementsByTag("details")

        for (detail in details) {
            val rememberId = detail.attr("data-remember-id")

            //只处理en和zh
            if (rememberId != "vnlang-en" && rememberId != "vnlang-zh") continue

            //时间td
            val patchDate = detail.select(".releases tbody td.tc1")
                .mapNotNull { Utils.fillDateStr(it.text()) }
                .map { LocalDate.parse(it.trim(), DateTimeFormatter.ISO_LOCAL_DATE) }
                .maxOfOrNull { it }


            if ("vnlang-zh" == rememberId) zhPatchDate = patchDate
            if ("vnlang-en" == rememberId) enPatchDate = patchDate
        }

        return Triple(true, zhPatchDate, enPatchDate)
    }

}
