package com.example.busmanager.data.mapper

import com.example.busmanager.data.local.entity.TerminalPassengerRecordEntity
import com.example.busmanager.domain.terminal.model.TerminalPassengerRecord

fun TerminalPassengerRecordEntity.toDomain(): TerminalPassengerRecord = TerminalPassengerRecord(
    id = id,
    terminalId = terminalId,
    terminalName = terminalName,
    busId = busId,
    busCode = busCode,
    routeId = routeId,
    routeName = routeName,
    day = day,
    month = month,
    year = year,
    dateFormatted = dateFormatted,
    timeFormatted = timeFormatted,
    boardedCount = boardedCount,
    alightedCount = alightedCount,
    timestamp = timestamp
)

fun TerminalPassengerRecord.toEntity(): TerminalPassengerRecordEntity = TerminalPassengerRecordEntity(
    id = id,
    terminalId = terminalId,
    terminalName = terminalName,
    busId = busId,
    busCode = busCode,
    routeId = routeId,
    routeName = routeName,
    day = day,
    month = month,
    year = year,
    dateFormatted = dateFormatted,
    timeFormatted = timeFormatted,
    boardedCount = boardedCount,
    alightedCount = alightedCount,
    timestamp = timestamp
)
