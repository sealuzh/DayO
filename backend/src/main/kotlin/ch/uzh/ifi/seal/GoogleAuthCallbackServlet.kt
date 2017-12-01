package ch.uzh.ifi.seal

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet
import com.google.api.client.http.GenericUrl
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class GoogleAuthCallbackServlet : AbstractAuthorizationCodeCallbackServlet() {
    companion object : KLogging()

    @Autowired private lateinit var flow: AuthorizationCodeFlow
    @Autowired private lateinit var loggedInUserInfo: LoggedInUserInfo

    override fun getUserId(request: HttpServletRequest): String = loggedInUserInfo.getUser().email
    override fun initializeFlow() = flow

    override fun getRedirectUri(request: HttpServletRequest): String {
        val url = GenericUrl(request.requestURL.toString())
        url.rawPath = "/googleAuthCallback"
        return url.build()
    }

    override fun onSuccess(req: HttpServletRequest, resp: HttpServletResponse, credential: Credential) {
        logger.info { "Successfully authorized user ${getUserId(req)}. Redirecting to frontend." }
        resp.sendRedirect(CLIENT_URL)
    }
}