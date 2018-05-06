package controllers

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import javax.imageio.ImageIO
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
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

  private val uploadPath = Paths.get(s"$imageBaseDir")

  val logger = Logger(classOf[ImageController])

  private def isImage(file: File): Boolean = {
    val iis = ImageIO.createImageInputStream(file)
    try {
      val readers = ImageIO.getImageReaders(iis)
      readers.hasNext
    } finally {
      iis.close()
    }
  }

  private def randomFilename: String = UUID.randomUUID().toString

  def formUpload = Action(parse.multipartFormData) { request =>
    logger.info(s"${getClass.getSimpleName}    upload file with form upload")
    request.body.file("image").map { picture =>
      val filename = randomFilename

      if (!Files.exists(uploadPath) && Files.notExists(uploadPath)) {
        Files.createDirectories(uploadPath)
      }

      val targetPath = uploadPath.resolve(filename)
      picture.ref.moveTo(targetPath, replace = true)
      val path = targetPath.toAbsolutePath
      logger.info(s"save file to $path")
      logger.info(s"target file $path exists ${Files.exists(path)}")
      Ok(Json.obj("uuid" -> filename))
    }.getOrElse {
      UnprocessableEntity
    }
  }

  def directUpload = Action(parse.temporaryFile) async { implicit request =>
    Future {
      val path = uploadPath.resolve(randomFilename)
      val targetFile = path.toFile

      if (!targetFile.getParentFile.exists()) {
        targetFile.getParentFile.mkdirs()
      }
      val temporaryFile = request.body
      logger.debug(s"temporary file path ${temporaryFile.path.toString}")

      if (isImage(temporaryFile.path.toFile)) {
        temporaryFile.moveTo(targetFile)
        Ok(Json.obj("uuid" -> targetFile.getName))
      } else {
        UnprocessableEntity
      }
    }
  }

  def find(uuid: String) = Action async Future {
    val file = uploadPath.resolve(uuid)
    logger.info(s"try send file ${file.toAbsolutePath.toString}")
    if (Files.exists(file.toAbsolutePath))
      Ok.sendFile(file.toFile.getAbsoluteFile)
    else NoContent
  }
}
