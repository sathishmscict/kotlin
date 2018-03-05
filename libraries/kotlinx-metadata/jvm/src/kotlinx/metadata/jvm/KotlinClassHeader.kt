/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.jvm

class KotlinClassHeader(
    kind: Int?,
    metadataVersion: IntArray?,
    bytecodeVersion: IntArray?,
    data1: Array<String>?,
    data2: Array<String>?,
    extraString: String?,
    packageName: String?,
    extraInt: Int?
) {
    val kind: Int = kind ?: 1

    val metadataVersion: IntArray = metadataVersion ?: intArrayOf()

    val bytecodeVersion: IntArray = bytecodeVersion ?: intArrayOf()

    val data1: Array<String> = data1 ?: emptyArray()

    val data2: Array<String> = data2 ?: emptyArray()

    val extraString: String = extraString ?: ""

    val packageName: String = packageName ?: ""

    val extraInt: Int = extraInt ?: 0

    companion object {
        const val CLASS_KIND = 1
        const val FILE_FACADE_KIND = 2
        const val SYNTHETIC_CLASS_KIND = 3
        const val MULTI_FILE_CLASS_FACADE_KIND = 4
        const val MULTI_FILE_CLASS_PART_KIND = 5
    }
}
