package au.com.kbrsolutions.notesnuageuses.features.eventbus

import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.DriveAccessEvents

class DriveAccessEventsHandler(
        private val listener: OnDriveAccessEventsHandlerInteractionListener) {

    fun onMessageEvent(event: DriveAccessEvents) {
    val request = event.request
    val msgContents = event.msgContents
//        val isProblem = event.isProblem

    when (request) {

        DriveAccessEvents.Events.MESSAGE -> {
            listener.showMessage(msgContents)
            Log.v("DriveAccessEventsHandle", """onMessageEvent.DriveAccessEvents -
                    |msgContents: $msgContents
                    |""".trimMargin())
        }
    }
}

    /**
     * This interface must be implemented by activities that call this
     * class.
     */
    interface OnDriveAccessEventsHandlerInteractionListener {

        fun showMessage(message: String)
    }
}