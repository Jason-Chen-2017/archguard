package com.thoughtworks.archguard.scanner.domain.system

import com.thoughtworks.archguard.systeminfo.domain.SystemInfo

interface SystemInfoRepository {
    fun getSystemInfo(id: Long): SystemInfo?
    fun updateSystemInfo(systemInfo: SystemInfo): Int
    fun updateScanningSystemToScanFail()
    fun removeNotClearRelatedData(id: Long)
    fun setSystemWorkspace(id: Long, workspace: String)
}
