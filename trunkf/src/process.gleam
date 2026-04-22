import common/session
import gleam/list
import gleam/option.{type Option}
import lustre/element/keyed

import gleam/dynamic/decode
import gleam/fetch
import gleam/fetch/form_data
import gleam/http
import gleam/http/request
import gleam/javascript/promise
import gleam/json
import gleam/string
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event

pub fn default_msg(session: session.Session) {
  Init(session:)
}

pub fn default_model() {
  DisplayStatus("Fetching pages")
}

pub fn update(_: Model, msg: Msg, wrapper: fn(Msg) -> a) -> #(Model, Effect(a)) {
  let #(model, effect) = case msg {
    Init(session) -> #(
      default_model(),
      effect.map(effect.from(request_pages(session, option.None, _)), wrapper),
    )

    ProcessError(error:, message:) -> #(
      DisplayError(error:, message:),
      effect.none(),
    )

    FetchPages(session:, result:, response:, process:, max_id:) -> #(
      ProcessPosts(session, result, response, max_id),
      case result {
        [] ->
          effect.map(effect.from(request_pages(session, max_id, _)), wrapper)
        _ ->
          case process {
            False -> effect.none()
            True ->
              effect.map(
                effect.from(patch_status(session, result, response, max_id, _)),
                wrapper,
              )
          }
      },
    )
  }
  #(model, effect)
}

pub fn view(model: Model) -> Element(Msg) {
  html.div([attribute.class("centernb")], case model {
    DisplayStatus(text) -> [html.text(text)]

    DisplayError(error, message) -> {
      [
        html.h2([], [html.text("Something went wrong.")]),
        html.text(message),
        html.h3([], [html.text("Error message:")]),
        html.text(error),
      ]
    }

    ProcessPosts(session, list, results, max_id) -> {
      [
        keyed.div(
          [],
          list.append(
            list.map(list, fn(status) { #(status.id, status_line(status)) }),
            list.map(results, fn(status) {
              case status {
                Ok(status) -> #(status.id, status_line(status))
                Error(error) -> #(
                  error,
                  html.text("Something went wrong: " <> error),
                )
              }
            }),
          ),
        ),
        case results {
          [] ->
            button_text_paragraph(
              "The posts above are ready to be processed",
              "Process these posts?",
              FetchPages(
                session:,
                result: list,
                response: results,
                max_id: max_id,
                process: True,
              ),
            )
          _ -> element.none()
        },
      ]
    }
  })
}

fn status_line(status: Status) -> Element(Msg) {
  html.div([], [
    html.div([], [
      html.text("ID :" <> status.id <> " Created: " <> status.date),
    ]),
    html.div([], [
      html.text("Text: " <> status.body),
    ]),
  ])
}

pub fn text_paragraph(text: String) {
  html.p([], [html.text(text)])
}

fn button_text_paragraph(text: String, button: String, on_click: Msg) {
  html.p([], [
    html.text(text),
    html.button([event.on_click(on_click)], [html.text(button)]),
  ])
}

fn patch_status(
  session: session.Session,
  statuses: List(Status),
  results: List(Result(Status, String)),
  max_id: Option(String),
  dispatch,
) {
  case statuses {
    [status, ..posts] -> {
      let response =
        request.new()
        |> request.set_host(session.domain)
        |> request.set_path(
          "/api/v1/statuses/" <> status.id <> "/interaction_policy",
        )
        |> request.set_method(http.Put)
        |> request.set_header("Authorization", "Bearer " <> session.token)
        |> request.set_body({
          form_data.new()
          |> form_data.append("quote_approval_policy", "public")
        })
        |> fetch.form_data_to_fetch_request
        |> fetch.raw_send

      promise.map_try(response, fn(a) { Ok(fetch.from_fetch_response(a)) })
      |> promise.try_await(fetch.read_text_body)
      |> promise.map(fn(response) {
        let info = case response {
          Ok(resp) -> {
            let status_decoder = {
              use date <- decode.field("created_at", decode.string)
              use body <- decode.field("content", decode.string)
              decode.success(Status(
                id: status.id,
                date: date <> "<processed>",
                body: "<processed>" <> string.slice(body, 0, 60),
              ))
            }
            case json.parse(from: resp.body, using: status_decoder) {
              Ok(text) -> Ok(text)
              Error(_) ->
                Error(resp.body <> "Error parsing json, should not happen")
            }
          }
          Error(_) -> Error("Error updating status. should not happen.")
        }
        case info {
          Ok(_) ->
            dispatch(FetchPages(
              session:,
              result: posts,
              response: [info, ..results],
              max_id: option.Some(status.id),
              process: True,
            ))
          Error(error) -> {
            dispatch(ProcessError(error, "error doing stuff with status"))
          }
        }
      })
      Nil
    }

    [] -> {
      dispatch(FetchPages(
        session:,
        result: [],
        response: results,
        max_id:,
        process: False,
      ))

      Nil
    }
  }
}

fn request_pages(session: session.Session, max_id: Option(String), dispatch) {
  request.new()
  |> request.set_host(session.domain)
  |> request.set_path(
    "/api/v1/accounts/"
    <> session.user_id
    <> "/statuses?limit=20"
    <> case max_id {
      option.Some(id) -> "&max_id=" <> id
      option.None -> ""
    },
  )
  |> request.set_method(http.Get)
  |> request.set_header("Authorization", "Bearer " <> session.token)
  |> fetch.send
  |> promise.try_await(fetch.read_text_body)
  |> promise.map(fn(response) {
    let result = case response {
      Ok(resp) -> {
        let client_info_decoder = {
          use id <- decode.field("id", decode.string)
          use date <- decode.field("created_at", decode.string)
          use body <- decode.field("content", decode.string)
          decode.success(Status(id:, date:, body: string.slice(body, 0, 80)))
        }
        case
          json.parse(from: resp.body, using: decode.list(client_info_decoder))
        {
          Ok(list) -> Ok(list)
          Error(_) ->
            Error(
              resp.body
              <> "Error parsing result. Was the domain you entered correct?",
            )
        }
      }
      Error(_) ->
        Error(
          "Error fetching your mastodon posts. Are you experiencing network errors?",
        )
    }
    case result {
      Ok(result) ->
        dispatch(FetchPages(
          session:,
          result:,
          response: [],
          max_id: case list.last(result) {
            Ok(status) -> option.Some(status.id)
            Error(_) -> option.None
          },
          process: False,
        ))
      Error(error) ->
        dispatch(ProcessError(
          error,
          "Error requesting pages, network problems?",
        ))
    }
  })
  Nil
}

pub opaque type Model {
  DisplayStatus(status: String)
  DisplayError(error: String, message: String)
  ProcessPosts(
    session: session.Session,
    posts: List(Status),
    response: List(Result(Status, String)),
    max_id: option.Option(String),
  )
}

pub opaque type Msg {
  Init(session: session.Session)
  ProcessError(error: String, message: String)
  FetchPages(
    session: session.Session,
    result: List(Status),
    response: List(Result(Status, String)),
    max_id: option.Option(String),
    process: Bool,
  )
}

pub type Status {
  Status(id: String, date: String, body: String)
}
