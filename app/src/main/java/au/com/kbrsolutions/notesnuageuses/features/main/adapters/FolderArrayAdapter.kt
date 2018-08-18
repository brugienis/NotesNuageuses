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
import au.com.kbrsolutions.notesnuageuses.features.main.HomeActivity
import au.com.kbrsolutions.notesnuageuses.features.main.fragments.FolderFragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class FolderArrayAdapter<T>(
        private val mActivity: HomeActivity,
        context: Context, folderFragment:
        FolderFragment,
        private val objects: List<FolderItem>) : ArrayAdapter<FolderItem>(context, -1, objects) {

    private var fileNameTv: TextView? = null
    private var fileUpdateTsTv: TextView? = null
    private var fileImage: ImageView? = null
    private var infoImage: ImageView? = null
    private val folderOnClickListener: OnClickListener
    //	@SuppressLint("SimpleDateFormat")
    //	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    private val dateFormat = SimpleDateFormat("yyyy-MMM.d hh:mm:ss", Locale.getDefault())

    init {
        folderOnClickListener = folderFragment
        //		Log.i(LOC_CAT_TAG, "constructor - end - objects.size(): " + objects.size());
    }

    fun getFolderItem(idx: Int): FolderItem {
        return objects[idx]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //		Log.i(LOC_CAT_TAG, "getView - start");
        var v = convertView
        if (v == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.fragment_list_screen_layout, parent, false)
        }
        fileImage = v!!.findViewById<View>(R.id.folderFileImageId) as ImageView

        infoImage = v.findViewById<View>(R.id.infoImageId) as ImageView
        infoImage!!.setOnClickListener(folderOnClickListener)

        fileNameTv = v.findViewById<View>(R.id.fileNameId) as TextView
        fileUpdateTsTv = v.findViewById<View>(R.id.fileUpdateTsId) as TextView

        val folderItem = objects[position]
        //		Log.i(LOC_CAT_TAG, "getView - name/mIsTrashed: " + folderItem.fileName + "/" + folderItem.mIsTrashed);
        val mimeType: String?
        if (folderItem != null) {
            if (fileNameTv != null) {
                fileNameTv!!.text = folderItem.fileName
                //				fileUpdateTsTv.setText(mActivity.getString(R.string.folder_array_adapter_updated) + "  " + dateFormat.format(folderItem.fileUpdateTime));
                fileUpdateTsTv!!.setText(mActivity.getString(R.string.folder_array_adapter_updated) + "  " + DateFormat.getDateTimeInstance().format(folderItem.fileUpdateTime))
            }
            mimeType = folderItem.mimeType
            if (mimeType != null) {
                if (folderItem.isTrashed) {        //mActivity.foldersData.getCurrFolderMetadataInfo().get(position).mIsTrashed) {
                    fileImage!!.setBackgroundColor(Color.LTGRAY)
                    //					Log.i(LOC_CAT_TAG, "getView - will  grey name/mIsTrashed: " + folderItem.fileName + "/" + folderItem.mIsTrashed);
                } else {
                    fileImage!!.setBackgroundColor(mActivity.getResources().getColor(R.color.view_not_in_focus))
                    //					Log.i(LOC_CAT_TAG, "getView - won't grey name/mIsTrashed: " + folderItem.fileName + "/" + folderItem.mIsTrashed);
                }
                if (mimeType == BaseActivity.MIME_TYPE_FOLDER) {
                    fileImage!!.setImageResource(R.mipmap.ic_type_folder)
                } else if (mimeType == BaseActivity.MIME_TYPE_PNG_FILE) {
                    fileImage!!.setImageResource(R.mipmap.ic_type_image)
                } else {
                    fileImage!!.setImageResource(R.mipmap.ic_type_file)
                }
            }
        } else {
            Log.i(LOC_CAT_TAG, "getView - end - folderItem null")
        }
        return v
    }

    companion object {
        private val LOC_CAT_TAG = "FolderArrayAdapter"
    }

    //	private OnClickListener MyOnClickListener  = new OnClickListener() {
    //
    //		@Override
    //		public void onClick(View v) {
    //			Log.i(LOC_CAT_TAG, "onClick - start");
    //      final int position = getListView().getPositionForView(v);
    //      if (position != ListView.INVALID_POSITION) {
    //          showMessage(getString(R.string.you_want_to_buy_format, CHEESES[position]));
    //      }
    //		}
    //
    //	};
}
