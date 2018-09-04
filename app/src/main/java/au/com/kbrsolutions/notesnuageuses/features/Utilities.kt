package au.com.kbrsolutions.notesnuageuses.features

class Utilities {

    companion object {
        /**
         * Returns object hash code - use for testing.
         *
         * @param   o object
         * @return  object's hashCode
         */
        fun getClassHashCode(o: Any): String {
            return String.format("0x%08X", o.hashCode())
        }

    }
}