package au.com.kbrsolutions.notesnuageuses.features.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack.removeTopFragment
import au.com.kbrsolutions.notesnuageuses.features.events.ActivitiesEvents
import au.com.kbrsolutions.notesnuageuses.features.tasks.RetrieveDriveFolderInfoTask
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

//          cd /Users/bohdan/AndroidStudioProjects/NotesNuageuses
//          git push -u origin
//          https://developers.google.com/drive/android/intro
//          https://developers.google.com/drive/android/examples/

class HomeActivity : BaseActivity() {

    lateinit var eventBus: EventBus
    private val mTestMode: Boolean = false
    private var handleCancellableFuturesCallable: HandleCancellableFuturesCallable? = null
    private var cancellableFuture: Future<String>? = null
    private var handleNonCancellableFuturesCallable: HandleNonCancellableFuturesCallable? = null
    private var nonCancellableFuture: Future<String>? = null
    private var mExecutorService: ExecutorService? = null


    var fragmentsStack = FragmentsStack

    init {
        fragmentsStack.init(mTestMode);
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

    private val TAG = "HomeActivity"

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
        Log.v(TAG, "onCreate start - : ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        Log.v(TAG, "onCreate before eventBus")
        eventBus = EventBus.getDefault()
        eventBus.register(this)
        //        asyncEventBus = EventBus.getDefault();
        //        asyncEventBus.register(this);
        Log.v(TAG, "onCreate after eventBus")
        mExecutorService = Executors.newCachedThreadPool()
        Log.v(TAG, "onCreate end   - : ")
    }

    override fun onDriveClientReady() {
        Log.v(TAG, "onDriveClientReady start - : ")
        val folderFramentsCnt = fragmentsStack.getFolderFragmentCount()
        if (folderFramentsCnt == 0 || foldersData.getCurrFolderLevel() !== folderFramentsCnt - 1) {
            fragmentsStack.init(mTestMode)
//			isNotConnectedToGoogleDrive("onConnected");
            handleCancellableFuturesCallable!!.submitCallable(RetrieveDriveFolderInfoTask.Builder()
                    .activity(this)
                    .eventBus(eventBus)
                    .driveResourceClient(mDriveResourceClient)
                    .selectedFolderTitle(getString(R.string.app_root_folder_name))
                    .parentFolderLevel(-1)
//                    .parentFolderDriveId(null)
//                    .selectedFolderDriveId(null)
//                    .currentFolderDriveId(null)
                    .foldersData(foldersData)
                    .build())
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ActivitiesEvents) {
        //    public void onEventMainThread(ActivitiesEvents event) {
        val request = event.request
        val msg = event.msgContents
        var actionCancelled = false
        val fragmentsEnum: FragmentsEnum
        var logMsg: String
        when (request) {

//            CREATE_FOLDER ->
//                handleNonCancellableFuturesCallable.submitCallable(
//                        CreateDriveFolderTask.Builder()
//                                .setActivity(this)
//                                .setEventBus(eventBus)
//                                .setDriveResourceClient(mDriveResourceClient)
//                                .setNewFolderName(event.getFileName())
//                                .setParentFolderLevel(event.getFolderLevel())
//                                .setTParentFolderDriveId(event.getCurrFolderDriveId())
//                                .setFoldersData(foldersData)
//                                .build())

//            FOLDER_CREATED -> {
//                //				addMsgToActivityLogShowOnScreen("Got request to create folder");
//                val ar = fragmentsStack.getFragmentsList()
//                logMsg = "FragmentStack start"
//                for (fragmentsEnum2 in ar) {
//                    logMsg = "FragmentStack: $fragmentsEnum2"
//                }
//                logMsg = "FragmentStack end"
//                val currFolder = fragmentsStack.getCurrFragment()
//                if (currFolder == FragmentsEnum.EMPTY_FOLDER_FRAGMENT) {                            // folder is no longer empty
//                    val success = fragmentsStack.replaceCurrFragment("onEventMainThread FOLDER_CREATED" + event.getRequest(), currFolder, FragmentsEnum.FOLDER_FRAGMENT)
//                    setFragment(FragmentsEnum.FOLDER_FRAGMENT, getSupportActionBarTitle() as String, false, FragmentsCallingSourceEnum.ON_EVENT_MAIN_THREAD, null)
//                } else {
//                    updateFolderListAdapter(null)
//                }
//            }

//            CREATE_FOLDER_PROBLEMS -> addMsgToActivityLogShowOnScreen(event.msgContents, true, true)

//            DELETE_FILE -> {
//                handleNonCancellableFuturesCallable.submitCallable(DeleteFileOnGoogleDriveTask.Builder()
//                        .setActivity(this)
//                        //                        .setGoogleApiClient(mGoogleApiClient)
//                        .setEventBus(eventBus)
//                        .setFoldersData(foldersData)
//                        .setSelectedFiledDriveId(event.setSelectedFileDriveId)
//                        .setIdxInTheFolderFilesList(event.idxInTheFolderFilesList)
//                        .setThisFileFolderLevel(event.folderLevel)
//                        .setThisFileFolderDriveId(event.currFolderDriveId)
//                        .build())
//                removeTopFragment("onEventMainThread-DELETE_FILE", false)
//            }

//            ActivitiesEvents.HomeEvents.DELETE_FILE_START -> {
//                foldersData.updateFolderItemView(event.getFileItemId(), event.getFolderLevel(), event.getCurrFolderDriveId(),
//                        FileMetadataInfo(event.parentFileName, getString(R.string.home_app_file_deleting, event.fileName), event.setSelectedFileDriveId, event.isFolder, event.mimeType, event.createDt, event.updateDt, event.fileItemId, true, event.isTrashed))
//                updateFolderListAdapter(null)
//            }

//            ActivitiesEvents.HomeEvents.DELETE_FILE_FINISHED -> {
//                Log.v(TAG, "file successfully deleted: " + event.getFileName())
//                // TODO: 29/06/2015 add logic to remove deleted file details from FoldersData and UI
//                foldersData.updateFolderItemViewAfterFileDelete(event.getFileItemId(), event.getFolderLevel(), event.getCurrFolderDriveId(),
//                        FileMetadataInfo(event.parentFileName, event.getFileName(), event.setSelectedFileDriveId, event.isFolder, event.mimeType, event.createDt, event.updateDt, event.fileItemId, true, event.isTrashed))
//                // fixme: do we need updateFolderListAdapter()
//                Log.v(TAG, "DELETE_FILE_FINISHED - " + foldersData.allCurrFolderFilesTrashedOrThereAreNoFiles() + "/" + foldersData.getCurrentFolderTrashedFilesCnt() + "/" +
//                        foldersData.getCurrFolderData().filesMetadatasInfo.size())
//                val folderDataAfterDelete = foldersData.getCurrFolderData()
//                if (foldersData.allCurrFolderFilesTrashedOrThereAreNoFiles()) {
//                    removeTopFragment("onEventMainThread-TRASH_FILE_FINISHED", actionCancelled)
//                    setFolderFragment(folderDataAfterDelete)
//                } else {
//                    updateFolderListAdapter(null)
//                }
//            }

//            DELETE_FILE_PROBLEMS -> {
//                addMsgToActivityLogShowOnScreen(event.msgContents, true, true)
//                foldersData.updateFolderItemView(event.getFileItemId(), event.getFolderLevel(), event.getCurrFolderDriveId(),
//                        FileMetadataInfo(event.parentFileName, event.fileName, event.setSelectedFileDriveId, event.isFolder, event.mimeType, event.createDt, event.updateDt, event.fileItemId, true, false))
//                updateFolderListAdapter(null)
//            }

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
                Log.v(TAG, "onMessageEvent - newFolderTitle/filesMetadatasInfo.size(): " + if (folderData1 == null) "null" else folderData1!!.newFolderTitle + "/" + folderData1!!.filesMetadatasInfo.size)
//                if (folderData1 != null && folderData1!!.filesMetadatasInfo.size() === 0) {
//                    setFolderFragment(folderData1!!) // empty folder
//                } else {
//                    retrievingAppFolderDriveInfoTaskDone(folderData1)
//                }
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

            else -> throw RuntimeException("TAG - onEventMainThread - no code to handle request: $request")
        }
    }

    private fun startFuturesHandlers(source: String) {
        if (cancellableFuture == null) {
            handleCancellableFuturesCallable = HandleCancellableFuturesCallable(mExecutorService)
            cancellableFuture = mExecutorService.submit(handleCancellableFuturesCallable)
        }
        if (nonCancellableFuture == null) {
            handleNonCancellableFuturesCallable = HandleNonCancellableFuturesCallable(mExecutorService)
            nonCancellableFuture = mExecutorService.submit(handleNonCancellableFuturesCallable)
        }
    }
}
