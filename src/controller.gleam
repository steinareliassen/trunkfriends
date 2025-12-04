import gleam/list
import gleam/string
import model

import gleam/dynamic/decode
import gleam/fetch
import gleam/fetch/form_data
import gleam/http
import gleam/http/request
import gleam/javascript/promise
import gleam/json
import gleam/option.{type Option}

pub fn fetch_domain(domain: String, dispatch) {
  let response =
    request.new()
    |> request.set_host(domain)
    |> request.set_path("/api/v1/apps")
    |> request.set_method(http.Post)
    |> request.set_body({
      form_data.new()
      |> form_data.append("client_name", "quoteupdate")
      |> form_data.append("redirect_uris", "urn:ietf:wg:oauth:2.0:oob")
      |> form_data.append("scopes", "read write")
      |> form_data.append(
        "website",
        "https://github.com/steinareliassen/quoteupdate",
      )
    })
    |> fetch.form_data_to_fetch_request
    |> fetch.raw_send

  promise.map_try(response, fn(a) { Ok(fetch.from_fetch_response(a)) })
  |> promise.try_await(fetch.read_text_body)
  |> promise.map(fn(response) {
    let info = case response {
      Ok(resp) -> {
        let client_info_decoder = {
          use client_id <- decode.field("client_id", decode.string)
          use client_secret <- decode.field("client_secret", decode.string)
          decode.success(#(client_id, client_secret))
        }
        case json.parse(from: resp.body, using: client_info_decoder) {
          Ok(text) -> Ok(text)
          Error(_) ->
            Error(
              resp.body
              <> "Error parsing result. Was the domain you entered correct?",
            )
        }
      }
      Error(_) ->
        Error(
          "Error registering to use the service, was the domain name correct?",
        )
    }
    dispatch(model.FetchClientInfo(domain, info))
  })
  Nil
}

pub fn patch_status(
  domain: String,
  token: String,
  user_id: String,
  statuses: List(model.Status),
  results: List(Result(model.Status, String)),
  max_id: Option(String),
  dispatch,
) {
  case statuses {
    [status, ..posts] -> {
      let response =
        request.new()
        |> request.set_host(domain)
        |> request.set_path(
          "/api/v1/statuses/" <> status.id <> "/interaction_policy",
        )
        |> request.set_method(http.Put)
        |> request.set_header("Authorization", "Bearer " <> token)
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
              decode.success(model.Status(
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
            dispatch(model.FetchPages(
              domain:,
              token:,
              user_id:,
              result: posts,
              response: [info, ..results],
              max_id: option.Some(status.id),
              process: True,
            ))
          Error(error) -> {
            dispatch(model.ProcessError(error, "error doing stuff with status"))
          }
        }
      })
      Nil
    }

    [] -> {
      dispatch(model.FetchPages(
        domain:,
        token:,
        user_id:,
        result: [],
        response: results,
        max_id:,
        process: False,
      ))

      Nil
    }
  }
}

pub fn request_pages(
  domain: String,
  token: String,
  user_id: String,
  max_id: Option(String),
  dispatch,
) {
  request.new()
  |> request.set_host(domain)
  |> request.set_path(
    "/api/v1/accounts/"
    <> user_id
    <> "/statuses?limit=20"
    <> case max_id {
      option.Some(id) -> "&max_id=" <> id
      option.None -> ""
    },
  )
  |> request.set_method(http.Get)
  |> request.set_header("Authorization", "Bearer " <> token)
  |> fetch.send
  |> promise.try_await(fetch.read_text_body)
  |> promise.map(fn(response) {
    let result = case response {
      Ok(resp) -> {
        let client_info_decoder = {
          use id <- decode.field("id", decode.string)
          use date <- decode.field("created_at", decode.string)
          use body <- decode.field("content", decode.string)
          decode.success(model.Status(
            id:,
            date:,
            body: string.slice(body, 0, 80),
          ))
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
        dispatch(model.FetchPages(
          domain:,
          token:,
          user_id:,
          result:,
          response: [],
          max_id: case list.last(result) {
            Ok(status) -> option.Some(status.id)
            Error(_) -> option.None
          },
          process: False,
        ))
      Error(error) ->
        dispatch(model.ProcessError(
          error,
          "Error requesting pages, network problems?",
        ))
    }
  })
  Nil
}

pub fn fetch_user_info(domain: String, token: String, dispatch) {
  request.new()
  |> request.set_host(domain)
  |> request.set_path("api/v1/accounts/verify_credentials")
  |> request.set_method(http.Get)
  |> request.set_header("Authorization", "Bearer " <> token)
  |> fetch.send
  |> promise.try_await(fetch.read_text_body)
  |> promise.map(fn(response) {
    let result = case response {
      Ok(resp) -> {
        let client_info_decoder = {
          use access_token <- decode.field("id", decode.string)
          decode.success(access_token)
        }
        case json.parse(from: resp.body, using: client_info_decoder) {
          Ok(text) -> Ok(text)
          Error(_) ->
            Error(
              resp.body
              <> "Error parsing result. Was the domain you entered correct?",
            )
        }
      }
      Error(_) ->
        Error(
          "Error registering to use the service, was the domain name correct?",
        )
    }
    dispatch(model.FetchUserInfo(domain:, token:, result:))
  })
  Nil
}

pub fn fetch_token(
  domain: String,
  client_id: String,
  secret: String,
  code: String,
  dispatch,
) {
  let response =
    request.new()
    |> request.set_host(domain)
    |> request.set_path("/oauth/token")
    |> request.set_method(http.Post)
    |> request.set_body({
      form_data.new()
      |> form_data.append("client_id", client_id)
      |> form_data.append("client_secret", secret)
      |> form_data.append("code", code)
      |> form_data.append("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
      |> form_data.append("scopes", "read write")
      |> form_data.append("grant_type", "authorization_code")
    })
    |> fetch.form_data_to_fetch_request
    |> fetch.raw_send

  promise.map_try(response, fn(a) { Ok(fetch.from_fetch_response(a)) })
  |> promise.try_await(fetch.read_text_body)
  |> promise.map(fn(response) {
    let token_result = case response {
      Ok(resp) -> {
        let client_info_decoder = {
          use access_token <- decode.field("access_token", decode.string)
          decode.success(access_token)
        }
        case json.parse(from: resp.body, using: client_info_decoder) {
          Ok(text) -> Ok(text)
          Error(_) ->
            Error(
              resp.body
              <> "Error parsing result. Was the domain you entered correct?",
            )
        }
      }
      Error(_) ->
        Error(
          "Error registering to use the service, was the domain name correct?",
        )
    }
    dispatch(model.FetchTokenInfo(domain, token_result))
  })
  Nil
}
