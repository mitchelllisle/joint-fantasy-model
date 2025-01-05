package io.github.mitchelllisle.fantasy

import io.circe.Decoder
import io.circe.parser.decode
import sttp.client3._


class FantasyDraftAPI(leagueId: Int = 8999) {
  private val baseUrl = "https://draft.premierleague.com/api"
  private val footballApiUrl = "https://footballapi.pulselive.com/football"

  private val backend = HttpURLConnectionBackend()

  private val teamColours = Seq(
    TeamColour(1, 1, "Arsenal", "#e20712", "#ffffff"),
    TeamColour(2, 2, "Aston Villa", "#410e1e", "#94c0e6"),
    TeamColour(3, 127, "Bournemouth", "#c4090e", "#000000"),
    TeamColour(4, 130, "Brentford", "#bf0300", "#ffba1c"),
    TeamColour(5, 131, "Brighton & Hove Albion", "#0154a6", "#ffffff"),
    TeamColour(11, 26, "Leicester City", "#062d88", "#faba00"),
    TeamColour(6, 4, "Chelsea", "#001489", "#ffffff"),
    TeamColour(7, 6, "Crystal Palace", "#ee2b20", "#0055a6"),
    TeamColour(8, 7, "Everton", "#004593", "#ffffff"),
    TeamColour(9, 34, "Fulham", "#ffffff", "#000000"),
    TeamColour(12, 10, "Liverpool", "#e31921", "#ffffff"),
    TeamColour(10, 8, "Ipswich Town", "#0c3d91", "#ffffff"),
    TeamColour(13, 11, "Manchester City", "#7ab1e2", "#ffffff"),
    TeamColour(14, 12, "Manchester United", "#db0712", "#000000"),
    TeamColour(15, 23, "Newcastle United", "#000000", "#ffffff"),
    TeamColour(16, 15, "Nottingham Forest", "#ed3039", "#ffffff"),
    TeamColour(17, 20, "Southampton", "#ed3039", "#ffffff"),
    TeamColour(18, 21, "Tottenham Hotspur", "#ffffff", "#131f54"),
    TeamColour(19, 25, "West Ham United", "#410e1e", "#94c0e6"),
    TeamColour(20, 38, "Wolverhampton Wanderers", "#faba00", "#000000")
  )

  /** Attaches owner information to players based on draft picks
   *
   * @param players List of players to update
   * @param picks List of draft picks containing owner information
   * @return Updated list of players with owner information attached
   */
  def attachOwners(players: List[Player], picks: List[Pick]): List[Player] = {
    picks.foreach { p =>
      players.find(_.id == p.element).foreach { player =>
        player.copy(owner = Some(p.user.get))
      }
    }
    players
  }

  /** Gets match results and calculates points for each team
   *
   * @param details League details containing match information
   * @param users List of users in the league
   * @return List of match results with calculated points
   */
  def getMatchResults(details: LeagueDetails, users: List[LeagueEntry]): List[MatchResult] = {
    details.matches.filter(_.finished).flatMap { e =>
      val team1 = users.find(_.id == e.league_entry_1).get.player_first_name
      val team2 = users.find(_.id == e.league_entry_2).get.player_first_name
      val team1_points = e.league_entry_1_points
      val team2_points = e.league_entry_2_points

      val (team1_result_points, team2_result_points) = if (team1_points > team2_points) {
        (3, 0)
      } else if (team1_points < team2_points) {
        (0, 3)
      } else {
        (1, 1)
      }

      List(
        MatchResult(
          gameweek = e.event,
          finished = e.finished,
          team = team1,
          total_points = team1_points,
          points = team1_result_points
        ),
        MatchResult(
          gameweek = e.event,
          finished = e.finished,
          team = team2,
          total_points = team2_points,
          points = team2_result_points
        )
      )
    }
  }

  /** Calculates cumulative points for each team across matches
   *
   * @param matchResults List of match results to accumulate points from
   * @return List of match results with cumulative points added
   */
  def getCumulativeSum(matchResults: List[MatchResult]): List[MatchResultWithAcc] = {
    val teamPoints = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)
    matchResults.map { matchResult =>
      teamPoints(matchResult.team) += matchResult.points
      MatchResultWithAcc(
        id = matchResult.id,
        gameweek = matchResult.gameweek,
        finished = matchResult.finished,
        team = matchResult.team,
        total_points = matchResult.total_points,
        points = matchResult.points,
        points_acc = teamPoints(matchResult.team)
      )
    }
  }

  /** Gets all players with team information attached
   *
   * @return List of players with team details
   */
  def getPlayers: List[Player] = {
    val endpoint = "bootstrap-static"
    val response = makeRequest[BootstrapStaticResponse](endpoint)
    response.elements.map { e =>
      val team = response.teams.find(_.id == e.team).get
      val teamColour = teamColours.find(_.id == e.team).get
      e.copy(
        event = Some(response.events.current),
        name = Some(e.web_name),
        team_code = Some(team.code),
        team_name = Some(team.name),
        team_primary_colour = Some(teamColour.mainColor),
        team_secondary_colour = Some(teamColour.secondaryColor)
      )
    }
  }

  /** Makes an HTTP request to the API
   *
   * @param endpoint API endpoint to call
   * @param url Base URL to use (defaults to main API URL)
   * @param params Optional query parameters
   * @return Decoded response of type T
   */
  private def makeRequest[T](endpoint: String, url: String = baseUrl, params: Option[Map[String, String]] = None)(implicit decoder: Decoder[T]): T = {
    val urlWithParams = params match {
      case Some(p) =>
        val paramString = p.map { case (k, v) => s"$k=$v" }.mkString("&")
        s"$url/$endpoint?$paramString"
      case None => s"$url/$endpoint"
    }

    val response = basicRequest
      .get(uri"$urlWithParams")
      .response(asString.getRight)
      .send(backend)
      .body

    decode[T](response) match {
      case Right(decoded) => decoded
      case Left(error) => throw new RuntimeException(s"unable to parse response: $error")
    }
  }

  /** Gets all draft picks for a given gameweek
   *
   * @param users List of users to get picks for
   * @param gameweek Gameweek number
   * @param players List of players to reference
   * @return List of picks with player and owner information
   */
  def getAllPicks(users: List[LeagueEntry], gameweek: Int, players: List[Player]): List[Pick] = {
    users.flatMap(u => getPicks(u, gameweek)).flatMap { p =>
      players.find(_.id == p.element).map(_.copy(owner = Some(p.user.get))).map(_ => p)
    }
  }

  /** Gets draft picks for a specific user and gameweek
   *
   * @param user User to get picks for
   * @param gameweek Gameweek number
   * @return List of picks for the user
   */
  private def getPicks(user: LeagueEntry, gameweek: Int): List[Pick] = {
    val endpoint = s"entry/${user.entry_id}/event/$gameweek"
    val response = makeRequest[PicksResponse](endpoint)
    response.picks.map(p => p.copy(user = Some(user.player_first_name)))
  }

  /** Gets full league details
   *
   * @return League details including standings and matches
   */
  def getDetails: LeagueDetails = {
    val endpoint = s"league/$leagueId/details"
    makeRequest[LeagueDetails](endpoint)
  }

  /** Gets formatted standings with user information
   *
   * @param details League details containing raw standings
   * @param users List of users to attach to standings
   * @return List of standings with user details
   */
  def getStandings(details: LeagueDetails, users: List[LeagueEntry]): List[StandingDetails] = {
    details.standings.map { e =>
      val user = users.find(_.id == e.league_entry).getOrElse(throw new RuntimeException(s"user ${e.league_entry} not found"))
      StandingDetails(
        id = e.league_entry,
        name = user.entry_name,
        user = user.player_first_name,
        rank = e.rank,
        total = e.total,
        points_for = e.points_for,
        points_against = e.points_against,
        matches_won = e.matches_won,
        matches_lost = e.matches_lost,
        matches_drawn = e.matches_drawn
      )
    }
  }

  /** Gets the current gameweek number
   *
   * @return Current gameweek number
   */
  def getCurrentGameweek: Int = {
    makeRequest[BootstrapStaticResponse]("bootstrap-static").events.current
  }

  /** Gets stats for all teams
   *
   * @return Sequence of team stats with colors and other details
   */
  def getAllTeamStats: Seq[TeamStats] = {
    teamColours.map { teamData =>
      val response = getTeamStats(teamData.secondId)
      TeamStats(
        id = teamData.id,
        secondId = teamData.secondId,
        name = teamData.name,
        mainColor = teamData.mainColor,
        secondaryColor = teamData.secondaryColor,
        stats = response.stats
      )
    }
  }

  /** Gets stats for a specific team
   *
   * @param teamId Team ID to get stats for
   * @param comp Competition ID (defaults to 1 for Premier League)
   * @param seasonId Season ID (defaults to current season)
   * @return Team statistics response
   */
  private def getTeamStats(teamId: Int, comp: Int = 1, seasonId: Int = 719): TeamStatsResponse = {
    val params = Some(Map("comp" -> comp.toString, "season" -> seasonId.toString))
    makeRequest[TeamStatsResponse](url = footballApiUrl, endpoint = s"stats/team/$teamId", params = params)
  }
}
