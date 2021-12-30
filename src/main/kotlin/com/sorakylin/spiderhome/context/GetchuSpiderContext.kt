package com.sorakylin.spiderhome.context

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.sorakylin.spiderhome.model.OriginConst
import com.sorakylin.spiderhome.model.ReleaseGame
import com.sorakylin.spiderhome.Utils
import org.jsoup.Jsoup
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//https://dl.getchu.com/search/search_list.php?
// search_category_id=101&search_keyword=&btnWordSearch=%B8%A1%BA%F7&action=search&set_category_flag=1
//pageID=1
class GetchuSpiderContext : AbstractSpiderContext(Charset.forName("EUC-JP")) {

    private val template = "https://dl.getchu.com/search/search_list.php?" +
            "search_category_id=101&search_keyword=&btnWordSearch=%B8%A1%BA%F7&action=search&set_category_flag=1"

    override fun doGetPageUrl(currentPage: Int): String {
        return "${template}&pageID=${currentPage}"
    }

    override fun parseSearchResultHtml(html: String): List<ReleaseGame> {
        val items = Jsoup.parse(html)
            .getElementsByClass("m_main_c").first()
            .getElementsByClass("waku_main").first()
            .getElementsByTag("td")
            .filter { it.hasAttr("valign") && it.hasAttr("width") && "430" == it.attr("width") }

        val resultList = mutableListOf<ReleaseGame>();

        for (item in items) {
            val game = ReleaseGame()

            val title = item.getElementsByTag("a").first()
            game.name = title.text()
            game.itemLink = title.attr("href")

            var price = item.getElementsByClass("redboldtext").first().text()
            price = price.replace(Regex("[,.円]"), "")
            game.price = price.toInt()

            // check existed
            if (getFilteredGame().any { it.itemLink == game.itemLink }) continue

            //补充字段设置，判定时间是不是2021年，如果不是. 就代表这个数据和之后的都不需要了，结束
            val date = parseReleaseDate(game.itemLink!!);
            if (date.year < 2021) {
                println("————————执行完毕，已不存在2021年的游戏————————")
                setDone(true)
                return resultList
            }

            game.releaseDate = date
            game.complete = true
            game.origin = OriginConst.getchu

            println("【解析完毕】游戏：${game.name}, 地址：${game.itemLink}, 发售日期：${Utils.formatLocalDate(date)}")
            resultList.add(game);
        }

        return resultList
    }

    private fun parseReleaseDate(link: String): LocalDate {
        val (_, _, result) = link.httpGet().apply {
            val randomIp = Utils.getRandomIp()
            this.header("x-forwarded-for", randomIp).header("x-real-ip", randomIp)
        }.responseString(charset)

        val html = when (result) {
            is Result.Failure -> throw result.getException()
            is Result.Success -> result.get()
        }

        val value = Regex("<td>\\d{4}年\\d{2}月\\d{2}日</td>")
            .find(html)!!.value
            .replace("<td>", "").replace("</td>", "")

        return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
    }

}