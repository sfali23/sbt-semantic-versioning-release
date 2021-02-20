package com.alphasystem.sbt.release.test

import com.alphasystem.sbt.release.VersionComponent
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{ Decoder, Json }
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop._

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path, Paths, StandardOpenOption }
import scala.io.Source

object DataGenerator {

  private def toJson(basePath: String, resourceName: String) = {
    val lines =
      Source.fromResource(s"$basePath/$resourceName.csv").getLines().toList
    val headers = parseLine(lines.head).toList
    (
      headers,
      lines
        .drop(1)
        .map(parseLine)
        .map(toJsonRow(headers))
        .mkString("[", ", ", "]")
    )
  }

  def saveJson(basePath: String, resourceName: String): Path =
    Files
      .write(
        Paths
          .get(s"$resourceName.json"),
        parse(toJson(basePath, resourceName)._2)
          .getOrElse(Json.Null)
          .asJson
          .spaces2
          .getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE
      )

  private def headerFor3(headers: List[String]) =
    headers match {
      case List(a, b, c) => (a, b, c)
    }

  private[release] def tableFor3[T, A, B, C](
    basePath: String,
    resourceName: String,
    toDataTuple: T => (A, B, C)
  )(implicit decoder: Decoder[T]
  ): TableFor3[A, B, C] = {
    val (headers, json) = toJson(basePath, resourceName)
    decode[List[T]](json) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headerFor3(headers), rows: _*)
    }
  }

  private def headerFor4(headers: List[String]) =
    headers match {
      case List(a, b, c, d) => (a, b, c, d)
    }

  private[release] def tableFor4[T, A, B, C, D](
    basePath: String,
    resourceName: String,
    toDataTuple: T => (A, B, C, D)
  )(implicit decoder: Decoder[T]
  ): TableFor4[A, B, C, D] = {
    val (headers, json) = toJson(basePath, resourceName)
    decode[List[T]](json) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headerFor4(headers), rows: _*)
    }
  }

  private def headerFor6(headers: List[String]) =
    headers match {
      case List(a, b, c, d, e, f) => (a, b, c, d, e, f)
    }

  private[release] def tableFor6[T, A, B, C, D, E, F](
    basePath: String,
    resourceName: String,
    toDataTuple: T => (A, B, C, D, E, F)
  )(implicit decoder: Decoder[T]
  ): TableFor6[A, B, C, D, E, F] = {
    val (headers, json) = toJson(basePath, resourceName)
    decode[List[T]](json) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headerFor6(headers), rows: _*)
    }
  }

  private def headerFor7(headers: List[String]) =
    headers match {
      case List(a, b, c, d, e, f, g) => (a, b, c, d, e, f, g)
    }

  private[release] def tableFor7[T, A, B, C, D, E, F, G](
    basePath: String,
    resourceName: String,
    toDataTuple: T => (A, B, C, D, E, F, G)
  )(implicit decoder: Decoder[T]
  ): TableFor7[A, B, C, D, E, F, G] = {
    val (headers, json) = toJson(basePath, resourceName)
    decode[List[T]](json) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headerFor7(headers), rows: _*)
    }
  }

  private def headerFor8(headers: List[String]) =
    headers match {
      case List(a, b, c, d, e, f, g, h) => (a, b, c, d, e, f, g, h)
    }

  private[release] def tableFor8[T, A, B, C, D, E, F, G, H](
    basePath: String,
    resourceName: String,
    toDataTuple: T => (A, B, C, D, E, F, G, H)
  )(implicit decoder: Decoder[T]
  ): TableFor8[A, B, C, D, E, F, G, H] = {
    val (headers, json) = toJson(basePath, resourceName)
    decode[List[T]](json) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headerFor8(headers), rows: _*)
    }
  }

  private def toJsonRow(
    headers: List[String]
  )(dataLine: Array[String]
  ): String =
    headers
      .zip(dataLine)
      .collect {
        case (header, item) if header == "bump" =>
          val bump =
            if (item == "null") VersionComponent.NONE
            else VersionComponent.valueOf(item)
          s""""$header": ${bump.asJson.noSpaces}"""

        case (header, item) if header == "tagNames" =>
          s""""$header": ${toTagNames(item).asJson.noSpaces}"""

        case (header, item) if header == "matching" =>
          s""""$header": ${toVersionMatching(item).asJson.noSpaces}"""

        case (header, item) if item == "true" || item == "false" =>
          s""""$header": $item"""

        case (header, item) if item != "null" =>
          val value = if (item.startsWith("\"")) item else s""""$item""""
          s""""$header": $value"""

      }
      .mkString("{", ",", "}")

  private def parseLine(line: String) =
    line.split("\\|").map(_.trim)

}
