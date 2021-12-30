package com.sorakylin.spiderhome.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDate


//实体类
interface ReleaseGame : Entity<ReleaseGame> {
    companion object : Entity.Factory<ReleaseGame>()

    var id: Int?
    var name: String?
    var origin: String?
    var itemLink: String?
    var patchZhDate: LocalDate?
    var patchEnDate: LocalDate?
    var releaseDate: LocalDate?
    var price: Int?
    var complete: Boolean
}

//表定义
object ReleaseGameTB : Table<ReleaseGame>("release_game_2021") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("game_name").bindTo { it.name }
    val origin = varchar("item_origin").bindTo { it.origin }
    val itemLink = varchar("item_link").bindTo { it.itemLink }
    val patchZhDate = date("patch_zh_date").bindTo { it.patchZhDate }
    val patchEnDate = date("patch_en_date").bindTo { it.patchEnDate }
    val releaseDate = date("release_date").bindTo { it.releaseDate }
    val price = int("game_price").bindTo { it.price }
    val complete = boolean("handle_complete").bindTo { it.complete }
}


