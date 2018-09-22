package au.com.kbrsolutions.notesnuageuses.features.eventbus.events

class DriveAccessEvents(
        var request: Events,
        var msgContents: String,
        var isProblem: Boolean) {

    enum class Events {
        MESSAGE
    }

    class Builder(private var request: Events) {
        private lateinit var msgContents: String
        private var isProblem: Boolean = false

        fun msgContents(msgContents: String) = apply { this.msgContents = msgContents }

        fun isProblem(isProblem: Boolean) = apply { this.isProblem = isProblem }

        fun build() = DriveAccessEvents(
                request,
                msgContents,
                isProblem)
    }

}

