package io.github.mitchelllisle

import io.github.mitchelllisle.{fantasy => fpl}
import io.github.mitchelllisle.models._
import io.github.mitchelllisle.duckdb.DuckDB

object Main {
  private val duckdb = new DuckDB("fantasy.db")

  private def createUserDim(leagueEntries: List[fpl.LeagueEntry]): Unit = {
    val userDim = leagueEntries.map(e => UserDim(
      e.id, e.entry_id, e.entry_name, e.player_first_name, e.player_first_name, e.short_name, e.waiver_pick)
    )
    duckdb.createTable[UserDim]()
    duckdb.append[UserDim](userDim)
  }

  private def createPlayerDim(players: List[fpl.Player]): Unit = {
    val playerDim = players.map(p => PlayerDim(
      p.id,
      p.name.getOrElse("").replace("'", ""),
      p.team,
      p.element_type,
      p.first_name.replace("'", "\""),
      p.web_name.replace("'", ""),
    )
    )
    duckdb.createTable[PlayerDim]()
    duckdb.append[PlayerDim](playerDim)
  }

  private def createPlayerGamePerformanceFact(players: List[fpl.Player]): Unit = {
    val playerPerformance = players.map(p => PlayerGamePerformanceFact(
      p.event.get,
      p.id,
      p.team_code.get,
      p.minutes,
      p.assists,
      p.bonus,
      p.bps,
      p.clean_sheets,
      p.goals_conceded,
      p.goals_scored,
      p.own_goals,
      p.penalties_missed,
      p.penalties_saved,
      p.red_cards,
      p.saves,
      p.starts,
      p.yellow_cards,
      p.total_points,
      p.event_points
    )
    )
    duckdb.createTable[PlayerGamePerformanceFact]()
    duckdb.append[PlayerGamePerformanceFact](playerPerformance)
  }

  private def createPlayerExpectedStatsFact(players: List[fpl.Player]): Unit = {
    val playerExpectedStats = players.map(p => PlayerExpectedStatsFact(
      p.event.get,
      p.id,
      p.expected_assists.toFloat,
      p.expected_goal_involvements.toFloat,
      p.expected_goals.toFloat,
      p.expected_goals_conceded.toFloat
      )
    )
    duckdb.createTable[PlayerExpectedStatsFact]()
    duckdb.append[PlayerExpectedStatsFact](playerExpectedStats)
  }

  private def createTeamDimension(teamStats: Seq[fpl.TeamStats]): Unit = {
    val teamDim = teamStats.map(t => TeamDimension(
      t.id,
      t.name,
      t.mainColor,
      t.secondaryColor
    )
    )
    duckdb.createTable[TeamDimension]()
    duckdb.append[TeamDimension](teamDim.toList)
  }

  def main(args: Array[String]): Unit = {
    val draft = new fpl.FantasyDraftAPI()

    val details = draft.getDetails
    val players = draft.getPlayers
    val teamStats = draft.getAllTeamStats

    createUserDim(details.league_entries)
    createPlayerDim(players)
    createPlayerGamePerformanceFact(players)
    createPlayerExpectedStatsFact(players)
    createTeamDimension(teamStats)
//    duckdb.exportDatabase("fantasy")

//    val teamStats = draft.getAllTeamStats
//
//    val currentGameweek = draft.getCurrentGameweek
//
//    val standings = draft.getStandings(details, details.league_entries)
//    val allPicks = draft.getAllPicks(details.league_entries, currentGameweek, players)
//    val playersWithOwners = draft.attachOwners(players, allPicks)
//
//    val matchResults = draft.getMatchResults(details, details.league_entries)
//    val withCumSum = draft.getCumulativeSum(matchResults)
  }
}
