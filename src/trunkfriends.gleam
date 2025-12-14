import auth/auth.{type Model as AuthModel, type Msg as AuthMsg, text_paragraph}
import common/session
import gleam/dynamic/decode
import gleam/json
import gleam/list
import gleam/option
import lustre
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event
import process/process.{type Model as ProcessModel, type Msg as ProcessMsg}

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
  AuthModel(AuthModel)
  ProcessModel(ProcessModel)
  BackupRestore(option.Option(String))
}

type Msg {
  AboutMsg
  AuthWrapperMsg(AuthMsg)
  AuthResultMsg(session.Session)
  ProcessWrapperMsg(ProcessMsg)
  RestoreMsg(String)
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
  echo "updating!"
  case msg {
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
      #(Model(model.servers, section: AboutModel), effect.none())
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
      echo "doing restore!"
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
      echo "session " <> session.domain
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

      Model(_, AuthModel(model)) ->
        element.map(auth.view(model), fn(msg) { AuthWrapperMsg(msg) })

      Model(_, ProcessModel(model)) ->
        element.map(process.view(model), fn(msg) { ProcessWrapperMsg(msg) })

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
