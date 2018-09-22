package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.os.Bundle
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.RenameFileEvents
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity

class RenameFileEventHandler(private val listener: OnRenameFileEventHandlerInteractionListener) {
    fun onMessageEvent(event: RenameFileEvents) {
        val request = event.request
        val msgContents = event.msgContents
//        val isProblem = event.isProblem

        when (request) {

            RenameFileEvents.Events.RENAME_FILE_START -> {
                Log.v("EventBusEventsHandler", """onMessageEvent.RenameFileEvents -
                    |msgContents: $msgContents
                    |""".trimMargin())
            }

            RenameFileEvents.Events.RENAME_FILE_FINISHED -> {
                listener.showMessage(msgContents)
                Log.v("EventBusEventsHandler", """onMessageEvent.RenameFileEvents -
                    |msgContents: $msgContents
                    |""".trimMargin())

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

        fun setFragment(
                fragmentId: HomeActivity.FragmentsEnum,
                titleText: String,
                addFragmentToStack: Boolean,
                foldersAddData: FolderData?,
                fragmentArgs: Bundle?)
        fun showMessage(message: String)
        fun updateFolderListAdapter()
        fun removeTopFragment(source: String, actionCancelled: Boolean): Boolean
        fun setActionBarTitle(title: CharSequence)
        fun setFolderFragment(folderData: FolderData)
    }
}