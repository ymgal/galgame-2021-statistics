package com.sorakylin.spiderhome

import com.sorakylin.spiderhome.model.ReleaseGame
import com.sorakylin.spiderhome.model.ReleaseGameTB
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.removeIf
import org.ktorm.entity.sequenceOf

object GameRepository {

    private val database = Database.connect(
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql://192.168.194.132:5432/postgres",
        user = "dev",
        password = "614",
    )

    fun removeNotCompleteData(origin: String): Int {
        return database
            .sequenceOf(ReleaseGameTB)
            .removeIf { (it.complete eq false) and (it.origin eq origin) }
    }

    fun findExistedCompleteGames(vararg origins: String): List<ReleaseGame> {
        if (origins.isEmpty()) return listOf()

        return database.from(ReleaseGameTB)
            .select()
            .where((ReleaseGameTB.origin inList origins.asList()) and ReleaseGameTB.complete eq true)
            .orderBy(ReleaseGameTB.releaseDate.desc(), ReleaseGameTB.id.asc())
            .map { row -> ReleaseGameTB.createEntity(row) }
    }

    fun insert(releaseGames: List<ReleaseGame>) {
        if (releaseGames.isEmpty()) return

        //懒得写了，循环插就完事
        releaseGames.forEach { insert(it) }
    }

    fun insert(releaseGames: ReleaseGame) {
        database.sequenceOf(ReleaseGameTB).add(releaseGames)
    }
}