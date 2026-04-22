export function storeServers(servers) {
  console.log("removing and setting! " + servers);
  localStorage.removeItem("servers");
  localStorage.setItem("servers", servers);
}

export function getServers() {
  console.log("getting servers!");
  const a = localStorage.getItem("servers");
  console.log("a " + a);
  return localStorage.getItem("servers");
}
