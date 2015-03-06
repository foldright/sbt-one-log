package com.zavakid.sbt

/**
 *
 * copy from https://github.com/jrudolph/sbt-dependency-graph/blob/master/src/main/scala/net/virtualvoid/sbt/graph/IvyGraphMLDependencies.scala
 * and delete some unneed function
 */

import java.io.File

import sbinary.{DefaultProtocol, Format}
import sbt.ConsoleLogger

import scala.collection.mutable
import scala.collection.mutable.{Set => MSet}
import scala.xml.parsing.ConstructingParser
import scala.xml.{Document, Node, NodeSeq}

object IvyGraphMLDependencies extends App {

  case class ModuleId(organisation: String,
                      name: String,
                      version: String) {
    def idString: String = organisation + ":" + name + ":" + version
  }

  case class Module(id: ModuleId,
                    license: Option[String] = None,
                    extraInfo: String = "",
                    evictedByVersion: Option[String] = None,
                    error: Option[String] = None) {
    def hadError: Boolean = error.isDefined

    def isUsed: Boolean = !evictedByVersion.isDefined
  }

  type Edge = (ModuleId, ModuleId)

  case class ModuleGraph(nodes: Seq[Module], edges: Seq[Edge]) {
    lazy val modules: Map[ModuleId, Module] =
      nodes.map(n => (n.id, n)).toMap

    def module(id: ModuleId): Module = modules(id)

    lazy val dependencyMap: Map[ModuleId, Seq[Module]] =
      createMap(identity)

    lazy val reverseDependencyMap: Map[ModuleId, Seq[Module]] =
      createMap { case (a, b) => (b, a)}

    def createMap(bindingFor: ((ModuleId, ModuleId)) => (ModuleId, ModuleId)): Map[ModuleId, Seq[Module]] = {
      val m = new mutable.HashMap[ModuleId, MSet[Module]] with mutable.MultiMap[ModuleId, Module]
      edges.foreach { entry =>
        val (f, t) = bindingFor(entry)
        m.addBinding(f, module(t))
      }
      m.toMap.mapValues(_.toSeq.sortBy(_.id.idString)).withDefaultValue(Nil)
    }
  }

  def graph(ivyReportFile: String): ModuleGraph =
    buildGraph(buildDoc(ivyReportFile))

  def buildGraph(doc: Document): ModuleGraph = {
    def edgesForModule(id: ModuleId, revision: NodeSeq): Seq[Edge] =
      for {
        caller <- revision \ "caller"
        callerModule = moduleIdFromElement(caller, caller.attribute("callerrev").get.text)
      } yield (moduleIdFromElement(caller, caller.attribute("callerrev").get.text), id)

    val moduleEdges: Seq[(Module, Seq[Edge])] = for {
      mod <- doc \ "dependencies" \ "module"
      revision <- mod \ "revision"
      rev = revision.attribute("name").get.text
      moduleId = moduleIdFromElement(mod, rev)
      module = Module(moduleId,
        (revision \ "license").headOption.flatMap(_.attribute("name")).map(_.text),
        evictedByVersion = (revision \ "evicted-by").headOption.flatMap(_.attribute("rev").map(_.text)),
        error = revision.attribute("error").map(_.text))
    } yield (module, edgesForModule(moduleId, revision))

    val (nodes, edges) = moduleEdges.unzip

    val info = (doc \ "info").head
    def infoAttr(name: String): String =
      info.attribute(name).getOrElse(throw new IllegalArgumentException("Missing attribute " + name)).text
    val rootModule = Module(ModuleId(infoAttr("organisation"), infoAttr("module"), infoAttr("revision")))

    ModuleGraph(rootModule +: nodes, edges.flatten)
  }

  def reverseGraphStartingAt(graph: ModuleGraph, root: ModuleId): ModuleGraph = {
    val depsMap = graph.reverseDependencyMap

    def visit(module: ModuleId, visited: Set[ModuleId]): Seq[(ModuleId, ModuleId)] =
      if (visited(module))
        Nil
      else
        depsMap.get(module) match {
          case Some(deps) =>
            deps.flatMap { to =>
              (module, to.id) +: visit(to.id, visited + module)
            }
          case None => Nil
        }

    val edges = visit(root, Set.empty)
    val nodes = edges.foldLeft(Set.empty[ModuleId])((set, edge) => set + edge._1 + edge._2).map(graph.module)
    ModuleGraph(nodes.toSeq, edges)
  }

  def ignoreScalaLibrary(scalaVersion: String, graph: ModuleGraph): ModuleGraph = {
    def isScalaLibrary(m: Module) = isScalaLibraryId(m.id)
    def isScalaLibraryId(id: ModuleId) = id.organisation == "org.scala-lang" && id.name == "scala-library"

    def dependsOnScalaLibrary(m: Module): Boolean =
      graph.dependencyMap(m.id).exists(isScalaLibrary)

    def addScalaLibraryAnnotation(m: Module): Module = {
      if (dependsOnScalaLibrary(m))
        m.copy(extraInfo = m.extraInfo + " [S]")
      else
        m
    }

    val newNodes = graph.nodes.map(addScalaLibraryAnnotation).filterNot(isScalaLibrary)
    val newEdges = graph.edges.filterNot(e => isScalaLibraryId(e._2))
    ModuleGraph(newNodes, newEdges)
  }

  def moduleIdFromElement(element: Node, version: String): ModuleId =
    ModuleId(element.attribute("organisation").get.text, element.attribute("name").get.text, version)

  private def buildDoc(ivyReportFile: String) = ConstructingParser.fromSource(io.Source.fromFile(ivyReportFile), preserveWS = false).document()
}

object ModuleGraphProtocol extends DefaultProtocol {

  import com.zavakid.sbt.IvyGraphMLDependencies._

  implicit def seqFormat[T: Format]: Format[Seq[T]] = wrap[Seq[T], List[T]](_.toList, _.toSeq)

  implicit val ModuleIdFormat: Format[ModuleId] = asProduct3(ModuleId)(ModuleId.unapply(_).get)
  implicit val ModuleFormat: Format[Module] = asProduct5(Module)(Module.unapply(_).get)
  implicit val ModuleGraphFormat: Format[ModuleGraph] = asProduct2(ModuleGraph)(ModuleGraph.unapply(_).get)
}
