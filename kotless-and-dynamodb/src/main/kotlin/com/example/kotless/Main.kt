package com.example.kotless

import io.kotless.dsl.lang.http.Get

@Get("/")
fun hello() = findItem()

@Get("/put")
fun put() = putItem()