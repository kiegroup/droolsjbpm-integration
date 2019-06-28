import BaseClient from "./baseClient";

class PlanClient extends BaseClient {
  constructor() {
    super("plans");
  }

  getAll() {
    return this.instance.get().then(response => response.data);
  }

  get(id) {
    return this.instance.get("" + id).then(response => response.data);
  }

  create(plan) {
    return this.instance.post("", plan).then(response => response.data);
  }

  update(id, plan) {
    return this.instance.put("" + id, plan).then(response => response.data);
  }

  delete(id) {
    return this.instance.delete("" + id);
  }
}

export default new PlanClient();
