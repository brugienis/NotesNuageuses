package au.com.kbrsolutions.notesnuageuses.features.espresso

import android.util.Log
import java.util.*

object ActiveFlagsController {

    var isEspressoTestRunning: Boolean = false

    private val mActiveFlagsSet = TreeSet<String>()

    private val TAG = ActiveFlagsController::class.java.simpleName

    private fun updateActiveFlagsSet(flagName: String, flagActive: Boolean) {
        if (flagActive) {
            if (!mActiveFlagsSet.contains(flagName)) {
                mActiveFlagsSet.add(flagName)
            }
        } else {
            if (mActiveFlagsSet.contains(flagName)) {
                mActiveFlagsSet.remove(flagName)
            }
        }
    }

    fun performEndOfTestValidations(source: String) {
        if (mActiveFlagsSet.size != 1) {
            throw RuntimeException(
                    "BR - " +
                            TAG +
                            ".performEndOfTestValidations - mActiveFlagsSet size must be 1 but is: " +
                            mActiveFlagsSet.size + "; " +
                            mActiveFlagsSet)
        }
        isEspressoTestRunning = false
    }

    //---------------- FolderFragment -----------------------------

    private var mIsFolderFragmentActive: Boolean = false

    fun setEspressoFolderFragmentActiveFlag(source: String, active: Boolean) {
        Log.v("ActiveFlagsController", """setEspressoFolderFragmentActiveFlag -
            | isEspressoTestRunning: $isEspressoTestRunning
            | active:                 $active
            | """.trimMargin())
        if (!isEspressoTestRunning) {
            return
        }
        mIsFolderFragmentActive = active
        updateActiveFlagsSet("mIsFolderFragmentActive", active)
    }

    fun isFolderFragmentActive(): Boolean {
        return mIsFolderFragmentActive
    }

}
