package io.github.mitchelllisle.models

case class UserDim(
                 id: Int,
                 entryId: Option[Int],
                 name: String,
                 firstName: String,
                 lastName: String,
                 shortName: String,
                 waiverPick: Int
               )
