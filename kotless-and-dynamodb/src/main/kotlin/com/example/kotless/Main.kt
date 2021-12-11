package com.example.kotless

import io.kotless.dsl.lang.http.Get

@Get("/put")
fun put() = putItem()

@Get("/find")
fun find() = findItem()