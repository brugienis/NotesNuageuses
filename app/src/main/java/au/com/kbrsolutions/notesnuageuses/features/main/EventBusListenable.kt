package au.com.kbrsolutions.notesnuageuses.features.main

import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.*

interface EventBusListenable {

    fun onMessageEvent(event: DriveAccessEvents)

    fun onMessageEvent(event: FilesDownloadEvents)

    fun onMessageEvent(event: FilesUploadEvents)

    fun onMessageEvent(event: FoldersEvents)

    fun onMessageEvent(event: FileDeleteEvents)
}