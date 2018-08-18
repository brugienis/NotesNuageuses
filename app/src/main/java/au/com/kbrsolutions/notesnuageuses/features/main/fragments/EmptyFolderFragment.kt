package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import kotlinx.android.synthetic.main.fragment_empty_folder.*

class EmptyFolderFragment : Fragment() {

    private var mTrashedFilesCnt: Int = 0
    private var mContext: Context? = null

    private val TAG = "EmptyFolderFragment"

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.v(TAG, "onCreateView - start");
        val rootView = inflater.inflate(R.layout.fragment_empty_folder, container, false)

        var xemptyFragmentInfoId = rootView.findViewById(R.id.emptyFragmentInfoId) as TextView
//            val xemptyFragmentInfoId = view.emptyFragmentInfoId

        Log.v(TAG, "onCreateView - emptyFragmentInfoId: $emptyFragmentInfoId xemptyFragmentInfoId: $xemptyFragmentInfoId")

//            todo: investigate why 'synthetic' emptyFragmentInfoId is null
        emptyFragmentInfoId?.text = resources.getString(R.string.empty_folder, mTrashedFilesCnt)
        xemptyFragmentInfoId?.text = resources.getString(R.string.empty_folder, mTrashedFilesCnt)
//        view.emptyFragmentInfoId = resources.getString(R.string.empty_folder, mTrashedFilesCnt)
        return rootView
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.folder_fragment_menu, menu)
        return
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //		Log.i(LOC_CAT_TAG, "onPrepareOptionsMenu - start - googleApiClientConnected: " + mContext.isGoogleApiClientConnected());
        var menuItem: MenuItem
//        if (mContext!!.isGoogleApiClientConnected()) {
//            if (!mContext!!.isCameraIsAvailable()) {
//                menuItem = menu.findItem(R.id.menuQuickPhoto)
//                menuItem.isVisible = false                                    // do both to hide a menu item
//                menuItem.isEnabled = false
//            }
//            menuItem = menu.findItem(R.id.menuCreateFile)
//            menuItem.isVisible = true
//            menuItem.isEnabled = true
//            menuItem = menu.findItem(R.id.menuRefresh)
//            menuItem.isVisible = true
//            menuItem.isEnabled = true
//        } else
//        {
        menuItem = menu.findItem(R.id.menuCreateFile)
        menuItem.isVisible = false
        menuItem.isEnabled = false
        menuItem = menu.findItem(R.id.menuRefresh)
        menuItem.isVisible = false
        menuItem.isEnabled = false
//        }
        return
    }

    fun setTrashedFilesCnt(trashedFilesCnt: Int) {
        this.mTrashedFilesCnt = trashedFilesCnt
    }

    companion object {

        private val LOG_TAG = EmptyFolderFragment::class.java.simpleName
    }
}// Empty constructor required for fragment subclasses
