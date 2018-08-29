package au.com.kbrsolutions.notesnuageuses.features.main.dialogs

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import kotlinx.android.synthetic.main.create_file_dialog.view.*
import org.greenrobot.eventbus.EventBus

class CreateFileDialog : DialogFragment() {

    private var fileNameTv: TextView? = null
    private var createFolderTv: TextView? = null
    private var createPhotoTv: TextView? = null
    private var createTextTv: TextView? = null
    private var cancelTv: TextView? = null
    private var mActivity: HomeActivity? = null
    private var eventBus: EventBus? = null

    private var listener: OnCreateFileDialogInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateFileDialogInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnCreateFileDialogInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.create_file_dialog, container, false)

        fileNameTv = rootView.createDialogFileNameId

        rootView.createDialogFolderId!!.setOnClickListener { createFolder() }

        rootView.createDialogPhotoId!!.setOnClickListener { createPhotoNote() }

        rootView.createDialogTextId!!.setOnClickListener { createTextNote() }

        rootView.createDialogCancelId!!.setOnClickListener { cancelCreateFileDialog() }

        return rootView
    }

    private fun createFolder() {
        Log.i(LOC_CAT_TAG, "createFolder - start")
        listener!!.createFolder(fileNameTv!!.text)
        dismiss()
        // move code below to a new method in HomeActivity
        //		FolderFileNameDialog fileNameDialogFragment = FolderFileNameDialog.newInstance();
        //		fileNameDialogFragment.setParentFragment(this);
        //		fileNameDialogFragment.show(mActivity.getFragmentManager(), "dialog");
    }

    private fun cancelCreateFileDialog() {
        Log.i(LOC_CAT_TAG, "createFolder - start")
        dismiss()
    }

    private fun createPhotoNote() {
        Log.i(LOC_CAT_TAG, "createPhoto - start")
        listener!!.createPhotoNote()
        dismiss()
    }

    private fun createTextNote() {
        Log.i(LOC_CAT_TAG, "createFolder - start")
        listener!!.createTextNote()
        dismiss()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
    interface OnCreateFileDialogInteractionListener {
        fun createFolder(fileName: CharSequence)
        fun createPhotoNote()
        fun createTextNote()

    }

    companion object {
        //	private FolderNameSelectable folderNameSelectable;

        private val LOC_CAT_TAG = CreateFileDialog::class.java.simpleName

        fun newInstance(): CreateFileDialog {

            return CreateFileDialog()
        }
    }
}
