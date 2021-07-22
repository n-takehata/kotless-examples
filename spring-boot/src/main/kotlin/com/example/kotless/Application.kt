package com.example.kotless

import io.kotless.dsl.spring.Kotless
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

@SpringBootApplication
open class Application : Kotless() {
    override val bootKlass: KClass<*> = this::class
}

@RestController
object Main {
    @GetMapping("/")
    fun main() = "Hello World!"
}