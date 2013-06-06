package com.ovea.tajin.framework.scheduling

interface JobExecutor {
    void execute(Map<String , ?> params)
}
