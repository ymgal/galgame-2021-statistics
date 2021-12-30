package com.sorakylin.spiderhome.context

import com.sorakylin.spiderhome.model.OriginConst
import com.sorakylin.spiderhome.model.ReleaseGame
import com.sorakylin.spiderhome.Utils
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DLsiteSpiderContext : AbstractSpiderContext() {

    private val template =
        "https://www.dlsite.com/pro/fsr/=/language/jp/sex_category%5B0%5D/male/ana_flg/off/work_category%5B0%5D/pc/order%5B0%5D/release_d/genre_and_or/or/options_and_or/or/options%5B0%5D/JPN/options%5B1%5D/CHI/options%5B2%5D/CHI_HANS/options%5B3%5D/CHI_HANT/options%5B4%5D/NM/per_page/100/show_type/1/lang_options%5B0%5D/%E6%97%A5%E6%96%87/lang_options%5B1%5D/%E4%B8%AD%E6%96%87/lang_options%5B2%5D/%E4%B8%AD%E6%96%87%28%E7%AE%80%E4%BD%93%E5%AD%97%29/lang_options%5B3%5D/%E4%B8%AD%E6%96%87%28%E7%B9%81%E4%BD%93%E5%AD%97%29/lang_options%5B4%5D/%E4%B8%8D%E9%99%90%E8%AF%AD%E7%A7%8D/page/"

    override fun doGetPageUrl(currentPage: Int): String {
        return template + currentPage;
    }

    override fun parseSearchResultHtml(html: String): List<ReleaseGame> {
        val items = Jsoup.parse(html)
            .getElementsByClass("n_worklist").first()
            .getElementsByTag("tr")

        val resultList = mutableListOf<ReleaseGame>();


        for (item in items) {
            val game = ReleaseGame()

            //过滤一道tag
            val is3d = item.getElementsByClass("search_tag").first()
                ?.children()
                ?.any { "3D作品" == it.text() } ?: false

            if (is3d) continue

            //名字和商品地址
            val title = item.getElementsByClass("work_name").first()
                .getElementsByTag("a")
            game.name = title.text().trim()
            game.itemLink = title.attr("href")

            // 如果有strike则拿strike(打折，这个是原价)。 否则去拿 work_price
            val priceWrap = item.getElementsByClass("work_price_wrap").first()

            var priceTag = priceWrap.getElementsByClass("strike")
            if (priceTag.isEmpty()) priceTag = priceWrap.getElementsByClass("work_price")

            val price = priceTag.first().text()
                .replace("<i>", "")
                .replace("</i>", "")
                .replace("JPY", "")
                .replace("円", "")
                .replace(",", "")
                .toInt()
            game.price = price

            // 拿发售日期
            val saleDate = item.getElementsByClass("sales_date").text().removePrefix("販売日: ")
            val date = LocalDate.parse(saleDate, DateTimeFormatter.ofPattern("yyyy年MM月dd日"))


            if (getFilteredGame().any { it.itemLink == game.itemLink }) continue

            if (date.year < 2021) {
                println("————————执行完毕————————")
                setDone(true)
                return resultList
            }

            game.releaseDate = date
            game.complete = true
            game.origin = OriginConst.dl

            println("【解析完毕】游戏：${game.name}, 地址：${game.itemLink}, 发售日期：${Utils.formatLocalDate(date)}")
            resultList.add(game);
        }

        return resultList
    }


}