// "Add '@JvmDefault' annotation" "true"

// WITH_RUNTIME
interface Bar : Foo {
    <caret>override fun foo() {

    }
}
