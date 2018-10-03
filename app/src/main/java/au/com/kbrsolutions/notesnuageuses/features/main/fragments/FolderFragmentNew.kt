package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ListView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import kotlinx.android.synthetic.main.fragment_folder.view.*
import java.util.*

class FolderFragmentNew : Fragment(), OnClickListener {

    private var mTrashedFilesCnt: Int = 0
    private var mArgsProcessed = false
    private lateinit var mFolderArrayAdapter: FolderArrayAdapter<FolderItem>
    private var mFolderItemsList = ArrayList<FolderItem>()
    private lateinit var mFolderListView: ListView

    private var listener: FolderFragment.OnFolderFragmentInteractionListener? = null

    private enum class TouchedObject {
        MENU_QUICK_PHOTO, MENU_CREATE_FILE, MENU_REFRESH, FILE_OR_FOLDER
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FolderFragment.OnFolderFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnFolderFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true

        if (!mArgsProcessed) {
            arguments?.let {
                mFolderItemsList = it.getParcelableArrayList(ARG_FOLDER_ITEMS_LIST_KEY)
                mTrashedFilesCnt = it.getInt(EmptyFolderFragment.ARG_TRASH_FILES_CNT_KEY)
            }
            mArgsProcessed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_folder, container, false)

        mFolderArrayAdapter = FolderArrayAdapter(
                listener as Context,
                this,
                mFolderItemsList)
        Log.v("FolderFragmentNew", """onCreateView -
            |mFolderItemsList: ${mFolderItemsList}
            |""".trimMargin())
        mFolderListView = view.folderListView as NestedScrollingListView

        mFolderListView.setAdapter(mFolderArrayAdapter)
//        mFolderListView.setNestedScrollingEnabled(true)
        mFolderListView.setItemsCanFocus(true)

        mFolderListView.setOnItemClickListener { adapterView: AdapterView<*>?,
                                                view: View?,
                                                position: Int,
                                                l: Long ->
            handleRowSelected(adapterView, position)
        }

        return view
    }

    /*
        List's view row was clicked - download folder or file details
     */
    private fun handleRowSelected(adapterView: AdapterView<*>?, position: Int) {
        Log.v("FolderFragmentNew", """handleRowSelected - position: ${position} """)
        listener!!.handleOnFolderOrFileClick(position)
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = listView
        listView.setOnTouchListener { _, m ->
            handleTouch(m)
            false
        }
    }*/

    private fun handleTouch(m: MotionEvent) {
//        mContext!!.handleTouchEvent(m, listView)
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView.onItemLongClickListener = OnItemLongClickListener { parent, view, position, id ->
//            mContext!!.showSelectedFileDetails(position)
            true
        }
    }*/

    private fun handleCreateFileOptionSelected() {
//        if (isAppFinishing) {
//            return
//        }
        if (delaysExpired(TouchedObject.MENU_CREATE_FILE)) {
//            val dialog = CreateFileDialog.newInstance()
//            dialog.show(fragmentManager, "dialog")
            listener!!.showFileDialog()
        }
    }

   /* override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        // fixLater: Aug 24, 2018 - uncomment below - isAppFinishing is set up in removeTopFragment(...)
//        if (isAppFinishing) {
//            return
//        } else
            synchronized(this) {
                if (!delaysExpired(TouchedObject.FILE_OR_FOLDER)) {
                    return
                }
            }
        listener!!.handleOnFolderOrFileClick(position)

    }*/

    override fun onClick(v: View) {
        val position = mFolderListView.getPositionForView(v)
        if (position != ListView.INVALID_POSITION) {
            v.setBackgroundColor(resources.getColor(R.color.action_view_clicked))
            listener!!.showSelectedFileDetails(position)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_fragment_menu, menu)
        //		this.mMenu = menu;
        return
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var menuItem: MenuItem
        // TOP: why mContext.appIsFinishing() below?
//        if (mContext!!.isGoogleApiClientConnected() && !mContext!!.appIsFinishing()) {
//        if (!mContext!!.appIsFinishing()) {
//            if (!mContext!!.isCameraIsAvailable()) {
//                menuItem = menu.findItem(R.id.menuQuickPhoto)
//                menuItem.isVisible = false                                // do both to hide a menu item
//                menuItem.isEnabled = false
//            }
//            menuItem = menu.findItem(R.id.menuCreateFile)
//            menuItem.isVisible = true
//            menuItem.isEnabled = true
//            menuItem = menu.findItem(R.id.menuRefresh)
//            menuItem.isVisible = true
//            menuItem.isEnabled = true
//        } else {

            menuItem = menu.findItem(R.id.menuQuickPhoto)
            menuItem.isVisible = false                                    // do both to hide a menu item
            menuItem.isEnabled = false
            menuItem = menu.findItem(R.id.menuCreateFile)
            menuItem.isVisible = true
            menuItem.isEnabled = true
            menuItem = menu.findItem(R.id.menuRefresh)
            menuItem.isVisible = true
            menuItem.isEnabled = true
//        }
        return
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event

        when (item.itemId) {
//            R.id.menuQuickPhoto -> handleCameraOptionSelected()
//            R.id.menuRefresh -> handleRefreshOptionSelected()
            R.id.menuCreateFile -> handleCreateFileOptionSelected()
//            R.id.activity_log_clearActivityLog -> actvityListAdapter.clear()
//            R.id.action_settings -> handleSettings()
//            R.id.action_about -> handleAbout()
//            R.id.action_legal_notices -> handleLegalNotices()
//            R.id.action_show_root_folder -> handleShowRootFolder()
//            R.id.action_resend_file -> {
//                val resendPhotoToGoogleDriveCallable = ResendFileToGoogleDriveCallable()
//                handleNonCancellableFuturesCallable.submitCallable(resendPhotoToGoogleDriveCallable)

//            R.id.menuShowTrashed -> {
//                showTrashedFiles = true
//                handleMenuShowTrashed()
//                invalidateOptionsMenu()
//            }
//            R.id.menuHideTrashed -> {
//                showTrashedFiles = false
//                //                if (filesMetadataInfo.size() != foldersData.getCurrentFolderTrashedFilesCnt()) {
//                handleMenuHideTrashed()
//                invalidateOptionsMenu()
//            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // FIXME: take from settings
    private val mQuickCameraDelayNanos = 5000000000L            // 5  seconds
    private var mPrevQuickCameraTouchTimeNonos: Long = 0
    private val mCreateFileDelayNanos = 1000000000L            // 1  seconds
    private var mPrevCreateFileTouchTimeNonos: Long = 0
    private val mFileOrFolderDelayNanos = 1000000000L            // 1  seconds
    private var mPrevFileOrFolderTouchTimeNonos: Long = 0
    private val mRefreshDelayNanos = 15000000000L            // 15 seconds			// 15 * 1000 * 1000 * 1000; DON'T DO that - it will be truncated to the max value that can fit in the integer

    private var mPrevRefreshTouchTimeNonos: Long = 0

    // fixLater: Sep 15, 2018 - move to util
    private fun delaysExpired(touchedObject:TouchedObject): Boolean {
        var delaysExpired = false
        val currTimeNonos = System.nanoTime()
        var diff: Long = -1
        var delayNonos: Long = -1
        val quickCameraElapsedTime = currTimeNonos - mPrevQuickCameraTouchTimeNonos
        val createFileElapsedTime = currTimeNonos - mPrevCreateFileTouchTimeNonos
        val fileOrFolderElapsedTime = currTimeNonos - mPrevFileOrFolderTouchTimeNonos
        val refreshElapsedTime = currTimeNonos - mPrevRefreshTouchTimeNonos

        val quickCameraDelayExpired = quickCameraElapsedTime > mQuickCameraDelayNanos
        val createFileDelayExpired = createFileElapsedTime > mCreateFileDelayNanos
        val fileOrFolderDelayExpired = fileOrFolderElapsedTime > mFileOrFolderDelayNanos
        val refreshDelayExpired = refreshElapsedTime > mRefreshDelayNanos

        when (touchedObject) {
            TouchedObject.MENU_QUICK_PHOTO -> if (quickCameraDelayExpired) {
                delaysExpired = true
                diff = currTimeNonos - mPrevQuickCameraTouchTimeNonos
                delayNonos = mQuickCameraDelayNanos
                mPrevQuickCameraTouchTimeNonos = currTimeNonos
            }

            TouchedObject.MENU_CREATE_FILE -> if (quickCameraDelayExpired && createFileDelayExpired) {
                delaysExpired = true
                diff = currTimeNonos - mPrevCreateFileTouchTimeNonos
                delayNonos = mCreateFileDelayNanos
                mPrevCreateFileTouchTimeNonos = currTimeNonos
            }

            TouchedObject.FILE_OR_FOLDER -> if (quickCameraDelayExpired && createFileDelayExpired && fileOrFolderDelayExpired) {
                delaysExpired = true
                diff = currTimeNonos - mPrevFileOrFolderTouchTimeNonos
                delayNonos = mFileOrFolderDelayNanos
                mPrevFileOrFolderTouchTimeNonos = currTimeNonos
            }

            TouchedObject.MENU_REFRESH -> if (quickCameraDelayExpired && createFileDelayExpired && fileOrFolderDelayExpired && refreshDelayExpired) {
                delaysExpired = true
                diff = currTimeNonos - mPrevRefreshTouchTimeNonos
                delayNonos = mRefreshDelayNanos
                mPrevRefreshTouchTimeNonos = currTimeNonos
            }

//            else -> throw RuntimeException("delaysExpired - no code to handle case: $touchedObject")
        }

        return delaysExpired
    }

    fun setNewValues(folderItemsList: ArrayList<FolderItem>, trashedFilesCnt: Int) {
        this.mTrashedFilesCnt = trashedFilesCnt
        mFolderItemsList = folderItemsList
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the context and potentially other fragments contained in that
     * context.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFolderFragmentInteractionListener {
        fun showFileDialog()
        fun handleOnFolderOrFileClick(position: Int)
        fun showSelectedFileDetails(position: Int)
    }

    companion object {
        private val TAG = FolderFragmentNew::class.java.simpleName
        private const val ARG_TRASH_FILES_CNT_KEY = "arg_trash_files_cnt_key"
        private const val ARG_FOLDER_ITEMS_LIST_KEY = "arg_folder_items_list_key"

        @JvmStatic
        fun newInstance(folderItemsList: ArrayList<FolderItem>, trashFilesCnt: Int) =
                FolderFragmentNew().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(ARG_FOLDER_ITEMS_LIST_KEY, folderItemsList)
                        putInt(ARG_TRASH_FILES_CNT_KEY, trashFilesCnt)
                    }
//                    Log.v("FolderFragment", "newInstance - arguments: $arguments ")
                }

    }

}
