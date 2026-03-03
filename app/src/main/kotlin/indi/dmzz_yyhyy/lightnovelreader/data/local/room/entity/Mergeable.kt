package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

interface Mergeable<T> {
    fun merge(new: T): T
}