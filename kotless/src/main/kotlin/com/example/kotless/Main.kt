package com.example.kotless

import io.kotless.dsl.lang.http.Get

object Main {
    @Get("/")
    fun main() = "Hello world!"
}