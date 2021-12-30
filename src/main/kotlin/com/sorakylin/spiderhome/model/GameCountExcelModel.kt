package com.sorakylin.spiderhome.model

import com.alibaba.excel.annotation.ExcelProperty
import com.alibaba.excel.annotation.write.style.ColumnWidth
import com.sorakylin.spiderhome.LocalDateConverter
import java.time.LocalDate

open class GameCountExcelModel(
    @ExcelProperty(value = ["游戏名"])
    @ColumnWidth(50)
    var name: String?,

    @ColumnWidth(15)
    @ExcelProperty(value = ["价格(JPY)"])
    var price: Int?,

    @ExcelProperty(value = ["发售时间"], converter = LocalDateConverter::class)
    @ColumnWidth(15)
    var date: LocalDate?,

    @ColumnWidth(15)
    @ExcelProperty(value = ["来源"])
    var origin: String?,

    @ColumnWidth(50)
    @ExcelProperty(value = ["链接"])
    var link: String?,
)