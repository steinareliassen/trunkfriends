import gleam/dynamic/decode
import gleam/fetch
import gleam/fetch/form_data
import gleam/http
import gleam/http/request
import gleam/javascript/promise
import gleam/json

pub fn fetch_client_info(
  domain: String,
  success: fn(String, String, String) -> a,
  error: fn(String, String) -> a,
  dispatch,
) {
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
    let result = case response {
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
    case result {
      Ok(#(client_id, secret)) -> dispatch(success(domain, client_id, secret))
      Error(fault_message) ->
        dispatch(error("Error fetching client info", fault_message))
    }
  })
  Nil
}

pub fn fetch_user_info(
  domain: String,
  token: String,
  success: fn(String, String, String) -> a,
  error: fn(String, String) -> a,
  dispatch,
) {
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
          use user_id <- decode.field("id", decode.string)
          decode.success(user_id)
        }
        case json.parse(from: resp.body, using: client_info_decoder) {
          Ok(text) -> Ok(text)
          Error(_) ->
            Error(resp.body <> "Error parsing result containing token")
        }
      }
      Error(_) ->
        Error(
          "Error registering to use the service, was the domain name correct?",
        )
    }
    case result {
      Ok(client_id) -> {
        dispatch(success(domain, token, client_id))
      }
      Error(fault_msg) -> {
        dispatch(error("Unable to fetch token", fault_msg))
      }
    }
  })

  Nil
}

pub fn fetch_token(
  domain: String,
  client_id: String,
  secret: String,
  code: String,
  success: fn(String, String) -> a,
  error: fn(String, String) -> a,
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
    let result = case response {
      Ok(resp) -> {
        let client_info_decoder = {
          use access_token <- decode.field("access_token", decode.string)
          decode.success(access_token)
        }
        case json.parse(from: resp.body, using: client_info_decoder) {
          Ok(text) -> Ok(text)
          Error(_) -> Error(resp.body <> "Error parsing result.")
        }
      }
      Error(_) -> Error("Error calling endpoint")
    }
    case result {
      Ok(token) -> {
        dispatch(success(domain, token))
      }
      Error(fault_message) -> {
        dispatch(error("Unable to fetch token", fault_message))
      }
    }
  })
  Nil
}
