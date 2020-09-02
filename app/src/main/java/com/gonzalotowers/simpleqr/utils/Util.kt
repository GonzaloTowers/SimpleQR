package com.gonzalotowers.simpleqr.utils

class Util {

    companion object {

        fun getExtension(url: String): String? {
            val filenameArray = url.split("\\.").toTypedArray()
            return filenameArray[filenameArray.size - 1]
        }

        fun containsFile(url: String): Boolean {
            return url.contains("pdf", ignoreCase = true) || url.contains("docx", ignoreCase = true)
        }
    }

}