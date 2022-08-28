package ru.netology.nerecipe.dto

import androidx.fragment.app.Fragment

data class FragmentList(
    var fragment: Fragment,
    var className: String,
    var tag: String,
    var titleId: Int,
    var fragmentId: Int,
)
