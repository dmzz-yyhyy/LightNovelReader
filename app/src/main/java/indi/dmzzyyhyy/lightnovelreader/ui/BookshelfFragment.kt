package indi.dmzzyyhyy.lightnovelreader.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import indi.dmzzyyhyy.lightnovelreader.R

class BookshelfFragment : Fragment() {
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_bookshelf, null)
        return view
    }
}