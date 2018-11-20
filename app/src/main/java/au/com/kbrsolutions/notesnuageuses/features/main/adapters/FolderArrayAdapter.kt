package au.com.kbrsolutions.notesnuageuses.features.main.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FolderFragment
import kotlinx.android.synthetic.main.fragment_folder_list_screen.view.*
import java.text.DateFormat

class FolderArrayAdapter<T>(
        private val mContext: Context,
        private val mOnClickListener: OnFolderArrayAdapterInteractionListener,
        private val objects: List<FolderItem>) : ArrayAdapter<FolderItem>(
        mContext, -1, objects) {

    init {
        // fixLater: Oct 16, 2018 - shouldn't it be OnFolderArrayAdapterInteractionListener?
        if (mOnClickListener !is FolderFragment.OnFolderFragmentNewInteractionListener) {
            throw RuntimeException(mOnClickListener.toString() +
                    " must implement OnFolderArrayAdapterInteractionListener")
        }
    }

    private var fileNameTv: TextView? = null
    private var fileUpdateTsTv: TextView? = null
    private var fileImage: ImageView? = null
    private var infoImage: ImageView? = null

    fun getFolderItem(idx: Int): FolderItem {
        return objects[idx]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                    as LayoutInflater
            view = inflater.inflate(R.layout.fragment_folder_list_screen, parent, false)
        }
        fileImage = view!!.folderFileImageId as ImageView

        infoImage = view.infoImageId as ImageView
        infoImage!!.setOnClickListener { _ ->
            handleClickOnInfoImage(position)
        }

        fileNameTv = view.fileNameId as TextView
        fileUpdateTsTv = view.fileUpdateTsId

        val folderItem = objects[position]
        if (fileNameTv != null) {
            fileNameTv!!.text = folderItem.fileName
            //				fileUpdateTsTv.setText(context.getString(R.string.folder_array_adapter_updated) + "  " + dateFormat.format(folderItem.fileUpdateTime));
            fileUpdateTsTv!!.setText("${mContext.getString(R.string.folder_array_adapter_updated)}  " +
                    "${DateFormat.getDateTimeInstance().format(folderItem.fileUpdateTime)}")
        }
        val mimeType = folderItem.mimeType
        if (folderItem.isTrashed) {        //context.foldersData.getCurrFolderMetadataInfo().get(position).mIsTrashed) {
            fileImage!!.setBackgroundColor(Color.LTGRAY)
        } else {
            fileImage!!.setBackgroundColor(mContext.getResources().getColor(R.color.view_not_in_focus))
        }
        when (mimeType) {
            BaseActivity.MIME_TYPE_FOLDER -> fileImage!!.setImageResource(R.mipmap.ic_type_folder)
            BaseActivity.MIME_TYPE_PNG_FILE -> fileImage!!.setImageResource(R.mipmap.ic_type_image)
            else -> fileImage!!.setImageResource(R.mipmap.ic_type_file)
        }

        return view
    }

    private fun handleClickOnInfoImage(position: Int) {
        Log.v("FolderArrayAdapter", """handleClickOnInfoImage - position: $position """)
        mOnClickListener.showSelectedFileDetails(position)
    }

    /**
     * This interface must be implemented by class that created this class.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFolderArrayAdapterInteractionListener {

        fun showSelectedFileDetails(position: Int)
    }
}
