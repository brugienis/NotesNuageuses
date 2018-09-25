package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.content.Context
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FilesUploadEvents

class FileUploadEventsHandler(
        private val listener: OnFileUploadEventsHandlerInteractionListener) {
    fun onMessageEvent(event: FilesUploadEvents) {
        val request = event.request
        val msgContents = event.msgContents
        val context = listener as Context

        Log.v("EventBusEventsHandler", """onMessageEvent.FilesUploadEvents -
            |request: $request
            |msgContents: $msgContents"
            |""".trimMargin())

        when (request) {

            FilesUploadEvents.Events.TEXT_UPLOADING -> {

                if (event.thisFileDriveId == null) {            // no file DriveId - we are going to upload a new file.
                    // We will know the DriveId after successful send
                    insertFolderItemView(event, context)
                } else {
                    updateFolderItem(event)
                }
                listener.removeTopFragment(
                        "onEventMainThread - $request",
                        false)
            }

            FilesUploadEvents.Events.TEXT_UPLOADED -> {

                listener.showMessage(event.msgContents)

                updateFolderItem(event)

                /*
                foldersData.updateFolderItemView(
                        event.fileItemId,
                        event.folderLevel,
                        event.currFolderDriveId,
                        FileMetadataInfo(
                                event.parentFileName,
                                event.fileName,
                                event.thisFileDriveId ,
                                false,
                                event.mimeType,
                                event.createDt,
                                event.updateDt,
                                event.fileItemId,
                                true,
                                false
                        )
                )
                        */

                listener.updateFolderListAdapter()
            }

            FilesUploadEvents.Events.UPLOAD_PROBLEMS -> {

            }

            else -> throw RuntimeException(
                    "${this.javaClass.simpleName} - onMessageEvent.FilesDownloadEvents - " +
                            "no code to handle " +
                            "request: $request")
        }
    }

    private fun insertFolderItemView(event: FilesUploadEvents, context: Context) {
        FoldersData.insertFolderItemView(
                event.fileItemId,
                event.folderLevel,
                event.currFolderDriveId,
                0,
                FileMetadataInfo(
                        event.parentFileName,
                        event.fileName +
                                context.resources.getString(
                                        R.string.home_app_file_encrypting, " "),
                        event.thisFileDriveId,
                        false,
                        event.mimeType,
                        event.createDt,
                        event.updateDt,
                        event.fileItemId,
                        true,
                        false))
    }

    private fun updateFolderItem(event: FilesUploadEvents) {
        // fixLater: Sep 17, 2018 - pass around idxInTheFolderFilesList - changes done. TEST it.
        FoldersData.updateFolderItemView(
                event.fileItemId,
                event.folderLevel,
                event.currFolderDriveId,
                event.idxInTheFolderFilesList,
                FileMetadataInfo(
                        event.parentFileName,
                        event.fileName,
                        event.thisFileDriveId,
                        false,
                        event.mimeType,
                        event.createDt,
                        event.updateDt,
                        event.fileItemId,
                        true,
                        false
                )
        )
    }

    /**
     * This interface must be implemented by activities that call this
     * class.
     */
    interface OnFileUploadEventsHandlerInteractionListener {

        fun showMessage(message: String)
        fun removeTopFragment(source: String, actionCancelled: Boolean): Boolean
        fun updateFolderListAdapter()
    }

}