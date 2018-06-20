package s

import java.nio.file.Paths

import graphql.GraphQL
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring
import graphql.schema.idl._

object StarWars extends App {

  val schemaUrl = Thread.currentThread.getContextClassLoader
    .getResource("starwars.graphql")
  val schemaPath = Paths.get(schemaUrl.toURI)
  val schemaFile = schemaPath.toFile

  val schemaParser = new SchemaParser
  val typeDefinitionRegistry = schemaParser.parse(schemaFile)

  val runtimeWiring = RuntimeWiring
    .newRuntimeWiring()
    .`type`(
      newTypeWiring("Query")
        .dataFetcher("hero", StarWarsData.heroDataFetcher)
        .dataFetcher("human", StarWarsData.humanDataFetcher)
        .dataFetcher("droid", StarWarsData.droidDataFetcher)
        .build()
    )
    .`type`(
      newTypeWiring("Character")
        .typeResolver(StarWarsData.characterTypeResolver)
        .build()
    )
    .build()

  val schemaGenerator = new SchemaGenerator
  val graphQLSchema =
    schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

  val graphQL = GraphQL.newGraphQL(graphQLSchema).build

  private def runQuery(query: String): Unit = {
    val executionResult = graphQL.execute(query)
    val errors = executionResult.getErrors
    if (errors.isEmpty) println(executionResult.getData)
    else println(errors)
  }

  runQuery("""
      |{
      |  hero {
      |    name
      |    friends {
      |      name
      |    }
      |    ...on Droid {
      |      primaryFunction
      |    }
      |  }
      |}
    """.stripMargin)

  runQuery("""
      |{
      |  hero(episode: JEDI) {
      |    name
      |    friends {
      |      name
      |    }
      |    ...on Human {
      |      homePlanet
      |    }
      |  }
      |}
    """.stripMargin)

  runQuery("""
      |{
      |  human(id: "3") {
      |    name
      |    appearsIn
      |    homePlanet
      |  }
      |}
    """.stripMargin)

  runQuery("""
             |{
             |  droid(id: "1") {
             |    name
             |    appearsIn
             |    primaryFunction
             |  }
             |}
           """.stripMargin)
}
