package au.com.kbrsolutions.notesnuageuses.features.events

import au.com.kbrsolutions.notesnuageuses.features.core.FolderData

class FoldersEvents(
        var request: Events,
        var msgContents: String?,
        var foldersAddData: FolderData?,
        var newFileName: String?) {

    enum class Events {
        CREATE_FOLDER,
        FOLDER_CREATED,
        CREATE_FOLDER_PROBLEMS,
        FOLDER_DATA_RETRIEVED,
        FOLDER_DATA_RETRIEVE_PROBLEM,
        DELETE_FILE, DELETE_FILE_START,
        CREATE_FILE_DIALOG_CANCELLED,
    }


//    private fun ActivitiesEvents(

    class Builder(private var request: Events) {
        private var msgContents: String? = null
        private var foldersAddData: FolderData? = null
        private var newFileName: String? = null

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun foldersAddData(foldersAddData: FolderData) =
                apply { this.foldersAddData = foldersAddData }

        fun newFileName(newFileName: String) =
                apply { this.newFileName = newFileName }

        fun build() = FoldersEvents(
                request,
                msgContents,
                foldersAddData,
                newFileName)
    }
}

