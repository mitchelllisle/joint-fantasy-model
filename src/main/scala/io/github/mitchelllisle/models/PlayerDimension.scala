package io.github.mitchelllisle.models

case class PlayerDim(
                            id: Int,
                            name: String,
                            teamId: Int,
                            position: Int,
                            firstName: String,
                            webName: String
                          )

final case class PlayerGamePerformanceFact(
                                            eventId: Int,
                                            playerId: Int,
                                            teamId: Int,
                                            minutes: Int,
                                            assists: Int,
                                            bonus: Int,
                                            bps: Int,
                                            cleanSheets: Int,
                                            goalsConceded: Int,
                                            goalsScored: Int,
                                            ownGoals: Int,
                                            penaltiesMissed: Int,
                                            penaltiesSaved: Int,
                                            redCards: Int,
                                            saves: Int,
                                            starts: Int,
                                            yellowCards: Int,
                                            totalPoints: Int,
                                            eventPoints: Int
                                          )

final case class PlayerExpectedStatsFact(
                                          eventId: Int,
                                          playerId: Int,
                                          expectedAssists: Float,
                                          expectedGoalInvolvements: Float,
                                          expectedGoals: Float,
                                          expectedGoalsConceded: Float
                                        )
