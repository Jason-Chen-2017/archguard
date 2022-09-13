package com.thoughtworks.archguard.scanner.domain.analyser

import com.thoughtworks.archguard.scanner.domain.exception.EntityNotFoundException
import com.thoughtworks.archguard.scanner.domain.system.SystemInfo
import com.thoughtworks.archguard.scanner.domain.system.SystemInfoRepository
import com.thoughtworks.archguard.scanner.domain.system.SystemBuilder
import com.thoughtworks.archguard.scanner.infrastructure.command.StreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

@Service
class AnalysisService(@Autowired val systemInfoRepository: SystemInfoRepository) {
    fun getSystemBuilder(id: Long, streamConsumer: StreamConsumer): SystemBuilder {
        val systemInfo = systemInfoRepository.getSystemInfo(id)
            ?: throw EntityNotFoundException(SystemInfo::class.java, id)
        checkAnalysable(systemInfo)
        return SystemBuilder(systemInfo, File(systemInfo.workdir), streamConsumer)
    }

    fun checkAnalysable(systemInfo: SystemInfo) {
        if (systemInfo.repoType == "ZIP") {
            systemInfo.getRepoList().forEach {
                if (!Files.exists(Paths.get(it))) {
                    throw FileNotFoundException("zip file has been deleted: $it")
                }
            }
        }
    }
}
