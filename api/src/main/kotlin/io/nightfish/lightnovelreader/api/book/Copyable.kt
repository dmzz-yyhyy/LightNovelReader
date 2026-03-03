package io.nightfish.lightnovelreader.api.book

interface Copyable<T> {
    fun copy(): T
}