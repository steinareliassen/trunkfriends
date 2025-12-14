import api/auth.{fetch_client_info, fetch_token, fetch_user_info}
import common/session
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
      wrap_up(
        effect.from(fetch_client_info(domain, FetchClientInfo, ProcessError, _)),
        wrapper,
      )
      #(Domain(domain), effect.none())
    }

    FetchClientInfo(domain, client_id, secret) -> #(
      ClientInfo(domain:, client_id:, secret:),
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
          effect.from(fetch_token(
            domain,
            client_id,
            secret,
            code,
            FetchTokenInfo,
            ProcessError,
            _,
          )),
          wrapper,
        ),
      )
    }

    FetchTokenInfo(domain, token) -> #(
      DisplayStatus("Done fetching token, fetching user info"),
      wrap_up(
        effect.from(fetch_user_info(
          domain,
          token,
          FetchUserInfo,
          ProcessError,
          _,
        )),
        wrapper,
      ),
    )

    FetchUserInfo(domain:, token:, user_id:) -> #(
      DisplayStatus("Got userinfo, fetching first set of pages..."),
      effect.from(fn(dispatch) {
        dispatch(giveback(session.Session(domain:, token:, user_id:)))
      }),
    )

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
  FetchClientInfo(domain: String, client_id: String, secret: String)
  FetchTokenInfo(domain: String, token: String)
  FetchUserInfo(domain: String, token: String, user_id: String)
}

pub type Status {
  Status(id: String, date: String, body: String)
}
