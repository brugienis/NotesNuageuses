package au.com.kbrsolutions.notesnuageuses.features.main.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import au.com.kbrsolutions.notesnuageuses.R
import au.com.kbrsolutions.notesnuageuses.features.base.BaseActivity
import java.text.DateFormat

class FolderArrayAdapter<T>(
        private val mContext: Context,
        private val folderOnClickListener: OnClickListener,
        private val objects: List<FolderItem>) : ArrayAdapter<FolderItem>(
        mContext, -1, objects) {

    private var fileNameTv: TextView? = null
    private var fileUpdateTsTv: TextView? = null
    private var fileImage: ImageView? = null
    private var infoImage: ImageView? = null

    fun getFolderItem(idx: Int): FolderItem {
        return objects[idx]
    }

    // fixLater: Sep 28, 2018 - remove findViewById<View>(...) calls
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.v("FolderArrayAdapter", """getView - position: ${position} """)
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                    as LayoutInflater
            view = inflater.inflate(R.layout.fragment_folder_list_screen, parent, false)
        }
        fileImage = view!!.findViewById<View>(R.id.folderFileImageId) as ImageView

        infoImage = view.findViewById<View>(R.id.infoImageId) as ImageView
        infoImage!!.setOnClickListener(folderOnClickListener)

        fileNameTv = view.findViewById<View>(R.id.fileNameId) as TextView
        fileUpdateTsTv = view.findViewById<View>(R.id.fileUpdateTsId) as TextView

        val folderItem = objects[position]
        val mimeType: String?
        if (folderItem != null) {
            if (fileNameTv != null) {
                fileNameTv!!.text = folderItem.fileName
                //				fileUpdateTsTv.setText(context.getString(R.string.folder_array_adapter_updated) + "  " + dateFormat.format(folderItem.fileUpdateTime));
                fileUpdateTsTv!!.setText(mContext.getString(R.string.folder_array_adapter_updated) + "  " + DateFormat.getDateTimeInstance().format(folderItem.fileUpdateTime))
            }
            mimeType = folderItem.mimeType
            if (mimeType != null) {
                if (folderItem.isTrashed) {        //context.foldersData.getCurrFolderMetadataInfo().get(position).mIsTrashed) {
                    fileImage!!.setBackgroundColor(Color.LTGRAY)
                    //					Log.i(TAG, "getView - will  grey name/mIsTrashed: " + folderItem.fileName + "/" + folderItem.mIsTrashed);
                } else {
                    fileImage!!.setBackgroundColor(mContext.getResources().getColor(R.color.view_not_in_focus))
                    //					Log.i(TAG, "getView - won't grey name/mIsTrashed: " + folderItem.fileName + "/" + folderItem.mIsTrashed);
                }
                when (mimeType) {
                    BaseActivity.MIME_TYPE_FOLDER -> fileImage!!.setImageResource(R.mipmap.ic_type_folder)
                    BaseActivity.MIME_TYPE_PNG_FILE -> fileImage!!.setImageResource(R.mipmap.ic_type_image)
                    else -> fileImage!!.setImageResource(R.mipmap.ic_type_file)
                }
            }
        }

        return view
    }
}
