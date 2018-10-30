package au.com.kbrsolutions.notesnuageuses.espresso.helpers

import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import com.azimolabs.conditionwatcher.Instruction

class WaitForFolderIsActiveInstruction(val homeActivity: HomeActivity): Instruction() {

//    val homeActivity: HomeActivity

//    constructor(activity: HomeActivity) {
//        homeActivity = activity
//        this()
//    }


    override fun getDescription(): String {
        return "Waiting for Folder fragment"
    }

    override fun checkCondition(): Boolean {

//        val activity = (InstrumentationRegistry.getTargetContext().applicationContext as TestApplication)
//                .currentActivity

//        return activity != null && (activity as HomeActivity).isAboutFragmentFragmentActive()
        return homeActivity.isAboutFragmentFragmentActive()
    }
}