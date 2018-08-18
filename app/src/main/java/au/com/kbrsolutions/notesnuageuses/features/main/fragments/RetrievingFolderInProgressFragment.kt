package au.com.kbrsolutions.notesnuageuses.features.main.fragments

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity


class RetrievingFolderInProgressFragment : Fragment() {

    private var mContext: Context? = null

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        this.mContext = activity as HomeActivity
    }

    /*
	 * uncomment onCreate if there are menu items specific for this fragment
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //    setHasOptionsMenu(true);		// remove comment if custom menu items should be added
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        return inflater.inflate(R.layout.fragment_retrieving_folder_in_progress, container,
                false)
    }

    companion object {

        private val LOG_TAG = RetrievingFolderInProgressFragment::class.java.simpleName
    }

}// Empty constructor required for fragment subclasses