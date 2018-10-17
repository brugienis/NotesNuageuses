package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.os.Bundle
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FilesDownloadEvents
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity

class FileDownloadEventHandler(
        private val listener: OnFileDownloadEventHandlerInteractionListener) {
    fun onMessageEvent(event: FilesDownloadEvents) {
    val request = event.request
    val msgContents = event.msgContents
    Log.v("EventBusEventsHandler", """onMessageEvent.FilesDownloadEvents -
            |request: $request
            |msgContents: $msgContents"
            |""".trimMargin())

    when (request) {

        FilesDownloadEvents.Events.FILE_DOWNLOADED -> {

            val fileTitle = event.fileName
            val fileContents = event.textContents
            val thisFileDriveId = event.downloadedFileDriveId.encodeToString()
            val fileItemId = event.fileItemId
            val idxInTheFolderFilesList = event.idxInTheFolderFilesList
            val args = Bundle()
            args.putString(HomeActivity.FILE_NAME_KEY, fileTitle)
            args.putString(HomeActivity.FILE_CONTENTS_KEY, fileContents)
            args.putString(HomeActivity.THIS_FILE_DRIVE_ID_KEY, thisFileDriveId)
            args.putLong(HomeActivity.FILE_ITEM_ID_KEY, fileItemId)
            args.putInt(HomeActivity.IDX_IN_THE_FOLDER_FILES_LIST_KEY, idxInTheFolderFilesList)
            idxInTheFolderFilesList

            listener.setActionBarTitle(fileTitle)
            listener.removeDownloadFragment()
            listener.setFragment(
                    HomeActivity.FragmentsEnum.FILE_FRAGMENT,
                    event.fileName,
                    true,
                    null,
                    args)
        }

        else -> throw RuntimeException(
                "${this.javaClass.simpleName} - onMessageEvent.FilesDownloadEvents - " +
                        "no code to handle " +
                        "request: $request")
    }
}

    /**
     * This interface must be implemented by activities that call this
     * class.
     */
    interface OnFileDownloadEventHandlerInteractionListener {

        fun setFragment(
                fragmentId: HomeActivity.FragmentsEnum,
                titleText: String,
                addFragmentToStack: Boolean,
                foldersAddData: FolderData?,
                fragmentArgs: Bundle?)
//        fun showMessage(message: String)
        fun updateFolderListAdapter()
        fun removeTopFragment(source: String, actionCancelled: Boolean): Boolean
        fun setActionBarTitle(title: CharSequence)
        fun removeDownloadFragment()
    }
}