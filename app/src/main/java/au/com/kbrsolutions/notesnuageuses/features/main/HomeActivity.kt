package au.com.kbrsolutions.notesnuageuses.features.main

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import au.com.kbrsolutions.notesnuageuses.features.core.FileMetadataInfo
import au.com.kbrsolutions.notesnuageuses.features.core.FolderData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack.addFragment
import au.com.kbrsolutions.notesnuageuses.features.events.FilesEvents
import au.com.kbrsolutions.notesnuageuses.features.events.FoldersEvents
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import au.com.kbrsolutions.notesnuageuses.features.main.dialogs.CreateFileDialog
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.*
import au.com.kbrsolutions.notesnuageuses.features.tasks.CreateDriveFolderTask
import au.com.kbrsolutions.notesnuageuses.features.tasks.DownloadFolderInfoTask
import au.com.kbrsolutions.notesnuageuses.features.tasks.GetFileFromDriveTask
import au.com.kbrsolutions.notesnuageuses.features.tasks.SendFileToDriveTask
import com.google.android.gms.drive.DriveId
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

class HomeActivity : BaseActivity(),
        EmptyFolderFragment.OnEmptyFolderFragmentInteractionListener,
        FolderFragment.OnFolderFragmentInteractionListener,
        CreateFileDialog.OnCreateFileDialogInteractionListener,
        TextFragment.OnTextFragmentInteractionListener,
        FileFragment.OnFileFragmentInteractionListener {

    private lateinit var eventBus: EventBus
    private var mToolbar: Toolbar? = null
    private val mTestMode: Boolean = false
    private var handleCancellableFuturesCallable: HandleCancellableFuturesCallable? = null
    private var mCancellableFuture: Future<String>? = null
    private var handleNonCancellableFuturesCallable: HandleNonCancellableFuturesCallable? = null
    private var mNonCancellableFuture: Future<String>? = null
    private var mExecutorService: ExecutorService? = null
    private var showTrashedFiles: Boolean = false
    private var newFragmentSet: Boolean = false
    private var currFragment: FragmentsEnum? = null
    private var isAppFinishing = false
    private var mTitle: CharSequence? = null

    private var emptyFolderFragment: EmptyFolderFragment? = null
    private var folderFragment: FolderFragment? = null
    private var downloadFragment: DownloadFragment? = null
    private var folderArrayAdapter: FolderArrayAdapter<FolderItem>? = null
    private var textFragment: TextFragment? = null
    private var fileFragment: FileFragment? = null

    companion object {
        private val TAG = HomeActivity::class.java.simpleName

        const val EMPTY_FOLDER_TAG = "empty_folder_tag"
        const val TEXT_FRAGMENT_TAG = "text_fragment_tag"
        const val FOLDER_TAG = "folder_tag"
        const val RETRIEVE_FOLDER_PROGRESS_TAG = "retrieve_folder_progress_tag"
        const val RETRIEVING_FOLDER_TITLE_KEY = "retrieving_folder_title_key"
        const val FILE_NAME_KEY = "file_name_key"
    }

    private var fragmentsStack = FragmentsStack

    init {
        fragmentsStack.initialize(mTestMode)
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
        DOWNLOAD_FRAGMENT,
        ROBOTIUM_TEST,
        LEGAL_NOTICES,
        SETTINGS_FRAGMENT
    }

    enum class FragmentsCallingSourceEnum {
        NAVIGATION_DRAWER,
        REMOVE_TOP_FRAGMENT,

        UPDATE_FOLDER_LIST_ADAPTER,
        ON_ACTIVITY_RESULTS,
        ON_EVENT_MAIN_THREAD,
        LEGAL_NOTICES,

        ACTIVITY_NOT_FRAGMENT
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        Log.v("HomeActivity", "onCreate - item.itemId: ${item.itemId} ")

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

        // https://freakycoder.com/android-notes-24-how-to-add-back-button-at-toolbar-941e6577418e
//        mToolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.v("HomeActivity", "onSupportNavigateUp - onCreate start ")
        onBackPressed()
        return true
    }

    override fun onDriveClientReady() {
        startFuturesHandlers()
        val folderFragmentsCnt = fragmentsStack.getFolderFragmentCount()
        if (folderFragmentsCnt == 0 || foldersData.getCurrFolderLevel() != folderFragmentsCnt - 1) {
            fragmentsStack.initialize(mTestMode)
            val rootFolderName = getString(R.string.app_root_folder_name)
            val args = Bundle()
            args.putString(RETRIEVING_FOLDER_TITLE_KEY, rootFolderName)

            setFragment(
                    FragmentsEnum.DOWNLOAD_FRAGMENT,
                    rootFolderName,
                    true,
                    FragmentsCallingSourceEnum.ACTIVITY_NOT_FRAGMENT,
                    null,
                    args)

            handleCancellableFuturesCallable!!.submitCallable(DownloadFolderInfoTask.Builder()
                    .context(this)
                    .eventBus(eventBus)
                    .driveResourceClient(mDriveResourceClient)
                    .selectedFolderTitle(rootFolderName)
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
        if (folderData.isEmptyOrAllFilesTrashed && !showTrashedFiles) {
            setFragment(
                    FragmentsEnum.EMPTY_FOLDER_FRAGMENT,
                    folderData.newFolderTitle,
                    true,
                    FragmentsCallingSourceEnum.UPDATE_FOLDER_LIST_ADAPTER,
                    folderData,
                    null)
        } else {
            setFragment(
                    FragmentsEnum.FOLDER_FRAGMENT,
                    folderData.newFolderTitle,
                    true,
                    FragmentsCallingSourceEnum.UPDATE_FOLDER_LIST_ADAPTER,
                    folderData,
                    null)
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

            FragmentsEnum.EMPTY_FOLDER_FRAGMENT -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: -1
                if (emptyFolderFragment == null) {
                    emptyFolderFragment = EmptyFolderFragment.newInstance(trashFilesCnt)
                } else {
                    emptyFolderFragment!!.setTrashedFilesCnt(trashFilesCnt)
                }
                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, emptyFolderFragment, EMPTY_FOLDER_TAG)
                fragmentTransaction.commit()
            }

            FragmentsEnum.TEXT_VIEW_FRAGMENT -> {
                val fileName = fragmentArgs!!.getString(FILE_NAME_KEY)
//                if (textFragment == null) {
//                    textFragment =
//                            TextFragment.newInstance(fileName)
//                } else {
//                    textFragment!!.setFileName(fileName)
//                }
                if (fileFragment == null) {
                    fileFragment =
                            FileFragment.newInstance(fileName)
                } else {
                    fileFragment!!.setFileName(fileName)
                }

                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, fileFragment, TEXT_FRAGMENT_TAG)
                fragmentTransaction.commit()
            }

            FragmentsEnum.DOWNLOAD_FRAGMENT -> {
                val retrievingFolderName = fragmentArgs!!.getString(RETRIEVING_FOLDER_TITLE_KEY)
                if (downloadFragment == null) {
                    downloadFragment =
                            DownloadFragment.newInstance(retrievingFolderName)
                } else {
                    downloadFragment!!.setRetrievingFolderName(retrievingFolderName)
                }

                fragmentManager
                        .beginTransaction()
                        .replace(
                                R.id.fragments_frame,
                                downloadFragment,
                                RETRIEVE_FOLDER_PROGRESS_TAG)
                        .commit()
            }

            FragmentsEnum.FOLDER_FRAGMENT -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: -1
                val folderItemsList = ArrayList<FolderItem>()

                val list: ArrayList<FileMetadataInfo>? = foldersAddData?.filesMetadatasInfo
                        ?: foldersData.getCurrFolderMetadataInfo()

                for ((itemIdxInList, folderMetadataInfo) in list!!.withIndex()) {
                    if (!folderMetadataInfo.isTrashed || folderMetadataInfo.isTrashed && showTrashedFiles) {
                        folderItemsList.add(FolderItem(
                                folderMetadataInfo.fileTitle,
                                folderMetadataInfo.updateDt,
                                folderMetadataInfo.mimeType,
                                folderMetadataInfo.isTrashed,
                                itemIdxInList))
                    }
                }

                if (folderFragment == null) {
                    folderFragment = FolderFragment.newInstance(trashFilesCnt)
                } else {
                    folderFragment!!.setTrashedFilesCnt(trashFilesCnt)
                }

                if (folderArrayAdapter == null) {
                    folderArrayAdapter = FolderArrayAdapter(this, folderFragment!!, folderItemsList)
                    folderFragment!!.listAdapter = folderArrayAdapter
                } else {
                    folderArrayAdapter!!.clear()
                        folderArrayAdapter!!.addAll(folderItemsList)
                    // fixLater: Aug 22, 2018 - folderArrayAdapter.notify() missing?
                }

                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, folderFragment, FOLDER_TAG)
                fragmentTransaction.commit()
            }

//            HomeActivity.FragmentsEnum.FILE_DETAILS_FRAGMENT -> fragmentManager.beginTransaction().replace(R.id.fragments_frame, fileDetailFragment).commit()

//            HomeActivity.FragmentsEnum.LEGAL_NOTICES -> fragmentManager.beginTransaction().replace(R.id.fragments_frame, legalNoticesFragment).commit()

            // fixLater: Aug 21, 2018 - show error message in release version
            else -> throw RuntimeException("$TAG - setFragment - no code to handle fragmentId: $fragmentId")
        }
        //		currFragment = fragmentId;
        if (addFragmentToStack) {
            addFragment(fragmentId, titleText, foldersAddData)
        }
        setCurrFragment(fragmentId)
        setNewFragmentSet(true)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onMessageEvent(event: ActivitiesEvents) {
////        val request = event.request
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FilesEvents) {
        val request = event.request
        val msgContents = event.msgContents
        Log.v(TAG, "onMessageEvent.FilesEvents - request: $request msgContents: $msgContents")

        when (request) {

            FilesEvents.Events.TEXT_UPLOADING -> {
                Log.v(TAG, "onMessageEvent.FilesEvents - request" +
                        " $request msgContents: $msgContents")
            }

            FilesEvents.Events.TEXT_UPLOADED -> {
                Log.v(TAG, "onMessageEvent.FilesEvents - request" +
                        " $request msgContents: $msgContents")
            }

            FilesEvents.Events.UPLOAD_PROBLEMS -> {
                Log.v(TAG, "onMessageEvent.FilesEvents - request" +
                        " $request msgContents: $msgContents")
            }

            FilesEvents.Events.FILE_DOWNLOADING -> {
                Log.v(TAG, "onMessageEvent.FilesEvents - request" +
                        " $request msgContents: $msgContents")
            }

            FilesEvents.Events.FILE_DOWNLOADED -> {
                textFragment!!.showDownloadedTextNote(
                        event.createDt,
                        event.fileName,
                        event.setSelectedFileDriveId,
                        event.textContents)
            }

            FilesEvents.Events.FILE_DOWNLOAD_PROBLEMS -> {
                Log.v(TAG, "onMessageEvent.FilesEvents - request" +
                        " $request msgContents: $msgContents")
            }

            else -> throw RuntimeException(
                    "$TAG - onMessageEvent.FilesEvents - no code to handle request: $request")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FoldersEvents) {
        val request = event.request
        val msgContents = event.msgContents
        Log.v(TAG, "onMessageEvent.FoldersEvents - request: $request")

        when (request) {

            FoldersEvents.Events.CREATE_FILE_DIALOG_CANCELLED -> {
                removeTopFragment(
                        "onMessageEvent.FoldersEvents-CREATE_FILE_DIALOG_CANCELLED",
                        true
                )
            }

            FoldersEvents.Events.FOLDER_DATA_RETRIEVED -> {
//                if (dismissRefreshProgressBarCallableRunnable != null) {
//                    handler.removeCallbacks(dismissRefreshProgressBarCallableRunnable)            // remove just in case if there is already one waiting in a queue
//                }
//                // fixme: why do I need the line below
//                removeExpiredFileLockInfoElementsRunnableRunScheduled = true
//                dismissRefreshProgressBarCallableRunnable = DismissRefreshProgressBarCallableRunnable()
//                handler.postDelayed(dismissRefreshProgressBarCallableRunnable, DISSMISS_REFRESH_PROGRESS_DELAY_MILLS)

                val folderData1 = event.foldersAddData
                val folderName = folderData1!!.newFolderTitle
                setActionBarTitle(folderName)

                if (folderData1 != null && folderData1.isEmptyOrAllFilesTrashed) {
                    setFolderFragment(folderData1) // empty folder
                    Log.v("HomeActivity", "onMessageEvent - fragmentsStack: $fragmentsStack ")
                } else {
                    retrievingAppFolderDriveInfoTaskDone(folderData1)
                }
            }

            FoldersEvents.Events.FOLDER_DATA_RETRIEVE_PROBLEM -> {
                Log.v("HomeActivity", "onMessageEvent - msgContents: $msgContents")
//                addMsgToActivityLogShowOnScreen(event.msgContents, true, true)
//                if (dismissRefreshProgressBarCallableRunnable != null) {
//                    handler.removeCallbacks(dismissRefreshProgressBarCallableRunnable)            // remove just in case if there is already one waiting in a queue
//                }
//                dismissRefreshProgressBarCallableRunnable = DismissRefreshProgressBarCallableRunnable()
//                handler.postDelayed(dismissRefreshProgressBarCallableRunnable, DISSMISS_REFRESH_PROGRESS_DELAY_MILLS)
            }

//            ActivitiesEvents.HomeEvents.SHOW_MESSAGE -> addMsgToActivityLogShowOnScreen(msgContents, true, true)

            FoldersEvents.Events.FOLDER_CREATED -> {
//				addMsgToActivityLogShowOnScreen("Folder created");
                val folderName = event.newFileName ?: "undefined"
                setActionBarTitle(folderName)
                val stackFragmentsAfterAdd = fragmentsStack.getFragmentsList()
                Log.v(TAG, " - actualStackFragmentsAfterAdd: ${printCollection("after fragments added", stackFragmentsAfterAdd)}")
                val currFolder = fragmentsStack . getCurrFragment ()
                if (currFolder == FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {    // folder is no longer empty
                    fragmentsStack.replaceCurrFragment (
                            "onMessageEvent FoldersEvents FOLDER_CREATED" +
                                    event.request,
                            currFolder, FragmentsEnum.FOLDER_FRAGMENT)

                    setFragment(
                            FragmentsEnum.FOLDER_FRAGMENT,
                            folderName,
                            false,
                            FragmentsCallingSourceEnum.ON_EVENT_MAIN_THREAD,
                            null,
                            null)
                } else {
                    updateFolderListAdapter()
                }
            }

            else -> throw RuntimeException(
                    "$TAG - onMessageEvent.FoldersEvents - no code to handle request: $request")
        }
    }

    // fixLater: Aug 28, 2018 - move logic to the onSupportNavigateUp
    override fun onBackPressed() {
        handleCancellableFuturesCallable!!.cancelCurrFuture()
        val currTitle = supportActionBar!!.title
        val finishRequired = removeTopFragment("onBackPressed", true)
        if (finishRequired) {
            super.onBackPressed()
        }
    }

    @Synchronized
    fun removeTopFragment(source: String, actionCancelled: Boolean): Boolean {
        Log.v("HomeActivity", """removeTopFragment - source: ${source} """)
        val fragmentsStackResponse = fragmentsStack.removeTopFragment(source, actionCancelled)
        if (fragmentsStackResponse == null) {
            isAppFinishing = true
            finish()
            return true
        }
//        if (fragmentsStackResponse.viewFragmentsCleanupRequired) {
//            if (textFragment != null) {
//                textFragment.cleanup("removeTopFragment")
//                //	A_MUST:		is it still a PROBLEM? : if keyboard was visible in text fragment, it is still visible after Cancel or Back button press
//                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
//            }
//            if (imageContentsFragment != null) {
//                imageContentsFragment.cleanup()
//            }
//        }
        if (fragmentsStackResponse.updateFolderListAdapterRequired) {
            updateFolderListAdapter()
        }
        val fragmentToSet = fragmentsStackResponse.fragmentToSet
        if (fragmentToSet != FragmentsEnum.NONE) {
            setFragment(
                    fragmentToSet,
                    fragmentsStackResponse.titleToSet!!,
                    false,
                    FragmentsCallingSourceEnum.REMOVE_TOP_FRAGMENT,
                    null,
                    null
            )
        }
        if (fragmentsStackResponse.menuOptionsChangeRequired) {
            invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
        }
        if (!fragmentsStackResponse.finishRequired) {
            mTitle = fragmentsStackResponse.titleToSet
            setActionBarTitle(mTitle!!, "removeTopFragment")
        }
        if (fragmentsStackResponse.finishRequired) {
            isAppFinishing = true
        }
        //		Log.i(TAG, "removeTopFragment end");
        return fragmentsStackResponse.finishRequired
    }

    private fun setActionBarTitle(title: CharSequence, source: String = "undefined") {
//        Log.v("HomeActivity", "setActionBarTitle - source: $source ")
        mTitle = title
        supportActionBar!!.title = title
    }

    private fun retrievingAppFolderDriveInfoTaskDone(folderData: FolderData?) {
        //        Log.v(TAG, "retrievingAppFolderDriveInfoTaskDone - folderData: " + folderData);
        if (folderData != null && folderData.newFolderData) {   // folder info not in FoldersData
            setFolderFragment(folderData)
        } else {
            updateFolderListAdapter()
        }
    }

    private fun updateFolderListAdapter() {
        val currFolderMetadataInfo = foldersData.getCurrFolderMetadataInfo()
        val folderItemsList = ArrayList<FolderItem>()
        for ((itemIdxInList, folderMetadataInfo) in currFolderMetadataInfo!!.withIndex()) {
            if (!folderMetadataInfo.isTrashed || folderMetadataInfo.isTrashed && showTrashedFiles) {
                folderItemsList.add(FolderItem(
                        folderMetadataInfo.fileTitle,
                        folderMetadataInfo.updateDt,
                        folderMetadataInfo.mimeType,
                        folderMetadataInfo.isTrashed,
                        itemIdxInList
                ))
            }
        }

        folderArrayAdapter!!.clear()
        folderArrayAdapter!!.addAll(folderItemsList)
    }

    private fun startFuturesHandlers() {
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
            menuItem.title = resources.getString(
                    R.string.menu_hide_trashed_files,
                    foldersData.getCurrentFolderTrashedFilesCnt())
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
            menuItem.title = resources.getString(
                    R.string.menu_show_trashed_files,
                    foldersData.getCurrentFolderTrashedFilesCnt())
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
            R.id.action_connect_to_google_drive ->
                sendTextFileToDrive(null,"test1.txt",
                        "Hello Drive".toByteArray(Charsets.UTF_8))
            R.id.menuQuickPhoto -> handleCameraOptionSelected()
            R.id.menuRefresh -> handleRefreshOptionSelected()
//            R.id.menuCreateFile -> handleCreateFileOptionSelected()
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

    override fun sendTextFileToDrive(
            existingFileDriveId: DriveId?,
            fileName: String,
            fileContents: ByteArray) {

        val sendTextToGoogleDriveCallable = SendFileToDriveTask.Builder()
                .context(applicationContext)
                .eventBus(eventBus)
                .driveResourceClient(mDriveResourceClient)
                .parentFolderLevel(foldersData.getCurrFolderLevel())
                .parentFolderDriveId(foldersData.getCurrFolderDriveId()!!)
                .existingFileDriveId(existingFileDriveId)
                .fileName(fileName)
                .replaceFile(false)
                .mimeType(MIME_TYPE_TEXT_FILE)
                .contents(fileContents)
                .foldersData(foldersData)
                .build()

        handleNonCancellableFuturesCallable!!.submitCallable(sendTextToGoogleDriveCallable)
    }

    override fun showFileDialog() {
        val dialog = CreateFileDialog.newInstance()
        dialog.show(fragmentManager, "dialog")
    }
    override fun createFolder(fileName: CharSequence) {
        handleNonCancellableFuturesCallable!!.submitCallable(
                CreateDriveFolderTask.Builder()
                        .activity(this)
                        .eventBus(eventBus)
                        .driveResourceClient(mDriveResourceClient)
                        .newFolderName(fileName.toString())
                        .parentFolderLevel(foldersData.getCurrFolderLevel())
                        .parentFolderDriveId(foldersData.getCurrFolderDriveId())
                        .foldersData(foldersData)
                        .build())
        /*

    public void processFolderName(String fileName) {
        eventBus.post(new ActivitiesEvents.Builder(ActivitiesEvents.HomeEvents.CREATE_FOLDER)
                .setFileName(fileName)
                .setCurrFolderLevel(foldersData.getCurrFolderLevel())
                .setCurrFolderDriveId(foldersData.getCurrFolderDriveId())
                .build());
    }
         */
    }

    override fun createPhotoNote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTextNote(fileName: CharSequence) {
        val args = Bundle()
        args.putString(FILE_NAME_KEY, fileName.toString())

        setFragment(
                FragmentsEnum.TEXT_VIEW_FRAGMENT,
                fileName.toString(),
                true,
                FragmentsCallingSourceEnum.ACTIVITY_NOT_FRAGMENT,
                null,
                args)


        setActionBarTitle(fileName)

//        sendTextFileToDrive(null, fileName.toString(),
//                "Hello Drive".toByteArray(Charsets.UTF_8))
    }

    /*
            for debug only
     */
    // fixLater: Aug 26, 2018 - remove when not needed
    private fun handleShowRootFolder() {
        fragmentsStack.initialize(mTestMode)
        onDriveClientReady()
    }

    override fun handleOnFolderOrFileClick(position: Int) {
        val idx = getIdxOfClickedFolderItem(position)
        val folderMetadataArrayInfo = foldersData.getCurrFolderMetadataInfo()
        val folderMetadataInfo: FileMetadataInfo = folderMetadataArrayInfo!![idx]
        if (folderMetadataInfo.isFolder) {
            startDownloadFolderInfo(folderMetadataInfo)
        } else {
            Log.v("HomeActivity", """handleOnFolderOrFileClick - position: $position """)
            startDownloadFileContents(folderMetadataInfo)
        }
    }

    private fun startDownloadFileContents(folderMetadataInfo: FileMetadataInfo) {
        Log.v("HomeActivity", """startDownloadFileContents -
            |folderMetadataInfo: ${folderMetadataInfo.fileTitle} """.trimMargin())

        val args = Bundle()
        val fileTitle = folderMetadataInfo.fileTitle
        args.putString(FILE_NAME_KEY, fileTitle)
        setFragment(
                FragmentsEnum.TEXT_VIEW_FRAGMENT,
                folderMetadataInfo.fileTitle,
                true,
                FragmentsCallingSourceEnum.ACTIVITY_NOT_FRAGMENT,
                null,
                args)

        handleCancellableFuturesCallable!!.submitCallable(GetFileFromDriveTask.Builder()
                .context(applicationContext)
                .eventBus(eventBus)
                .driveResourceClient(mDriveResourceClient)
                .selectedDriveId(folderMetadataInfo.fileDriveId)
                .fileName(fileTitle)
                .mimeType(folderMetadataInfo.mimeType)
                .build())
    }

    private fun startDownloadFolderInfo(folderMetadataInfo: FileMetadataInfo) {
        val currFolderLevel = foldersData.getCurrFolderLevel()
        val folderMetadataArrayInfo = foldersData.getCurrFolderMetadataInfo()
        val currFolderParentDriveId = foldersData.getCurrFolderDriveId()

//        val folderMetadataInfo: FileMetadataInfo = folderMetadataArrayInfo!![idx]
//        Log.v("HomeActivity", """
//            | startDownloadFolderInfo -
//            | folderMetadataInfo - fileDriveId: ${folderMetadataInfo.fileDriveId}
//            | parentDriveId: ${foldersData.getCurrParentDriveId(currFolderLevel)}
//            |""".trimMargin())
        val selectedDriveId = folderMetadataInfo.fileDriveId
        val selectedFileTitle = folderMetadataInfo.fileTitle
        if (folderMetadataInfo.isFolder) {
            val rootFolderName = folderMetadataInfo.fileTitle
            val args = Bundle()
            args.putString(RETRIEVING_FOLDER_TITLE_KEY, rootFolderName)

            setFragment(
                    FragmentsEnum.DOWNLOAD_FRAGMENT,
                    getString(R.string.retrieving_folder_title),
                    true,
                    FragmentsCallingSourceEnum.ACTIVITY_NOT_FRAGMENT,
                    null,
                    args)

            handleCancellableFuturesCallable!!.submitCallable(DownloadFolderInfoTask.Builder()
                    .context(applicationContext)
                    .eventBus(eventBus)
                    .driveResourceClient(mDriveResourceClient)
                    .selectedFolderTitle(selectedFileTitle)
                    .parentFolderLevel(currFolderLevel)
                    .selectedFolderDriveId(selectedDriveId)
                    .parentFolderDriveId(currFolderParentDriveId!!)
                    .foldersData(foldersData)
                    .build())
        }
    }
    override fun onUpButtonPressedInFragment() {
        onBackPressed()
    }

    /**
     * When 'showTrashed' files is false, the List<FolderItem> passed to the FolderArrayAdapter can
     * be shorter then the list of all files in the folder if there are some trashed files.
     * This method translates the index of the file item touched to its index of all files in
     * the folder.
     *
     * @param position - index of the file item touched on the folder screen
     * @return - index of the item in the folder's items list
    </FolderItem> */
    private fun getIdxOfClickedFolderItem(position: Int): Int {
//        val folderArrayAdapter = listAdapter as FolderArrayAdapter<*>
        return folderArrayAdapter!!.getFolderItem(position).itemIdxInList
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

    // fixLater: Aug 27, 2018 - used for debugging. Remove later
    private fun printCollection(msg: String, coll: Array<HomeActivity.FragmentsEnum>) {
        Log.i(TAG, "\nprintCollection $msg")
        coll.forEach { Log.i(TAG, it.toString()) }
        Log.i(TAG, "\nend")
    }
}
