package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FileDeleteEvents

// fixLater: Sep 25, 2018 - created 3 niotes and then deleted them - hide/show shows (-3)? 
class FileDeleteEventsHandler(
        private val listener: OnFileDeleteEventsHandlerInteractionListener) {
    fun onMessageEvent(event: FileDeleteEvents) {
    val request = event.request
    val msgContents = event.msgContents
    Log.v("FileDeleteEventsHandler", """  onMessageEvent.FileDeleteEvents -
                    |request: $request
                    |msgContents: $msgContents
                    |""".trimMargin())

    when (request) {

        FileDeleteEvents.Events.TRASH_FILE_FINISHED -> {

            val idxInTheFolderFilesList = event.idxInTheFolderFilesList
            Log.v("FileDeleteEventsHandler", """onMessageEvent - index
                    |idxInTheFolderFilesList: $idxInTheFolderFilesList
                    |isFileDeleted          : ${event.isFileDeleted}
                    |""".trimMargin())

            if (event.isFileDeleted) {
                FoldersData.updateFolderItemViewAfterFileDelete(
                        event.fileItemId,
                        event.thisFileFolderLevel,
                        event.parentFolderDriveId,
                        idxInTheFolderFilesList,
                        FileMetadataInfo(
                                event.parentFileName,
                                event.fileName,
                                event.thisFileDriveId,
                                event.isFolder,
                                event.mimeType,
                                event.createDt,
                                event.updateDt,
                                event.fileItemId,
                                true,
                                event.isTrashed)
                )
            } else {
                FoldersData.updateFolderItemView(
                        event.fileItemId,
                        event.thisFileFolderLevel,
                        event.parentFolderDriveId,
                        idxInTheFolderFilesList,
                        FileMetadataInfo(
                                event.parentFileName,
                                event.fileName,
                                event.thisFileDriveId,
                                event.isFolder,
                                event.mimeType,
                                event.createDt,
                                event.updateDt,
                                event.fileItemId,
                                true,
                                event.isTrashed)
                )
            }
            // fixme: do we need updateFolderListAdapter()
            val folderData = FoldersData.getCurrFolderData()
            if (FoldersData.allCurrFolderFilesTrashedOrThereAreNoFiles()) {
                FragmentsStack.removeTopFragment("onEventMainThread-TRASH_FILE_FINISHED",
                        false)
                listener.setFolderFragment(folderData)
            } else {
                listener.updateFolderListAdapter()
            }

        }
    }
}

    /**
     * This interface must be implemented by activities that call this
     * class.
     */
    interface OnFileDeleteEventsHandlerInteractionListener {

        fun showMessage(message: String)
        fun updateFolderListAdapter()
        fun setFolderFragment(folderData: FolderData)
    }
}