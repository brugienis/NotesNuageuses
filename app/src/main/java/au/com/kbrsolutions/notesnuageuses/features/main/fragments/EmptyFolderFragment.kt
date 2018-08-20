package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import kotlinx.android.synthetic.main.fragment_empty_folder.view.*

class EmptyFolderFragment : Fragment() {

    private var mTrashedFilesCnt: Int = 0
    private var mContext: Context? = null
    private var mArgsProcessed = false

    init {
        Log.v("EmptyFolderFragment", " init - mTrashedFilesCnt: ${mTrashedFilesCnt} ")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context as HomeActivity
    }

    /*
	 * uncomment onCreate if there are menu items specific for this fragment
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        Log.v("EmptyFolderFragment", "onCreate - savedInstanceState: $savedInstanceState")

        if (!mArgsProcessed) {
            arguments?.let {
                mTrashedFilesCnt = it.getInt(ARG_TRASH_FILES_CNT_KEY)
            }
            mArgsProcessed = true
        }
        Log.v("EmptyFolderFragment", "onCreate - mTrashedFilesCnt: $mTrashedFilesCnt ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("EmptyFolderFragment", "onDestroy - mTrashedFilesCnt: $mTrashedFilesCnt ")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_empty_folder, container, false)
        Log.v("EmptyFolderFragment", "onCreateView - mTrashedFilesCnt: ${mTrashedFilesCnt} ")

        rootView.emptyFragmentInfoId.text = resources.getString(R.string.empty_folder, mTrashedFilesCnt)
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_fragment_menu, menu)
        return
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var menuItem: MenuItem = menu.findItem(R.id.menuCreateFile)
        menuItem.isVisible = false
        menuItem.isEnabled = false
        menuItem = menu.findItem(R.id.menuRefresh)
        menuItem.isVisible = false
        menuItem.isEnabled = false
        return
    }

    fun setTrashedFilesCnt(trashedFilesCnt: Int) {
        this.mTrashedFilesCnt = trashedFilesCnt
        Log.v("EmptyFolderFragment", "setTrashedFilesCnt - mTrashedFilesCnt: ${mTrashedFilesCnt} ")
    }

    companion object {

        private val TAG = EmptyFolderFragment::class.java.simpleName

        const val ARG_TRASH_FILES_CNT_KEY = "arg_trash_files_cnt_key"

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(trashFilesCnt: Int) =
                EmptyFolderFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_TRASH_FILES_CNT_KEY, trashFilesCnt)
                    }
                    Log.v("EmptyFolderFragment", "newInstance - arguments: $arguments ")
                }

    }
}