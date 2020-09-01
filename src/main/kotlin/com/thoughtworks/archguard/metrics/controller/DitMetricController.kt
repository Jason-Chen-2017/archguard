package com.thoughtworks.archguard.metrics.controller

import com.thoughtworks.archguard.metrics.appl.MetricsService
import com.thoughtworks.archguard.module.domain.model.JClassVO
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/systems/{systemId}/metric/dit")
class DitMetricController(val metricsService: MetricsService) {
    @GetMapping("/class")
    fun getClassDitMetric(@PathVariable("systemId") systemId: Long,
                          @RequestParam className: String,
                          @RequestParam moduleName: String): Int {
        return metricsService.getClassDit(systemId, JClassVO(className, moduleName))
    }
}
