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
import au.com.kbrsolutions.notesnuageuses.features.events.*
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import au.com.kbrsolutions.notesnuageuses.features.main.dialogs.CreateFileDialog
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.DownloadFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.EmptyFolderFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FileFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FolderFragment
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
        FileFragment.OnFileFragmentInteractionListener,
        EventBusEventsHandler.OnEventBusEventsHandlerInteractionListener {

    private lateinit var eventBus: EventBus
    private var mToolbar: Toolbar? = null
    private val mTestMode: Boolean = false
    private lateinit var handleCancellableFuturesCallable: HandleCancellableFuturesCallable
    private var mCancellableFuture: Future<String>? = null
    private lateinit var handleNonCancellableFuturesCallable: HandleNonCancellableFuturesCallable
    private var mNonCancellableFuture: Future<String>? = null
    private lateinit var mExecutorService: ExecutorService
    private var showTrashedFiles: Boolean = false
    private var newFragmentSet: Boolean = false
    private var currFragment: FragmentsEnum? = null
    private var isAppFinishing = false
    private var mTitle: CharSequence? = null

    private var emptyFolderFragment: EmptyFolderFragment? = null
    private var folderFragment: FolderFragment? = null
    private var downloadFragment: DownloadFragment? = null
    private var folderArrayAdapter: FolderArrayAdapter<FolderItem>? = null
    private var fileFragment: FileFragment? = null

    companion object {
        private val TAG = HomeActivity::class.java.simpleName

        const val EMPTY_FOLDER_TAG = "empty_folder_tag"
        const val FILE_FRAGMENT_TAG = "file_fragment_tag"
        const val FOLDER_TAG = "folder_tag"
        const val RETRIEVE_FOLDER_PROGRESS_TAG = "retrieve_folder_progress_tag"

        const val RETRIEVING_FOLDER_TITLE_KEY = "retrieving_folder_title_key"
        const val FILE_NAME_KEY = "file_name_key"
        const val THIS_FILE_DRIVE_ID_KEY = "this_file_drive_id_key"
        const val FILE_CONTENTS_KEY = "file_contents_key"
    }

    private var fragmentsStack = FragmentsStack
    private val eventBusListenable: EventBusListenable =
            EventBusEventsHandler(this, foldersData, fragmentsStack)

    init {
        fragmentsStack.initialize(mTestMode)
    }

    enum class FragmentsEnum {
        LOG_IN_FRAGMENT,
        ACTIVITY_LOG_FRAGMENT,
        FOLDER_FRAGMENT,
        TRASH_FOLDER_FRAGMENT,
        //		SAVE_FILE_OPTIONS,
        FILE_FRAGMENT,
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
                    null,
                    args)

            handleCancellableFuturesCallable.submitCallable(DownloadFolderInfoTask.Builder()
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
     * The fileContents of the 'folderData' will be added to the FoldersData and FragmentsStack at the
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
    override fun setFolderFragment(folderData: FolderData) {
        if (folderData.isEmptyOrAllFilesTrashed && !showTrashedFiles) {
            setFragment(
                    FragmentsEnum.EMPTY_FOLDER_FRAGMENT,
                    folderData.newFolderTitle,
                    true,
                    folderData,
                    null)
        } else {
            setFragment(
                    FragmentsEnum.FOLDER_FRAGMENT,
                    folderData.newFolderTitle,
                    true,
                    folderData,
                    null)
        }
    }

    override fun setFragment(
            fragmentId: FragmentsEnum,
            titleText: String,
            addFragmentToStack: Boolean,
            foldersAddData: FolderData?,
            fragmentArgs: Bundle?) {
        val fragmentManager = fragmentManager
        val fragmentTransaction: FragmentTransaction

        when (fragmentId) {

            FragmentsEnum.EMPTY_FOLDER_FRAGMENT -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: -1
                if (emptyFolderFragment == null) {
                    emptyFolderFragment = EmptyFolderFragment.newInstance(trashFilesCnt)
                } else {
                    emptyFolderFragment!!.setTrashedFilesCnt(trashFilesCnt)
                }
                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, emptyFolderFragment,
                        EMPTY_FOLDER_TAG)
                fragmentTransaction.commit()
            }

            FragmentsEnum.FILE_FRAGMENT -> {
                val fileName = fragmentArgs!!.getString(FILE_NAME_KEY)
                val fileContents = fragmentArgs.getString(FILE_CONTENTS_KEY)
                val thisFileDriveId = if (fragmentArgs.containsKey(THIS_FILE_DRIVE_ID_KEY)) {
                    DriveId.decodeFromString(
                            fragmentArgs.getString(THIS_FILE_DRIVE_ID_KEY))
                } else {
                    null
                }
                if (fileFragment == null) {
                    fileFragment =
                            FileFragment.newInstance(fileName, fileContents, thisFileDriveId)
                } else {
                    fileFragment!!.setFileDetails(fileName, fileContents, thisFileDriveId)
                }

                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, fileFragment, FILE_FRAGMENT_TAG)
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

        if (addFragmentToStack) {
            addFragment(fragmentId, titleText, foldersAddData)
        }
        setCurrFragment(fragmentId)
        setNewFragmentSet(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: DriveAccessEvents) {
        eventBusListenable.onMessageEvent(event)
        /*
        val request = event.request
        val msgContents = event.msgContents
//        val isProblem = event.isProblem

        when (request) {

            DriveAccessEvents.Events.MESSAGE -> {
                showMessage(msgContents)
            }
        }
        */
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FilesDownloadEvents) {
        eventBusListenable.onMessageEvent(event)
        /*
        val request = event.request
        val msgContents = event.msgContents
        Log.v(TAG, "onMessageEvent.FilesDownloadEvents - request: $request " +
                "msgContents: $msgContents")

        when (request) {

            FilesDownloadEvents.Events.FILE_DOWNLOADED -> {

                val fileTitle = event.fileName
                val fileContents = event.textContents
                val thisFileDriveId = event.downloadedFileDriveId.encodeToString()
                val args = Bundle()
                args.putString(FILE_NAME_KEY, fileTitle)
                args.putString(FILE_CONTENTS_KEY, fileContents)
                args.putString(THIS_FILE_DRIVE_ID_KEY, thisFileDriveId)

                setFragment(
                        FragmentsEnum.FILE_FRAGMENT,
                        event.fileName,
                        true,
                        null,
                        args)
            }

            else -> throw RuntimeException(
                    "$TAG - onMessageEvent.FilesDownloadEvents - no code to handle " +
                            "request: $request")
        }
        */
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FilesUploadEvents) {
        eventBusListenable.onMessageEvent(event)

        /*
        val request = event.request
        val msgContents = event.msgContents
        Log.v(TAG, "onMessageEvent.FilesUploadEvents - request: $request " +
                "msgContents: $msgContents")

        when (request) {

            FilesUploadEvents.Events.TEXT_UPLOADING -> {

                if (event.thisFileDriveId == null) {            // no file DriveId - we are going to upload a new file.
                                                                // We will know the DriveId after successful send
                    foldersData.insertFolderItemView(
                            event.fileItemId,
                            event.folderLevel,
                            event.currFolderDriveId,
                            0,
                            FileMetadataInfo(
                                    event.parentFileName,
                                    event.fileName +
                                            getString(R.string.home_app_file_encrypting, " "),
                                    event.thisFileDriveId,
                                    false,
                                    event.mimeType,
                                    event.createDt,
                                    event.updateDt,
                                    event.fileItemId,
                                    true,
                                    false))
                } else {
                    foldersData.updateFolderItemView(
                            event.fileItemId,
                            event.folderLevel,
                            event.currFolderDriveId,
                            FileMetadataInfo(
                                    event.parentFileName,
                                    event.fileName +
                                            getString(R.string.home_app_file_uploading,
                                                    " "),
                                    event.thisFileDriveId,
                                    false,
                                    event.mimeType,
                                    event.createDt,
                                    event.updateDt,
                                    event.fileItemId,
                                    true,
                                    false)
                    )
                }
                removeTopFragment("onEventMainThread - $request", false)
            }

            FilesUploadEvents.Events.TEXT_UPLOADED -> {

                showMessage(event.msgContents)

                showMessage(msgContents)
                foldersData.updateFolderItemView(
                        event.fileItemId,
                        event.folderLevel,
                        event.currFolderDriveId,
                        FileMetadataInfo(
                                event.parentFileName,
                                event.fileName,
                                event.thisFileDriveId ,
                                false,
                                event.mimeType,
                                event.createDt,
                                event.updateDt,
                                event.fileItemId,
                                true,
                                false
                        )
                )

                updateFolderListAdapter()
            }

            FilesUploadEvents.Events.UPLOAD_PROBLEMS -> {
                Log.v(TAG, "onMessageEvent.FilesUploadEvents - request" +
                        " $request msgContents: $msgContents")
            }

            else -> throw RuntimeException(
                    "$TAG - onMessageEvent.FilesUploadEvents - no code to handle request: $request")
        }
        */
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FoldersEvents) {
        eventBusListenable.onMessageEvent(event)

        /*
        val request = event.request
        val msgContents = event.msgContents
        Log.v(TAG, "onMessageEvent.FoldersEvents - request: $request")

        when (request) {

            // fixLater: Sep 09, 2018 - below not tested
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
                            null,
                            null)
                } else {
                    updateFolderListAdapter()
                }
            }

            else -> throw RuntimeException(
                    "$TAG - onMessageEvent.FoldersEvents - no code to handle request: $request")
        }
        */
    }

    // fixLater: Aug 28, 2018 - move logic to the onSupportNavigateUp
    override fun onBackPressed() {
        handleCancellableFuturesCallable.cancelCurrFuture()
        val currTitle = supportActionBar!!.title
        val finishRequired = removeTopFragment("onBackPressed", true)
        if (finishRequired) {
            super.onBackPressed()
        }
    }

    @Synchronized
    override fun removeTopFragment(source: String, actionCancelled: Boolean): Boolean {
        Log.v("HomeActivity", """removeTopFragment - source: $source """)
        val fragmentsStackResponse = fragmentsStack.removeTopFragment(source, actionCancelled)
        if (fragmentsStackResponse == null) {
            isAppFinishing = true
            finish()
            return true
        }

        if (fragmentsStackResponse.updateFolderListAdapterRequired) {
            updateFolderListAdapter()
        }
        val fragmentToSet = fragmentsStackResponse.fragmentToSet
        if (fragmentToSet != FragmentsEnum.NONE) {
            setFragment(
                    fragmentToSet,
                    fragmentsStackResponse.titleToSet!!,
                    false,
                    null,
                    null
            )
        }
        if (fragmentsStackResponse.menuOptionsChangeRequired) {
            invalidateOptionsMenu() // forces call to onPrepareOptionsMenu()
        }
        if (!fragmentsStackResponse.finishRequired) {
            mTitle = fragmentsStackResponse.titleToSet
            setActionBarTitle(mTitle!!)
        }
        if (fragmentsStackResponse.finishRequired) {
            isAppFinishing = true
        }
        //		Log.i(TAG, "removeTopFragment end");
        return fragmentsStackResponse.finishRequired
    }

    override fun setActionBarTitle(title: CharSequence) {
        mTitle = title
        supportActionBar!!.title = title
    }

    private fun retrievingAppFolderDriveInfoTaskDone(folderData: FolderData?) {
        if (folderData != null && folderData.newFolderData) {   // folder info not in FoldersData
            setFolderFragment(folderData)
        } else {
            updateFolderListAdapter()
        }
    }

    override fun updateFolderListAdapter() {
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
            handleCancellableFuturesCallable = HandleCancellableFuturesCallable(mExecutorService)
            mCancellableFuture = mExecutorService.submit(handleCancellableFuturesCallable)
        }
        if (mNonCancellableFuture == null) {
            handleNonCancellableFuturesCallable = HandleNonCancellableFuturesCallable(mExecutorService)
            mNonCancellableFuture = mExecutorService.submit(handleNonCancellableFuturesCallable)
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
        val currFolderLevel = foldersData.getCurrFolderLevel()

        val sendTextToGoogleDriveCallable = SendFileToDriveTask.Builder()
                .context(applicationContext)
                .eventBus(eventBus)
                .driveResourceClient(mDriveResourceClient)
                .parentFolderLevel(currFolderLevel)
                .parentFolderDriveId(foldersData.getCurrFolderDriveId()!!)
                .existingFileDriveId(existingFileDriveId)
                .fileName(fileName)
                .mimeType(MIME_TYPE_TEXT_FILE)
                .contents(fileContents)
                .parentFileName(foldersData.getFolderTitle(currFolderLevel)!!)
                .build()

        handleNonCancellableFuturesCallable.submitCallable(sendTextToGoogleDriveCallable)
    }

    override fun showFileDialog() {
        val dialog = CreateFileDialog.newInstance()
        dialog.show(fragmentManager, "dialog")
    }
    override fun createFolder(fileName: CharSequence) {
        handleNonCancellableFuturesCallable.submitCallable(
                CreateDriveFolderTask.Builder()
                        .activity(this)
                        .eventBus(eventBus)
                        .driveResourceClient(mDriveResourceClient)
                        .newFolderName(fileName.toString())
                        .parentFolderLevel(foldersData.getCurrFolderLevel())
                        .parentFolderDriveId(foldersData.getCurrFolderDriveId())
                        .foldersData(foldersData)
                        .build())
    }

    override fun createPhotoNote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTextNote(fileName: CharSequence) {
        val args = Bundle()
        args.putString(FILE_NAME_KEY, fileName.toString())
        args.putString(FILE_CONTENTS_KEY, "")

        setFragment(
                FragmentsEnum.FILE_FRAGMENT,
                fileName.toString(),
                true,
                null,
                args)

        setActionBarTitle(fileName)
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
            startDownloadFileContents(folderMetadataInfo)
        }
    }

    private fun startDownloadFileContents(folderMetadataInfo: FileMetadataInfo) {
        val args = Bundle()
        val fileTitle = folderMetadataInfo.fileTitle
        args.putString(FILE_NAME_KEY, fileTitle)
        showDownloadFragment(fileTitle + getString(
                R.string.retrieving_file_file))

        setActionBarTitle(fileTitle)

        handleCancellableFuturesCallable.submitCallable(GetFileFromDriveTask.Builder()
                .context(applicationContext)
                .eventBus(eventBus)
                .driveResourceClient(mDriveResourceClient)
                .selectedDriveId(folderMetadataInfo.fileDriveId!!)
                .fileName(fileTitle)
                .mimeType(folderMetadataInfo.mimeType)
                .build())
    }

    private fun startDownloadFolderInfo(folderMetadataInfo: FileMetadataInfo) {
        val currFolderLevel = foldersData.getCurrFolderLevel()
        val currFolderParentDriveId = foldersData.getCurrFolderDriveId()

        val selectedDriveId = folderMetadataInfo.fileDriveId
        val selectedFileTitle = folderMetadataInfo.fileTitle

        val folderName = folderMetadataInfo.fileTitle

        showDownloadFragment(folderName + getString(
                        R.string.retrieving_file_folder))

        handleCancellableFuturesCallable.submitCallable(DownloadFolderInfoTask.Builder()
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

    private fun showDownloadFragment(fileName: String) {
        val args = Bundle()
        args.putString(RETRIEVING_FOLDER_TITLE_KEY, fileName)
        setFragment(
                FragmentsEnum.DOWNLOAD_FRAGMENT,
                getString(R.string.retrieving_folder_title),
                true,
                null,
                args)
    }

    override fun onUpButtonPressedInFragment() {
        val text = fileFragment!!.getText()
        Log.v("HomeActivity", """onUpButtonPressedInFragment - text: $text """)
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
