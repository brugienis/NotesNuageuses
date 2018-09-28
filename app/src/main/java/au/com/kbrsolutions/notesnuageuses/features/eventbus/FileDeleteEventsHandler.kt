package au.com.kbrsolutions.notesnuageuses.features.eventbus

import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FileDeleteEvents

class FileDeleteEventsHandler(
        private val listener: OnFileDeleteEventsHandlerInteractionListener) {

    fun onMessageEvent(event: FileDeleteEvents) {
        val request = event.request
        val msgContents = event.msgContents
        listener.showMessage(msgContents)

        when (request) {

            FileDeleteEvents.Events.REMOVE_FILE_FINISHED -> {

                val idxInTheFolderFilesList = event.idxInTheFolderFilesList

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
                val folderData = FoldersData.getCurrFolderData()
                if (FoldersData.currFolderIsEmptyOrAllFilesAreTrashed()) {
                    FragmentsStack.removeTopFragment("onEventMainThread-REMOVE_FILE_FINISHED",
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