import BaseClient from "./baseClient";

class MigrationClient extends BaseClient {
  constructor() {
    super("migrations");
  }

  getAll() {
    return this.instance.get().then(res => res.data);
  }

  get(id) {
    return this.instance.get("" + id).then(res => res.data);
  }

  getResults(id) {
    return this.instance
      .get(this.buildUrl("" + id, "results"))
      .then(res => res.data);
  }

  create(migration) {
    return this.instance.post("", migration).then(res => res.data);
  }

  update(id, migration) {
    return this.instance.put("" + id, migration).then(res => res.data);
  }

  delete(id) {
    return this.instance.delete("" + id);
  }
}

export default new MigrationClient();
