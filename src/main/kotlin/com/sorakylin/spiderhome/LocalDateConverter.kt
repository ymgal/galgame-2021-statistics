package com.sorakylin.spiderhome

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.enums.CellDataTypeEnum
import com.alibaba.excel.metadata.property.ExcelContentProperty
import com.alibaba.excel.metadata.GlobalConfiguration
import com.alibaba.excel.metadata.data.ReadCellData
import com.alibaba.excel.metadata.data.WriteCellData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateConverter : Converter<LocalDate> {

    override fun supportJavaTypeKey(): Class<LocalDate> {
        return LocalDate::class.java
    }

    override fun supportExcelTypeKey(): CellDataTypeEnum {
        return CellDataTypeEnum.STRING
    }

    override fun convertToJavaData(
        cellData: ReadCellData<*>,
        contentProperty: ExcelContentProperty,
        globalConfiguration: GlobalConfiguration
    ): LocalDate? {
        return LocalDate.parse(cellData.stringValue, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    override fun convertToExcelData(
        value: LocalDate,
        contentProperty: ExcelContentProperty,
        globalConfiguration: GlobalConfiguration
    ): WriteCellData<*>? {
        return WriteCellData<String>(Utils.formatLocalDate(value))
    }
}