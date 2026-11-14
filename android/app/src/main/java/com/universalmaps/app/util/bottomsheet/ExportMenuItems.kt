@file:JvmName("ExportMenuItems")

package com.universalmaps.app.util.bottomsheet

import com.universalmaps.app.R
import com.universalmaps.app.sdk.bookmarks.data.FileType
import java.util.function.Consumer

@JvmOverloads
fun create(hasAreas: Boolean = false, onSelected: Consumer<FileType>): ArrayList<MenuBottomSheetItem> = arrayListOf(
    MenuBottomSheetItem(R.string.export_file, R.drawable.ic_file_kmz) { onSelected.accept(FileType.Kml) },
).apply {
    if (!hasAreas) {
        add(MenuBottomSheetItem(R.string.export_file_gpx, R.drawable.ic_file_gpx) { onSelected.accept(FileType.Gpx) })
    }
    add(
        MenuBottomSheetItem(R.string.export_file_geojson, R.drawable.ic_file_geojson) {
            onSelected.accept(FileType.GeoJson)
        },
    )
}
