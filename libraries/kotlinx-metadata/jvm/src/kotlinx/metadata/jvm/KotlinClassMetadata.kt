/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.jvm

import kotlinx.metadata.ClassVisitor
import kotlinx.metadata.InconsistentKotlinMetadataException
import kotlinx.metadata.LambdaVisitor
import kotlinx.metadata.PackageVisitor
import kotlinx.metadata.impl.ClassWriter
import kotlinx.metadata.impl.LambdaWriter
import kotlinx.metadata.impl.PackageWriter
import kotlinx.metadata.impl.accept
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.metadata.jvm.serialization.JvmStringTable
import kotlin.LazyThreadSafetyMode.PUBLICATION

sealed class KotlinClassMetadata(val header: KotlinClassHeader) {
    class Class internal constructor(header: KotlinClassHeader) : KotlinClassMetadata(header) {
        private val classData by lazy(PUBLICATION) {
            val data1 = (header.data1.takeIf(Array<*>::isNotEmpty)
                    ?: throw InconsistentKotlinMetadataException("data1 must not be empty"))
            JvmProtoBufUtil.readClassDataFrom(data1, header.data2)
        }

        fun accept(v: ClassVisitor) {
            val (strings, proto) = classData
            proto.accept(v, strings)
        }

        class Writer : ClassWriter() {
            @JvmOverloads
            fun write(
                metadataVersion: IntArray = KotlinClassHeader.COMPATIBLE_METADATA_VERSION,
                bytecodeVersion: IntArray = KotlinClassHeader.COMPATIBLE_BYTECODE_VERSION,
                extraInt: Int = 0
            ): KotlinClassMetadata.Class {
                val (d1, d2) = JvmProtoBufUtil.writeData(t.build(), c.strings as JvmStringTable)
                val metadata = KotlinClassHeader(
                    KotlinClassHeader.CLASS_KIND, metadataVersion, bytecodeVersion, d1, d2, null, null, extraInt
                )
                return KotlinClassMetadata.Class(metadata)
            }
        }
    }

    class FileFacade internal constructor(header: KotlinClassHeader) : KotlinClassMetadata(header) {
        private val packageData by lazy(PUBLICATION) {
            val data1 = (header.data1.takeIf(Array<*>::isNotEmpty)
                    ?: throw InconsistentKotlinMetadataException("data1 must not be empty"))
            JvmProtoBufUtil.readPackageDataFrom(data1, header.data2)
        }

        fun accept(v: PackageVisitor) {
            val (strings, proto) = packageData
            proto.accept(v, strings)
        }

        class Writer : PackageWriter() {
            @JvmOverloads
            fun write(
                metadataVersion: IntArray = KotlinClassHeader.COMPATIBLE_METADATA_VERSION,
                bytecodeVersion: IntArray = KotlinClassHeader.COMPATIBLE_BYTECODE_VERSION,
                extraInt: Int = 0
            ): KotlinClassMetadata.FileFacade {
                val (d1, d2) = JvmProtoBufUtil.writeData(t.build(), c.strings as JvmStringTable)
                val metadata = KotlinClassHeader(
                    KotlinClassHeader.FILE_FACADE_KIND, metadataVersion, bytecodeVersion, d1, d2, null, null, extraInt
                )
                return KotlinClassMetadata.FileFacade(metadata)
            }
        }
    }

    class SyntheticClass internal constructor(header: KotlinClassHeader) : KotlinClassMetadata(header) {
        private val functionData by lazy(PUBLICATION) {
            header.data1.takeIf(Array<*>::isNotEmpty)?.let { data1 ->
                JvmProtoBufUtil.readFunctionDataFrom(data1, header.data2)
            }
        }

        val isLambda: Boolean
            get() = header.data1.isNotEmpty()

        fun accept(v: LambdaVisitor) {
            if (!isLambda) throw IllegalStateException(
                "accept(LambdaVisitor) is only possible for synthetic classes which are lambdas (isLambda = true)"
            )

            val (strings, proto) = functionData!!
            proto.accept(v, strings)
        }

        class Writer : LambdaWriter() {
            @JvmOverloads
            fun write(
                metadataVersion: IntArray = KotlinClassHeader.COMPATIBLE_METADATA_VERSION,
                bytecodeVersion: IntArray = KotlinClassHeader.COMPATIBLE_BYTECODE_VERSION,
                extraInt: Int = 0
            ): KotlinClassMetadata.SyntheticClass {
                val proto = t?.build()
                val (d1, d2) =
                        if (proto != null) JvmProtoBufUtil.writeData(proto, c.strings as JvmStringTable)
                        else Pair(emptyArray(), emptyArray())
                val metadata = KotlinClassHeader(
                    KotlinClassHeader.SYNTHETIC_CLASS_KIND, metadataVersion, bytecodeVersion, d1, d2, null, null, extraInt
                )
                return KotlinClassMetadata.SyntheticClass(metadata)
            }
        }
    }

    class MultiFileClassFacade internal constructor(header: KotlinClassHeader) : KotlinClassMetadata(header) {
        val partClassNames: List<String> = header.data1.asList()

        class Writer {
            @JvmOverloads
            fun write(
                partClassNames: List<String>,
                metadataVersion: IntArray = KotlinClassHeader.COMPATIBLE_METADATA_VERSION,
                bytecodeVersion: IntArray = KotlinClassHeader.COMPATIBLE_BYTECODE_VERSION,
                extraInt: Int = 0
            ): KotlinClassMetadata.MultiFileClassFacade {
                val metadata = KotlinClassHeader(
                    KotlinClassHeader.MULTI_FILE_CLASS_FACADE_KIND, metadataVersion, bytecodeVersion, partClassNames.toTypedArray(),
                    null, null, null, extraInt
                )
                return KotlinClassMetadata.MultiFileClassFacade(metadata)
            }
        }
    }

    class MultiFileClassPart internal constructor(header: KotlinClassHeader) : KotlinClassMetadata(header) {
        private val packageData by lazy(PUBLICATION) {
            val data1 = (header.data1.takeIf(Array<*>::isNotEmpty)
                    ?: throw InconsistentKotlinMetadataException("data1 must not be empty"))
            JvmProtoBufUtil.readPackageDataFrom(data1, header.data2)
        }

        val facadeClassName: String
            get() = header.extraString

        fun accept(v: PackageVisitor) {
            val (strings, proto) = packageData
            proto.accept(v, strings)
        }

        class Writer : PackageWriter() {
            @JvmOverloads
            fun write(
                facadeClassName: String,
                metadataVersion: IntArray = KotlinClassHeader.COMPATIBLE_METADATA_VERSION,
                bytecodeVersion: IntArray = KotlinClassHeader.COMPATIBLE_BYTECODE_VERSION,
                extraInt: Int = 0
            ): KotlinClassMetadata.MultiFileClassPart {
                val (d1, d2) = JvmProtoBufUtil.writeData(t.build(), c.strings as JvmStringTable)
                val metadata = KotlinClassHeader(
                    KotlinClassHeader.MULTI_FILE_CLASS_PART_KIND, metadataVersion, bytecodeVersion, d1, d2, facadeClassName, null, extraInt
                )
                return KotlinClassMetadata.MultiFileClassPart(metadata)
            }
        }
    }

    class Unknown internal constructor(header: KotlinClassHeader) : KotlinClassMetadata(header)

    companion object {
        /**
         * Reads and parses the given Kotlin metadata and returns the correct type of [KotlinClassMetadata] encoded by this metadata,
         * or `null` if this metadata encodes an unsupported kind of Kotlin classes or has an unsupported metadata version.
         *
         * Throws [InconsistentKotlinMetadataException] if the metadata has inconsistencies which signal that it may have been
         * modified by a separate tool.
         */
        @JvmStatic
        fun read(header: KotlinClassHeader): KotlinClassMetadata? {
            // We only support metadata of version 1.1.* (this is Kotlin from 1.0 until today)
            val version = header.metadataVersion
            if (version.getOrNull(0) != 1 || version.getOrNull(1) != 1) return null

            return try {
                when (header.kind) {
                    KotlinClassHeader.CLASS_KIND -> KotlinClassMetadata.Class(header)
                    KotlinClassHeader.FILE_FACADE_KIND -> KotlinClassMetadata.FileFacade(header)
                    KotlinClassHeader.SYNTHETIC_CLASS_KIND -> KotlinClassMetadata.SyntheticClass(header)
                    KotlinClassHeader.MULTI_FILE_CLASS_FACADE_KIND -> KotlinClassMetadata.MultiFileClassFacade(header)
                    KotlinClassHeader.MULTI_FILE_CLASS_PART_KIND -> KotlinClassMetadata.MultiFileClassPart(header)
                    else -> KotlinClassMetadata.Unknown(header)
                }
            } catch (e: InconsistentKotlinMetadataException) {
                throw e
            } catch (e: Throwable) {
                throw InconsistentKotlinMetadataException("Exception occurred when reading Kotlin metadata", e)
            }
        }
    }
}
