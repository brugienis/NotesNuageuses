package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.ListFragment
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemLongClickListener
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity

class FolderFragment : ListFragment(), OnClickListener {

    //	private TextView emptyFolderViewTv;
    //	private LinearLayout nonEmptyFolderViewLl;
    private var mContext: Context? = null
    //	private Object mActionMode;
    //	private int selectedItem = -1;
    //	private boolean isFolderEmpty = false;
    //	private Menu mMenu;
    private var trashedFilesCnt = 11

    companion object {
        private val LOG_TAG = "FolderFragment"
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        this.mContext = activity as HomeActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = listView
        listView.setOnTouchListener { v, m ->
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

//    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
//        super.onListItemClick(l, v, position, id)
//        if (mContext!!.isNotConnectedToGoogleDrive(LOG_TAG + "onListItemClick")) {
//            return
//        }
//        mContext!!.fileOrFolderClicked(position)
//    }

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

    fun setTrashedFilesCnt(trashedFilesCnt: Int) {
        this.trashedFilesCnt = trashedFilesCnt
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
            menuItem.isVisible = false
            menuItem.isEnabled = false
            menuItem = menu.findItem(R.id.menuRefresh)
            menuItem.isVisible = false
            menuItem.isEnabled = false
//        }
        return
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
        fun appIsFinishing()

        /* The method below is used in Espresso testing */
//        fun setEspressoFavoriteStopsFragmentActiveFlag(onPause: String, active: Boolean)
//
//        fun setEspressoFavoriteStopsFragmentStopRemovedFlag(source: String, removed: Boolean)
    }

}
