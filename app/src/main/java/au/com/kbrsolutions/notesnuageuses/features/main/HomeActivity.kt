package au.com.kbrsolutions.notesnuageuses.features.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import au.com.kbrsolutions.notesnuageuses.features.core.FragmentsStack
import au.com.kbrsolutions.notesnuageuses.features.tasks.RetrieveDriveFolderInfoTask
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
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

        eventBus = EventBus.getDefault()
        eventBus.register(this)
        //        asyncEventBus = EventBus.getDefault();
        //        asyncEventBus.register(this);
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
}
