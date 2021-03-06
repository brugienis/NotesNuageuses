package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.os.Bundle
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.FoldersEvents
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity

class FolderEventsHandler(private val listener: OnFolderEventsHandlerInteractionListener) {

    fun onMessageEvent(event: FoldersEvents) {
        val request = event.request
        val msgContents = event.msgContents

        when (request) {

            // fixLater: Sep 09, 2018 - below not tested
            FoldersEvents.Events.CREATE_FILE_DIALOG_CANCELLED -> {
                FragmentsStack.removeTopFragment(
                        "FolderEventsHandler.onMessageEvent-CREATE_FILE_DIALOG_CANCELLED",
                        true
                )
            }

            FoldersEvents.Events.FOLDER_DATA_RETRIEVED -> {

                val folderData = event.foldersAddData
                val folderName = folderData!!.newFolderTitle
                listener.setActionBarTitle(folderName)

                if (folderData != null && folderData.isEmptyOrAllFilesTrashed) {
                    listener.setFragment(
                            HomeActivity.FragmentsEnum.FOLDER_FRAGMENT_NEW,
                            folderData.newFolderTitle,
                            true,
                            folderData,
                            null)
                } else {
                    retrievingAppFolderDriveInfoTaskDone(folderData)
                }
            }

            FoldersEvents.Events.FOLDER_DATA_RETRIEVE_PROBLEM -> {
                Log.v("FolderEventsHandler", "onMessageEvent - msgContents: $msgContents")
            }

//            ActivitiesEvents.HomeEvents.SHOW_MESSAGE -> addMsgToActivityLogShowOnScreen(msgContents, true, true)

            FoldersEvents.Events.FOLDER_CREATED -> {
                val folderName = event.newFileName ?: "undefined"
                listener.setActionBarTitle(folderName)
                listener.updateFolderListAdapter()
            }
            else -> throw RuntimeException(
                    "${FolderEventsHandler::class.simpleName} - FolderEventsHandler.onMessageEvent - " +
                            "no code to handle request: $request")
        }
    }

    private fun retrievingAppFolderDriveInfoTaskDone(folderData: FolderData?) {
        if (folderData != null && folderData.newFolderData) {   // folder info not in FoldersData
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

    // fixLater: Aug 27, 2018 - used for debugging. Remove later
    private fun printCollection(msg: String, coll: Array<HomeActivity.FragmentsEnum>) {
        Log.i("FolderEventsHandler", "\nprintCollection $msg")
        coll.forEach { Log.i("${FolderEventsHandler::class.simpleName}", it.toString()) }
        Log.i("FolderEventsHandler", "\nend")
    }

    /**
     * This interface must be implemented by activities that call this
     * class.
     */
    interface OnFolderEventsHandlerInteractionListener {

        fun setFragment(
                fragmentId: HomeActivity.FragmentsEnum,
                titleText: String,
                addFragmentToStack: Boolean,
                foldersAddData: FolderData?,
                fragmentArgs: Bundle?)
        fun showMessage(message: String)
        fun updateFolderListAdapter()
        fun setActionBarTitle(title: CharSequence)
    }
}