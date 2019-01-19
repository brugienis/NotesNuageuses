package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.espresso.ActiveFlagsController
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderArrayAdapter
import au.com.kbrsolutions.notesnuageuses.features.main.adapters.FolderItem
import kotlinx.android.synthetic.main.fragment_folder.view.*
import java.util.*

class FolderFragment : Fragment() {

    private var mTrashedFilesCnt: Int = 0
    private var mShowTrashedFiles: Boolean = false
    private var mArgsProcessed = false
    private lateinit var mFolderArrayAdapter: FolderArrayAdapter<FolderItem>
    private var mFolderItemsList = ArrayList<FolderItem>()
    private lateinit var mFolderListView: ListView
    private lateinit var mFolderEmptyView: TextView

    private var listener: OnFolderFragmentInteractionListener? = null

    private enum class TouchedObject {
        MENU_QUICK_PHOTO, MENU_CREATE_FILE, MENU_REFRESH, FILE_OR_FOLDER
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as? OnFolderFragmentInteractionListener ?:
                throw RuntimeException(context.toString() +
                        " must implement OnFolderFragmentInteractionListener")

        if (context !is FolderArrayAdapter.OnFolderArrayAdapterInteractionListener)
                throw RuntimeException(context.toString() +
                        " must implement FolderArrayAdapter.OnFolderArrayAdapterInteractionListener")
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
                mTrashedFilesCnt = it.getInt(ARG_TRASH_FILES_CNT_KEY)
            }
            mArgsProcessed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_folder, container, false)

        mFolderArrayAdapter = FolderArrayAdapter(
                listener as Context,
                listener as FolderArrayAdapter.OnFolderArrayAdapterInteractionListener,
                mFolderItemsList)

        mFolderEmptyView = rootView.emptyFolderInfoId
        updateEmptyFolderInfo()

        mFolderListView = rootView.folderListView as NestedScrollingListView

        mFolderListView.adapter = mFolderArrayAdapter
//        mFolderListView.setNestedScrollingEnabled(true)
        mFolderListView.itemsCanFocus = true

        mFolderListView.setOnItemClickListener { adapterView: AdapterView<*>?,
                                                 view: View?,
                                                 position: Int,
                                                 l: Long ->
            handleRowSelected(adapterView, position)
        }

        showTrashedFiles(mShowTrashedFiles)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (ActiveFlagsController.isEspressoTestRunning) {
            ActiveFlagsController.setEspressoFolderFragmentActiveFlag("$TAG.onResume",
                    true)
        }
    }

    private fun updateEmptyFolderInfo() {
        listener?.let {
            mFolderEmptyView.text = (it as Context).getString(
                    R.string.empty_folder,
                    mTrashedFilesCnt)
        }
    }

    /*
        List's view row was clicked - download folder or file details
     */
    private fun handleRowSelected(adapterView: AdapterView<*>?, position: Int) {
        listener!!.handleOnFolderOrFileClick(position)
    }

    fun getFolderItem(idx: Int): FolderItem {
        return mFolderItemsList[idx]
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_fragment_menu, menu)
        return
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var menuItem: MenuItem = menu.findItem(R.id.menuCreateFile)
        menuItem.isVisible = true
        menuItem.isEnabled = true
        return
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event

        when (item.itemId) {
            R.id.menuCreateFile -> handleCreateFileOptionSelected()

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
            TouchedObject.MENU_QUICK_PHOTO ->
                if (quickCameraDelayExpired) {
                    delaysExpired = true
                    diff = currTimeNonos - mPrevQuickCameraTouchTimeNonos
                    delayNonos = mQuickCameraDelayNanos
                    mPrevQuickCameraTouchTimeNonos = currTimeNonos
                }

            TouchedObject.MENU_CREATE_FILE ->
                if (quickCameraDelayExpired && createFileDelayExpired) {
                    delaysExpired = true
                    diff = currTimeNonos - mPrevCreateFileTouchTimeNonos
                    delayNonos = mCreateFileDelayNanos
                    mPrevCreateFileTouchTimeNonos = currTimeNonos
                }

            TouchedObject.FILE_OR_FOLDER ->
                if (quickCameraDelayExpired && createFileDelayExpired && fileOrFolderDelayExpired) {
                    delaysExpired = true
                    diff = currTimeNonos - mPrevFileOrFolderTouchTimeNonos
                    delayNonos = mFileOrFolderDelayNanos
                    mPrevFileOrFolderTouchTimeNonos = currTimeNonos
                }

            TouchedObject.MENU_REFRESH ->
                if (quickCameraDelayExpired && createFileDelayExpired && fileOrFolderDelayExpired && refreshDelayExpired) {
                    delaysExpired = true
                    diff = currTimeNonos - mPrevRefreshTouchTimeNonos
                    delayNonos = mRefreshDelayNanos
                    mPrevRefreshTouchTimeNonos = currTimeNonos
                }

//            else -> throw RuntimeException("delaysExpired - no code to handle case: $touchedObject")
        }

        return delaysExpired
    }

    fun showTrashedFiles(showTrashedFiles: Boolean) {
        mShowTrashedFiles = showTrashedFiles
        refreshUi()
    }

    private fun refreshUi() {
        when {
            mFolderItemsList.size == 0 -> {

                mFolderEmptyView.visibility = View.VISIBLE
                mFolderListView.visibility = View.INVISIBLE
            }

            else -> {

                mFolderEmptyView.visibility = View.INVISIBLE
                mFolderListView.visibility = View.VISIBLE
            }
        }
    }

    fun setNewValues(folderItemsList: ArrayList<FolderItem>, trashedFilesCnt: Int) {
        this.mTrashedFilesCnt = trashedFilesCnt
        mFolderItemsList = folderItemsList

        mFolderArrayAdapter.clear()
        mFolderArrayAdapter.addAll(folderItemsList)
        updateEmptyFolderInfo()
        refreshUi()
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
//        fun showSelectedFileDetails(position: Int)
    }

    companion object {
        private val TAG = FolderFragment::class.java.simpleName
        private const val ARG_TRASH_FILES_CNT_KEY = "arg_trash_files_cnt_key"
        private const val ARG_FOLDER_ITEMS_LIST_KEY = "arg_folder_items_list_key"

        @JvmStatic
        fun newInstance(folderItemsList: ArrayList<FolderItem>, trashFilesCnt: Int) =
                FolderFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(ARG_FOLDER_ITEMS_LIST_KEY, folderItemsList)
                        putInt(ARG_TRASH_FILES_CNT_KEY, trashFilesCnt)
                    }
                }

    }

}
