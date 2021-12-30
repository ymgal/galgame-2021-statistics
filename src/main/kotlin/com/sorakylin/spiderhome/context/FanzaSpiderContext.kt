package com.sorakylin.spiderhome.context

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.sorakylin.spiderhome.model.OriginConst
import com.sorakylin.spiderhome.model.ReleaseGame
import com.sorakylin.spiderhome.Utils
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * https://dlsoft.dmm.co.jp/list/limit=120/sort=date/page=1/
 * 排除：https://dlsoft.dmm.co.jp/list/article=keyword/id=7401/limit=120/sort=date/page=1/
 */
class FanzaSpiderContext(private val origin: String) : AbstractSpiderContext() {

    init {
        if (origin != OriginConst.fanza3d && origin != OriginConst.fanza) {
            throw UnsupportedOperationException()
        }

        FuelManager.instance.baseHeaders = mapOf(
            Headers.COOKIE to "uid=admx7484e3e411x3b5; _clicks=10,14,0,0,35,35,36,0,0,1618620024,xuidx78dd4587e4xfbb,607a2e77%2D7740%2D4ed2%2D8ad3%2D14e7ac1dcb2b,,0,null,null,null; olg_translate_language=zh-CHT; app_uid=ygb2CmHIGKnAvbTfz3D2Ag==; _gcl_au=1.1.1713892224.1640504662; _ga=GA1.1.66678085.1640504663; i3_ab=4f7528b8-4ef8-4fb7-8e39-4e91b2e1abf4; ckcy=1; __utma=125690133.66678085.1640504663.1640504755.1640504755.1; __utmc=125690133; __utmz=125690133.1640504755.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _dga=GA1.3.66678085.1640504663; age_check_done=1; guest_id=VwhBD14OUUALF1gA; cklg=ja; _dga=GA1.4.66678085.1640504663; layout=pc; _dga_gid=GA1.4.1022216352.1640761435; adpf_uid=iTGJsvEYxoCljOvD; LSS_SESID={\"digital\":\"date\"}; dlsoft_check_item_history=eyJjaWQiOiJoaWdoXzAwNzgiLCJzaXRlIjoiYXBjZ2FtZSJ9<<>>eyJjaWQiOiJkaWdpYl8wMDQzIiwic2l0ZSI6ImFwY2dhbWUifQ==<<>>eyJjaWQiOiJwdXJwbGVfMDAwNyIsInNpdGUiOiJhcGNnYW1lIn0=<<>>eyJjaWQiOiJmdXNpb25fMDAwMSIsInNpdGUiOiJhcGNnYW1lIn0=<<>>eyJjaWQiOiJuZXh0XzAzMzQiLCJzaXRlIjoiYXBjZ2FtZSJ9; _gaSessionTime=2021-12-30 15:07:43; _gaReferrer=https://dlsoft.dmm.co.jp/list/article=keyword/id=7401/limit=120/sort=ranking/; _dc_gtm_UA-48257133-2=1; laravel_session=eyJpdiI6ImlzNUJoOUYrTHhUaGkyK3lNYm9McXc9PSIsInZhbHVlIjoiQW84RjVyVVRtWFIwdnBybHhBQmRETFwvNWc5akc1RWNEYUlqS2xSR0ljQlVjS1Z6MEdVNTVlVVNhYjRHV2JhQ2FCSFRJV3J3TXBaYmg3NnpXRnVHc3VRPT0iLCJtYWMiOiJiNTFhNWVjYjE2MzczOGZhOGNhMDBiNTlhYTRlZjZkZTY1NGRiY2ZlZTIxNDY4OWY0MzkyOGRiYWJhOWMzNzkwIn0=; _dd_s=logs=0&expire=1640845381958; _ga_G34HHM5C8N=GS1.1.1640842522.7.1.1640844499.0; _ga_V3D5MK9Z8V=GS1.1.1640842522.6.1.1640844499.0"
        )
    }

    override fun doGetPageUrl(currentPage: Int): String {
        return if (origin == OriginConst.fanza3d) {
            "https://dlsoft.dmm.co.jp/list/article=keyword/id=7401/limit=120/sort=date/page=$currentPage/"
        } else {
            "https://dlsoft.dmm.co.jp/list/limit=30/sort=date/page=$currentPage/"
        }
    }

    override fun parseSearchResultHtml(html: String): List<ReleaseGame> {
        val items = Jsoup.parse(html).getElementById("list").children()

        //3d的拿个一页就行了，第一页就到多年以前了。直接设置结束。
        if (origin == OriginConst.fanza3d) setDone(true);

        val resultList = mutableListOf<ReleaseGame>();

        for (item in items) {
            val game = ReleaseGame()

            val tmb = item.getElementsByClass("tmb").first()

            game.itemLink = tmb.getElementsByTag("a").first().attr("href")
            game.name = tmb.getElementsByClass("txt").first().text()
            game.origin = origin

            //过滤3D的&已存在的
            if (getFilteredGame().any { it.itemLink == game.itemLink }) continue
            //3D的就不往下走了
            if (origin == OriginConst.fanza3d) {
                game.complete = true
                resultList.add(game)
                println("【解析完毕-3D】游戏：${game.name}, 地址：${game.itemLink}")
                continue
            }

            //补充价格和发售日
            val (price, date) = parsePriceAndReleaseDate(game.itemLink!!)

            if (date.year < 2021) {
                println("————————执行完毕，已不存在2021年的游戏————————")
                setDone(true)
                return resultList
            }

            game.price = price
            game.releaseDate = date
            game.complete = true

            println("【解析完毕】游戏：${game.name}, 地址：${game.itemLink}, 价格：$price, 发售日：$date")
            resultList.add(game);
        }

        return resultList
    }

    private fun parsePriceAndReleaseDate(link: String): Pair<Int, LocalDate> {
        val (_, _, result) = link.httpGet().responseString()

        val html = when (result) {
            is Result.Failure -> throw result.getException()
            is Result.Success -> result.get()
        }

        val dom = Jsoup.parse(html)

        var dateText = dom.getElementsByClass("container02").first()
            .getElementsByClass("type-left")
            .first { it.text().trim() == "配信開始日" }
            .nextElementSibling().nextElementSibling()
            .text().trim()

        // 处理这种格式的： 2021/12/24～2022/01/31 23:59
        if (dateText.contains('～')) dateText = dateText.split('～').first()

        val date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        val priceBox = dom.getElementsByClass("normal-display").first()
            .nextElementSibling()

        // 如果是有这个 'red' class字段，则表示没有打折，直接拿。 否则要在往下一层
        val price = if (priceBox.hasClass("red")) {
            priceBox.text().replace(Regex("[,.円]"), "").toInt()
        } else {
            priceBox.children().first().text().replace(Regex("[,.円]"), "").toInt()
        }

        return Pair(price, date)
    }

}