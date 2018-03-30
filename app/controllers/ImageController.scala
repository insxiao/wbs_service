package controllers

import java.io.FileInputStream
import java.nio.file.Paths
import java.util.UUID

import javax.imageio.ImageIO
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

@Singleton
class ImageController @Inject()(val cc: ControllerComponents, val config: Configuration)
                               (implicit override val executionContext: ExecutionContext, override val userService: UserService)
  extends AbstractController(cc) with AuthorizationFunction {
  private val imageBaseDir = config.getOptional[String]("upload.image.dir")
    .orElse(config.getOptional[String]("upload.basedir").map(_ + "/image"))
    .getOrElse("upload/image")

  def upload = (Action(parse.temporaryFile) andThen tokenAuthenticate) async { implicit request =>
    Future {
      val path = Paths.get(s"$imageBaseDir/${UUID.randomUUID()}")
      val file = path.toFile

      if (!file.getParentFile.exists()) {
        file.getParentFile.mkdirs()
      }

      val temporaryFile = request.body

      val iis = ImageIO.createImageInputStream(temporaryFile.path.toFile)
      try {
        val readers = ImageIO.getImageReaders(iis)
        if (readers.hasNext) {
          temporaryFile.moveTo(file)
          Ok(Json.obj("uuid" -> file.getName))
        } else {
          UnprocessableEntity
        }
      } finally {
        iis.clone()
      }
    }
  }

  def find(uuid: String) = Action async Future {
    val file = Paths.get(s"$imageBaseDir/$uuid").toFile
    if (file.exists())
      Ok.sendFile(file)
    else NoContent
  }
}
