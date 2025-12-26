import auth.{type Model as AuthModel, type Msg as AuthMsg, text_paragraph}
import common/session
import gleam/dynamic/decode
import gleam/int
import gleam/json
import gleam/list
import gleam/option
import history.{type Model as HistoryModel, type Msg as HistoryMsg}
import lustre
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event
import overview.{type Model as OverviewModel, type Msg as OverviewMsg}
import process.{type Model as ProcessModel, type Msg as ProcessMsg}
import refresh.{type Model as RefreshModel, type Msg as RefreshMsg}

@external(javascript, "./ffi/export.ffi.mjs", "start")
pub fn start() -> Int {
  300
}

pub fn main() {
  let assert Ok(_) =
    lustre.application(init, update, view) |> lustre.start("#app", Nil)
  Nil
}

type Model {
  Model(servers: List(session.Session), section: Section)
}

type Section {
  AboutModel
  ErrorModel(String)
  AuthModel(AuthModel)
  RefreshModel(RefreshModel)
  ProcessModel(ProcessModel)
  //HistoryModel(HistoryModel)
  //OverviewModel(OverviewModel)
  BackupRestore(option.Option(String))
}

type Msg {
  AboutMsg
  AuthWrapperMsg(AuthMsg)
  AuthResultMsg(session.Session)
  RefreshFollowingMsg(RefreshMsg)
  ProcessWrapperMsg(ProcessMsg)
  //  HistoryMsg(HistoryMsg)
  //OverviewMsg(OverviewMsg)
  RestoreMsg(String)
  ErrorMsg(String)
  DoRestoreMsg(String)
}

fn init(_) -> #(Model, Effect(Msg)) {
  let #(model, effect) = auth.init()
  #(
    Model([], AuthModel(model)),
    effect.map(effect, fn(msg) { AuthWrapperMsg(msg) }),
  )
}

fn update(model: Model, msg: Msg) -> #(Model, Effect(Msg)) {
  case msg {
    ErrorMsg(msg) -> {
      #(Model(servers: model.servers, section: ErrorModel(msg)), effect.none())
    }
    AuthWrapperMsg(msg) -> {
      case model {
        Model(servers, model) -> {
          let #(model, effect) =
            auth.update(
              case model {
                AuthModel(model) -> model
                _ -> auth.default_model()
              },
              msg,
              AuthWrapperMsg,
              AuthResultMsg,
            )
          #(Model(servers, AuthModel(model)), effect)
        }
      }
    }
    AboutMsg -> {
      let x = start()
      #(
        Model(model.servers, section: ErrorModel("x" <> int.to_string(x))),
        effect.none(),
      )
    }
    AuthResultMsg(session) -> {
      #(
        Model(section: BackupRestore(option.None), servers: [
          session,
          ..model.servers
        ]),
        effect.none(),
      )
    }
    RefreshFollowingMsg(msg) -> {
      let servers = model.servers
      let #(model, effect) = case model {
        Model(servers, model) -> {
          refresh.update(
            case model {
              RefreshModel(model) -> model
              _ -> {
                let assert Ok(server) = list.first(servers)
                refresh.default_model(server)
              }
            },
            msg,
            RefreshFollowingMsg,
          )
        }
      }
      #(Model(servers, RefreshModel(model)), effect)
    }
    ProcessWrapperMsg(msg) -> {
      case model {
        Model(servers, model) -> {
          let #(model, effect) =
            process.update(
              case model {
                ProcessModel(model) -> model
                _ -> process.default_model()
              },
              msg,
            )
          #(Model(servers, ProcessModel(model)), effect.none())
        }
      }
    }
    RestoreMsg(value) -> {
      #(Model(model.servers, BackupRestore(option.Some(value))), effect.none())
    }
    DoRestoreMsg(value) -> {
      let session_decoder = {
        use domain <- decode.field("domain", decode.string)
        use token <- decode.field("token", decode.string)
        use user_id <- decode.field("user_id", decode.string)
        decode.success(session.Session(domain:, token:, user_id:))
      }
      let session = case json.parse(from: value, using: session_decoder) {
        Ok(text) -> text
        Error(_) -> session.Session("error", "error", "error")
      }
      #(
        Model(
          [session, ..model.servers],
          BackupRestore(option.Some("restored!")),
        ),
        effect.none(),
      )
    }
  }
}

fn view(model: Model) -> Element(Msg) {
  html.div([attribute.class("center")], [
    html.button([event.on_click(AboutMsg)], [html.text("About")]),
    html.button([event.on_click(AuthWrapperMsg(auth.default_msg()))], [
      html.text("Add server"),
    ]),
    html.span([], case list.first(model.servers) {
      Ok(session) -> {
        [
          html.button(
            [event.on_click(RefreshFollowingMsg(refresh.default_msg(session)))],
            [
              html.text("Refresh followers"),
            ],
          ),
          html.button(
            [event.on_click(ProcessWrapperMsg(process.default_msg(session)))],
            [
              html.text("Process posts"),
            ],
          ),
        ]
      }
      Error(_) -> {
        [element.none()]
      }
    }),
    html.button([event.on_click(RestoreMsg(""))], [
      html.text("Backup/Restore"),
    ]),
    case model.servers {
      [] ->
        html.div([], [
          text_paragraph("Welcome to Trunkfriends."),
          text_paragraph(
            "Trunkfriends is a tool to manage and backup your mastodon account(s).
      We cannot find any configured servers, so lets start off by connecting
      to the mastodon instance you want to work with first.",
          ),
          text_paragraph(
            "This introduction message will go away once the first
      connetion is added",
          ),
          text_paragraph(
            "If you have already used trunkfriends before, and have a backup file
      you want to restore, click 'Restore Backup'. From there, you can also
      import files from the retired Desktop version of Trunkfriends.",
          ),
          text_paragraph(
            "You most likely ended up on this page because you have found info about
      Trunkfriends somewhere, and want to try it. If this is not the case,
      and you are curious as to what this is, click on the 'About' button
      to read more.",
          ),
          text_paragraph(
            "Trunkfriends is written in Gleam, and runs entirely in your web browser.
      The full source code with a simple install setup is provided if you prefer
      to build and run it yourself. The only thing required is Docker or a
      compatible container engine like Podman.",
          ),
        ])
      _ -> text_paragraph("We have a server!")
    },
    case model {
      Model(_, AboutModel) -> text_paragraph("About!")

      Model(_, ErrorModel(s)) -> text_paragraph(s)

      Model(_, AuthModel(model)) ->
        element.map(auth.view(model), fn(msg) { AuthWrapperMsg(msg) })

      Model(_, ProcessModel(model)) ->
        element.map(process.view(model), fn(msg) { ProcessWrapperMsg(msg) })

      Model(_, RefreshModel(model)) ->
        element.map(refresh.view(model), fn(msg) { RefreshFollowingMsg(msg) })

      Model(servers, BackupRestore(restored)) -> {
        let restored = case restored {
          option.Some(value) -> value
          option.None -> ""
        }
        html.div([], [
          html.div(
            [],
            list.map(servers, fn(x) {
              html.text(
                "{ 'domain':'"
                <> x.domain
                <> "','token':'"
                <> x.token
                <> "','user_id','"
                <> x.user_id
                <> "'}",
              )
            }),
          ),
          html.div([], [
            html.input([
              attribute.value(restored),
              event.on_input(RestoreMsg),
            ]),
            html.button([event.on_click(DoRestoreMsg(restored))], [
              html.text("Restore server"),
            ]),
          ]),
        ])
      }
    },
  ])
}
