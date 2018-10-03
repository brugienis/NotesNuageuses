package au.com.kbrsolutions.notesnuageuses.features.main.adapters

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class FolderItem(
        val fileName: String,
        val fileUpdateTime: Date,
        val mimeType: String,
        val isTrashed: Boolean,
        val itemIdxInList: Int) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readSerializable() as Date,
            source.readString(),
            1 == source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(fileName)
        writeSerializable(fileUpdateTime)
        writeString(mimeType)
        writeInt((if (isTrashed) 1 else 0))
        writeInt(itemIdxInList)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<FolderItem> = object : Parcelable.Creator<FolderItem> {
            override fun createFromParcel(source: Parcel): FolderItem = FolderItem(source)
            override fun newArray(size: Int): Array<FolderItem?> = arrayOfNulls(size)
        }
    }
}