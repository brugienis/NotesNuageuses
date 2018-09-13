package au.com.kbrsolutions.notesnuageuses.features.events

import android.content.Context
import android.os.Bundle
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack.removeTopFragment
import au.com.kbrsolutions.notesnuageuses.features.main.EventBusListenable
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity

class EventBusEventsHandler(private val listener: OnEventBusEventsHandlerInteractionListener)
    : EventBusListenable {

    override fun onMessageEvent(event: DriveAccessEvents) {
        val request = event.request
        val msgContents = event.msgContents
//        val isProblem = event.isProblem

        when (request) {

            DriveAccessEvents.Events.MESSAGE -> {
                listener.showMessage(msgContents)
                Log.v("EventBusEventsHandler", """onMessageEvent.DriveAccessEvents -
                    |msgContents: $msgContents
                    |""".trimMargin())
            }
        }
    }

    override fun onMessageEvent(event: FilesDownloadEvents) {
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
                val args = Bundle()
                args.putString(HomeActivity.FILE_NAME_KEY, fileTitle)
                args.putString(HomeActivity.FILE_CONTENTS_KEY, fileContents)
                args.putString(HomeActivity.THIS_FILE_DRIVE_ID_KEY, thisFileDriveId)

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

    override fun onMessageEvent(event: FilesUploadEvents) {
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
                    /*
                    foldersData.insertFolderItemView(
                            event.fileItemId,
                            event.folderLevel,
                            event.currFolderDriveId,
                            0,
                            FileMetadataInfo(
                                    event.parentFileName,
                                    event.fileName +
                                            context.resources.
                                            getString(
                                                    R.string.home_app_file_encrypting, " "),
                                    event.thisFileDriveId,
                                    false,
                                    event.mimeType,
                                    event.createDt,
                                    event.updateDt,
                                    event.fileItemId,
                                    true,
                                    false))
                                    */
                } else {
                    updateFolderItem(event, context)

                    /*
                    foldersData.updateFolderItemView(
                            event.fileItemId,
                            event.folderLevel,
                            event.currFolderDriveId,
                            FileMetadataInfo(
                                    event.parentFileName,
                                    event.fileName +
                                            context.resources.
                                            getString(R.string.home_app_file_uploading,
                                                    " "),
                                    event.thisFileDriveId,
                                    false,
                                    event.mimeType,
                                    event.createDt,
                                    event.updateDt,
                                    event.fileItemId,
                                    true,
                                    false)
                    )
                    */
                }
                listener.removeTopFragment(
                        "onEventMainThread - $request",
                        false)
            }

            FilesUploadEvents.Events.TEXT_UPLOADED -> {

                listener.showMessage(event.msgContents)

                updateFolderItem(event, context)

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

    override fun onMessageEvent(event: FoldersEvents) {
        val request = event.request
        val msgContents = event.msgContents
        Log.v("EventBusEventsHandler", """  onMessageEvent.FoldersEvents -
                    |request: $request
                    |msgContents: $msgContents
                    |""".trimMargin())

        when (request) {

            // fixLater: Sep 09, 2018 - below not tested
            FoldersEvents.Events.CREATE_FILE_DIALOG_CANCELLED -> {
                removeTopFragment(
                        "onMessageEvent.FoldersEvents-CREATE_FILE_DIALOG_CANCELLED",
                        true
                )
            }

            FoldersEvents.Events.FOLDER_DATA_RETRIEVED -> {
//                if (dismissRefreshProgressBarCallableRunnable != null) {
//                    handler.removeCallbacks(dismissRefreshProgressBarCallableRunnable)            // remove just in case if there is already one waiting in a queue
//                }
//                // fixme: why do I need the line below
//                removeExpiredFileLockInfoElementsRunnableRunScheduled = true
//                dismissRefreshProgressBarCallableRunnable = DismissRefreshProgressBarCallableRunnable()
//                handler.postDelayed(dismissRefreshProgressBarCallableRunnable, DISSMISS_REFRESH_PROGRESS_DELAY_MILLS)

                val folderData = event.foldersAddData
                val folderName = folderData!!.newFolderTitle
                listener.setActionBarTitle(folderName)

                if (folderData != null && folderData.isEmptyOrAllFilesTrashed) {
                    listener.setFolderFragment(folderData) // empty folder
                    Log.v("HomeActivity", "EventBusEventsHandler - " +
                            "fragmentsStack: $FragmentsStack ")
                } else {
                    retrievingAppFolderDriveInfoTaskDone(folderData)
                }
            }

            FoldersEvents.Events.FOLDER_DATA_RETRIEVE_PROBLEM -> {
                Log.v("HomeActivity", "onMessageEvent - msgContents: $msgContents")
//                addMsgToActivityLogShowOnScreen(event.msgContents, true, true)
//                if (dismissRefreshProgressBarCallableRunnable != null) {
//                    handler.removeCallbacks(dismissRefreshProgressBarCallableRunnable)            // remove just in case if there is already one waiting in a queue
//                }
//                dismissRefreshProgressBarCallableRunnable = DismissRefreshProgressBarCallableRunnable()
//                handler.postDelayed(dismissRefreshProgressBarCallableRunnable, DISSMISS_REFRESH_PROGRESS_DELAY_MILLS)
            }

//            ActivitiesEvents.HomeEvents.SHOW_MESSAGE -> addMsgToActivityLogShowOnScreen(msgContents, true, true)

            FoldersEvents.Events.FOLDER_CREATED -> {
//				addMsgToActivityLogShowOnScreen("Folder created");
                val folderName = event.newFileName ?: "undefined"
                listener.setActionBarTitle(folderName)
                val stackFragmentsAfterAdd = FragmentsStack.getFragmentsList()
                Log.v("${EventBusEventsHandler::class.simpleName}", " - " +
                        "actualStackFragmentsAfterAdd: " +
                        "${printCollection("after fragments added", stackFragmentsAfterAdd)}")
                val currFolder = FragmentsStack . getCurrFragment ()
                if (currFolder == HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {    // folder is no longer empty
                    FragmentsStack.replaceCurrFragment (
                            "onMessageEvent FoldersEvents FOLDER_CREATED" +
                                    event.request,
                            currFolder, HomeActivity.FragmentsEnum.FOLDER_FRAGMENT)

                            listener.setFragment(
                            HomeActivity.FragmentsEnum.FOLDER_FRAGMENT,
                            folderName,
                            false,
                            null,
                            null)
                } else {
                    listener.updateFolderListAdapter()
                }
            }
            else -> throw RuntimeException(
                    "${EventBusEventsHandler::class.simpleName} - onMessageEvent.FoldersEvents - " +
                            "no code to handle request: $request")
        }
    }

    private fun retrievingAppFolderDriveInfoTaskDone(folderData: FolderData?) {
        if (folderData != null && folderData.newFolderData) {   // folder info not in FoldersData
            listener.setFolderFragment(folderData)
        } else {
            listener.updateFolderListAdapter()
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
                                context.resources.
                                        getString(
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

    private fun updateFolderItem(event: FilesUploadEvents, context: Context) {
        FoldersData.updateFolderItemView(
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
    }

    // fixLater: Aug 27, 2018 - used for debugging. Remove later
    private fun printCollection(msg: String, coll: Array<HomeActivity.FragmentsEnum>) {
        Log.i("${EventBusEventsHandler::class.simpleName}", "\nprintCollection $msg")
        coll.forEach { Log.i("${EventBusEventsHandler::class.simpleName}", it.toString()) }
        Log.i("${EventBusEventsHandler::class.simpleName}", "\nend")
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the context and potentially other fragments contained in that
     * context.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnEventBusEventsHandlerInteractionListener {

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