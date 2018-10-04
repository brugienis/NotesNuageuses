package au.com.kbrsolutions.notesnuageuses.features.main

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
import au.com.kbrsolutions.notesnuageuses.features.core.FoldersData
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack.addFragment
import au.com.kbrsolutions.notesnuageuses.features.eventbus.*
import au.com.kbrsolutions.notesnuageuses.features.eventbus.events.*
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import au.com.kbrsolutions.notesnuageuses.features.main.dialogs.CreateFileDialog
import au.com.kbrsolutions.notesnuageuses.features.main.dialogs.RenameFileDialog
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.*
import au.com.kbrsolutions.notesnuageuses.features.tasks.*
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
        FolderArrayAdapter.OnFolderArrayAdapterInteractionListener,
        CreateFileDialog.OnCreateFileDialogInteractionListener,
        DriveAccessEventsHandler.OnDriveAccessEventsHandlerInteractionListener,
        FolderEventsHandler.OnFolderEventsHandlerInteractionListener,
        RenameFileDialog.OnRenameFileDialogInteractionListener,
        RenameFileEventHandler.OnRenameFileEventHandlerInteractionListener,
        FileDownloadEventHandler.OnFileDownloadEventHandlerInteractionListener,
        FileUploadEventsHandler.OnFileUploadEventsHandlerInteractionListener,
        FileDeleteEventsHandler.OnFileDeleteEventsHandlerInteractionListener,
        FileFragment.OnFileFragmentInteractionListener,
        FileDetailsFragment.OnFileDetailsFragmentInteractionListener {

    private lateinit var eventBus: EventBus
    private var mToolbar: Toolbar? = null
    private val mTestMode: Boolean = false
    private lateinit var handleCancellableFuturesCallable: HandleCancellableFuturesCallable
    private var mCancellableFuture: Future<String>? = null
    private lateinit var handleNonCancellableFuturesCallable: BaseActivity.HandleNonCancellableFuturesCallable
    private var mNonCancellableFuture: Future<String>? = null
    private lateinit var mExecutorService: ExecutorService
    private var showTrashedFiles: Boolean = false
    private var newFragmentSet: Boolean = false
    private var currFragment: FragmentsEnum? = null
    private var isAppFinishing = false
    private var mTitle: CharSequence? = null

    private var emptyFolderFragment: EmptyFolderFragment? = null
    private var folderFragment: FolderFragment? = null
    private var folderFragmentNew: FolderFragmentNew? = null
    private var fileDetailsFragment: FileDetailsFragment? = null
    private var downloadFragment: DownloadFragment? = null
    private var folderArrayAdapter: FolderArrayAdapter<FolderItem>? = null
    private var fileFragment: FileFragment? = null

    private val driveAccessEventsHandler = DriveAccessEventsHandler(this)
    private val folderEventsHandler = FolderEventsHandler(this)
    private val fileUploadEventsHandler = FileUploadEventsHandler(this)
    private val fileDownloadEventHandler = FileDownloadEventHandler(this)
    private val renameFileEventHandler = RenameFileEventHandler(this)
    private val fileDeleteEventsHandler = FileDeleteEventsHandler(this)

    init {
        FragmentsStack.initialize(mTestMode)
    }

    enum class FragmentsEnum {
        LOG_IN_FRAGMENT,
        ACTIVITY_LOG_FRAGMENT,
//        FOLDER_FRAGMENT,
        FOLDER_FRAGMENT_NEW,
        TRASH_FOLDER_FRAGMENT,
        FILE_FRAGMENT,
        FILE_DETAILS_FRAGMENT,
        IMAGE_VIEW_FRAGMENT,
        NONE,
        EMPTY_FOLDER_FRAGMENT,
        DOWNLOAD_FRAGMENT,
        ROBOTIUM_TEST,
        LEGAL_NOTICES,
        SETTINGS_FRAGMENT
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

        // https://freakycoder.com/android-notes-24-how-to-add-back-button-at-toolbar-941e6577418e
//        mToolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDriveClientReady() {
        startFuturesHandlers()
        val folderFragmentsCnt = FragmentsStack.getFolderFragmentCount()
        if (folderFragmentsCnt == 0 || FoldersData.getCurrFolderLevel() != folderFragmentsCnt - 1) {
            FragmentsStack.initialize(mTestMode)
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
     * For example, if the 'FoldersData' contains details of the root folder, there are no folders
     * details in both the FoldersData and FragmentsStack.
     * The call to FoldersData.getCurrFolderLevel() would return -1.
     *
     * It means, you have to inspect the details of the 'folderData' to figure out which folder to
     * show - Folder or Empty Folder.
     *
     * if there are no files in the 'folderData.filesMetadataInfoList' or all files are trashed, show
     * Empty Folder.
     *
     */
    override fun setFolderFragment(folderData: FolderData) {
//        if (folderData.isEmptyOrAllFilesTrashed && !showTrashedFiles) {
        Log.v("HomeActivity", """setFolderFragment -
            |folderData.isEmptyOrAllFilesTrashed: ${folderData.isEmptyOrAllFilesTrashed}
            |showTrashedFiles:                    $showTrashedFiles
            |""".trimMargin())
        if (folderData.isEmptyOrAllFilesTrashed && !showTrashedFiles) {
            setFragment(
                    FragmentsEnum.EMPTY_FOLDER_FRAGMENT,
                    folderData.newFolderTitle,
                    true,
                    folderData,
                    null)
        } else {
            setFragment(
                    FragmentsEnum.FOLDER_FRAGMENT_NEW,
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
        Log.v("HomeActivity", """setFragment - fragmentId: ${fragmentId} """)
//        val fragmentManager = fragmentManager
        val fragmentManager = supportFragmentManager
//        val fragmentTransaction: FragmentTransaction
        val fragmentTransaction: android.support.v4.app.FragmentTransaction

        when (fragmentId) {

            FragmentsEnum.EMPTY_FOLDER_FRAGMENT -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: 0
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
                val fileItemId = fragmentArgs.getLong(FILE_ITEM_ID_KEY)
                val idxInTheFolderFilesList = fragmentArgs.getInt(IDX_IN_THE_FOLDER_FILES_LIST_KEY)
                if (fileFragment == null) {
                    fileFragment =
                            FileFragment.newInstance(
                                    fileName,
                                    fileContents,
                                    thisFileDriveId,
                                    fileItemId,
                                    idxInTheFolderFilesList)
                } else {
                    fileFragment!!.setFileDetails(
                            fileName,
                            fileContents,
                            thisFileDriveId,
                            fileItemId,
                            idxInTheFolderFilesList)
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

            /*FragmentsEnum.FOLDER_FRAGMENT -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: -1
                val folderItemsList = ArrayList<FolderItem>()

                // fixLater: Sep 17, 2018 - why not the same logic as in updateFolderListAdapter()?
                val list: ArrayList<FileMetadataInfo>? = foldersAddData?.filesMetadataInfoList
                        ?: FoldersData.getCurrFolderMetadataInfo()

                list!!.withIndex()
                        .forEach { (itemIdxInList, folderMetadataInfo) ->
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
            }*/

            FragmentsEnum.FOLDER_FRAGMENT_NEW -> {
                val trashFilesCnt = foldersAddData?.trashedFilesCnt ?: -1
                val folderItemsList = ArrayList<FolderItem>()

                // fixLater: Sep 17, 2018 - why not the same logic as in updateFolderListAdapter()?
                val list: ArrayList<FileMetadataInfo>? = foldersAddData?.filesMetadataInfoList
                        ?: FoldersData.getCurrFolderMetadataInfo()

                list!!.withIndex()
                        .forEach { (itemIdxInList, folderMetadataInfo) ->
                    if (!folderMetadataInfo.isTrashed || folderMetadataInfo.isTrashed && showTrashedFiles) {
                        folderItemsList.add(FolderItem(
                                folderMetadataInfo.fileTitle,
                                folderMetadataInfo.updateDt,
                                folderMetadataInfo.mimeType,
                                folderMetadataInfo.isTrashed,
                                itemIdxInList))
                    }
                }

                Log.v("HomeActivity", """setFragment -
                    |list.size:       ${list.size}
                    |list:            ${list}
                    |folderItemsList: ${folderItemsList}
                    | """.trimMargin())

                if (folderFragmentNew == null) {
                    folderFragmentNew = FolderFragmentNew.newInstance(
                            folderItemsList,
                            trashFilesCnt)
                } else {
                    folderFragmentNew!!.setNewValues(
                            folderItemsList,
                            trashFilesCnt)
                }

                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, folderFragmentNew, FOLDER_TAG)
                fragmentTransaction.commit()
            }

            FragmentsEnum.FILE_DETAILS_FRAGMENT ->
                fragmentManager.beginTransaction().replace(
                        R.id.fragments_frame,
                        fileDetailsFragment).commit()

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
    fun onMessageEvent(event: RenameFileEvents) {
        renameFileEventHandler.onMessageEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: DriveAccessEvents) {
        driveAccessEventsHandler.onMessageEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FilesDownloadEvents) {
        fileDownloadEventHandler.onMessageEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FilesUploadEvents) {
        fileUploadEventsHandler.onMessageEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FoldersEvents) {
        folderEventsHandler.onMessageEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FileDeleteEvents) {
        fileDeleteEventsHandler.onMessageEvent(event)
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
        val fragmentsStackResponse = FragmentsStack.removeTopFragment(source, actionCancelled)
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

        return fragmentsStackResponse.finishRequired
    }

    override fun setActionBarTitle(title: CharSequence) {
        mTitle = title
        supportActionBar!!.title = title
    }

    // fixLater: Sep 16, 2018 - do I need it?
    private fun retrievingAppFolderDriveInfoTaskDone(folderData: FolderData?) {
        if (folderData != null && folderData.newFolderData) {   // folder info not in FoldersData
            setFolderFragment(folderData)
        } else {
            updateFolderListAdapter()
        }
    }

    override fun trashOrDeleteFile(
            selectedFileDriveId: DriveId,
            position: Int,
            currFolderLevel: Int,
            currFolderDriveId: DriveId,
            deleteFile: Boolean) {

        handleNonCancellableFuturesCallable.submitCallable(
                RemoveFileFromDriveTask.Builder()
                        .context(applicationContext)
                        .eventBus(eventBus)
                        .driveResourceClient(mDriveResourceClient)
                        .thisFileDriveId(selectedFileDriveId)
                        .idxInTheFolderFilesList(position)
                        .thisFileFolderLevel(currFolderLevel)
                        .parentFolderDriveId(FoldersData.getCurrFolderDriveId()!!)
                        .deleteThisFile(deleteFile)
                        .build())

        removeTopFragment("onEventMainThread-TRASH_FILE", false)
    }

    override fun updateFolderListAdapter() {
        val currFolderMetadataInfo = FoldersData.getCurrFolderMetadataInfo()
        val folderItemsList = ArrayList<FolderItem>()
        currFolderMetadataInfo!!
                .withIndex()
                .forEach { (itemIdxInList, folderMetadataInfo) ->
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

        // fixLater: Oct 03, 2018 - folderArrayAdapter is not in use anymore - add code in
        // fixLater: Oct 03, 2018 - FolderFragmentNew setNewFolderItemsList(...) and remove
        // fixLater: Oct 03, 2018 - folderArrayAdapter
//        folderArrayAdapter!!.clear()
//        folderArrayAdapter!!.addAll(folderItemsList)
        if (folderFragmentNew != null) {
            folderFragmentNew!!.setNewValues(
                    folderItemsList,
                    FoldersData.getCurrFolderData().trashedFilesCnt)
        }
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
    override fun startRenameFile(
            thisFileDriveId: DriveId,
            newFileName: String,
            idxInTheFolderFilesList: Int,
            thisFileFolderLevel: Int,
            thisFileFolderDriveId: DriveId) {

        handleNonCancellableFuturesCallable.submitCallable(
                RenameFileOnDriveTask.Builder()
                        .context(applicationContext)
                        .eventBus(eventBus)
                        .driveResourceClient(mDriveResourceClient)
                        .thisFileDriveId(thisFileDriveId)
                        .newFileName(newFileName)
                        .idxInTheFolderFilesList(idxInTheFolderFilesList)
                        .thisFileFolderLevel(thisFileFolderLevel)
                        .thisFileFolderDriveId(thisFileFolderDriveId)
                        .build())
        removeTopFragment("onEventMainThread-RENAME_FILE", false)
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
        mExecutorService.shutdown()
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
        } else if (
//                FragmentsStack.getCurrFragment() !== FragmentsEnum.FOLDER_FRAGMENT &&
                FragmentsStack.getCurrFragment() !== FragmentsEnum.FOLDER_FRAGMENT_NEW &&
                FragmentsStack.getCurrFragment() !== FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {
            menuItem = menu.findItem(R.id.menuShowTrashed)
            menuItem.isVisible = false
            menuItem.isEnabled = false
            menuItem = menu.findItem(R.id.menuHideTrashed)
            menuItem.isVisible = false
            menuItem.isEnabled = false
        }
        if (showTrashedFiles) {
            menuItem = menu.findItem(R.id.menuHideTrashed)
            menuItem.isVisible = true
            menuItem.isEnabled = true
            menuItem.title = resources.getString(
                    R.string.menu_hide_trashed_files,
                    FoldersData.getCurrentFolderTrashedFilesCnt())
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
            menuItem.title = resources.getString(
                    R.string.menu_show_trashed_files,
                    FoldersData.getCurrentFolderTrashedFilesCnt())
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
//            R.id.action_connect_to_google_drive ->
//                sendTextFileToDrive(null,"test1.txt",
//                        "Hello Drive".toByteArray(Charsets.UTF_8), -1)
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
                //                if (filesMetadataInfo.size() != FoldersData.getCurrentFolderTrashedFilesCnt()) {
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
            fileContents: ByteArray,
            fileItemId:Long,
            idxInTheFolderFilesList: Int) {
        Log.v("HomeActivity", """sendTextFileToDrive - before send task
            |fileName:                  ${fileName}
            |FragmentsStack.toString(): ${FragmentsStack.toString()}
            |""".trimMargin())

        val currFolderLevel = FoldersData.getCurrFolderLevel()

        val sendTextToGoogleDriveCallable = SendFileToDriveTask.Builder()
                .context(applicationContext)
                .eventBus(eventBus)
                .driveResourceClient(mDriveResourceClient)
                .parentFolderLevel(currFolderLevel)
                .parentFolderDriveId(FoldersData.getCurrFolderDriveId()!!)
                .existingFileDriveId(existingFileDriveId)
                .fileName(fileName)
                .mimeType(MIME_TYPE_TEXT_FILE)
                .contents(fileContents)
                .parentFileName(FoldersData.getFolderTitle(currFolderLevel)!!)
                .fileItemId(fileItemId)
                .idxInTheFolderFilesList(idxInTheFolderFilesList)
                .build()

        handleNonCancellableFuturesCallable.submitCallable(sendTextToGoogleDriveCallable)
    }

    override fun showFileDialog() {
        val dialog = CreateFileDialog.newInstance()
        dialog.show(fragmentManager, "dialog")
    }

    override fun showRenameFiledialog(args: Bundle) {
        val renameFileDialog = RenameFileDialog.newInstance(args)
        renameFileDialog.isCancelable = false
        renameFileDialog.show(fragmentManager, "rename dialog")
    }

    override fun createFolder(fileName: CharSequence) {
        handleNonCancellableFuturesCallable.submitCallable(
                CreateDriveFolderTask.Builder()
                        .activity(this)
                        .eventBus(eventBus)
                        .driveResourceClient(mDriveResourceClient)
                        .newFolderName(fileName.toString())
                        .parentFolderLevel(FoldersData.getCurrFolderLevel())
                        .parentFolderDriveId(FoldersData.getCurrFolderDriveId())
                        .build())
    }

    override fun createPhotoNote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTextNote(fileName: CharSequence) {
        val args = Bundle()
        args.putString(FILE_NAME_KEY, fileName.toString())
        args.putString(FILE_CONTENTS_KEY, "")
        args.putLong(FILE_ITEM_ID_KEY, System.currentTimeMillis())

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
        FragmentsStack.initialize(mTestMode)
        onDriveClientReady()
    }

    override fun showSelectedFileDetails(position: Int) {
        Log.v("HomeActivity", """showSelectedFileDetails - position: ${position} """)
        if (fileDetailsFragment == null) {
            fileDetailsFragment = FileDetailsFragment()
        }
        val folderMetadatasInfo = FoldersData.getCurrFolderMetadataInfo()
        val idxOfClickedFolderItem = getIdxOfClickedFolderItem(position)
        val folderMetadataInfo = folderMetadatasInfo!!.get(idxOfClickedFolderItem)

        fileDetailsFragment!!.setSelectedFileInfo(
                idxOfClickedFolderItem,
                folderMetadataInfo,
                FoldersData.getCurrFolderLevel(),
                FoldersData.getCurrFolderDriveId()!!)

        setActionBarTitle(folderMetadataInfo.fileTitle)

        setFragment(
                FragmentsEnum.FILE_DETAILS_FRAGMENT,
                folderMetadataInfo.fileTitle,
                true,
                null,
                null)
    }

    override fun handleOnFolderOrFileClick(position: Int) {
        val idxInTheFolderFilesList = getIdxOfClickedFolderItem(position)
        val folderMetadataArrayInfo = FoldersData.getCurrFolderMetadataInfo()
        Log.v("HomeActivity", """handleOnFolderOrFileClick -
            |idxInTheFolderFilesList: ${idxInTheFolderFilesList}
            |folderMetadataArrayInfo: ${folderMetadataArrayInfo}
            |""".trimMargin())
        val fileMetadataInfo: FileMetadataInfo = folderMetadataArrayInfo!![idxInTheFolderFilesList]
        if (fileMetadataInfo.isFolder) {
            startDownloadFolderInfo(fileMetadataInfo)
        } else {
            startDownloadFileContents(idxInTheFolderFilesList, fileMetadataInfo)
        }
    }

    private fun startDownloadFileContents(
            idxInTheFolderFilesList: Int,
            fileMetadataInfo: FileMetadataInfo) {
        val args = Bundle()
        val fileTitle = fileMetadataInfo.fileTitle
        args.putString(FILE_NAME_KEY, fileTitle)
        showDownloadFragment(fileTitle + getString(
                R.string.retrieving_file_file))

        setActionBarTitle(fileTitle)

        handleCancellableFuturesCallable.submitCallable(GetFileFromDriveTask.Builder()
                .context(applicationContext)
                .eventBus(eventBus)
                .driveResourceClient(mDriveResourceClient)
                .selectedDriveId(fileMetadataInfo.fileDriveId!!)
                .fileName(fileTitle)
                .mimeType(fileMetadataInfo.mimeType)
                .fileItemId(fileMetadataInfo.fileItemId)
                .idxInTheFolderFilesList(idxInTheFolderFilesList)
                .build())
    }

    private fun startDownloadFolderInfo(folderMetadataInfo: FileMetadataInfo) {
        val currFolderLevel = FoldersData.getCurrFolderLevel()
        val currFolderParentDriveId = FoldersData.getCurrFolderDriveId()

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
//        return folderArrayAdapter!!.getFolderItem(position).itemIdxInList
        return folderFragmentNew!!.getFolderItem(position).itemIdxInList
    }

    private fun handleMenuHideTrashed() {
        if (FoldersData.currFolderIsEmptyOrAllFilesAreTrashed()) {
            val currFragment = FragmentsStack.getCurrFragment()
            FragmentsStack.replaceCurrFragment(
                    "handleMenuHideTrashed", currFragment, FragmentsEnum.EMPTY_FOLDER_FRAGMENT)
            setFragment(
                    FragmentsEnum.EMPTY_FOLDER_FRAGMENT,
                    mTitle.toString(),
                    false,
                    null,
                    null)
        } else {
            updateFolderListAdapter()
        }
    }

    private fun handleMenuShowTrashed() {
        val currFragment = FragmentsStack.getCurrFragment()
        if (currFragment == FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {
            FragmentsStack.replaceCurrFragment(
                    "handleMenuShowTrashed",
                    currFragment,
//                    FragmentsEnum.FOLDER_FRAGMENT)
                    FragmentsEnum.FOLDER_FRAGMENT_NEW)
            setFragment(
//                    FragmentsEnum.FOLDER_FRAGMENT,
                    FragmentsEnum.FOLDER_FRAGMENT_NEW,
                    mTitle.toString(),
                    false,
                    null,
                    null)
        } else {
            updateFolderListAdapter()
        }
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
        const val FILE_ITEM_ID_KEY = "file_item_id_key"
        const val IDX_IN_THE_FOLDER_FILES_LIST_KEY = "idx_in_the_folder_files_list_key"
    }
}
