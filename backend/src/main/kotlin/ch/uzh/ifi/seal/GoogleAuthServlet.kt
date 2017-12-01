package ch.uzh.ifi.seal

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet
import com.google.api.client.http.GenericUrl
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class GoogleAuthServlet : AbstractAuthorizationCodeServlet() {
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

    override fun service(req: HttpServletRequest, res: HttpServletResponse) {
        deleteExpiredTokenIfNeeded(req)

        super.service(req, res)
    }

    private fun deleteExpiredTokenIfNeeded(req: HttpServletRequest) {
        val email = loggedInUserInfo.getUser().email
        val credential = flow.loadCredential(email)
        if (credential != null) {
            val expiresMinusNow = credential.expirationTimeMilliseconds - System.currentTimeMillis()
            val expirationInMinutes = expiresMinusNow / 60000
            if (expiresMinusNow < 0) {
                logger.info { "Deleted expired token for $email (expired ${-expirationInMinutes} minutes ago)" }
                flow.credentialDataStore.delete(getUserId(req))
            } else {
                logger.info { "Token for $email is still valid for $expirationInMinutes minutes" }
            }
        } else {
            logger.info { "User $email doesn't have token, skipping expiration check" }
        }
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        logger.info { "User ${getUserId(request)} is already authorized. Redirecting to frontend." }
        response.sendRedirect(CLIENT_URL)
    }
}