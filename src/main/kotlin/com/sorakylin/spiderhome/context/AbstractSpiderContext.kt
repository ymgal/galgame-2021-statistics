package com.sorakylin.spiderhome.context

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.sorakylin.spiderhome.model.ReleaseGame
import java.net.Proxy
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 爬虫全都是相同的流程
 * 提取一个 Template method
 */
abstract class AbstractSpiderContext constructor(val charset: Charset = Charset.defaultCharset()) :
    AbstractIterator<AbstractSpiderContext>() {

    //当前页
    private var currentPage = 1

    //是否已处理完毕
    private var doneFlag = AtomicBoolean(false)

    //已存在的游戏
    private val filteredGame: MutableList<ReleaseGame> = mutableListOf()

    private var proxyPool: MutableList<Proxy> = mutableListOf();

    fun useProxy(proxy: Proxy): AbstractSpiderContext {
        this.proxyPool.add(proxy)
        return this
    }

    fun appendFilteredGame(games: List<ReleaseGame>): AbstractSpiderContext {
        val links = filteredGame.mapNotNull { it.itemLink }.toSet()
        val appendList = games.filter { !links.contains(it.itemLink) }.toList()
        filteredGame.addAll(appendList)
        return this
    }

    fun getFilteredGame(): List<ReleaseGame> {
        return Collections.unmodifiableList(filteredGame)
    }

    //设置结束标志，由子类在合适的时机调用
    protected fun setDone(done: Boolean): Unit = doneFlag.set(done);

    //页面获取
    abstract fun doGetPageUrl(currentPage: Int): String

    //页面解析
    abstract fun parseSearchResultHtml(html: String): List<ReleaseGame>

    //主入口
    fun handle(): List<ReleaseGame> {
        val url = doGetPageUrl(currentPage)

        FuelManager.instance.proxy = proxyPool.randomOrNull() ?: Proxy.NO_PROXY

        val (_, _, result) = url.httpGet().responseString(charset)

        val html = when (result) {
            is Result.Failure -> throw result.getException()
            is Result.Success -> result.get()
        }

        //这一页的所有游戏, 字段完备。 size > 0
        val games: List<ReleaseGame> = parseSearchResultHtml(html)

        println("【context】 本次获取量: ${games.size}, 当前页: ${currentPage}")

        //finally
        currentPage++

        return games
    }

    final override fun computeNext(): Unit = if (doneFlag.get()) done() else setNext(this);
}