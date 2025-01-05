package io.github.mitchelllisle.fantasy

import io.circe.Decoder
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.semiauto.deriveDecoder

trait AutoDeriveDecoder[T] {
  implicit def decoder(implicit ev: DerivedDecoder[T]): Decoder[T] = ev
}

object AutoDeriveDecoder {
  implicit def derivedDecoder[T](implicit ev: DerivedDecoder[T]): Decoder[T] = deriveDecoder[T]
}

final case class Player(
                         event: Option[Int],
                         owner: Option[String],
                         added: String,
                         assists: Int,
                         bonus: Int,
                         bps: Int,
                         chance_of_playing_next_round: Option[Int],
                         chance_of_playing_this_round: Option[Int],
                         clean_sheets: Int,
                         code: Int,
                         corners_and_indirect_freekicks_order: Option[Int],
                         corners_and_indirect_freekicks_text: String,
                         creativity: String,
                         creativity_rank: Int,
                         creativity_rank_type: Int,
                         direct_freekicks_order: Option[Int],
                         direct_freekicks_text: String,
                         draft_rank: Int,
                         dreamteam_count: Int,
                         element_type: Int,
                         ep_next: Option[String],
                         ep_this: Option[String],
                         event_points: Int,
                         expected_assists: String,
                         expected_goal_involvements: String,
                         expected_goals: String,
                         expected_goals_conceded: String,
                         first_name: String,
                         form: String,
                         form_rank: Option[Int],
                         form_rank_type: Option[Int],
                         goals_conceded: Int,
                         goals_scored: Int,
                         ict_index: String,
                         ict_index_rank: Int,
                         ict_index_rank_type: Int,
                         id: Int,
                         in_dreamteam: Boolean,
                         influence: String,
                         influence_rank: Int,
                         influence_rank_type: Int,
                         minutes: Int,
                         news: String,
                         news_added: Option[String],
                         news_return: Option[String],
                         news_updated: Option[String],
                         own_goals: Int,
                         penalties_missed: Int,
                         penalties_order: Option[Int],
                         penalties_saved: Int,
                         penalties_text: String,
                         points_per_game: String,
                         points_per_game_rank: Option[Int],
                         points_per_game_rank_type: Option[Int],
                         red_cards: Int,
                         saves: Int,
                         second_name: String,
                         squad_number: Option[Int],
                         starts: Int,
                         status: String,
                         team: Int,
                         threat: String,
                         threat_rank: Int,
                         threat_rank_type: Int,
                         total_points: Int,
                         web_name: String,
                         yellow_cards: Int,
                         name: Option[String],
                         team_code: Option[Int],
                         team_name: Option[String],
                         team_primary_colour: Option[String],
                         team_secondary_colour: Option[String]
                       ) extends AutoDeriveDecoder[Player]

final case class MatchResult(
                              id: Option[Int] = None,
                              gameweek: Int,
                              finished: Boolean,
                              team: String,
                              total_points: Int,
                              points: Int
                            ) extends AutoDeriveDecoder[MatchResult]

final case class MatchResultWithAcc(
                                     id: Option[Int] = None,
                                     gameweek: Int,
                                     finished: Boolean,
                                     team: String,
                                     total_points: Int,
                                     points: Int,
                                     points_acc: Int
                                   ) extends AutoDeriveDecoder[MatchResultWithAcc]

final case class StandingDetails(
                                  id: Int,
                                  name: String,
                                  user: String,
                                  rank: Int,
                                  total: Int,
                                  points_for: Int,
                                  points_against: Int,
                                  matches_won: Int,
                                  matches_lost: Int,
                                  matches_drawn: Int
                                ) extends AutoDeriveDecoder[StandingDetails]

final case class League(
                         admin_entry: Option[Int],
                         closed: Option[Boolean],
                         draft_dt: Option[String],
                         draft_pick_time_limit: Option[Int],
                         draft_status: Option[String],
                         draft_tz_show: Option[String],
                         id: Option[Int],
                         ko_rounds: Option[Int],
                         make_code_public: Option[Boolean],
                         max_entries: Option[Int],
                         min_entries: Option[Int],
                         name: Option[String],
                         scoring: Option[String],
                         start_event: Option[Int],
                         stop_event: Option[Int],
                         trades: Option[String],
                         transaction_mode: Option[String],
                         variety: Option[String],
                       ) extends AutoDeriveDecoder[League]

final case class LeagueEntry(
                              entry_id: Option[Int],
                              entry_name: String,
                              id: Int,
                              joined_time: String,
                              player_first_name: String,
                              player_last_name: String,
                              short_name: String,
                              waiver_pick: Int
                            ) extends AutoDeriveDecoder[LeagueEntry]

final case class LeagueDetailsMatch(
                                     event: Int,
                                     finished: Boolean,
                                     league_entry_1: Int,
                                     league_entry_1_points: Int,
                                     league_entry_2: Int,
                                     league_entry_2_points: Int,
                                     started: Boolean,
                                     winning_league_entry: Option[Int],
                                     winning_method: Option[String]
                                   ) extends AutoDeriveDecoder[LeagueDetailsMatch]

final case class Standing(
                           last_rank: Int,
                           league_entry: Int,
                           matches_drawn: Int,
                           matches_lost: Int,
                           matches_played: Int,
                           matches_won: Int,
                           points_against: Int,
                           points_for: Int,
                           rank: Int,
                           rank_sort: Int,
                           total: Int
                         ) extends AutoDeriveDecoder[Standing]

final case class LeagueDetails(
                                league: League,
                                league_entries: List[LeagueEntry],
                                matches: List[LeagueDetailsMatch],
                                standings: List[Standing]
                              ) extends AutoDeriveDecoder[LeagueDetails]

final case class Pick(element: Int, position: Int, is_captain: Boolean, is_vice_captain: Boolean, user: Option[String]) extends AutoDeriveDecoder[Pick]

final case class PicksResponse(picks: List[Pick]) extends AutoDeriveDecoder[PicksResponse]

final case class TeamColour(id: Int, secondId: Int, name: String, mainColor: String, secondaryColor: String)

final case class TeamStats(
                            id: Int,
                            secondId: Int,
                            name: String,
                            mainColor: String,
                            secondaryColor: String,
                            stats: List[Stat]
                          ) extends AutoDeriveDecoder[TeamStats]


final case class Events(current: Int) extends AutoDeriveDecoder[Events]

final case class TeamResponse(
                       code: Int,
                       id: Int,
                       name: String,
                       pulse_id: Int,
                       short_name: String
                     ) extends AutoDeriveDecoder[TeamResponse]

final case class BootstrapStaticResponse(
                                          events: Events,
                                          elements: List[Player],
                                          teams: List[TeamResponse]
                                        ) extends AutoDeriveDecoder[BootstrapStaticResponse]

final case class Kickoff(
                          completeness: Option[Int],
                          millis: Option[Long],
                          label: Option[String],
                          gmtOffset: Option[Int] = None
                        ) extends AutoDeriveDecoder[Kickoff]

final case class Clock(
                        secs: Option[Int],
                        label: Option[String]
                      ) extends AutoDeriveDecoder[Clock]

final case class Competition(
                              abbreviation: Option[String],
                              description: Option[String],
                              level: Option[String],
                              source: Option[String],
                              id: Int
                            ) extends AutoDeriveDecoder[Competition]

final case class CompSeason(
                             label: Option[String],
                             competition: Option[Competition],
                             id: Option[Int]
                           ) extends AutoDeriveDecoder[CompSeason]

final case class Gameweek(
                           id: Int,
                           compSeason: CompSeason,
                           gameweek: Option[Int]
                         ) extends AutoDeriveDecoder[Gameweek]

final case class Club(
                       name: String,
                       shortName: String,
                       abbr: String,
                       id: Int
                     ) extends AutoDeriveDecoder[Club]

final case class Team(
                       name: String,
                       club: Club,
                       teamType: String,
                       shortName: String,
                       id: Int
                     ) extends AutoDeriveDecoder[Team]

final case class TeamScore(
                            team: Team,
                            score: Int
                          ) extends AutoDeriveDecoder[TeamScore]

final case class Ground(
                         name: Option[String],
                         city: Option[String],
                         source: Option[String],
                         id: Option[Int],
                       ) extends AutoDeriveDecoder[Ground]

final case class Stat(
                       name: String,
                       value: Double,
                       description: String,
                     ) extends AutoDeriveDecoder[Stat]

final case class Match(
                        gameweek: Option[Gameweek],
                        kickoff: Option[Kickoff],
                        provisionalKickoff: Option[Kickoff],
                        teams: Option[List[TeamScore]],
                        replay: Option[Boolean],
                        ground: Option[Ground],
                        neutralGround: Option[Boolean],
                        status: Option[String],
                        phase: Option[String],
                        outcome: Option[String],
                        attendance: Option[Int],
                        clock: Option[Clock],
                        fixtureType: Option[String],
                        extraTime: Option[Boolean],
                        shootout: Option[Boolean],
                        behindClosedDoors: Option[Boolean],
                        id: Int
                      ) extends AutoDeriveDecoder[Match]

final case class TeamStatsResponse(
                                    greatestVictory: Option[Match],
                                    worstDefeat: Option[Match],
                                    entity: Team,
                                    stats: List[Stat]
                                  ) extends AutoDeriveDecoder[TeamStatsResponse]
