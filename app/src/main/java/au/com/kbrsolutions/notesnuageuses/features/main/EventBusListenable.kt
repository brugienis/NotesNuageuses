package au.com.kbrsolutions.notesnuageuses.features.main

import au.com.kbrsolutions.notesnuageuses.features.events.DriveAccessEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FilesDownloadEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FilesUploadEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FoldersEvents

interface EventBusListenable {

    fun onMessageEvent(event: DriveAccessEvents)

    fun onMessageEvent(event: FilesDownloadEvents)

    fun onMessageEvent(event: FilesUploadEvents)

    fun onMessageEvent(event: FoldersEvents)
}