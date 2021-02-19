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

  private def toJson(basePath: String, resourceName: String): String = {
    val lines =
      Source.fromResource(s"$basePath/$resourceName.csv").getLines().toList
    val headers = parseLine(lines.head).toList
    lines
      .drop(1)
      .map(parseLine)
      .map(toJsonRow(headers))
      .mkString("[", ", ", "]")
  }

  def saveJson(basePath: String, resourceName: String): Path =
    Files
      .write(
        Paths
          .get(s"$resourceName.json"),
        parse(toJson(basePath, resourceName))
          .getOrElse(Json.Null)
          .asJson
          .spaces2
          .getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE
      )

  private[release] def tableFor3[T, A, B, C](
    basePath: String,
    resourceName: String,
    headers: (String, String, String),
    toDataTuple: T => (A, B, C)
  )(implicit decoder: Decoder[T]
  ): TableFor3[A, B, C] = {
    decode[List[T]](toJson(basePath, resourceName)) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headers, rows: _*)
    }
  }

  private[release] def tableFor4[T, A, B, C, D](
    basePath: String,
    resourceName: String,
    headers: (String, String, String, String),
    toDataTuple: T => (A, B, C, D)
  )(implicit decoder: Decoder[T]
  ): TableFor4[A, B, C, D] = {
    decode[List[T]](toJson(basePath, resourceName)) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headers, rows: _*)
    }
  }

  private[release] def tableFor6[T, A, B, C, D, E, F](
    basePath: String,
    resourceName: String,
    headers: (String, String, String, String, String, String),
    toDataTuple: T => (A, B, C, D, E, F)
  )(implicit decoder: Decoder[T]
  ): TableFor6[A, B, C, D, E, F] = {
    decode[List[T]](toJson(basePath, resourceName)) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headers, rows: _*)
    }
  }

  private[release] def tableFor7[T, A, B, C, D, E, F, G](
    basePath: String,
    resourceName: String,
    headers: (String, String, String, String, String, String, String),
    toDataTuple: T => (A, B, C, D, E, F, G)
  )(implicit decoder: Decoder[T]
  ): TableFor7[A, B, C, D, E, F, G] = {
    decode[List[T]](toJson(basePath, resourceName)) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headers, rows: _*)
    }
  }

  private[release] def tableFor8[T, A, B, C, D, E, F, G, H](
    basePath: String,
    resourceName: String,
    headers: (String, String, String, String, String, String, String, String),
    toDataTuple: T => (A, B, C, D, E, F, G, H)
  )(implicit decoder: Decoder[T]
  ): TableFor8[A, B, C, D, E, F, G, H] = {
    decode[List[T]](toJson(basePath, resourceName)) match {
      case Left(ex) => throw ex
      case Right(values) =>
        val rows = values.map(value => toDataTuple(value))
        Table(headers, rows: _*)
    }
  }

  private def toJsonRow(
    headers: List[String]
  )(dataLine: Array[String]
  ): String =
    headers
      .zip(dataLine)
      .collect {
        case (header, item) if item != "X" =>
          if (header == "tagNames") {
            s""""$header": ${toTagNames(item).asJson.noSpaces}"""
          } else if (header == "matching") {
            //
            s""""$header": ${toVersionMatching(item)
              .asJson
              .noSpaces}"""
          } else if (header == "annotated") {
            s""""$header": $item"""
          } else if (header == "bump") {
            s""""$header": ${VersionComponent.valueOf(item).asJson.noSpaces}"""
          } else {
            s""""$header": "$item""""
          }

      }
      .mkString("{", ",", "}")

  private def parseLine(line: String) =
    line.split("\\|").map(_.replaceAll("'", "")).map(_.trim)

}