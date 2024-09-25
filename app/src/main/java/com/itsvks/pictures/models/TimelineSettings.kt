package com.itsvks.pictures.models

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.itsvks.pictures.util.MediaOrder

@Stable
@Entity(tableName = "timeline_settings")
data class TimelineSettings(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  @ColumnInfo(defaultValue = "0")
  val groupTimelineByMonth: Boolean = false,
  @ColumnInfo(defaultValue = "0")
  val groupTimelineInAlbums: Boolean = false,
  @ColumnInfo(defaultValue = """{"orderType":{"type":"com.itsvks.pictures.util.OrderType.Descending"},"orderType_date":{"type":"com.itsvks.pictures.util.OrderType.Descending"}}""")
  val timelineMediaOrder: MediaOrder = MediaOrder.Default,
  @ColumnInfo(defaultValue = """{"orderType":{"type":"com.itsvks.pictures.util.OrderType.Descending"},"orderType_date":{"type":"com.itsvks.pictures.util.OrderType.Descending"}}""")
  val albumMediaOrder: MediaOrder = MediaOrder.Default
)
