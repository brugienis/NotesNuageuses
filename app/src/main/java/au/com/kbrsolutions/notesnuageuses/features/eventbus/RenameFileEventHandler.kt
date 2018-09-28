package au.com.kbrsolutions.notesnuageuses.features.eventbus

import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.RenameFileEvents

class RenameFileEventHandler(private val listener: OnRenameFileEventHandlerInteractionListener) {
    fun onMessageEvent(event: RenameFileEvents) {
        val request = event.request
        val msgContents = event.msgContents
//        val isProblem = event.isProblem

        when (request) {

            RenameFileEvents.Events.RENAME_FILE_START -> {
                listener.showMessage(msgContents)
            }

            RenameFileEvents.Events.RENAME_FILE_FINISHED -> {

                FoldersData.updateFolderItemView(
                        event.fileItemId,
                        event.thisFileFolderLevel,
                        event.parentFolderDriveId,
                        event.idxInTheFolderFilesList,
                        FileMetadataInfo(
                                event.parentFileName,
                                event.newFileName,
                                event.thisFileDriveId,
                                event.isFolder,
                                event.mimeType,
                                event.createDt,
                                event.updateDt,
                                event.fileItemId,
                                true,
                                event.isTrashed)
                )
                listener.updateFolderListAdapter()
            }
        }
    }

    /**
     * This interface must be implemented by activities that call this
     * class.
     */
    interface OnRenameFileEventHandlerInteractionListener {

        fun showMessage(message: String)
        fun updateFolderListAdapter()
    }
}