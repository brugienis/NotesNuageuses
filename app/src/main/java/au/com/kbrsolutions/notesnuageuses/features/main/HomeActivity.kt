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
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.DownloadFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FileDetailsFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FileFragment
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FolderFragment
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
        FolderFragment.OnFolderFragmentNewInteractionListener,
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

    private var folderFragment: FolderFragment? = null
    private var fileDetailsFragment: FileDetailsFragment? = null
    private var downloadFragment: DownloadFragment? = null
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

    override fun setFragment(
            fragmentId: HomeActivity.FragmentsEnum,
            titleText: String,
            addFragmentToStack: Boolean,
            foldersAddData: FolderData?,
            fragmentArgs: Bundle?) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: android.support.v4.app.FragmentTransaction

        when (fragmentId) {

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
                fragmentTransaction.replace(R.id.fragments_frame, fileFragment!!, FILE_FRAGMENT_TAG)
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
                                downloadFragment!!,
                                RETRIEVE_FOLDER_PROGRESS_TAG)
                        .commit()
            }

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

                if (folderFragment == null) {
                    folderFragment = FolderFragment.newInstance(
                            folderItemsList,
                            trashFilesCnt)
                } else {
                    folderFragment!!.setNewValues(
                            folderItemsList,
                            trashFilesCnt)
                }

                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragments_frame, folderFragment!!, FOLDER_TAG)
                fragmentTransaction.commit()
            }

            FragmentsEnum.FILE_DETAILS_FRAGMENT -> {

                fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction . replace (
                        R.id.fragments_frame,
                        fileDetailsFragment!!,
                        FILE_DETAILS_TAG
                )
                fragmentTransaction.commit()
            }

//            HomeActivity.FragmentsEnum.LEGAL_NOTICES -> fragmentManager.beginTransaction().replace(R.id.fragments_frame, legalNoticesFragment).commit()

            // fixLater: Aug 21, 2018 - show error message in release version
            else -> throw RuntimeException("$TAG - setFragment - no code to handle fragmentId: $fragmentId")
        }

        if (addFragmentToStack) {
            addFragment(fragmentId, titleText, foldersAddData)
        }
        setCurrFragment(fragmentId)
        setNewFragmentSet(true)
        Log.v("HomeActivity", """setFragment - FragmentsStack: $FragmentsStack """)
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
        if (fragmentToSet != HomeActivity.FragmentsEnum.NONE) {
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

    override fun trashOrDeleteFile(
            selectedFileDriveId: DriveId,
            position: Int,
            currFolderLevel: Int,
            currFolderDriveId: DriveId,
            deleteFile: Boolean) {

        Log.v("HomeActivity", """trashOrDeleteFile - deleteFile: $deleteFile """)

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

        folderFragment?.let {
            it.setNewValues(
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

    private fun setCurrFragment(currFragment: HomeActivity.FragmentsEnum) {
        this.currFragment = currFragment
    }
//        fixme: method isFinishing() does not exist - it should be part of Activity
//    if (isFinishing()) {
        // A_MUST: any other adapters to clear?
//        actvityListAdapter.clear()
//        stopFuturesHandlers()
//        mExecutorService.shutdown()
//    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    override fun onDestroy() {
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
        if (FragmentsStack.getCurrFragment() !== HomeActivity.FragmentsEnum.FOLDER_FRAGMENT_NEW) {
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

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menuShowTrashed -> {
                showTrashedFiles = true
                handleMenuShowTrashed()
                invalidateOptionsMenu()
                folderFragment!!.showTrashedFiles(true)
            }

            R.id.menuHideTrashed -> {
                showTrashedFiles = false
                //                if (filesMetadataInfo.size() != FoldersData.getCurrentFolderTrashedFilesCnt()) {
                handleMenuHideTrashed()
                invalidateOptionsMenu()
                folderFragment!!.showTrashedFiles(false)
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
                HomeActivity.FragmentsEnum.FILE_FRAGMENT,
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
                HomeActivity.FragmentsEnum.FILE_DETAILS_FRAGMENT,
                folderMetadataInfo.fileTitle,
                true,
                null,
                null)
    }

    override fun handleOnFolderOrFileClick(position: Int) {
        val idxInTheFolderFilesList = getIdxOfClickedFolderItem(position)
        val folderMetadataArrayInfo = FoldersData.getCurrFolderMetadataInfo()
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
                HomeActivity.FragmentsEnum.DOWNLOAD_FRAGMENT,
//                getString(R.string.retrieving_folder_title),
                fileName,
                true,
                null,
                args)
    }

    override fun removeDownloadFragment() {
        FragmentsStack.removeSpecificTopFragment(
                HomeActivity.FragmentsEnum.DOWNLOAD_FRAGMENT,
                "removeDownloadFragment",
                false)
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
        return folderFragment!!.getFolderItem(position).itemIdxInList
    }

    private fun handleMenuHideTrashed() {
        updateFolderListAdapter()
    }

    private fun handleMenuShowTrashed() {
        updateFolderListAdapter()
    }

    // fixLater: Aug 27, 2018 - used for debugging. Remove later
    private fun printCollection(msg: String, coll: Array<HomeActivity.FragmentsEnum>) {
        Log.i(TAG, "\nprintCollection $msg")
        coll.forEach { Log.i(TAG, it.toString()) }
        Log.i(TAG, "\nend")
    }

    companion object {
        private val TAG = HomeActivity::class.java.simpleName

//        const val EMPTY_FOLDER_TAG = "empty_folder_tag"
        const val FILE_FRAGMENT_TAG = "file_fragment_tag"
        const val FILE_DETAILS_TAG = "file_details_tag"
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
