package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.ListFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter

class FolderFragment : ListFragment(), OnClickListener {

    private var mTrashedFilesCnt: Int = 0
    //	private TextView emptyFolderViewTv;
    //	private LinearLayout nonEmptyFolderViewLl;
    private var mContext: Context? = null
    //	private Object mActionMode;
    //	private int selectedItem = -1;
    //	private boolean isFolderEmpty = false;
    //	private Menu mMenu;
    private var listener: OnFolderFragmentInteractionListener? = null
    private var mArgsProcessed = false

    private enum class TouchedObject {
        MENU_QUICK_PHOTO, MENU_CREATE_FILE, MENU_REFRESH, FILE_OR_FOLDER
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFolderFragmentInteractionListener) {
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
                mTrashedFilesCnt = it.getInt(EmptyFolderFragment.ARG_TRASH_FILES_CNT_KEY)
            }
            mArgsProcessed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = listView
        listView.setOnTouchListener { _, m ->
            handleTouch(m)
            false
        }
    }

    private fun handleTouch(m: MotionEvent) {
//        mContext!!.handleTouchEvent(m, listView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView.onItemLongClickListener = OnItemLongClickListener { parent, view, position, id ->
//            mContext!!.showSelectedFileDetails(position)
            true
        }
    }

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

    /**
     * When 'showTrashed' files is false, the List<FolderItem> passed to the FolderArrayAdapter can be shorter then the list of all files in the folder
     * if there are some trashed files. This method translates the index of the file item touched to its index of all files in the folder.
     *
     * @param position - index of the file item touched on the folder screen
     * @return - index of the item in the folder's items list
    </FolderItem> */
    private fun getIdxOfClickedFolderItem(position: Int): Int {
        val folderArrayAdapter = listAdapter as FolderArrayAdapter<*>
        return folderArrayAdapter.getFolderItem(position).itemIdxInList
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
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

//        val foldersData = listener!!.getFoldersData()
//        val folderMetadatasInfo = foldersData.getCurrFolderMetadataInfo()
        listener!!.startDownloadFolderInfoAtIndex(position)

//        val folderMetadataInfo: FileMetadataInfo = folderMetadatasInfo.get(getIdxOfClickedFolderItem(position))
//        val selectedDriveId = folderMetadataInfo.fileDriveId
//        val selectedFileTitle = folderMetadataInfo.fileTitle
//        if (selectedDriveId == null) {
//            Log.v("FolderFragment", "onListItemClick - getString(R.string.file_not_uploaded_yet): ${getString(R.string.file_not_uploaded_yet)} ")
////            addMsgToActivityLogShowOnScreen(getString(R.string.file_not_uploaded_yet), false, true)
//            return
//        }
//        if (folderMetadataInfo.isFolder) run {
//            val currFolderLevel = foldersData.getCurrFolderLevel()
//            listener!!.setFragment(
//                    FragmentsEnum.RETRIEVE_FOLDER_PROGRESS_FRAGMENT,
//                    getString(R.string.retrieving_folder_title),
//                    true,
//                    FragmentsCallingSourceEnum.ACTIVITY_NOT_FRAGMENT,
//                    null,
//                    null)
//
//            val c: Callable<String> = DownloadFolderInfoTask.Builder()
//            .activity = listener
//                .eventBus = eventBus
//                .setDriveResourceClient(mDriveResourceClient)
//                .setSelectedFolderTitle(selectedFileTitle)
//                .setParentFolderLevel(foldersData.getCurrFolderLevel())
//                .setSelectedFiledDriveId(selectedDriveId)
//                .setTParentFolderDriveId(selectedDriveId)
//                .setCurrentFolderDriveId(foldersData.getCurrFolderDriveId())
//                .build()
//
//            listener!!.getHandleCancellableFuturesCallable().submitCallable(DownloadFolderInfoTask.Builder()
//                    .activity = listener
//                    .eventBus = eventBus
//                    .setDriveResourceClient(mDriveResourceClient)
//                    .setSelectedFolderTitle(selectedFileTitle)
//                    .setParentFolderLevel(foldersData.getCurrFolderLevel())
//                    .setSelectedFiledDriveId(selectedDriveId)
//                    .setTParentFolderDriveId(selectedDriveId)
//                    .setCurrentFolderDriveId(foldersData.getCurrFolderDriveId())
//                    .build())

            //            handleCancellableFuturesCallable.submitCallable(new RetrieveDriveFolderInfoCallable(
            //                    selectedFileTitle,
            //                    selectedDriveId,
            //                    foldersData.getCurrFolderLevel(),
            //                    foldersData.getFolderDriveId(currFolderLevel),
            //                    foldersData.getCurrFolderDriveId()));
//        }
//        mContext!!.fileOrFolderClicked(position)
    }

    override fun onClick(v: View) {
//        if (mContext!!.isNotConnectedToGoogleDrive(LOG_TAG + "onClick")) {
//            //			mContext.showMessage(getString(R.string.waiting_for_google_drive_connection));
//            return
//        } else {
//            val position = listView.getPositionForView(v)
//            if (position != ListView.INVALID_POSITION) {
//                v.setBackgroundColor(resources.getColor(R.color.action_view_clicked))
//                mContext!!.showSelectedFileDetails(position)
//            }
//        }
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

            else -> throw RuntimeException("delaysExpired - no code to handle case: $touchedObject")
        }

        return delaysExpired
    }

    fun setTrashedFilesCnt(trashedFilesCnt: Int) {
        this.mTrashedFilesCnt = trashedFilesCnt
        Log.v("EmptyFolderFragment", "setTrashedFilesCnt - mTrashedFilesCnt: ${mTrashedFilesCnt} ")
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFolderFragmentInteractionListener {
        fun showFileDialog()
        fun startDownloadFolderInfoAtIndex(position: Int)
    }

    companion object {
        private val TAG = FolderFragment::class.java.simpleName
        const val ARG_TRASH_FILES_CNT_KEY = "arg_trash_files_cnt_key"

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(trashFilesCnt: Int) =
                FolderFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_TRASH_FILES_CNT_KEY, trashFilesCnt)
                    }
                    Log.v("FolderFragment", "newInstance - arguments: $arguments ")
                }

    }

}
