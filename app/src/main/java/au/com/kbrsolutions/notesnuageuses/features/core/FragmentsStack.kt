package au.com.kbrsolutions.notesnuageuses.features.core

import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import java.util.*

object FragmentsStack {

    private var fragmentsArrayDeque = ArrayDeque<HomeActivity.FragmentsEnum>()
    private var fragmentsTitlesArrayDeque = ArrayDeque<String>()
    private val foldersData = FoldersData
    private var folderFragmentsCnt = 0
    private var mTestMode: Boolean = false

    private var allFoldersFragmentsEnumTypesSet: MutableSet<HomeActivity.FragmentsEnum>

    private var notEmptyFolderHomeActivity: MutableSet<HomeActivity.FragmentsEnum>

    init {
        allFoldersFragmentsEnumTypesSet = mutableSetOf(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT)
        allFoldersFragmentsEnumTypesSet.add(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT)
        allFoldersFragmentsEnumTypesSet.add(HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT)
        notEmptyFolderHomeActivity = mutableSetOf(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT)
    }

    private val TAG = "xyz" + FragmentsStack::class.java.simpleName

    @Synchronized
    fun init(mTestMode: Boolean) {
        foldersData.init()
        this.mTestMode = mTestMode
        fragmentsArrayDeque = ArrayDeque<HomeActivity.FragmentsEnum>()
        fragmentsTitlesArrayDeque = ArrayDeque()
        folderFragmentsCnt = 0
    }

    fun getCurrFragment(): HomeActivity.FragmentsEnum {
        return fragmentsArrayDeque.peekLast()                       // peek at the element at tail
    }

    fun getCurrFragmentTitle(): String {
        return fragmentsTitlesArrayDeque.peekLast()                 // peek at the element at tail
    }

    @Synchronized
    fun getFolderFragmentCount(): Int {
        var lFolderFramentsCnt = 0
        for (el in fragmentsArrayDeque) {
            if (allFoldersFragmentsEnumTypesSet.contains(el)) {
                lFolderFramentsCnt++
            }
        }
        return lFolderFramentsCnt
    }

    @Synchronized
    fun replaceCurrFragment(source: String, fragmentToReplace: HomeActivity.FragmentsEnum, replacementFragment: HomeActivity.FragmentsEnum): Boolean {
        var currTopFragment = fragmentsArrayDeque.peekLast()            // peek at the element at tail
        if (currTopFragment === fragmentToReplace) {
            currTopFragment = fragmentsArrayDeque.removeLast()          // remove fragmentToReplace
            fragmentsArrayDeque.addLast(replacementFragment)            // add replacementFragment
            return true
        } else {
            return false
        }
    }

    @Synchronized
    fun addFragment(
            fragmentId: HomeActivity.FragmentsEnum,
            fragmentTitle: String,
            foldersAddData: FolderData?): Boolean {
//        Log.v(TAG, "addFragment - start - folderFragmentsCnt/foldersAddData: " + folderFragmentsCnt + "/" +
//                if (foldersAddData == null) "null" else foldersAddData!!.newFolderTitle)
        var lFragmentTitle = fragmentTitle
        var menuOptionsChangeRequired = false
        if (fragmentsArrayDeque.peekLast() !== fragmentId) {
            menuOptionsChangeRequired = true
        }
        if (foldersAddData != null && allFoldersFragmentsEnumTypesSet.contains(fragmentId)) {
            foldersData.addFolderData(foldersAddData)
            lFragmentTitle = foldersAddData!!.newFolderTitle
            folderFragmentsCnt++
//            Log.v(TAG, "addFragment - fragmentId is in allFoldersMainActivity.FragmentsEnumTypesSet/folderFragmentsCnt: $fragmentId/$folderFragmentsCnt")
        }
        fragmentsArrayDeque.addLast(fragmentId)
        fragmentsTitlesArrayDeque.addLast(lFragmentTitle)                                    // add at the tail

        verify()
//        Log.v(TAG, "addFragment - end   - folderFragmentsCnt/foldersAddData: " + folderFragmentsCnt + "/" +
//                if (foldersAddData == null) "null" else foldersAddData!!.newFolderTitle)
        return menuOptionsChangeRequired
    }

    @Synchronized
    fun removeTopFragment(source: String, actionCancelled: Boolean): FragmentsStackResponse? {
        var fragmentToSet = HomeActivity.FragmentsEnum.NONE
        var updateFolderListAdapterRequired = false
        var viewFragmentsCleanupRequired = false
        var callFinishRequired = false
        var prevTopFragment: HomeActivity.FragmentsEnum? = null
        if (fragmentsArrayDeque.size == 0) {
            return null
        }
        if (fragmentsArrayDeque.size == 1) {
            fragmentsArrayDeque.clear()
            fragmentsTitlesArrayDeque.clear()
            folderFragmentsCnt = 0
            callFinishRequired = true
        } else {
            prevTopFragment = fragmentsArrayDeque.removeLast()              // get the element at tail
            fragmentsTitlesArrayDeque.removeLast()                          // remove the element at tail

            val currTopFragment = fragmentsArrayDeque.peekLast()            // peek at the element at tail

            when (prevTopFragment) {

                HomeActivity.FragmentsEnum.FOLDER_FRAGMENT -> {
                    folderFragmentsCnt--
                    foldersData.removeMostRecentFolderData()
                    removeFragmentsFromStackUntilSpecificFragmentFound()
                    if (fragmentsArrayDeque.size > 0) {                     // FOLDER_FRAGMENT found
                        updateFolderListAdapterRequired = true
                    } else {
                        callFinishRequired = true
                    }
                }

                HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT -> {
                    folderFragmentsCnt--
                    foldersData.removeMostRecentFolderData()
                    if (currTopFragment === HomeActivity.FragmentsEnum.FOLDER_FRAGMENT) {    // impossible case
                        fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                        updateFolderListAdapterRequired = true
                    } else if (currTopFragment === HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT) {
                        callFinishRequired = true
                    } else {
                        removeFragmentsFromStackUntilSpecificFragmentFound()
                        if (fragmentsArrayDeque.size > 0) {                            // FOLDER_FRAGMENT found
                            updateFolderListAdapterRequired = true
                            fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                        } else {
                            callFinishRequired = true
                        }
                    }
                }

                HomeActivity.FragmentsEnum.ACTIVITY_LOG_FRAGMENT -> if (currTopFragment === HomeActivity.FragmentsEnum.FOLDER_FRAGMENT) {
                    updateFolderListAdapterRequired = true
                    fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                } else if (currTopFragment === HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {
                    updateFolderListAdapterRequired = true
                    fragmentToSet = HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT
                } else if (fragmentsArrayDeque.size > 0) {
                    fragmentToSet = getCurrFragment()
                } else {
                    callFinishRequired = true
                }

            // TOP: will it also cover image fragment?
            /*
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, EMPTY_FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,       CREATE_FILE_FRAGMENT, TEXT_VIEW_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,                           , TEXT_VIEW_FRAGMENT] existing note was opened and Cancel clicked
					 */
                HomeActivity.FragmentsEnum.TEXT_VIEW_FRAGMENT -> {
                    viewFragmentsCleanupRequired = true
                    if (currTopFragment === HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {
                        if (actionCancelled) {
                            updateFolderListAdapterRequired = true
                            fragmentToSet = HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT
                        } else {                                                                                                                            // Save was clicked
                            updateFolderListAdapterRequired = true
                            fragmentsArrayDeque.removeLast()                                                                        // remove EMPTY_FOLDER_FRAGMENT
                            fragmentsArrayDeque.addLast(HomeActivity.FragmentsEnum.FOLDER_FRAGMENT)                    // add FOLDER_FRAGMENT
                            fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                            //						fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT;
                        }
                    } else {
                        updateFolderListAdapterRequired = true
                        fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                    }
                }

            // TOP: will it also cover image fragment?
            /*
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, EMPTY_FOLDER_FRAGMENT, TEXT_VIEW_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,       TEXT_VIEW_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,                           , TEXT_VIEW_FRAGMENT] existing note was opened and Cancel clicked
					 */
                HomeActivity.FragmentsEnum.IMAGE_VIEW_FRAGMENT -> {
                    viewFragmentsCleanupRequired = true
                    run {
                        updateFolderListAdapterRequired = true
                        fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                    }
                }

                HomeActivity.FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                HomeActivity.FragmentsEnum.FILE_DETAILS_FRAGMENT,
                HomeActivity.FragmentsEnum.LEGAL_NOTICES -> fragmentToSet = currTopFragment

                else -> {
                    if (mTestMode) {
                        throw RuntimeException("$TAG-FragmentStack - we should never be here - prevTopFragment/currTopFragment: $prevTopFragment/$currTopFragment")
                    }
                    when (currTopFragment) {
                        HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT, HomeActivity.FragmentsEnum.FOLDER_FRAGMENT -> fragmentToSet = currTopFragment            // HomeActivity.FragmentsEnum.FOLDER_FRAGMENT;

                        else -> if (currTopFragment === HomeActivity.FragmentsEnum.FOLDER_FRAGMENT) { // ??? it would be handled in the case above - probably remove it
                            updateFolderListAdapterRequired = true
                        } else {
                            removeFragmentsFromStackUntilSpecificFragmentFound()
                            if (fragmentsArrayDeque.size > 0) {        // FOLDER_FRAGMENT found
                                fragmentToSet = HomeActivity.FragmentsEnum.FOLDER_FRAGMENT
                            } else {
                                callFinishRequired = true
                            }
                        }
                    }
                }
            }
        }

        val menuOptionsChangeRequired = prevTopFragment !== fragmentToSet
        if (!callFinishRequired) {
            verify()
        }
        return FragmentsStackResponse(callFinishRequired, fragmentToSet, fragmentsTitlesArrayDeque.peekLast(), updateFolderListAdapterRequired, viewFragmentsCleanupRequired, menuOptionsChangeRequired)
    }

    private fun removeFragmentsFromStackUntilSpecificFragmentFound() {
        var topFragment = fragmentsArrayDeque.peekLast()
        do {
            if (FragmentsStack.allFoldersFragmentsEnumTypesSet.contains(topFragment)) {
                topFragment = fragmentsArrayDeque.removeLast()
                if (FragmentsStack.allFoldersFragmentsEnumTypesSet.contains(topFragment)) {
                    folderFragmentsCnt--
                    foldersData.removeMostRecentFolderData()
                }
                fragmentsTitlesArrayDeque.removeLast()                                                // remove the element at tail
            } else {
                break
            }
        } while ((topFragment == fragmentsArrayDeque.peekLast()) != null && fragmentsArrayDeque.size > 0)
    }

    private fun verify() {
        val localFolderFragmentsCnt = getFolderFragmentCount()
//        Log.v(TAG, "verify - localFolderFragmentsCnt/folderFragmentsCnt: $localFolderFragmentsCnt/$folderFragmentsCnt")
        if (localFolderFragmentsCnt != folderFragmentsCnt) {
            // todo: not in prod
            throw RuntimeException("verify - exception localFolderFragmentsCnt/folderFragmentsCnt: $localFolderFragmentsCnt/$folderFragmentsCnt")
        }

        if (foldersData.getCurrFolderLevel() !== folderFragmentsCnt - 1) {
            // todo: throw exception only in test
            throw RuntimeException("verify - exception currFolderLevel/folderFragmentsCnt: " + foldersData.getCurrFolderLevel() + "/" + folderFragmentsCnt)
        }
    }

    override fun toString(): String {
        return fragmentsArrayDeque.toString() + " - " + fragmentsTitlesArrayDeque.toString()
    }

    // - - - - - - - - helper for unit testing - - - - - -

    fun getStackSize(): Int {
        return fragmentsArrayDeque.size
    }

    fun getFragmentsList(): Array<HomeActivity.FragmentsEnum> {
        return fragmentsArrayDeque.toTypedArray<HomeActivity.FragmentsEnum>()
    }
}

data class FragmentsStackResponse(
        val finishRequired: Boolean,
        val fragmentToSet: HomeActivity.FragmentsEnum,
        val titleToSet: String?,
        val updateFolderListAdapterRequired: Boolean,
        val viewFragmentsCleanupRequired: Boolean,
        val menuOptionsChangeRequired: Boolean)