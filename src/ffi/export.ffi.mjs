const sleep = (delay) => new Promise((resolve) => setTimeout(resolve, delay));
class DbAccess {
  constructor(store) {
    this.store = store;
    this.completed = false;
    this.error = false;
    this.db = null;
    this.dbdone = false;
  }

  async createDb() {
    this.db = null;

    const openDb = window.indexedDB.open("trunkfriends", 4);

    // Register two event handlers to act on the database being opened successfully, or not
    openDb.onerror = (e) => {
      console.log("Error opening db");
    };

    openDb.onsuccess = (e) => {
      console.log("Initialized db");
      this.db = e.target.result;
      this.dbdone = true;
    };

    openDb.onupgradeneeded = (e) => {
      const openedDb = e.target.result;

      openedDb.onerror = (e) => {
        console.log("Error loading database.");
      };

      // Create an objectStore for this database
      openedDb.createObjectStore("events", {
        keyPath: "date",
      });

      openedDb.createObjectStore("users", {
        keyPath: "account",
      });

      openedDb.createObjectStore("servers", {
        keyPath: "id_account",
      });

      console.log("created");
    };
  }

  async storeKey(value) {
    // Store the result of opening the database in the db variable. This is used a lot below
    this.createDb();
    // Temporary stupid stuff, replace with promices
    while (this.dbdone == false) {
      await sleep(5);
    } // wait until db is open before we return.
    console.log("Done creating db, now using it");

    const request = this.db
      .transaction(this.store, "readwrite")
      .objectStore(this.store)
      .add(value); // (1)
    request.onsuccess = (e) => {
      console.log("Stored without error");
      this.completed = true;
    };

    request.onerror = (e) => {
      console.log("Stored with error");
      this.error = true;
      this.completed = true;
    };

    // Tempoorary stupid stuff, replace with promices
    while (this.completed == false) {
      console.log("write sleep");
      await sleep(5);
    } // wait until db is open before we return.
  }
}

export function start() {
  const deleteDb = window.indexedDB.deleteDatabase("trunkfriends");

  deleteDb.onerror = (e) => {
    console.log("Db not deleted, probably not there");
    new DbAccess().createDb();
  };

  deleteDb.onsuccess = (e) => {
    console.log("Db deleted successfully");
    new DbAccess().createDb();
  };

  return 100;
}

export default function storeKey(store, value) {
  value = {
    id_account: "1232142@dummy.social",
    token: "27839478wrfjsdifjh2134897324weisdfjsflk",
    user_name: "lettosprey",
  };

  const access = new DbAccess("servers");
  access.storeKey(value);
}

export async function getServers() {
  await sleep(1000);
  console.log("Getting servers!");
  const access = new DbAccess("servers");
  const servers = access.getKey("1232142@dummy.social");
  console.log("servers" + servers);
  return servers;
}
