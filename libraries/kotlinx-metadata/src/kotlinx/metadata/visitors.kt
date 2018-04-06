/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata

data class ClassName @JvmOverloads constructor(val name: String, val isLocal: Boolean = false) {
    override fun toString(): String = name
}

/**
 * A visitor to visit Kotlin declarations, which are containers of other declarations: functions, properties and type aliases.
 */
abstract class DeclarationContainerVisitor @JvmOverloads constructor(protected open val delegate: DeclarationContainerVisitor? = null) {
    /**
     * Visits a function in the container.
     *
     * @param flags function flags, consisting of [Flags.hasAnnotations], visibility flag, modality flag and [Flags.Function] flags
     * @param name the name of the function
     * @param ext platform-specific extensions for the function
     */
    open fun visitFunction(flags: Int, name: String, ext: FunctionVisitor.Extensions): FunctionVisitor? =
        delegate?.visitFunction(flags, name, ext)

    /**
     * Visits a property in the container.
     *
     * @param flags property flags, consisting of [Flags.hasAnnotations], visibility flag, modality flag and [Flags.Property] flags
     * @param name the name of the property
     * @param getterFlags property accessor flags, consisting of [Flags.hasAnnotations], visibility flag, modality flag
     *                    and [Flags.PropertyAccessor] flags
     * @param setterFlags property accessor flags, consisting of [Flags.hasAnnotations], visibility flag, modality flag
     *                    and [Flags.PropertyAccessor] flags
     * @param ext platform-specific extensions for the property
     */
    open fun visitProperty(
        flags: Int,
        name: String,
        getterFlags: Int,
        setterFlags: Int,
        ext: PropertyVisitor.Extensions
    ): PropertyVisitor? =
        delegate?.visitProperty(flags, name, getterFlags, setterFlags, ext)

    /**
     * Visits a type alias in the container.
     *
     * @param flags type alias flags, consisting of [Flags.hasAnnotations] and visibility flag
     * @param name the name of the type alias
     */
    open fun visitTypeAlias(flags: Int, name: String): TypeAliasVisitor? =
        delegate?.visitTypeAlias(flags, name)
}

/**
 * A visitor to visit Kotlin classes, including interfaces, objects, enum classes and annotation classes.
 *
 * When using this class, [visit] must be called first, followed by zero or more [visitTypeParameter] calls, followed by zero or more calls
 * to other visit* methods, followed by [visitEnd].
 */
abstract class ClassVisitor @JvmOverloads constructor(delegate: ClassVisitor? = null) : DeclarationContainerVisitor(delegate) {
    override val delegate: ClassVisitor?
        get() = super.delegate as ClassVisitor?

    /**
     * Visits the basic information about the class.
     *
     * @param flags class flags, consisting of [Flags.hasAnnotations], visibility flag, modality flag and [Flags.Class] flags
     * @param name the name of the class
     */
    open fun visit(flags: Int, name: ClassName) {
        delegate?.visit(flags, name)
    }

    /**
     * Visits a type parameter of the class.
     *
     * @param flags type parameter flags, consisting of [Flags.TypeParameter] flags
     * @param name the name of the type parameter
     * @param id the id of the type parameter, useful to be able to uniquely identify the type parameter in different contexts where
     *           the name isn't enough (e.g. `class A<T> { fun <T> foo(t: T) }`)
     * @param variance the declaration-site variance of the type parameter
     * @param ext platform-specific extensions for the type parameter
     */
    open fun visitTypeParameter(
        flags: Int, name: String, id: Int, variance: Variance, ext: TypeParameterVisitor.Extensions
    ): TypeParameterVisitor? =
        delegate?.visitTypeParameter(flags, name, id, variance, ext)

    /**
     * Visits a supertype of the class.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitSupertype(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitSupertype(flags, ext)

    /**
     * Visits a constructor of the class.
     *
     * @param flags constructor flags, consisting of [Flags.hasAnnotations], visibility flag and [Flags.Constructor] flags
     * @param ext platform-specific extensions for the constructor
     */
    open fun visitConstructor(flags: Int, ext: ConstructorVisitor.Extensions): ConstructorVisitor? =
        delegate?.visitConstructor(flags, ext)

    /**
     * Visits the name of the companion object of this class, if it has one.
     *
     * @param name the name of the companion object
     */
    open fun visitCompanionObject(name: String) {
        delegate?.visitCompanionObject(name)
    }

    /**
     * Visits the name of a nested class of this class.
     *
     * @param name the name of a nested class
     */
    open fun visitNestedClass(name: String) {
        delegate?.visitNestedClass(name)
    }

    /**
     * Visits the name of an enum entry, if this class is an enum class.
     *
     * @param name the name of an enum entry
     */
    open fun visitEnumEntry(name: String) {
        delegate?.visitEnumEntry(name)
    }

    /**
     * Visits the name of a direct subclass of this class, if this class is `sealed`.
     *
     * @param name the name of a direct subclass
     */
    open fun visitSealedSubclass(name: ClassName) {
        delegate?.visitSealedSubclass(name)
    }

    /**
     * Visits the version requirement on this class.
     */
    open fun visitVersionRequirement(): VersionRequirementVisitor? =
        delegate?.visitVersionRequirement()

    /**
     * Visits the end of the class.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit Kotlin package fragments, including single file facades and multi-file class parts.
 *
 * When using this class, [visitEnd] must be called exactly once and after calls to all other visit* methods.
 */
abstract class PackageVisitor @JvmOverloads constructor(delegate: PackageVisitor? = null) : DeclarationContainerVisitor(delegate) {
    override val delegate: PackageVisitor?
        get() = super.delegate as PackageVisitor?

    /**
     * Visits the end of the package fragment.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit the metadata of a synthetic class generated for a Kotlin lambda.
 *
 * When using this class, [visitFunction] must be called first, followed by [visitEnd].
 */
abstract class LambdaVisitor @JvmOverloads constructor(private val delegate: LambdaVisitor? = null) {
    /**
     * Visits the signature of a synthetic anonymous function, representing the lambda.
     *
     * @param flags function flags, consisting of [Flags.hasAnnotations], visibility flag, modality flag and [Flags.Function] flags
     * @param name the name of the function (`"<anonymous>"` for lambdas emitted by the Kotlin compiler)
     * @param ext platform-specific extensions for the function
     */
    open fun visitFunction(flags: Int, name: String, ext: FunctionVisitor.Extensions): FunctionVisitor? =
        delegate?.visitFunction(flags, name, ext)

    /**
     * Visits the end of the lambda.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a constructor of a Kotlin class.
 *
 * When using this class, [visitEnd] must be called exactly once and after calls to all other visit* methods.
 */
abstract class ConstructorVisitor @JvmOverloads constructor(private val delegate: ConstructorVisitor? = null) {
    /**
     * Classes implementing this interface can provide platform-specific extensions to the metadata of the constructor.
     */
    interface Extensions

    /**
     * Visits a value parameter of the constructor.
     *
     * @param flags value parameter flags, consisting of [Flags.ValueParameter] flags
     * @param name the name of the value parameter
     */
    open fun visitValueParameter(flags: Int, name: String): ValueParameterVisitor? =
        delegate?.visitValueParameter(flags, name)

    /**
     * Visits the version requirement on this constructor.
     */
    open fun visitVersionRequirement(): VersionRequirementVisitor? =
        delegate?.visitVersionRequirement()

    /**
     * Visits the end of the constructor.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a Kotlin function declaration.
 *
 * When using this class, zero or more calls to [visitTypeParameter] must be done first, followed by zero or more calls
 * to other visit* methods, followed by [visitEnd].
 */
abstract class FunctionVisitor @JvmOverloads constructor(private val delegate: FunctionVisitor? = null) {
    /**
     * Classes implementing this interface can provide platform-specific extensions to the metadata of the function.
     */
    interface Extensions

    /**
     * Visits a type parameter of the function.
     *
     * @param flags type parameter flags, consisting of [Flags.TypeParameter] flags
     * @param name the name of the type parameter
     * @param id the id of the type parameter, useful to be able to uniquely identify the type parameter in different contexts where
     *           the name isn't enough (e.g. `class A<T> { fun <T> foo(t: T) }`)
     * @param variance the declaration-site variance of the type parameter
     * @param ext platform-specific extensions for the type parameter
     */
    open fun visitTypeParameter(
        flags: Int, name: String, id: Int, variance: Variance, ext: TypeParameterVisitor.Extensions
    ): TypeParameterVisitor? =
        delegate?.visitTypeParameter(flags, name, id, variance, ext)

    /**
     * Visits the type of the receiver of the function, if this is an extension function.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitReceiverParameterType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitReceiverParameterType(flags, ext)

    /**
     * Visits a value parameter of the function.
     *
     * @param flags value parameter flags, consisting of [Flags.ValueParameter] flags
     * @param name the name of the value parameter
     */
    open fun visitValueParameter(flags: Int, name: String): ValueParameterVisitor? =
        delegate?.visitValueParameter(flags, name)

    /**
     * Visits the return type of the function.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitReturnType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitReturnType(flags, ext)

    /**
     * Visits the version requirement on this function.
     */
    open fun visitVersionRequirement(): VersionRequirementVisitor? =
        delegate?.visitVersionRequirement()

    /**
     * Visits the contract of the function.
     */
    open fun visitContract(): ContractVisitor? =
        delegate?.visitContract()

    /**
     * Visits the end of the function.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a Kotlin property declaration.
 *
 * When using this class, zero or more calls to [visitTypeParameter] must be done first, followed by zero or more calls
 * to other visit* methods, followed by [visitEnd].
 */
abstract class PropertyVisitor @JvmOverloads constructor(private val delegate: PropertyVisitor? = null) {
    /**
     * Classes implementing this interface can provide platform-specific extensions to the metadata of the property.
     */
    interface Extensions

    /**
     * Visits a type parameter of the property.
     *
     * @param flags type parameter flags, consisting of [Flags.TypeParameter] flags
     * @param name the name of the type parameter
     * @param id the id of the type parameter, useful to be able to uniquely identify the type parameter in different contexts where
     *           the name isn't enough (e.g. `class A<T> { fun <T> foo(t: T) }`)
     * @param variance the declaration-site variance of the type parameter
     * @param ext platform-specific extensions for the type parameter
     */
    open fun visitTypeParameter(
        flags: Int, name: String, id: Int, variance: Variance, ext: TypeParameterVisitor.Extensions
    ): TypeParameterVisitor? =
        delegate?.visitTypeParameter(flags, name, id, variance, ext)

    /**
     * Visits the type of the receiver of the property, if this is an extension property.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitReceiverParameterType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitReceiverParameterType(flags, ext)

    /**
     * Visits a value parameter of the setter of this property, if this is a `var` property.
     *
     * @param flags value parameter flags, consisting of [Flags.ValueParameter] flags
     * @param name the name of the value parameter (`"<set-?>"` for properties emitted by the Kotlin compiler)
     */
    open fun visitSetterParameter(flags: Int, name: String): ValueParameterVisitor? =
        delegate?.visitSetterParameter(flags, name)

    /**
     * Visits the type of the property.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitReturnType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitReturnType(flags, ext)

    /**
     * Visits the version requirement on this property.
     */
    open fun visitVersionRequirement(): VersionRequirementVisitor? =
        delegate?.visitVersionRequirement()

    /**
     * Visits the end of the property.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a Kotlin type alias declaration.
 *
 * When using this class, zero or more calls to [visitTypeParameter] must be done first, followed by zero or more calls
 * to other visit* methods, followed by [visitEnd].
 */
abstract class TypeAliasVisitor @JvmOverloads constructor(private val delegate: TypeAliasVisitor? = null) {
    /**
     * Visits a type parameter of the type alias.
     *
     * @param flags type parameter flags, consisting of [Flags.TypeParameter] flags
     * @param name the name of the type parameter
     * @param id the id of the type parameter, useful to be able to uniquely identify the type parameter in different contexts where
     *           the name isn't enough (e.g. `class A<T> { fun <T> foo(t: T) }`)
     * @param variance the declaration-site variance of the type parameter
     * @param ext platform-specific extensions for the type parameter
     */
    open fun visitTypeParameter(
        flags: Int, name: String, id: Int, variance: Variance, ext: TypeParameterVisitor.Extensions
    ): TypeParameterVisitor? =
        delegate?.visitTypeParameter(flags, name, id, variance, ext)

    /**
     * Visits the underlying type of the type alias, i.e. the type in the right-hand side of the type alias declaration.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitUnderlyingType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitUnderlyingType(flags, ext)

    /**
     * Visits the expanded type of the type alias, i.e. the full expansion of the underlying type, where all type aliases are substituted
     * with their expanded types. If not type aliases are used in the underlying type, expanded type is equal to the underlying type.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitExpandedType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitExpandedType(flags, ext)

    /**
     * Visits the annotation on the type alias.
     *
     * @param annotation annotation on the type alias
     */
    open fun visitAnnotation(annotation: Annotation) {
        delegate?.visitAnnotation(annotation)
    }

    /**
     * Visits the version requirement on this type alias.
     */
    open fun visitVersionRequirement(): VersionRequirementVisitor? =
        delegate?.visitVersionRequirement()

    /**
     * Visits the end of the type alias.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a value parameter of a Kotlin constructor, function or property setter.
 *
 * When using this class, either [visitType] or [visitVarargElementType] must be called first (depending on whether the value parameter
 * is `vararg` or not), followed by [visitEnd].
 */
abstract class ValueParameterVisitor @JvmOverloads constructor(private val delegate: ValueParameterVisitor? = null) {
    /**
     * Visits the type of the value parameter, if this is **not** a `vararg` parameter.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitType(flags, ext)

    /**
     * Visits the type of the value parameter, if this is a `vararg` parameter.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitVarargElementType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitVarargElementType(flags, ext)

    /**
     * Visits the end of the value parameter.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a type parameter of a Kotlin class, function or property.
 *
 * When using this class, zero or more [visitUpperBound] calls must be done first, followed by [visitEnd].
 */
abstract class TypeParameterVisitor @JvmOverloads constructor(private val delegate: TypeParameterVisitor? = null) {
    /**
     * Classes implementing this interface can provide platform-specific extensions to the metadata of the type parameter.
     */
    interface Extensions

    /**
     * Visits the upper bound of the type parameter.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitUpperBound(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitUpperBound(flags, ext)

    /**
     * Visits the end of the type parameter.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit a type. The type must have a classifier which is one of: a class [visitClass], type parameter [visitTypeParameter]
 * or type alias [visitTypeAlias]. If the type's classifier is a class or a type alias, it can have type arguments ([visitArgument] and
 * [visitStarProjection]). If the type's classifier is an inner class, it can have the outer type ([visitOuterType]), which captures
 * the generic type arguments of the outer class. Also, each type can have an abbreviation ([visitAbbreviatedType]) in case a type alias
 * was used originally at this site in the declaration (all types are expanded by default for metadata produced by the Kotlin compiler).
 * If [visitFlexibleTypeUpperBound] is called, this type is regarded as a flexible type, and its contents represent the lower bound,
 * and the result of the call represents the upper bound.
 *
 * When using this class, [visitEnd] must be called exactly once and after calls to all other visit* methods.
 */
abstract class TypeVisitor @JvmOverloads constructor(private val delegate: TypeVisitor? = null) {
    /**
     * Classes implementing this interface can provide platform-specific extensions to the metadata of the type.
     */
    interface Extensions

    /**
     * Visits the name of the class, if this type's classifier is a class.
     *
     * @param name the name of the class
     */
    open fun visitClass(name: ClassName) {
        delegate?.visitClass(name)
    }

    /**
     * Visits the name of the type alias, if this type's classifier is a type alias. Note that all types are expanded for metadata produced
     * by the Kotlin compiler, so the the type with a type alias classifier may only appear in a call to [visitAbbreviatedType].
     *
     * @param name the name of the type alias
     */
    open fun visitTypeAlias(name: ClassName) {
        delegate?.visitTypeAlias(name)
    }

    /**
     * Visits the id of the type parameter, if this type's classifier is a type parameter.
     *
     * @param the id of the type parameter
     */
    open fun visitTypeParameter(id: Int) {
        delegate?.visitTypeParameter(id)
    }

    /**
     * Visits the type projection used in a type argument of the type based on a class or on a type alias.
     * For example, in `MutableMap<in String?, *>`, `in String?` is the type projection which is the first type argument of the type.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param variance the variance of the type projection
     * @param ext platform-specific extensions for the type
     */
    open fun visitArgument(flags: Int, variance: Variance, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitArgument(flags, variance, ext)

    /**
     * Visits the star (`*`) projection used in a type argument of the type based on a class or on a type alias.
     * For example, in `MutableMap<in String?, *>`, `*` is the star projection which is the second type argument of the type.
     */
    open fun visitStarProjection() {
        delegate?.visitStarProjection()
    }

    /**
     * Visits the abbreviation of this type. Note that all types are expanded for metadata produced by the Kotlin compiler. For example:
     *
     *     typealias A<T> = MutableList<T>
     *
     *     fun foo(a: A<Any>) {}
     *
     * The type of the `foo`'s parameter in the metadata is actually `MutableList<Any>`, and its abbreviation is `A<Any>`.
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitAbbreviatedType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitAbbreviatedType(flags, ext)

    /**
     * Visits the outer type, if this type's classifier is an inner class. For example:
     *
     *     class A<T> { inner class B<U> }
     *
     *     fun foo(a: A<*>.B<Byte?>) {}
     *
     * The type of the `foo`'s parameter in the metadata is `B<Byte>` (a type whose classifier is class `B`, and it has one type argument,
     * type `Byte?`), and its outer type is `A<*>` (a type whose classifier is class `A`, and it has one type argument, star projection).
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitOuterType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitOuterType(flags, ext)

    /**
     * Visits the upper bound of the type, marking it as flexible and its contents as the lower bound. Flexible types in Kotlin include
     * platform types in Kotlin/JVM and `dynamic` type in Kotlin/JS.
     *
     * TODO: typflexibility id
     *
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitFlexibleTypeUpperBound(flags: Int, typeFlexibilityId: String?, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitFlexibleTypeUpperBound(flags, typeFlexibilityId, ext)

    /**
     * Visits the end of the type.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit the contents of a version requirement on a Kotlin declaration.
 *
 * Version requirement is an internal feature of the Kotlin compiler and the standard Kotlin library,
 * enabled for example with the internal [kotlin.internal.RequireKotlin] annotation.
 *
 * When using this class, [visit] must be called first, followed by [visitVersion], followed by [visitEnd].
 */
abstract class VersionRequirementVisitor @JvmOverloads constructor(private val delegate: VersionRequirementVisitor? = null) {
    /**
     * Visits the description of this version requirement.
     *
     * @param kind the kind of the version that this declaration requires: compiler, language or API version
     * @param level the level of the diagnostic that must be reported on the usages of the declaration in case
     *              the version requirement is not satisfied
     * @param errorCode optional error code to be displayed in the diagnostic
     * @param message optional message to be displayed in the diagnostic
     */
    open fun visit(kind: VersionRequirementVersionKind, level: VersionRequirementLevel, errorCode: Int?, message: String?) {
        delegate?.visit(kind, level, errorCode, message)
    }

    /**
     * Visits the version required by this requirement.
     *
     * @param major the major component of the version (e.g. "1" in "1.2.3")
     * @param minor the minor component of the version (e.g. "2" in "1.2.3")
     * @param patch the patch component of the version (e.g. "3" in "1.2.3")
     */
    open fun visitVersion(major: Int, minor: Int, patch: Int) {
        delegate?.visitVersion(major, minor, patch)
    }

    /**
     * Visits the end of the version requirement.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit the contents of the contract of a Kotlin function.
 *
 * Contracts are an internal feature of the standard Kotlin library, and their behavior and/or binary format
 * may change in a subsequent release.
 *
 * When using this class, zero or more calls to [visitEffect] must be done first, followed by [visitEnd].
 */
abstract class ContractVisitor @JvmOverloads constructor(private val delegate: ContractVisitor? = null) {
    /**
     * Visits an effect of this contract.
     * 
     * @param type optional type of the effect
     * @param invocationKind optional number of invocations of the lambda parameter of this function,
     *                       specified further in the effect expression
     */
    open fun visitEffect(type: EffectType?, invocationKind: EffectInvocationKind?): EffectVisitor? =
        delegate?.visitEffect(type, invocationKind)

    /**
     * Visits the end of the contract.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit an effect (a part of the contract of a Kotlin function).
 *
 * Contracts are an internal feature of the standard Kotlin library, and their behavior and/or binary format
 * may change in a subsequent release.
 *
 * When using this class, zero or more calls to [visitConstructorArgument] or [visitConclusionOfConditionalEffect] must be done first,
 * followed by [visitEnd].
 */
abstract class EffectVisitor @JvmOverloads constructor(private val delegate: EffectVisitor? = null) {
    /**
     * Visits the optional argument of the effect constructor, i.e. the constant value for the [EffectType.RETURNS_CONSTANT] effect,
     * or the parameter reference for the [EffectType.CALLS] effect.
     */
    open fun visitConstructorArgument(): EffectExpressionVisitor? =
        delegate?.visitConstructorArgument()

    /**
     * Visits the optional conclusion of the effect. If this method is called, the effect represents an implication with the
     * right-hand side handled by the returned visitor.
     */
    open fun visitConclusionOfConditionalEffect(): EffectExpressionVisitor? =
        delegate?.visitConclusionOfConditionalEffect()

    /**
     * Visits the end of the effect.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

/**
 * A visitor to visit the effect expression, the contents of an effect (a part of the contract of a Kotlin function).
 *
 * Contracts are an internal feature of the standard Kotlin library, and their behavior and/or binary format
 * may change in a subsequent release.
 *
 * When using this class, [visit] must be called first, followed by zero or more calls to other visit* methods, followed by [visitEnd].
 */
abstract class EffectExpressionVisitor @JvmOverloads constructor(private val delegate: EffectExpressionVisitor? = null) {
    /**
     * Visits the basic information of the effect expression.
     * 
     * @param flags effect expression flags, consisting of [Flags.EffectExpression] flags
     * @param parameterIndex optional 1-based index of the value parameter of the function, for effects which assert something about
     *                       the function parameters. The index 0 means the extension receiver parameter
     */
    open fun visit(flags: Int, parameterIndex: Int?) {
        delegate?.visit(flags, parameterIndex)
    }

    /**
     * Visits the constant value used in the effect expression. May be `true`, `false` or `null`.
     * 
     * @param value the constant value
     */
    open fun visitConstantValue(value: Any?) {
        delegate?.visitConstantValue(value)
    }

    /**
     * Visits the type used as the target of an `is`-expression in the effect expression.
     * 
     * @param flags type flags, consisting of [Flags.Type] flags
     * @param ext platform-specific extensions for the type
     */
    open fun visitIsInstanceType(flags: Int, ext: TypeVisitor.Extensions): TypeVisitor? =
        delegate?.visitIsInstanceType(flags, ext)

    /**
     * Visits the argument of an `&&`-expression. If this method is called, the expression represents the left-hand side and
     * the returned visitor handles the right-hand side.
     */
    open fun visitAndArgument(): EffectExpressionVisitor? =
        delegate?.visitAndArgument()

    /**
     * Visits the argument of an `||`-expression. If this method is called, the expression represents the left-hand side and
     * the returned visitor handles the right-hand side.
     */
    open fun visitOrArgument(): EffectExpressionVisitor? =
        delegate?.visitOrArgument()

    /**
     * Visits the end of the effect expression.
     */
    open fun visitEnd() {
        delegate?.visitEnd()
    }
}

enum class Variance {
    INVARIANT,
    IN,
    OUT,
}

enum class EffectType {
    RETURNS_CONSTANT,
    CALLS,
    RETURNS_NOT_NULL,
}

enum class EffectInvocationKind {
    AT_MOST_ONCE,
    EXACTLY_ONCE,
    AT_LEAST_ONCE,
}

enum class VersionRequirementLevel {
    WARNING,
    ERROR,
    HIDDEN,
}

enum class VersionRequirementVersionKind {
    LANGUAGE_VERSION,
    COMPILER_VERSION,
    API_VERSION,
}

class Annotation(val className: ClassName, val arguments: Map<String, AnnotationArgument<*>>)

sealed class AnnotationArgument<out T : Any> {
    abstract val value: T
    
    data class ByteValue(override val value: Byte) : AnnotationArgument<Byte>()
    data class CharValue(override val value: Char) : AnnotationArgument<Char>()
    data class ShortValue(override val value: Short) : AnnotationArgument<Short>()
    data class IntValue(override val value: Int) : AnnotationArgument<Int>()
    data class LongValue(override val value: Long) : AnnotationArgument<Long>()
    data class FloatValue(override val value: Float) : AnnotationArgument<Float>()
    data class DoubleValue(override val value: Double) : AnnotationArgument<Double>()
    data class BooleanValue(override val value: Boolean) : AnnotationArgument<Boolean>()
    
    data class StringValue(override val value: String) : AnnotationArgument<String>()
    data class KClassValue(override val value: ClassName) : AnnotationArgument<ClassName>()
    data class EnumValue(val enumClassName: ClassName, val enumEntryName: String) : AnnotationArgument<String>() {
        override val value: String = "$enumClassName.$enumEntryName"
    }
    data class AnnotationValue(override val value: Annotation) : AnnotationArgument<Annotation>()
    data class ArrayValue(override val value: List<AnnotationArgument<*>>) : AnnotationArgument<List<AnnotationArgument<*>>>()
}
