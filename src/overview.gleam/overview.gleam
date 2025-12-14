import common/session
import gleam/dynamic/decode
import gleam/fetch
import gleam/fetch/form_data
import gleam/http
import gleam/http/request
import gleam/javascript/promise
import gleam/json
import gleam/list
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event

pub fn default_msg() {
  Init
}

pub fn default_model() {
  Model([])
}

pub fn update(
  model: Model,
  message: Msg,
  wrapper: fn(Msg) -> a,
  giveback: fn(session.Session) -> a,
) -> #(Model, Effect(a)) {
  case message {
    Init -> #(default_model(), effect.none())
    SwapPage(_) -> #(model, effect.none())
  }
}

pub fn view(model: Model) -> Element(Msg) {
  let Model(lines) = model
  html.div([attribute.class("centernb")], [
    html.button([event.on_click(SwapPage(Previous))], [
      html.text("<< previous page"),
    ]),
    html.button([event.on_click(SwapPage(Next))], [html.text("next page >>")]),
    html.div(
      [attribute.class("centernb")],
      list.map(lines, fn(line) {
        html.div([], [html.text(line.account <> " did stuff")])
      }),
    ),
  ])
}

pub type Model {
  Model(List(Line))
}

pub opaque type Line {
  Line(account: String, following_you: Bool, you_follow: Bool, has_note: Bool)
}

type Action {
  FollowingYou
  UnfollowedYou
  YouFollowedThem
  YouUnfollowedThem
  AddedNote
  UpdatedNote
}

type Direction {
  Next
  Previous
}

pub opaque type Msg {
  Init
  SwapPage(Direction)
}

pub type Status {
  Status(id: String, date: String, body: String)
}
