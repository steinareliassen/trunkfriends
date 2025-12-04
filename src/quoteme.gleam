import controller
import gleam/list
import gleam/option
import lustre/element/keyed
import model

import lustre
import lustre/attribute
import lustre/effect.{type Effect}
import lustre/element.{type Element}
import lustre/element/html
import lustre/event

pub fn main() {
  let assert Ok(_) =
    lustre.application(init, update, view) |> lustre.start("#quoteme", Nil)
  Nil
}

fn init(_) -> #(model.Model, Effect(model.Msg)) {
  #(model.Model([], model.DomainField("")), effect.none())
}

fn update(
  model: model.Model,
  msg: model.Msg,
) -> #(model.Model, Effect(model.Msg)) {
  let model.Model(list, _) = model

  let #(steps, effect) = case msg {
    model.GiveDomain(domain) -> #(model.DomainField(domain), effect.none())
    model.GiveCode(domain: text, secret:, client_id:, code:) -> #(
      model.CodeField(domain: text, secret:, client_id:, code:),
      effect.none(),
    )
    model.RegisterClient(domain) -> {
      #(model.Domain(domain), effect.from(controller.fetch_domain(domain, _)))
    }

    model.FetchClientInfo(domain, result) -> #(
      case result {
        Ok(#(client_id, secret)) ->
          model.ClientInfo(domain:, client_id:, secret:)
        Error(error) ->
          model.DisplayError(error:, message: "Error fetching client info")
      },
      effect.none(),
    )

    model.RegisterToken(domain:, client_id:, secret:, code:) -> {
      #(
        model.DisplayStatus(
          "Obtained token for "
          <> domain
          <> ", now fetching the your user_id, needed to update your posts",
        ),
        effect.from(controller.fetch_token(domain, client_id, secret, code, _)),
      )
    }

    model.FetchTokenInfo(domain, token) ->
      case token {
        Ok(token) -> #(
          model.DisplayStatus("Done fetching token, fetching user info"),
          effect.from(controller.fetch_user_info(domain, token, _)),
        )
        Error(error) -> #(
          model.DisplayError(error:, message: "Error fetching token"),
          effect.none(),
        )
      }

    model.FetchUserInfo(domain:, token:, result:) ->
      case result {
        Ok(user_id) -> #(
          model.DisplayStatus("Got userinfo, fetching first set of pages..."),
          effect.from(controller.request_pages(
            domain,
            token,
            user_id,
            option.None,
            _,
          )),
        )
        Error(error) -> #(
          model.DisplayError(
            error:,
            message: "Something went wrong fetching user info",
          ),
          effect.none(),
        )
      }

    model.ProcessError(error:, message:) -> #(
      model.DisplayError(error:, message:),
      effect.none(),
    )

    model.FetchPages(
      domain:,
      token:,
      user_id:,
      result:,
      response:,
      process:,
      max_id:,
    ) -> #(
      model.ProcessPosts(domain, token, user_id, result, response, max_id),
      case result {
        [] ->
          effect.from(controller.request_pages(
            domain,
            token,
            user_id,
            max_id,
            _,
          ))
        _ ->
          case process {
            False -> effect.none()
            True ->
              effect.from(controller.patch_status(
                domain,
                token,
                user_id,
                result,
                response,
                max_id,
                _,
              ))
          }
      },
    )
  }
  #(model.Model(list, steps), effect)
}

fn view(model: model.Model) -> Element(model.Msg) {
  let model.Model(_, state) = model
  case state {
    model.DomainField(domain) ->
      html.div([], [
        html.h1([], [html.text("What mastodon instance are you using?")]),
        html.div([], [
          view_input(domain, model.GiveDomain, model.RegisterClient),
        ]),
      ])

    model.Domain(domain) ->
      html.div([], [
        html.text(
          "Registering client info for " <> domain <> " waiting for result",
        ),
      ])

    model.ClientInfo(domain, client_id, secret) -> {
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
            event.on_click(model.GiveCode(
              domain:,
              client_id:,
              secret:,
              code: "",
            )),
          ],
          [
            html.text("Continue to next page."),
          ],
        ),
      ])
    }

    model.CodeField(domain:, client_id:, secret:, code:) ->
      html.div([], [
        html.h1([], [html.text("Paste the code here and click continue")]),
        html.div([], [
          view_input_with_secret(
            domain,
            client_id,
            secret,
            code,
            model.GiveCode,
            model.RegisterToken,
          ),
        ]),
      ])

    model.Token(domain:) ->
      text_paragraph(
        "Registering client info for " <> domain <> " waiting for result",
      )

    model.DisplayStatus(text) -> text_paragraph(text)

    model.DisplayError(error, message) -> {
      html.div([], [
        html.h2([], [html.text("Something went wrong.")]),
        text_paragraph(message),
        html.h3([], [html.text("Error message:")]),
        text_paragraph(error),
      ])
    }

    model.ProcessPosts(domain, token, user_id, list, results, max_id) -> {
      html.div([], [
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
              model.FetchPages(
                domain:,
                token:,
                user_id:,
                result: list,
                response: results,
                max_id: max_id,
                process: True,
              ),
            )
          _ -> element.none()
        },
      ])
    }
  }
}

fn status_line(status: model.Status) -> Element(model.Msg) {
  html.div([], [
    html.div([], [
      html.text("ID :" <> status.id <> " Created: " <> status.date),
    ]),
    html.div([], [
      html.text("Text: " <> status.body),
    ]),
  ])
}

fn text_paragraph(text: String) {
  html.p([], [html.text(text)])
}

fn button_text_paragraph(text: String, button: String, on_click: model.Msg) {
  html.p([], [
    html.text(text),
    html.button([event.on_click(on_click)], [html.text(button)]),
  ])
}

fn view_input(
  model: String,
  handle_text: fn(String) -> model.Msg,
  handle_button: fn(String) -> model.Msg,
) -> Element(model.Msg) {
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
  handle_text: fn(String, String, String, String) -> model.Msg,
  handle_button: fn(String, String, String, String) -> model.Msg,
) -> Element(model.Msg) {
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
