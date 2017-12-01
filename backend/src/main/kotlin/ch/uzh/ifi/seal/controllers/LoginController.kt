package ch.uzh.ifi.seal.controllers

import ch.uzh.ifi.seal.data_access.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginController(val userRepository: UserRepository) {

    @GetMapping("login/{name}")
    fun login(@PathVariable name: String): String {

        val user = userRepository.findByName(name)

        if (user != null) {
            return "Hello there, $name!"
        } else {
            throw IllegalArgumentException("user with name $name was not found in our database")
        }
    }
}