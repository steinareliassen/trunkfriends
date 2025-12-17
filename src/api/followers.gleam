import common/session
import common/user
import gleam/option.{type Option}

import gleam/dynamic/decode
import gleam/fetch
import gleam/http
import gleam/http/request
import gleam/javascript/promise
import gleam/json
import gleam/list

pub fn request_pages(
  session: session.Session,
  max_id: Option(String),
  direction: String,
  process_result: fn(session.Session, List(user.User), Option(String)) -> a,
  error: fn(String, String) -> a,
  dispatch,
) {
  request.new()
  |> request.set_host(session.domain)
  |> request.set_path(
    "/api/v1/accounts/"
    <> session.user_id
    <> "/"
    <> direction
    <> "?limit=40"
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
        let user_info_decoder = {
          use id <- decode.field("id", decode.string)
          use account <- decode.field("acct", decode.string)
          use user_name <- decode.field("username", decode.string)
          use display_name <- decode.field("display_name", decode.string)
          decode.success(user.User(id:, account:, user_name:, display_name:))
        }
        case
          json.parse(from: resp.body, using: decode.list(user_info_decoder))
        {
          Ok(list) -> Ok(list)
          Error(_) ->
            Error(resp.body <> "Error parsing " <> direction <> " result.")
        }
      }
      Error(_) ->
        Error(
          "Error fetching your "
          <> direction
          <> " list. Check that the instance and your network is up.",
        )
    }
    case result {
      Ok(result) ->
        dispatch(
          process_result(session, result, case list.last(result) {
            Ok(status) -> option.Some(status.id)
            Error(_) -> option.None
          }),
        )
      Error(fault_message) ->
        dispatch(error(
          fault_message,
          "Error requesting pages, network problems?",
        ))
    }
  })
  Nil
}
