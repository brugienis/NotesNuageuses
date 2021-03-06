package au.com.kbrsolutions.notesnuageuses.features.core

import android.util.Log
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity.FragmentsEnum
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity.FragmentsEnum.*
import java.util.*

object FragmentsStack {

    private var fragmentsArrayDeque = ArrayDeque<FragmentsEnum>()
    private var fragmentsTitlesArrayDeque = ArrayDeque<String>()
    private val foldersData = FoldersData
    private var folderFragmentsCnt = 0
    private var mTestMode: Boolean = false

    private var allFoldersFragmentsEnumTypesSet: MutableSet<FragmentsEnum>

    private var notEmptyFolderHomeActivity: MutableSet<FragmentsEnum>

    init {
        allFoldersFragmentsEnumTypesSet = mutableSetOf(FOLDER_FRAGMENT_NEW)

        notEmptyFolderHomeActivity = mutableSetOf(FOLDER_FRAGMENT_NEW)
    }

    private val TAG = "xyz" + FragmentsStack::class.java.simpleName

    @Synchronized
    fun initialize(mTestMode: Boolean) {
        foldersData.init()
        this.mTestMode = mTestMode
        fragmentsArrayDeque = ArrayDeque()
        fragmentsTitlesArrayDeque = ArrayDeque()
        folderFragmentsCnt = 0
    }

    /*
        return the last element on the queue. If the queue id empty, return NONE enum
     */
    fun getCurrFragment(): FragmentsEnum {
        val last = fragmentsArrayDeque.peekLast()
        return return last ?: NONE       // peek at the element at tail
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
    fun replaceCurrFragment(source: String, fragmentToReplace: FragmentsEnum, replacementFragment: FragmentsEnum): Boolean {
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
            fragmentId: FragmentsEnum,
            fragmentTitle: String,
            foldersAddData: FolderData?): Boolean {
//        Log.v(TAG, "addFragment - start - folderFragmentsCnt/foldersAddData.title/fragmentId: " + folderFragmentsCnt + "/" +
//                fragmentId + "/" + if (foldersAddData == null) "null" else foldersAddData!!.newFolderTitle)
        var lFragmentTitle = fragmentTitle
        var menuOptionsChangeRequired = false
        if (fragmentsArrayDeque.peekLast() !== fragmentId) {
            menuOptionsChangeRequired = true
        }
        if (allFoldersFragmentsEnumTypesSet.contains(fragmentId)) {
            foldersData.addFolderData(foldersAddData ?: throw RuntimeException(
                    "$TAG - addFragment - foldersAddData for fragmentId : $fragmentId can NOT be null"))
            lFragmentTitle = foldersAddData.newFolderTitle
            folderFragmentsCnt++
//            Log.v(TAG, "addFragment - fragmentId is in allFoldersMainActivity.FragmentsEnumTypesSet/folderFragmentsCnt: $fragmentId/$folderFragmentsCnt")
        }
//        else {
//            Log.v(TAG, "addFragment - fragmentId is NOT in allFoldersMainActivity.FragmentsEnumTypesSet/folderFragmentsCnt: $fragmentId/$folderFragmentsCnt")
//        }
        fragmentsArrayDeque.addLast(fragmentId)
        fragmentsTitlesArrayDeque.addLast(lFragmentTitle)           // add at the tail
//        Log.v(TAG, "addFragment - end   - folderFragmentsCnt/fragmentId: $folderFragmentsCnt/$fragmentId foldersData level: ${foldersData.getCurrFolderLevel()}")

        verify()
        return menuOptionsChangeRequired
    }

    private fun removeLastFragment(): FragmentsEnum {
            fragmentsTitlesArrayDeque.removeLast()
        return fragmentsArrayDeque.removeLast()
    }

    @Synchronized
    fun removeSpecificTopFragment(
            fragmentToBeRemoved: FragmentsEnum,
            source: String,
            actionCancelled: Boolean): FragmentsStackResponse? {
        Log.v(TAG, "removeSpecificTopFragment - start ---------------------------------------")
        val currTopFragmentJustPeeked = fragmentsArrayDeque.peekLast()  // peek at the element at tail
        if (currTopFragmentJustPeeked == fragmentToBeRemoved) {
            return removeTopFragment("removeSpecificTopFragment", actionCancelled)
        } else {
            return null
        }
    }

    @Synchronized
    fun removeTopFragment(source: String, actionCancelled: Boolean): FragmentsStackResponse {
        Log.v(TAG, "removeTopFragment - start ---------------------------------------")
        var fragmentToSet = NONE
        var updateFolderListAdapterRequired = false
        var viewFragmentsCleanupRequired = false
        var callFinishRequired = false
        var startingTopFragmentRemoved: FragmentsEnum? = null

//        if (fragmentsArrayDeque.size == 0) {
        when (fragmentsArrayDeque.size) {

            0 -> {
                if (mTestMode) {
                    callFinishRequired = true
                } else {
                    throw RuntimeException("$TAG - removeTopFragment - should never happen")
                }
            }

            1 -> {
                val topFragment = removeLastFragment()
                if (allFoldersFragmentsEnumTypesSet.contains(topFragment)) {
                    foldersData.removeMostRecentFolderData()
                }
                fragmentsArrayDeque.clear()
                fragmentsTitlesArrayDeque.clear()
                folderFragmentsCnt = 0
                callFinishRequired = true
            }

            else -> {
                Log.v("FragmentsStack", """removeTopFragment - before removeLastFragment()
                                |fragmentsTitlesArrayDeque last: ${fragmentsTitlesArrayDeque.peekLast()}
                                |""".trimMargin())
                startingTopFragmentRemoved = removeLastFragment()
                Log.v("FragmentsStack", """removeTopFragment - after  removeLastFragment()
                                |fragmentsTitlesArrayDeque last: ${fragmentsTitlesArrayDeque.peekLast()}
                                |""".trimMargin())

                val currTopFragmentJustPeeked = fragmentsArrayDeque.peekLast()  // peek at the element at tail

                when (startingTopFragmentRemoved) {

                    FOLDER_FRAGMENT_NEW -> {
                        folderFragmentsCnt--
                        foldersData.removeMostRecentFolderData()
//                        Log.v(TAG, " - folderFragmentsCnt: $folderFragmentsCnt folders data level: ${foldersData.getCurrFolderLevel()}")
                        removeFragmentsFromStackUntilSpecificFragmentFound()
                        if (fragmentsArrayDeque.size > 0) {                     // FOLDER_FRAGMENT found
                            updateFolderListAdapterRequired = true
                        } else {
                            callFinishRequired = true
                        }
                    }

                    /*EMPTY_FOLDER_FRAGMENT -> {
                        folderFragmentsCnt--
                        foldersData.removeMostRecentFolderData()
                        if (currTopFragmentJustPeeked === FOLDER_FRAGMENT_NEW) {    // impossible case
                            fragmentToSet = FOLDER_FRAGMENT_NEW
                            updateFolderListAdapterRequired = true
                        } else if (currTopFragmentJustPeeked === ACTIVITY_LOG_FRAGMENT) {
                            callFinishRequired = true
                        } else {
                            removeFragmentsFromStackUntilSpecificFragmentFound()
                            if (fragmentsArrayDeque.size > 0) {                            // FOLDER_FRAGMENT found
                                updateFolderListAdapterRequired = true
                                fragmentToSet = FOLDER_FRAGMENT_NEW
                            } else {
                                callFinishRequired = true
                            }
                        }
                    }*/

                    ACTIVITY_LOG_FRAGMENT ->
                        when {
                            currTopFragmentJustPeeked === FOLDER_FRAGMENT_NEW -> {
                                updateFolderListAdapterRequired = true
                                fragmentToSet = FOLDER_FRAGMENT_NEW
                            }

                            /*currTopFragmentJustPeeked === EMPTY_FOLDER_FRAGMENT -> {
                                updateFolderListAdapterRequired = true
                                fragmentToSet = EMPTY_FOLDER_FRAGMENT
                            }*/

                            fragmentsArrayDeque.size > 0 -> fragmentToSet = getCurrFragment()

                            else -> callFinishRequired = true
                        }

                    // TOP: will it also cover image fragment?
                    /*
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, EMPTY_FOLDER_FRAGMENT, CREATE_FILE_FRAGMENT, FILE_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,       CREATE_FILE_FRAGMENT, FILE_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,                           , FILE_FRAGMENT] existing note was opened and Cancel clicked
					 */
                    FILE_FRAGMENT -> {
                        viewFragmentsCleanupRequired = true
                        if (currTopFragmentJustPeeked == DOWNLOAD_FRAGMENT) {
                            removeLastFragment()            // remove placeholder DOWNLOAD_FRAGMENT
                            fragmentToSet = FOLDER_FRAGMENT_NEW
                        }
                        /*else if (currTopFragmentJustPeeked == EMPTY_FOLDER_FRAGMENT) {
                            if (actionCancelled) {
                                updateFolderListAdapterRequired = true
                                fragmentToSet = EMPTY_FOLDER_FRAGMENT
                            } else {                                            // Save was clicked
                                updateFolderListAdapterRequired = true
                                removeLastFragment()                            // remove EMPTY_FOLDER_FRAGMENT
                                fragmentsArrayDeque.addLast(FOLDER_FRAGMENT_NEW) // add FOLDER_FRAGMENT
                                fragmentToSet = FOLDER_FRAGMENT_NEW
                            }
                        } */
                        else if (currTopFragmentJustPeeked == FOLDER_FRAGMENT_NEW) {
                            fragmentToSet = FOLDER_FRAGMENT_NEW
                        } else {
                            updateFolderListAdapterRequired = true
                            fragmentToSet = FOLDER_FRAGMENT_NEW
                        }
                    }

                    // TOP: will it also cover image fragment?
                    /*
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, EMPTY_FOLDER_FRAGMENT, FILE_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,       FILE_FRAGMENT]
					 * or
					 *			[ACTIVITY_LOG_FRAGMENT, FOLDER_FRAGMENT, FOLDER_FRAGMENT,                           , FILE_FRAGMENT] existing note was opened and Cancel clicked
					 */
                    IMAGE_VIEW_FRAGMENT -> {
                        viewFragmentsCleanupRequired = true
                        run {
                            updateFolderListAdapterRequired = true
                            fragmentToSet = FOLDER_FRAGMENT_NEW
                        }
                    }

                    DOWNLOAD_FRAGMENT,
                    FILE_DETAILS_FRAGMENT,
                    LEGAL_NOTICES -> fragmentToSet = currTopFragmentJustPeeked

                    else -> {
                        if (mTestMode) {
                            throw RuntimeException("$TAG-FragmentStack - we should never be here - prevTopFragment/currTopFragment: $startingTopFragmentRemoved/$currTopFragmentJustPeeked")
                        }
                        when (currTopFragmentJustPeeked) {
//                            EMPTY_FOLDER_FRAGMENT, FOLDER_FRAGMENT_NEW -> fragmentToSet = currTopFragmentJustPeeked            // HomeActivity.FragmentsEnum.FOLDER_FRAGMENT;
                            FOLDER_FRAGMENT_NEW -> fragmentToSet = currTopFragmentJustPeeked            // HomeActivity.FragmentsEnum.FOLDER_FRAGMENT;

                            else -> if (currTopFragmentJustPeeked === FOLDER_FRAGMENT_NEW) { // ??? it would be handled in the case above - probably remove it
                                updateFolderListAdapterRequired = true
                            } else {
                                removeFragmentsFromStackUntilSpecificFragmentFound()
                                if (fragmentsArrayDeque.size > 0) {        // FOLDER_FRAGMENT found
                                    fragmentToSet = FOLDER_FRAGMENT_NEW
                                } else {
                                    callFinishRequired = true
                                }
                            }
                        }
                    }
                }
            }
        }

        val menuOptionsChangeRequired = startingTopFragmentRemoved !== fragmentToSet

        Log.v("FragmentsStack", """removeTopFragment -
            |fragmentsArrayDeque.size:       ${fragmentsArrayDeque.size}
            |fragmentsTitlesArrayDeque.size: ${fragmentsTitlesArrayDeque.size}
            |""".trimMargin())
        if (!callFinishRequired) {
            verify()
        }
        return FragmentsStackResponse(
                callFinishRequired,
                fragmentToSet,
                fragmentsTitlesArrayDeque.peekLast(),
                updateFolderListAdapterRequired,
                viewFragmentsCleanupRequired,
                menuOptionsChangeRequired)
    }

    private fun removeFragmentsFromStackUntilSpecificFragmentFound() {
        Log.v(TAG, "removeTopFragment - start ---------------------------------------")
        var topFragment = fragmentsArrayDeque.peekLast()
        Log.v(TAG, "remove until specific - at start - topFragment: $topFragment foldersData level: ${foldersData.getCurrFolderLevel()}")

        do {
            Log.v(TAG, "do iter - remove until specific - topFragment: $topFragment fragmentsArrayDeque size: ${fragmentsArrayDeque.size}");
            if (!FragmentsStack.allFoldersFragmentsEnumTypesSet.contains(topFragment)) {
                val removed = removeLastFragment()
                Log.v(TAG, "remove until specific REMOVED from fragmentsArrayDeque - topFragment: $removed")
                if (FragmentsStack.allFoldersFragmentsEnumTypesSet.contains(topFragment)) {
                    folderFragmentsCnt--
                    foldersData.removeMostRecentFolderData()
                    Log.v(TAG, "remove until specific REMOVED from foldersData - topFragment: $topFragment")
                }
                topFragment = fragmentsArrayDeque.peekLast()
            } else {
                Log.v(TAG, " - breaking");
                break
            }
        } while (topFragment != null && fragmentsArrayDeque.size > 0)
    }

    private fun verify() {
        val localFolderFragmentsCnt = getFolderFragmentCount()
        Log.v("FragmentsStack", "verify - this: ${this} ")
//        Log.v("FragmentsStack", "verify - currFolderLevel: ${foldersData.getCurrFolderLevel()} localFolderFragmentsCnt: $localFolderFragmentsCnt folderFragmentsCnt: $folderFragmentsCnt")
        if (localFolderFragmentsCnt != folderFragmentsCnt) {
            // todo: not in prod
            throw RuntimeException("verify - exception localFolderFragmentsCnt/folderFragmentsCnt: $localFolderFragmentsCnt/$folderFragmentsCnt")
        }

        if (foldersData.getCurrFolderLevel() != folderFragmentsCnt - 1) {
            // todo: throw exception only in test
            throw RuntimeException("verify - exception currFolderLevel/folderFragmentsCnt: " + foldersData.getCurrFolderLevel() + "/" + folderFragmentsCnt)
        }
    }

    override fun toString(): String {
        return fragmentsArrayDeque.toString() + " - " + "$fragmentsTitlesArrayDeque"
    }

    // - - - - - - - - helper for unit testing - - - - - -

    fun getStackSize(): Int {
        return fragmentsArrayDeque.size
    }

    fun getFragmentsList(): Array<FragmentsEnum> {
        return fragmentsArrayDeque.toTypedArray<FragmentsEnum>()
    }
}

data class FragmentsStackResponse(
        val finishRequired: Boolean,
        val fragmentToSet: FragmentsEnum,
        val titleToSet: String?,
        val updateFolderListAdapterRequired: Boolean,
        val viewFragmentsCleanupRequired: Boolean,
        val menuOptionsChangeRequired: Boolean)