package s

import java.util.{ArrayList => JArrayList, List => JList}

import graphql.TypeResolutionEnvironment
import graphql.schema.{DataFetcher, DataFetchingEnvironment, TypeResolver}
import j.Episode
import j.Episode._

import scala.collection.JavaConverters._
import scala.collection.immutable

object StarWarsData {

  abstract class Character(
      private val id: String,
      private val name: String,
      private val appearsIn: List[Episode],
      private val friends: JList[Character] = new JArrayList[Character]()
  ) {
    val getId: String = id
    val getName: String = name
    val getFriends: JList[Character] = friends
    val getAppearsIn: JList[Episode] = appearsIn.asJava
  }

  case class Droid(
      id: String,
      name: String,
      primaryFunction: String,
      appearsIn: List[Episode]
  ) extends Character(id, name, appearsIn)

  case class Human(
      id: String,
      name: String,
      homePlanet: String,
      appearsIn: List[Episode]
  ) extends Character(id, name, appearsIn)

  val (artoo, luke, leia) = {
    val artoo = Droid("1", "R2D2", "Astromech", List(NEWHOPE, EMPIRE, JEDI))
    val luke = Human("2", "Luke", "Tatooine", List(NEWHOPE, EMPIRE, JEDI))
    val leia = Human("3", "Leïa", "Alderaan", List(NEWHOPE, EMPIRE, JEDI))
    artoo.getFriends.addAll(List(luke, leia).asJava)
    luke.getFriends.addAll(List(artoo, leia).asJava)
    leia.getFriends.addAll(List(artoo, luke).asJava)
    (artoo, luke, leia)
  }

  val humans: immutable.Seq[Human] = List(luke, leia)

  val droids: immutable.Seq[Droid] = List(artoo)

  val heroDataFetcher: DataFetcher[Character] =
    (env: DataFetchingEnvironment) => {
      val episodeO = Option(env.getArgument[String]("episode"))
        .map(Episode.valueOf)
      episodeO match {
        case None => artoo
        case Some(episode) =>
          episode match {
            case NEWHOPE => ???
            case EMPIRE  => ???
            case JEDI    => luke
            case _       => artoo
          }
      }
    }

  val humanDataFetcher: DataFetcher[Human] = (env: DataFetchingEnvironment) => {
    val id = env.getArgument[String]("id")
    humans.find(_.id == id).orNull
  }

  val droidDataFetcher: DataFetcher[Droid] = (env: DataFetchingEnvironment) => {
    val id = env.getArgument[String]("id")
    droids.find(_.id == id).orNull
  }

  val characterTypeResolver: TypeResolver =
    (env: TypeResolutionEnvironment) => {
      val character = env.getObject[Character]
      character match {
        case _: Droid => env.getSchema.getObjectType("Droid")
        case _: Human => env.getSchema.getObjectType("Human")
      }
    }

}
