/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.test

import kotlinx.metadata.ClassVisitor
import kotlinx.metadata.Flags
import kotlinx.metadata.FunctionVisitor
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.jvmSignature
import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataSmokeTest {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    private fun Class<*>.readMetadata(): KotlinClassHeader {
        return getAnnotation(Metadata::class.java).run {
            KotlinClassHeader(k, mv, bv, d1, d2, xs, pn, xi)
        }
    }

    @Test
    fun listInlineFunctions() {
        @Suppress("unused")
        class L {
            val x: Int inline get() = 42
            inline fun foo(f: () -> String) = f()
            fun bar() {}
        }

        val inlineFunctions = mutableListOf<String>()

        val klass = KotlinClassMetadata.read(L::class.java.readMetadata()) as KotlinClassMetadata.Class
        klass.accept(object : ClassVisitor() {
            override fun visitFunction(flags: Int, name: String, ext: FunctionVisitor.Extensions): FunctionVisitor? = null.also {
                val desc = ext.jvmSignature
                if (Flags.Function.isInline(flags) && desc != null) {
                    inlineFunctions += desc
                }
            }
        })

        assertEquals(
            listOf("foo(Lkotlin/jvm/functions/Function0;)Ljava/lang/String;"),
            inlineFunctions
        )
    }
}
