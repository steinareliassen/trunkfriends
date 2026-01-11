import auth.{type Model as AuthModel, type Msg as AuthMsg, text_paragraph}
import common/session
import gleam/dynamic/decode
import gleam/json
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

@external(javascript, "./ffi/export.ffi.mjs", "getServers")
pub fn get_servers() -> String {
  "unknown"
}

@external(javascript, "./ffi/export.ffi.mjs", "storeServers")
pub fn store_servers(_servers: String) -> String {
  "unknown"
}

pub fn main() {
  let assert Ok(_) =
    lustre.application(init, update, view) |> lustre.start("#app", Nil)
  Nil
}

type Model {
  Model(server: option.Option(session.Session), section: Section)
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
  ProcessMsg(ProcessMsg)
  //  HistoryMsg(HistoryMsg)
  //OverviewMsg(OverviewMsg)
  RestoreMsg(String)
  ErrorMsg(String)
  DoRestoreMsg(String)
}

fn init(_) -> #(Model, Effect(Msg)) {
  let #(model, effect) = auth.init()
  let value = get_servers()
  let session_decoder = {
    use domain <- decode.field("domain", decode.string)
    use token <- decode.field("token", decode.string)
    use user_id <- decode.field("user_id", decode.string)
    decode.success(session.Session(domain:, token:, user_id:))
  }
  let session = case json.parse(from: value, using: session_decoder) {
    Ok(text) -> {
      echo "decode ok! "
      option.Some(text)
    }
    Error(_e) -> {
      echo "decode not ok "
      option.None
    }
  }
  #(
    Model(session, AuthModel(model)),
    effect.map(effect, fn(msg) { AuthWrapperMsg(msg) }),
  )
}

fn update(model: Model, msg: Msg) -> #(Model, Effect(Msg)) {
  case msg {
    ErrorMsg(msg) -> {
      #(Model(..model, section: ErrorModel(msg)), effect.none())
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
      #(
        Model(
          ..model,
          section: ErrorModel(case model {
            Model(option.Some(x), _) ->
              "Server" <> x.domain <> " user " <> x.user_id
            _ -> "WE DO NOT HAVE A SERVER!"
          }),
        ),
        effect.none(),
      )
    }
    AuthResultMsg(session) -> {
      store_servers(
        "{ \"domain\":\""
        <> session.domain
        <> "\",\"token\":\""
        <> session.token
        <> "\",\"user_id\":\""
        <> session.user_id
        <> "\"}",
      )
      #(
        Model(section: BackupRestore(option.None), server: option.Some(session)),
        effect.none(),
      )
    }
    RefreshFollowingMsg(msg) -> {
      let assert option.Some(server) = model.server
      let #(model, effect) = case model {
        Model(_, model) -> {
          refresh.update(
            case model {
              RefreshModel(model) -> model
              _ -> {
                refresh.default_model(server)
              }
            },
            msg,
            RefreshFollowingMsg,
          )
        }
      }

      #(Model(option.Some(server), RefreshModel(model)), effect)
    }
    ProcessMsg(msg) -> {
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
      #(
        Model(..model, section: BackupRestore(option.Some(value))),
        effect.none(),
      )
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
          server: option.Some(session),
          section: BackupRestore(option.Some("restored!")),
        ),
        effect.none(),
      )
    }
  }
}

fn session_to_string(session: option.Option(session.Session)) {
  case session {
    option.Some(session) ->
      "{ 'domain':'"
      <> session.domain
      <> "','token':'"
      <> session.token
      <> "','user_id','"
      <> session.user_id
      <> "'}"
    _ -> "No server present!"
  }
}

fn view(model: Model) -> Element(Msg) {
  html.div([attribute.class("center")], [
    html.button([event.on_click(AboutMsg)], [html.text("About")]),
    html.button([event.on_click(AuthWrapperMsg(auth.default_msg()))], [
      html.text("Add server"),
    ]),
    html.span([], case model.server {
      option.Some(session) -> {
        [
          html.button(
            [event.on_click(RefreshFollowingMsg(refresh.default_msg(session)))],
            [
              html.text("Refresh followers"),
            ],
          ),
          html.button(
            [event.on_click(ProcessMsg(process.default_msg(session)))],
            [
              html.text("Process posts"),
            ],
          ),
        ]
      }
      option.None -> {
        [element.none()]
      }
    }),
    html.button([event.on_click(RestoreMsg(""))], [
      html.text("Backup/Restore"),
    ]),
    case model.server {
      option.None ->
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
        element.map(process.view(model), fn(msg) { ProcessMsg(msg) })

      Model(_, RefreshModel(model)) ->
        element.map(refresh.view(model), fn(msg) { RefreshFollowingMsg(msg) })

      Model(session, BackupRestore(restored)) -> {
        let restored = case restored {
          option.Some(value) -> value
          option.None -> ""
        }
        html.div([], [
          html.div([], [html.text(session_to_string(session))]),
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
