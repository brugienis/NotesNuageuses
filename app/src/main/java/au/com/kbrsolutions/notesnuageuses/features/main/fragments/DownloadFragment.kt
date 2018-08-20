package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.kbrsolutions.notesnuageuses.R
import kotlinx.android.synthetic.main.fragment_retrieving_folder_in_progress.view.*


class DownloadFragment : Fragment() {

    private var mContext: Context? = null
    private var mRetrievingFolderName:String = "Folder name undefined"
    private var mArgsProcessed = false

    init {
        Log.v("DownloadFragment", " - initialize ")
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        this.mContext = activity
    }

    /*
	 * uncomment onCreate if there are menu items specific for this fragment
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!mArgsProcessed) {
            arguments?.let {
                mRetrievingFolderName = it.getString(ARG_RETRIEVING_FOLDER_TITLE_KEY)
            }
            mArgsProcessed = true
        }

//        mRetrievingFolderName = arguments.getString(ARG_RETRIEVING_FOLDER_TITLE_KEY)
            setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_retrieving_folder_in_progress, container,
                false)
        rootView.retrievingFolderTitleId.text =
                resources.getString(R.string.retrieving_folder_folder, mRetrievingFolderName)
        return rootView
    }

    fun setRetrievingFolderName(retrievingFolderName: String) {
        mRetrievingFolderName = retrievingFolderName
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
    interface OnDownloadFragmentInteractionListener {
//        fun setRetrievingFolderName(retrievingFolderInProgressFragment: String)
    }

    companion object {

        private val TAG = DownloadFragment::class.java.simpleName

        const val ARG_RETRIEVING_FOLDER_TITLE_KEY = "retrieving_folder_title_key"

        @JvmStatic
        fun newInstance(retievingFolderName: String) =
                DownloadFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_RETRIEVING_FOLDER_TITLE_KEY, retievingFolderName)
                    }
                    Log.v("DownloadFragment", "newInstance - arguments: $arguments ")
                }

    }

}