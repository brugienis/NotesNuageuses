package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import au.com.kbrsolutions.notesnuageuses.R
import kotlinx.android.synthetic.main.fragment_empty_folder.view.*

class EmptyFolderFragment : Fragment() {

    private var mTrashedFilesCnt: Int = 0
    private var mContext: Context? = null
    private var mArgsProcessed = false

    private var listener: OnEmptyFolderFragmentInteractionListener? = null

    private enum class TouchedObject {
        MENU_QUICK_PHOTO, MENU_CREATE_FILE, MENU_REFRESH, FILE_OR_FOLDER
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnEmptyFolderFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /*
	 * uncomment onCreate if there are menu items specific for this fragment
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true

        if (!mArgsProcessed) {
            arguments?.let {
                mTrashedFilesCnt = it.getInt(ARG_TRASH_FILES_CNT_KEY)
            }
            mArgsProcessed = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_empty_folder, container, false)

        rootView.emptyFragmentInfoId.text = resources.getString(R.string.empty_folder, mTrashedFilesCnt)
        return rootView
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

    // FIXME: take from settings
    private val mQuickCameraDelayNanos = 5000000000L            // 5  seconds
    private var mPrevQuickCameraTouchTimeNonos: Long = 0
    private val mCreateFileDelayNanos = 1000000000L            // 1  seconds
    private var mPrevCreateFileTouchTimeNonos: Long = 0
    private val mFileOrFolderDelayNanos = 1000000000L            // 1  seconds
    private var mPrevFileOrFolderTouchTimeNonos: Long = 0
    private val mRefreshDelayNanos = 15000000000L            // 15 seconds			// 15 * 1000 * 1000 * 1000; DON'T DO that - it will be truncated to the max value that can fit in the integer

    private var mPrevRefreshTouchTimeNonos: Long = 0

    // fixLater: Aug 28, 2018 - move to utility class after merge with Folderfragment
    private fun delaysExpired(touchedObject: TouchedObject): Boolean {
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

    public fun createFolder(fileName: CharSequence) {
        Log.v("EmptyFolderFragment", "createFolder - called - fileName: $fileName ")
//        val fileNameDialogFragment = FolderFileNameDialog.newInstance()
//        fileNameDialogFragment.setParentFragment(this)
//        fileNameDialogFragment.show(fragmentManager, "dialog")
    }

    fun createPhotoNote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createTextNote() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_fragment_menu, menu)
        return
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var menuItem: MenuItem = menu.findItem(R.id.menuCreateFile)
        menuItem.isVisible = true
        menuItem.isEnabled = true
        menuItem = menu.findItem(R.id.menuRefresh)
        menuItem.isVisible = false
        menuItem.isEnabled = false
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

//    private fun handleCreateFileOptionSelected() {
//        createFolder()
//    }

    fun setTrashedFilesCnt(trashedFilesCnt: Int) {
        this.mTrashedFilesCnt = trashedFilesCnt
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the context and potentially other fragments contained in that
     * context.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnEmptyFolderFragmentInteractionListener {
        fun showFileDialog()
    }

    companion object {

//        private val TAG = EmptyFolderFragment::class.java.simpleName

        const val ARG_TRASH_FILES_CNT_KEY = "arg_trash_files_cnt_key"

        @JvmStatic
        fun newInstance(trashFilesCnt: Int) =
                EmptyFolderFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_TRASH_FILES_CNT_KEY, trashFilesCnt)
                    }
//                    Log.v("EmptyFolderFragment", "newInstance - arguments: $arguments ")
                }

    }
}