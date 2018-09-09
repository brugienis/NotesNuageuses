package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.kbrsolutions.notesnuageuses.R
import kotlinx.android.synthetic.main.fragment_retrieving_folder_in_progress.view.*


class DownloadFragment : Fragment() {

    private var mContext: Context? = null
    private var mRetrievingFolderFileName:String = "Folder/file name undefined"
    private var mArgsProcessed = false

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
                mRetrievingFolderFileName = it.getString(ARG_RETRIEVING_FOLDER_FILE_NAME_KEY)
            }
            mArgsProcessed = true
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_retrieving_folder_in_progress, container,
                false)
        rootView.retrievingFolderTitleId.text =
                resources.getString(R.string.retrieving_folder_file, mRetrievingFolderFileName)
        return rootView
    }

    fun setRetrievingFolderName(retrievingFolderName: String) {
        mRetrievingFolderFileName = retrievingFolderName
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
    interface OnDownloadFragmentInteractionListener {
//        fun setRetrievingFolderName(retrievingFolderInProgressFragment: String)
    }

    companion object {

        private val TAG = DownloadFragment::class.java.simpleName

        const val ARG_RETRIEVING_FOLDER_FILE_NAME_KEY = "arg_retrieving_folder_file_name_key"

        @JvmStatic
        fun newInstance(retrievingFolderFileName: String) =
                DownloadFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_RETRIEVING_FOLDER_FILE_NAME_KEY, retrievingFolderFileName)
                    }
//                    Log.v("DownloadFragment", "newInstance - arguments: $arguments ")
                }

    }

}