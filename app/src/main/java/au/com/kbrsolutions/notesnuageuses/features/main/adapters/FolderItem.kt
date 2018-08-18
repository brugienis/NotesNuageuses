package au.com.kbrsolutions.notesnuageuses.features.main.adapters

import java.util.*

class FolderItem(
        val fileName: String,
        val fileUpdateTime: Date,
        val mimeType: String,
        val isTrashed: Boolean,
        val itemIdxInList: Int)