package au.com.kbrsolutions.notesnuageuses.features.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

//          cd /Users/bohdan/AndroidStudioProjects/NotesNuageuses
//          git push -u origin
//          https://developers.google.com/drive/android/intro
//          https://developers.google.com/drive/android/examples/

class HomeActivity : BaseActivity() {

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
        Log.v(TAG, "onCreate end   - : ")
    }

    override fun onDriveClientReady() {
        Log.v(TAG, "onDriveClientReady start - : ")
        // do something
    }
}
