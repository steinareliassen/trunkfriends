import common/session
import gleam/dynamic/decode
import gleam/fetch
import gleam/fetch/form_data
import gleam/http
import gleam/http/request
import gleam/javascript/promise
import gleam/json
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event

pub fn init() -> #(Model, Effect(Msg)) {
  #(DomainField(""), effect.none())
}

pub fn default_msg() {
  GiveDomain("")
}

pub fn default_model() {
  DomainField("")
}

pub fn update(
  _: Model,
  message: Msg,
  wrapper: fn(Msg) -> a,
  giveback: fn(session.Session) -> a,
) -> #(Model, Effect(a)) {
  case message {
    Init -> #(DomainField(""), effect.none())
    GiveDomain(domain) -> #(DomainField(domain), effect.none())
    GiveCode(domain: text, secret:, client_id:, code:) -> #(
      CodeField(domain: text, secret:, client_id:, code:),
      effect.none(),
    )
    RegisterClient(domain) -> {
      #(Domain(domain), wrap_up(effect.from(fetch_domain(domain, _)), wrapper))
    }

    FetchClientInfo(domain, result) -> #(
      case result {
        Ok(#(client_id, secret)) -> ClientInfo(domain:, client_id:, secret:)
        Error(error) ->
          DisplayError(error:, message: "Error fetching client info")
      },
      effect.none(),
    )

    RegisterToken(domain:, client_id:, secret:, code:) -> {
      #(
        DisplayStatus(
          "Obtained token for "
          <> domain
          <> ", now fetching the your user_id, needed to update your posts",
        ),
        wrap_up(
          effect.from(fetch_token(domain, client_id, secret, code, _)),
          wrapper,
        ),
      )
    }

    FetchTokenInfo(domain, token) ->
      case token {
        Ok(token) -> #(
          DisplayStatus("Done fetching token, fetching user info"),
          wrap_up(effect.from(fetch_user_info(domain, token, _)), wrapper),
        )
        Error(error) -> #(
          DisplayError(error:, message: "Error fetching token"),
          effect.none(),
        )
      }

    FetchUserInfo(domain:, token:, result:) ->
      case result {
        Ok(user_id) -> #(
          DisplayStatus("Got userinfo, fetching first set of pages..."),
          effect.from(fn(dispatch) {
            dispatch(giveback(session.Session(domain:, token:, user_id:)))
          }),
        )
        Error(error) -> #(
          DisplayError(
            error:,
            message: "Something went wrong fetching user info",
          ),
          effect.none(),
        )
      }

    ProcessError(error:, message:) -> #(
      DisplayError(error:, message:),
      effect.none(),
    )
  }
}

fn wrap_up(effect: Effect(Msg), wrapper: fn(Msg) -> a) {
  effect.map(effect, fn(e) { wrapper(e) })
}

pub fn view(model: Model) -> Element(Msg) {
  html.div([attribute.class("centernb")], [
    case model {
      DomainField(domain) ->
        html.div([], [
          html.h1([], [html.text("What mastodon instance are you using?")]),
          html.div([], [
            view_input(domain, GiveDomain, RegisterClient),
          ]),
        ])

      Domain(domain) ->
        html.div([], [
          html.text(
            "Registering client info for " <> domain <> " waiting for result",
          ),
        ])

      ClientInfo(domain, client_id, secret) -> {
        let url =
          "https://"
          <> domain
          <> "/oauth/authorize?client_id="
          <> client_id
          <> "&scope=read+write&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code"
        element.fragment([
          html.div([], [
            text_paragraph(
              "We need an auth token to be allowed to do the requests on your behalf.",
            ),
            text_paragraph(
              "Below is a link, right click to open this in a new tab, and sign in to your mastodon account if needed",
            ),
            text_paragraph(
              "When asked to accept, do so. You will get a token with a Copy button next to it.",
            ),
            text_paragraph(
              "Press the copy button to copy the token, and go back this page.",
            ),
            text_paragraph(
              "Press the \"Continue to next page\" button where you will get a field to paste the token into",
            ),
          ]),
          html.h3([], [
            html.a([attribute.href(url)], [
              html.text(
                "Right click this link to open in new tab, and follow instructions",
              ),
            ]),
          ]),
          html.button(
            [
              event.on_click(GiveCode(domain:, client_id:, secret:, code: "")),
            ],
            [
              html.text("Continue to next page."),
            ],
          ),
        ])
      }

      CodeField(domain:, client_id:, secret:, code:) ->
        html.div([], [
          html.h1([], [html.text("Paste the code here and click continue")]),
          html.div([], [
            view_input_with_secret(
              domain,
              client_id,
              secret,
              code,
              GiveCode,
              RegisterToken,
            ),
          ]),
        ])

      Token(domain:) ->
        text_paragraph(
          "Registering client info for " <> domain <> " waiting for result",
        )

      DisplayStatus(text) -> text_paragraph(text)

      DisplayError(error, message) -> {
        html.div([], [
          html.h2([], [html.text("Something went wrong.")]),
          text_paragraph(message),
          html.h3([], [html.text("Error message:")]),
          text_paragraph(error),
        ])
      }
    },
    html.div([attribute.class("centernb")], []),
  ])
}

pub fn text_paragraph(text: String) {
  html.p([], [html.text(text)])
}

fn view_input(
  model: String,
  handle_text: fn(String) -> Msg,
  handle_button: fn(String) -> Msg,
) -> Element(Msg) {
  html.div([], [
    html.p([], [
      html.text(
        "Enter the mastodon server name (examples: meow.social, mastodon.au, tech.lgbt ...",
      ),
      html.input([
        attribute.value(model),
        event.on_input(handle_text),
      ]),
      html.button([event.on_click(handle_button(model))], [
        html.text("continue"),
      ]),
    ]),
  ])
}

fn view_input_with_secret(
  domain: String,
  client_id: String,
  secret: String,
  code: String,
  handle_text: fn(String, String, String, String) -> Msg,
  handle_button: fn(String, String, String, String) -> Msg,
) -> Element(Msg) {
  html.div([], [
    html.p([], [
      html.text(
        "Select the input field and paste (rightclick -> paste or shortcut like ctrl+v), then press continue",
      ),
      html.input([
        attribute.value(code),
        event.on_input(handle_text(domain, client_id, secret, _)),
      ]),
      html.button(
        [event.on_click(handle_button(domain, client_id, secret, code))],
        [
          html.text("continue"),
        ],
      ),
    ]),
  ])
}

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
    dispatch(FetchClientInfo(domain, info))
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
    dispatch(FetchUserInfo(domain:, token:, result:))
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
    dispatch(FetchTokenInfo(domain, token_result))
  })
  Nil
}

pub opaque type Model {
  DomainField(String)
  Domain(String)
  ClientInfo(domain: String, client_id: String, secret: String)
  CodeField(domain: String, client_id: String, secret: String, code: String)
  Token(domain: String)
  DisplayStatus(status: String)
  DisplayError(error: String, message: String)
}

pub opaque type Msg {
  Init
  GiveDomain(String)
  RegisterClient(String)
  ProcessError(error: String, message: String)
  GiveCode(domain: String, client_id: String, secret: String, code: String)
  RegisterToken(domain: String, client_id: String, secret: String, code: String)
  FetchClientInfo(domain: String, result: Result(#(String, String), String))
  FetchTokenInfo(domain: String, result: Result(String, String))
  FetchUserInfo(domain: String, token: String, result: Result(String, String))
}

pub type Status {
  Status(id: String, date: String, body: String)
}
