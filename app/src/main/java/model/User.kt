package model

data class User(
    val email: String = "",
    val password: String = ""
)
{
    init {
        println("test")
    }
}