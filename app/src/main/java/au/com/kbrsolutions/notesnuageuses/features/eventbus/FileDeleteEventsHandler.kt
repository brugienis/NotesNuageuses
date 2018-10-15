package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.os.Bundle
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FileDeleteEvents
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity

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
                // fixLater: Sep 30, 2018 - problem: You delete the last file - listener.setFolderFragment(folderData) is called 
                // fixLater: Sep 30, 2018 - and the empty list is shown instead of EmptyFragment showing counts - merge fragments? 
                val folderData = FoldersData.getCurrFolderData()
                Log.v("FileDeleteEventsHandler", """onMessageEvent -
                    |FoldersData.currFolderIsEmptyOrAllFilesAreTrashed(): ${FoldersData.currFolderIsEmptyOrAllFilesAreTrashed()}
                    |""".trimMargin())
                if (FoldersData.currFolderIsEmptyOrAllFilesAreTrashed()) {
                    FragmentsStack.removeTopFragment("onEventMainThread-REMOVE_FILE_FINISHED",
                            false)
//                    listener.setFolderFragment(folderData)
                    listener.setFragment(
                            HomeActivity.FragmentsEnum.FOLDER_FRAGMENT_NEW,
                            folderData.newFolderTitle,
                            true,
                            folderData,
                            null)
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

        fun setFragment(
                fragmentId: HomeActivity.FragmentsEnum,
                titleText: String,
                addFragmentToStack: Boolean,
                foldersAddData: FolderData?,
                fragmentArgs: Bundle?)
        fun showMessage(message: String)
        fun updateFolderListAdapter()
//        fun setFolderFragment(folderData: FolderData)
    }
}