package au.com.kbrsolutions.notesnuageuses.espresso.helpers

import au.com.kbrsolutions.notesnuageuses.features.espresso.ActiveFlagsController
import com.azimolabs.conditionwatcher.Instruction

//class WaitForFolderIsActiveInstruction(val homeActivity: HomeActivity): Instruction() {
class WaitForFolderIsActiveInstruction(): Instruction() {

    override fun getDescription(): String {
        return "Waiting for Folder fragment"
    }

    override fun checkCondition(): Boolean {
//        return homeActivity.isFolderFragmentActive()
        return ActiveFlagsController.isFolderFragmentActive()
    }
}