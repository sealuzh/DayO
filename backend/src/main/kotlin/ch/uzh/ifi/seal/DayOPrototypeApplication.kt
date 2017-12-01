package ch.uzh.ifi.seal

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.StoredCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.ErrorViewResolver
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.io.File
import java.time.LocalTime


@SpringBootApplication
class DayOPrototypeApplication : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        super.configure(http)
        /* redirecting for Google Auth after logging in, if needed */
        http.formLogin().defaultSuccessUrl("$SERVER_URL/googleAuth", true)
                .and().cors()
        http.csrf().disable()
    }

    /* setup of user authentication */
    override fun configure(auth: AuthenticationManagerBuilder) {
        val inMemory = auth.inMemoryAuthentication()
        users.forEach { user ->
            inMemory.withUser(user.login).password(user.password).roles("user")
        }
    }

    @Bean fun objectMapper() = Jackson2ObjectMapperBuilder()
            .modulesToInstall(KotlinModule::class.java, JavaTimeModule::class.java)
            .mixIn(LocalTime::class.java, LocalTimeMixin::class.java)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Bean fun googleAuthServletRegistrationBean(googleAuthServlet: GoogleAuthServlet) =
            ServletRegistrationBean(googleAuthServlet, "/googleAuth")

    @Bean fun googleAuthCallbackServletRegistrationBean(callbackServlet: GoogleAuthCallbackServlet) =
            ServletRegistrationBean(callbackServlet, "/googleAuthCallback")

    //TODO adjust the credentials for authorization of the application with Google here
    @Bean fun googleAuthorizationFlow(): AuthorizationCodeFlow = AuthorizationCodeFlow.Builder(
            BearerToken.authorizationHeaderAccessMethod(),
            NetHttpTransport(),
            JacksonFactory(),
            GenericUrl("https://accounts.google.com/o/oauth2/token"),
            BasicAuthentication("1008277532875-2k3od27h8sgmh7r573brr6gqnjj2n6r9.apps.googleusercontent.com",
                    "hJcfUlELh97tfLr1Wz0ZKOVS"),
            "1008277532875-2k3od27h8sgmh7r573brr6gqnjj2n6r9.apps.googleusercontent.com",
            "https://accounts.google.com/o/oauth2/auth")
            .setScopes(listOf("https://www.googleapis.com/auth/calendar"))
            .setCredentialDataStore(StoredCredential.getDefaultDataStore(
                    FileDataStoreFactory(File(System.getProperty("user.home"), ".credentials/calendar-dayO")))
            )
            .build()

    @Bean fun googleHttpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    @Bean fun jsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()
}

interface LocalTimeMixin {
    @JsonValue override fun toString(): String
}

fun main(args: Array<String>) {
    SpringApplication.run(DayOPrototypeApplication::class.java, *args)
}

@Configuration
class WebConfig : WebMvcConfigurerAdapter() {

    /**
     * [Spring Boot doc](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/cors.html)
     * Setting up cross-origin resource sharing (CORS), necessary if server and client are on different servers.
     */
    override fun addCorsMappings(registry: CorsRegistry?) {
        registry!!.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT")
                .allowedOrigins("http://localhost:4200")
    }

    @Bean fun supportPathBasedLocationStrategyWithoutHashes() = ErrorViewResolver { _, status, _ ->
        if (status == HttpStatus.NOT_FOUND) ModelAndView("index.html", mapOf<String, Any>(), HttpStatus.OK) else null
    }
}