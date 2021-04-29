import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.CaptionListResponse

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.Arrays
import java.util.Collection
import java.{util => ju}
import java.io.FileOutputStream

object Main extends App {

  /*
   * Initialize values to use in prgram
   *
   */

  val factory = new JacksonFactory()

  val PATH: String = System.getProperty("user.dir")
  val FILENAME: String = "/testFile.txt"
  val PATHTOFILE: String = PATH + FILENAME
  val scope: Collection[String] =
    Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl", "https://www.googleapis.com/auth/youtubepartner")
  val VIDEO_ID = "rHP-OPXK2ig"

  
  /*
    This is needed to OAuth authorization
   */
  val CLIENT_SECRET: String = "client_secret.json"

  def authorize(httpTransport: NetHttpTransport): Credential = {
    // Load client secrets.
    val in: InputStream = getClass().getResourceAsStream(CLIENT_SECRET)
    val clientSecrets: GoogleClientSecrets =
      GoogleClientSecrets.load(factory, new InputStreamReader(in))
    // Build flow and trigger user authorization request.
    val flow: GoogleAuthorizationCodeFlow =
      new GoogleAuthorizationCodeFlow.Builder(
        httpTransport,
        factory,
        clientSecrets,
        scope
      ).build()
    val credential: Credential =
      new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
        .authorize("user")
    credential
  }

  /*
    Create YouTube service instance to execute API request
  */

  def getService() = {
    val httpTransportClient = GoogleNetHttpTransport.newTrustedTransport()
    val auth = authorize(httpTransportClient)
    println(auth)
    new YouTube.Builder(httpTransportClient, factory, auth)
      .setApplicationName("ScalaTest")
      .build()
  }

  val youTube = getService()

  val ssss: ju.List[String] =
    Arrays.asList("id")

  val request = youTube.captions().download(VIDEO_ID)
  request.getMediaHttpDownloader()
  request.executeMediaAndDownloadTo(new FileOutputStream("outpot.txt"))
  
  


  // println(video.getSnippet.getDescription)

}
