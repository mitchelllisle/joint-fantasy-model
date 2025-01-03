package io.github.mitchelllisle

import io.github.mitchelllisle.fantasy.FantasyDraftAPI
import java.sql.DriverManager
import org.duckdb.DuckDBConnection


object Main {
  val conn: DuckDBConnection = DriverManager.getConnection("jdbc:duckdb:").asInstanceOf[DuckDBConnection]

  def main(args: Array[String]): Unit = {
    val draft = new FantasyDraftAPI()

    val details = draft.getDetails

    details.league.withConnection(conn).createTable.exportTable("league.csv")

    val teamStats = draft.getAllTeamStats

    val players = draft.getPlayers
    val users = draft.getUsers
    val currentGameweek = draft.getCurrentGameweek

    val standings = draft.getStandings(details, users)
    val allPicks = draft.getAllPicks(users, currentGameweek, players)
    val playersWithOwners = draft.attachOwners(players, allPicks)

    val matchResults = draft.getMatchResults(details, users)
    val withCumSum = draft.getCumulativeSum(matchResults)
  }
}
