import gleam/option

pub type Model {
  Model(servers: List(#(String, String)), steps: Steps)
}

pub type Steps {
  DomainField(String)
  Domain(String)
  ClientInfo(domain: String, client_id: String, secret: String)
  CodeField(domain: String, client_id: String, secret: String, code: String)
  Token(domain: String)
  DisplayStatus(status: String)
  DisplayError(error: String, message: String)
  ProcessPosts(
    domain: String,
    token: String,
    user_id: String,
    posts: List(Status),
    response: List(Result(Status, String)),
    max_id: option.Option(String),
  )
}

pub type Msg {
  GiveDomain(String)
  RegisterClient(String)
  ProcessError(error: String, message: String)
  GiveCode(domain: String, client_id: String, secret: String, code: String)
  RegisterToken(domain: String, client_id: String, secret: String, code: String)
  FetchClientInfo(domain: String, result: Result(#(String, String), String))
  FetchTokenInfo(domain: String, result: Result(String, String))
  FetchUserInfo(domain: String, token: String, result: Result(String, String))
  FetchPages(
    domain: String,
    token: String,
    user_id: String,
    result: List(Status),
    response: List(Result(Status, String)),
    max_id: option.Option(String),
    process: Bool,
  )
}

pub type Status {
  Status(id: String, date: String, body: String)
}
