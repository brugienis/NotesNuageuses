package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.dummy.DummyContent
import kotlinx.android.synthetic.main.fragment_retrieving_folder_in_progress.*
import kotlinx.android.synthetic.main.fragment_retrieving_folder_in_progress.view.*


class RetrievingFolderInProgressFragment : Fragment() {

    private var mContext: Context? = null
    private var mRetrievingFolderInProgressFragment:String = "Folder name undefined"

    init {
        Log.v("RetrievingFolderInProgressFragment", " - init ")
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        this.mContext = activity as HomeActivity
    }

    /*
	 * uncomment onCreate if there are menu items specific for this fragment
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("RetrievingFolderInProgressFragment", "onCreate - arguments: ${arguments} ")
        mRetrievingFolderInProgressFragment =
                arguments.getString(ARG_RETRIEVING_FOLDER_TITLE_KEY)
        //    setHasOptionsMenu(true);		// remove comment if custom menu items should be added
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_retrieving_folder_in_progress, container,
                false)
        rootView.retrievingFolderTitleId.text = resources.getString(
                R.string.retrieving_folder_folder,
                mRetrievingFolderInProgressFragment)
        return rootView
    }

    fun setRetrievingFolderInProgressFragment(retrievingFolderInProgressFragment: String) {
        mRetrievingFolderInProgressFragment = retrievingFolderInProgressFragment
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnRetrievingFolderInProgressFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onRetrievingFolderInProgressFragmentInteraction(item: DummyContent.DummyItem?)
    }

    companion object {

        private val TAG = RetrievingFolderInProgressFragment::class.java.simpleName

        const val ARG_RETRIEVING_FOLDER_TITLE_KEY = "retrieving_folder_title_key"

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(retievingFolderName: String) =
                RetrievingFolderInProgressFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_RETRIEVING_FOLDER_TITLE_KEY, retievingFolderName)
                    }
                    Log.v("RetrievingFolderInProgressFragment", "newInstance - arguments: ${arguments} ")
                }

    }

}