package io.github.mitchelllisle

import io.github.mitchelllisle.fantasy.FantasyDraftAPI
import io.github.mitchelllisle.duckdb.DuckDB

object Main {
  private val duckdb = new DuckDB("fantasy.db")

  def main(args: Array[String]): Unit = {
    val draft = new FantasyDraftAPI()

    val details = draft.getDetails
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
