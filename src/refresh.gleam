import api/followers
import common/session.{type Session}
import common/user.{type User}
import gleam/list
import gleam/option.{type Option}
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event

pub fn default_msg(session: Session) {
  Init(session:)
}

pub fn default_model(session: Session) {
  AskDoImport(session:)
}

pub fn update(
  _: Model,
  message: Msg,
  wrapper: fn(Msg) -> a,
) -> #(Model, Effect(a)) {
  case message {
    Init(session) -> #(default_model(session), effect.none())
    FetchUsers(session, users, max_id) -> #(
      DisplayFetchedUsers(users),
      effect.map(
        effect.from(followers.request_pages(
          session,
          max_id,
          "following",
          FetchUsers,
          GiveError,
          _,
        )),
        wrapper,
      ),
    )
    GiveError(error, message) -> #(DisplayError(error, message), effect.none())
  }
}

pub fn view(model: Model) -> Element(Msg) {
  html.div([attribute.class("centernb")], [
    case model {
      AskDoImport(session) ->
        html.div([], [
          html.h1([], [html.text("Refresh followers / following lists.")]),
          html.button(
            [event.on_click(FetchUsers(session, [], option.None))],
            [],
          ),
          html.p([], [
            html.text(
              "
              When doing this, TrunkFriends will fetch all the users that you follow,
              and all the users that follows you. The first time this is done, you
              will simply get an overview. In order for TrunkFriends to make sense,
              you need to do this on a regular intervall. How frequently, is up to you
              ",
            ),
          ]),
          html.p([], [
            html.text(
              "
              The second time you refresh your followers / following lists, you will
              only see the changes that has happened. What accounts have you followed and
              unfollowed since then. Who has unfollowed you. The same will be the case
              for every consecutive run.
              ",
            ),
          ]),
          html.p([], [
            html.text(
              "
                You will be able to scroll through each set of differences, and see what
                happened when.
              ",
            ),
          ]),
        ])

      DisplayFetchedUsers(users) ->
        html.p(
          [],
          list.map(users, fn(user) {
            html.text(user.display_name <> " " <> user.account)
          }),
        )

      DisplayStatus(text) -> text_paragraph(text)

      DisplayError(error, message) ->
        html.div([], [
          html.h2([], [html.text("Something went wrong.")]),
          text_paragraph(message),
          html.h3([], [html.text("Error message:")]),
          text_paragraph(error),
        ])
    },
    html.div([attribute.class("centernb")], []),
  ])
}

pub fn text_paragraph(text: String) {
  html.p([], [html.text(text)])
}

pub opaque type Model {
  AskDoImport(session: Session)
  DisplayFetchedUsers(fetched: List(User))
  DisplayStatus(message: String)
  DisplayError(error: String, message: String)
}

pub opaque type Msg {
  Init(session: Session)
  FetchUsers(session: Session, fetched: List(User), max_id: Option(String))
  GiveError(error: String, message: String)
}

pub type Status {
  Status(id: String, date: String, body: String)
}
