package com.example.applimit.data.repository

import com.example.applimit.data.db.TargetAppEntity
import com.example.applimit.domain.model.TargetApp

fun TargetAppEntity.toDomain(): TargetApp = TargetApp(
    id = id,
    packageName = packageName,
    appName = appName,
    limitMinutes = limitMinutes,
    cooldownMinutes = cooldownMinutes,
    extensionMinutes = extensionMinutes,
    isEnabled = isEnabled
)

fun TargetApp.toEntity(): TargetAppEntity = TargetAppEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    limitMinutes = limitMinutes,
    cooldownMinutes = cooldownMinutes,
    extensionMinutes = extensionMinutes,
    isEnabled = isEnabled
)
