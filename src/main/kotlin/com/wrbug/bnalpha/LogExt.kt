package com.wrbug.bnalpha

import com.wrbug.base.util.DateUtils

fun log(vararg msgs: String?) {
    val time = DateUtils.time
    msgs.forEach { msg ->
        val arr = msg?.split("\n")
        arr?.forEach {
            println(time + "\t" + it)
        }
    }

}