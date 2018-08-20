package au.com.kbrsolutions.notesnuageuses.features.main

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack.addFragment
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack.removeTopFragment
import au.com.kbrsolutions.notesnuageuses.features.events.ActivitiesEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FoldersEvents
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.DownloadFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.EmptyFolderFragment
import au.com.kbrsolutions.notesnuageuses.features.tasks.RetrieveDriveFolderInfoTask
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

//          cd /Users/bohdan/AndroidStudioProjects/NotesNuageuses
//          git push -u origin
//          https://developers.google.com/drive/android/intro
//          https://developers.google.com/drive/android/examples/

class HomeActivity : BaseActivity() {

    private lateinit var eventBus: EventBus
    private val mTestMode: Boolean = false
    private var handleCancellableFuturesCallable: HandleCancellableFuturesCallable? = null
    private var mCancellableFuture: Future<String>? = null
    private var handleNonCancellableFuturesCallable: HandleNonCancellableFuturesCallable? = null
    private var mNonCancellableFuture: Future<String>? = null
    private var mExecutorService: ExecutorService? = null
    private var showTrashedFiles: Boolean = false
    private var newFragmentSet: Boolean = false
    private var currFragment: FragmentsEnum? = null

    private var emptyFolderFragment: EmptyFolderFragment? = null
    private var downloadFragment: DownloadFragment? = null
    private var folderArrayAdapter: FolderArrayAdapter<FolderItem>? = null

    companion object {
        private val TAG = HomeActivity::class.java.simpleName
        const val EMPTY_FOLDER_TAG = "empty_folder_tag"
        const val RETRIEVE_FOLDER_PROGRESS_TAG = "retrieve_folder_progress_tag"
    }

    var fragmentsStack = FragmentsStack

    init {
        fragmentsStack.initialize(mTestMode);
    }

    enum class FragmentsEnum {
        LOG_IN_FRAGMENT,
        ACTIVITY_LOG_FRAGMENT,
        FOLDER_FRAGMENT,
        TRASH_FOLDER_FRAGMENT,
        //		SAVE_FILE_OPTIONS,
        TEXT_VIEW_FRAGMENT,
        FILE_DETAILS_FRAGMENT,
        //		CREATE_FILE_FRAGMENT,
        IMAGE_VIEW_FRAGMENT,
        NONE,
        EMPTY_FOLDER_FRAGMENT,
        RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
        ROBOTIUM_TEST,
        LEGAL_NOTICES,
        SETTINGS_FRAGMENT
    }

    internal enum class FragmentsCallingSourceEnum {
        NAVIGATION_DRAWER,
        REMOVE_TOP_FRAGMENT,
        UPDATE_FOLDER_LIST_ADAPTER,
        ON_ACTIVITY_RESULTS,
        ON_EVENT_MAIN_THREAD,
        LEGAL_NOTICES,

        ACTIVITY_NOT_FRAGMENT
    }

    private enum class TouchedObject {
        MENUE_QUICK_PHOTO, MENUE_CREATE_FILE, MENUE_REFRESH, FILE_OR_FOLDER
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                tabTitleId.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                tabTitleId.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                tabTitleId.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        eventBus = EventBus.getDefault()
        eventBus.register(this)

        mExecutorService = Executors.newCachedThreadPool()
        Log.v(TAG, "onCreate end   - : ")
    }

    override fun onDriveClientReady() {
        Log.v(TAG, "onDriveClientReady start - : ")
        startFuturesHandlers("onDriveClientReady")
        val folderFragmentsCnt = fragmentsStack.getFolderFragmentCount()
        if (folderFragmentsCnt == 0 || (foldersData.getCurrFolderLevel() !== folderFragmentsCnt - 1)) {
            fragmentsStack.initialize(mTestMode)
//			isNotConnectedToGoogleDrive("onConnected");
            val rootFolderName = getString(R.string.app_root_folder_name)
            val args = Bundle()
            args.putString(
                    getString(R.string.retrieving_folder_title_key),
                    rootFolderName
            )

            setFragment(
                    FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
                    rootFolderName,
                    true,
                    FragmentsCallingSourceEnum.ACTIVITY_NOT_FRAGMENT,
                    null,
                    args)

            handleCancellableFuturesCallable!!.submitCallable(RetrieveDriveFolderInfoTask.Builder()
                    .activity(this)
                    .eventBus(eventBus)
                    .driveResourceClient(mDriveResourceClient)
                    .selectedFolderTitle(getString(R.string.app_root_folder_name))
                    .parentFolderLevel(-1)
                    .foldersData(foldersData)
                    .build())
        }
    }

    /**
     *
     * This method is called after the details of the folder are retrieved from the Google Drive.
     * The details are stored in the 'folderData'.
     *
     * At the time of the call, the FoldersData and FragmentsStack have details of the previous
     * successfully retrieved folder.
     *
     * The contents of the 'folderData' will be added to the FoldersData and FragmentsStack at the
     * end of the setFolder() method.
     *
     * For example, if the 'foldersData' contains details of the root folder, there are no folders
     * details in both the FoldersData and FragmentsStack.
     * The call to foldersData.getCurrFolderLevel() would return -1.
     *
     * It means, you have to inspect the details of the 'folderData' to figure out which folder to
     * show - Folder or Empty Folder.
     *
     * if there are no files in the 'folderData.filesMetadatasInfo' or all files are trashed, show
     * Empty Folder.
     *
     */
    private fun setFolderFragment(folderData: FolderData) {
        Log.v(TAG, "setFolderFragment - folderData: $folderData")
        if (folderData.filesMetadatasInfo.size == 0 || folderData.filesMetadatasInfo.size == folderData.trashedFilesCnt && !showTrashedFiles) {
//            if (emptyFolderFragment == null) {
//                emptyFolderFragment = EmptyFolderFragment()
//            }
//            emptyFolderFragment!!.setTrashedFilesCnt(folderData.trashedFilesCnt)
            setFragment(
                    FragmentsEnum.EMPTY_FOLDER_FRAGMENT,
                    folderData.newFolderTitle,
                    true,
                    FragmentsCallingSourceEnum.UPDATE_FOLDER_LIST_ADAPTER,
                    folderData, null)
        } else {
            // TODO: deal with non empty folder later
//            if (folderFragment == null) {
//                folderFragment = FolderFragment()
//            }
//            folderFragment.setTrashedFilesCnt(folderData.trashedFilesCnt)
//            setFragment(FragmentsEnum.FOLDER_FRAGMENT, folderData.newFolderTitle, true, FragmentsCallingSourceEnum.UPDATE_FOLDER_LIST_ADAPTER, folderData)
        }
    }

    private fun setFragment(
            fragmentId: FragmentsEnum,
            titleText: String,
            addFragmentToStack: Boolean,
            callingSource: FragmentsCallingSourceEnum,
            foldersAddData: FolderData?,
            fragmentArgs: Bundle?) {
        val fragmentManager = fragmentManager
        val fragmentTransaction: FragmentTransaction
//        Log.v(TAG, "setFragment - callingSource/titleText/foldersAddData: " + callingSource + "/" + titleText + "/" +
//                (foldersAddData?.newFolderTitle ?: "null"))
        when (fragmentId) {

            HomeActivity.FragmentsEnum.EMPTY_FOLDER_FRAGMENT -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: -1
                Log.v("HomeActivity", "setFragment - emptyFolderFragment: ${emptyFolderFragment} ")
                if (emptyFolderFragment == null) {
                    emptyFolderFragment =
//                            EmptyFolderFragment.newInstance(trashFilesCnt)
                            EmptyFolderFragment.newInstance(33)
                } else {
                    emptyFolderFragment!!.setTrashedFilesCnt(trashFilesCnt)
                }
                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, emptyFolderFragment, EMPTY_FOLDER_TAG)
                fragmentTransaction.commit()
            }

            HomeActivity.FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT -> {
                if (downloadFragment == null) {
                    downloadFragment =
                            DownloadFragment
                                    .newInstance(
                                            fragmentArgs!!.getString(
                                                    getString(R.string.retrieving_folder_title_key),
                                                    getString(R.string.retrieving_folder_default_title)))
                } else {
                    downloadFragment!!.setRetrievingFolderName(
                            fragmentArgs!!.getString(
                                    getString(R.string.retrieving_folder_title_key),
                                    getString(R.string.retrieving_folder_default_title)))
                }

                fragmentManager
                        .beginTransaction()
                        .replace(
                                R.id.fragments_frame,
                                downloadFragment,
                                RETRIEVE_FOLDER_PROGRESS_TAG)
                        .commit()
            }

//            HomeActivity.FragmentsEnum.FILE_DETAILS_FRAGMENT -> fragmentManager.beginTransaction().replace(R.id.fragments_frame, fileDetailFragment).commit()

//            HomeActivity.FragmentsEnum.LEGAL_NOTICES -> fragmentManager.beginTransaction().replace(R.id.fragments_frame, legalNoticesFragment).commit()

            else -> if (!mTestMode) {
                throw RuntimeException("$TAG - setFragment - no code to handle fragmentId: $fragmentId")
            }
        }
        //		currFragment = fragmentId;
        if (addFragmentToStack) {
            addFragment(fragmentId, titleText, foldersAddData)
        }
        setCurrFragment(fragmentId)
        setNewFragmentSet(true)
        //        Log.i(TAG, "@#setFragment - end - getFolderFragmentCount/addFragmentToStack/fragmentsArrayDeque: " + fragmentsStack.getFolderFragmentCount() + "/" + addFragmentToStack + "/" + fragmentsStack.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ActivitiesEvents) {
//        val request = event.request
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FoldersEvents) {
        val request = event.request
//        val msg = event.msgContents
        var actionCancelled = false
//        val fragmentsEnum: HomeActivity.FragmentsEnum
//        var logMsg: String
        Log.v(TAG, "onMessageEvent - request: $request")
        when (request) {

            ActivitiesEvents.HomeEvents.CREATE_FILE_DIALOG_CANCELLED -> {
                actionCancelled = true
                removeTopFragment("onEventMainThread-CREATE_FILE_DIALOG_CANCELLED", actionCancelled)
            }

            ActivitiesEvents.HomeEvents.FOLDER_DATA_RETRIEVED -> {
//                if (dismissRefreshProgressBarCallableRunnable != null) {
//                    handler.removeCallbacks(dismissRefreshProgressBarCallableRunnable)            // remove just in case if there is already one waiting in a queue
//                }
//                // fixme: why do I need the line below
//                removeExpiredFileLockInfoElementsRunnableRunScheduled = true
//                dismissRefreshProgressBarCallableRunnable = DismissRefreshProgressBarCallableRunnable()
//                handler.postDelayed(dismissRefreshProgressBarCallableRunnable, DISSMISS_REFRESH_PROGRESS_DELAY_MILLS)
                val folderData1 = event.foldersAddData
//                Log.v(TAG, "onMessageEvent - newFolderTitle/filesMetadatasInfo.size(): ${if (folderData1 == null) "null" else folderData1!!.newFolderTitle + "/" + folderData1!!.filesMetadatasInfo.size}")
                if (folderData1 != null && folderData1.filesMetadatasInfo.size == 0) {
                    setFolderFragment(folderData1) // empty folder
                    Log.v("HomeActivity", "onMessageEvent - fragmentsStack: ${fragmentsStack} ")
                } else {
                    retrievingAppFolderDriveInfoTaskDone(folderData1)
                }
            }

            ActivitiesEvents.HomeEvents.FOLDER_DATA_RETRIEVE_PROBLEM -> {
//                addMsgToActivityLogShowOnScreen(event.msgContents, true, true)
//                if (dismissRefreshProgressBarCallableRunnable != null) {
//                    handler.removeCallbacks(dismissRefreshProgressBarCallableRunnable)            // remove just in case if there is already one waiting in a queue
//                }
//                dismissRefreshProgressBarCallableRunnable = DismissRefreshProgressBarCallableRunnable()
//                handler.postDelayed(dismissRefreshProgressBarCallableRunnable, DISSMISS_REFRESH_PROGRESS_DELAY_MILLS)
            }

//            ActivitiesEvents.HomeEvents.SHOW_MESSAGE -> addMsgToActivityLogShowOnScreen(msgContents, true, true)

            else -> throw RuntimeException("TAG - onEventMainThread - no code to handle folderRequest: $request")
        }
    }

    private fun retrievingAppFolderDriveInfoTaskDone(folderData: FolderData?) {
        //        Log.v(TAG, "retrievingAppFolderDriveInfoTaskDone - folderData: " + folderData);
        if (folderData != null && folderData.newFolderData) {
            setFolderFragment(folderData)
        } else {
            updateFolderListAdapter(null)
        }
    }

    private fun updateFolderListAdapter(folderName: String?) {
        //        Log.i(TAG, "updateFolderListAdapter - start");
        //		List<String> folderFilesList = foldersData.getCurrFoldersFilesList();
        val currFolderMetadataInfo = foldersData.getCurrFolderMetadataInfo()
        val folderItemsList = ArrayList<FolderItem>()
        for ((itemIdxInList, folderMetadataInfo) in currFolderMetadataInfo!!.withIndex()) {
            if (!folderMetadataInfo.isTrashed || folderMetadataInfo.isTrashed && showTrashedFiles) {
                folderItemsList.add(FolderItem(folderMetadataInfo.fileTitle, folderMetadataInfo.updateDt, folderMetadataInfo.mimeType, folderMetadataInfo.isTrashed, itemIdxInList))
                //                Log.i(TAG, "updateFolderListAdapter - added - fileTitle: " + folderMetadataInfo.fileTitle);
            }
        }
        //        folderArrayAdapter = new FolderArrayAdapter<FolderItem>(this, getApplicationContext(), folderFragment, folderItemsList);
        //        folderFragment.setListAdapter(folderArrayAdapter);
        //        folderArrayAdapter.notifyDataSetChanged();

        folderArrayAdapter!!.clear()
        folderArrayAdapter!!.addAll(folderItemsList)
    }

    private fun startFuturesHandlers(source: String) {
        if (mCancellableFuture == null) {
            handleCancellableFuturesCallable = HandleCancellableFuturesCallable(mExecutorService!!)
            mCancellableFuture = mExecutorService!!.submit(handleCancellableFuturesCallable)
        }
        if (mNonCancellableFuture == null) {
            handleNonCancellableFuturesCallable = HandleNonCancellableFuturesCallable(mExecutorService!!)
            mNonCancellableFuture = mExecutorService!!.submit(handleNonCancellableFuturesCallable)
        }
    }

    private fun stopFuturesHandlers() {
        if (mCancellableFuture != null) {
            mCancellableFuture!!.cancel(true)
            mCancellableFuture = null
        }
        if (mNonCancellableFuture != null) {
            mNonCancellableFuture!!.cancel(true)
            mNonCancellableFuture = null
        }

    }

    @Synchronized
    private fun setNewFragmentSet(newFragmentSet: Boolean) {
        this.newFragmentSet = newFragmentSet
    }

    private fun setCurrFragment(currFragment: FragmentsEnum) {
        this.currFragment = currFragment
    }
//        fixme: method isFinishing() does not exist - it should be part of Activity
//    if (isFinishing()) {
        // A_MUST: any other adapters to clear?
//        actvityListAdapter.clear()
//        stopFuturesHandlers()
//        mExecutorService.shutdown()
//    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    //    todo: check below
    override fun onResume() {
        super.onResume()
//        Log.v(TAG, "onResume - mExecutorService: $mExecutorService")
//        isAppFinishing = false;
//        isInForeground = true;
//        startFuturesHandlers("onResume");
//        connectToGoogleDrive("onResume");
//        getPrefs().registerOnSharedPreferenceChangeListener(mToastingPrefListener);
    }

//    todo: check below
    override fun onDestroy() {
//        activityLogFragment.setListAdapter(null)
//        if (folderFragment != null) {
//            folderFragment.setListAdapter(null)
//        }
        stopFuturesHandlers()
        mExecutorService!!.shutdown()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        var menuItem: MenuItem
        if (!mTestMode) {
            menuItem = menu.findItem(R.id.action_resend_file)
            menuItem.isVisible = false
            menuItem.isEnabled = false
            menuItem = menu.findItem(R.id.action_show_root_folder)
            menuItem.isVisible = false
            menuItem.isEnabled = false
        } else if (fragmentsStack.getCurrFragment() !== FragmentsEnum.FOLDER_FRAGMENT && fragmentsStack.getCurrFragment() !== FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {
            menuItem = menu.findItem(R.id.menuShowTrashed)
            menuItem.isVisible = false
            menuItem.isEnabled = false
            menuItem = menu.findItem(R.id.menuHideTrashed)
            menuItem.isVisible = false
            menuItem.isEnabled = false
        } else if (showTrashedFiles) {
            menuItem = menu.findItem(R.id.menuHideTrashed)
            menuItem.isVisible = true
            menuItem.isEnabled = true
            //            menuItem.setTitle("show trashed files - " + foldersData.getCurrentFolderTrashedFilesCnt());
            menuItem.title = resources.getString(R.string.menu_hide_trashed_files, foldersData.getCurrentFolderTrashedFilesCnt())
            menuItem = menu.findItem(R.id.menuShowTrashed)
            menuItem.isVisible = false
            menuItem.isEnabled = false
        } else {
            menuItem = menu.findItem(R.id.menuHideTrashed)
            menuItem.isVisible = false
            menuItem.isEnabled = false
            menuItem = menu.findItem(R.id.menuShowTrashed)
            menuItem.isVisible = true
            menuItem.isEnabled = true
            //            menuItem.setTitle("hide trashed files - " + foldersData.getCurrentFolderTrashedFilesCnt());
            //				menuItem.setTitle("show trashed files - " + trashedFilesCnt);
            menuItem.title = resources.getString(R.string.menu_show_trashed_files, foldersData.getCurrentFolderTrashedFilesCnt())
        }
        menuItem = menu.findItem(R.id.action_show_root_folder)
        menuItem.isVisible = true
        menuItem.isEnabled = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event

        when (item.itemId) {
            R.id.menuQuickPhoto -> handleCameraOptionSelected()
            R.id.menuRefresh -> handleRefreshOptionSelected()
            R.id.menuCreateFile -> handleCreateFileOptionSelected()
//            R.id.activity_log_clearActivityLog -> actvityListAdapter.clear()
            R.id.action_settings -> handleSettings()
            R.id.action_about -> handleAbout()
            R.id.action_legal_notices -> handleLegalNotices()
            R.id.action_show_root_folder -> handleShowRootFolder()
//            R.id.action_resend_file -> {
//                val resendPhotoToGoogleDriveCallable = ResendFileToGoogleDriveCallable()
//                handleNonCancellableFuturesCallable.submitCallable(resendPhotoToGoogleDriveCallable)

            R.id.menuShowTrashed -> {
                showTrashedFiles = true
                handleMenuShowTrashed()
                invalidateOptionsMenu()
            }
            R.id.menuHideTrashed -> {
                showTrashedFiles = false
                //                if (filesMetadataInfo.size() != foldersData.getCurrentFolderTrashedFilesCnt()) {
                handleMenuHideTrashed()
                invalidateOptionsMenu()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun handleShowRootFolder() {
        fragmentsStack.initialize(mTestMode)
        onDriveClientReady()
    }

    private fun handleMenuHideTrashed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleMenuShowTrashed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return
    }

//    private fun ResendFileToGoogleDriveCallable(): Callable<String> {
//
//    }

    private fun handleLegalNotices() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleAbout() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleSettings() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleCreateFileOptionSelected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleRefreshOptionSelected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleCameraOptionSelected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
