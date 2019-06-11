import BaseClient from "./baseClient";

class KieServerClient extends BaseClient {
  constructor() {
    super("kieservers");
  }

  getKieServers() {
    return this.instance.get().then(res => res.data);
  }

  getDefinitions(kieServerId) {
    return this.instance
      .get(this.buildUrl(kieServerId, "definitions"))
      .then(res => res.data);
  }

  getDefinition(kieServerId, containerId, processId) {
    return this.instance
      .get(this.buildUrl(kieServerId, "definitions", containerId, processId))
      .then(res => res.data);
  }

  getInstances(kieServerId, page, pageSize) {
    return this.instance
      .get(this.buildUrl(kieServerId, "instances"), {
        params: {
          page,
          pageSize
        }
      })
      .then(res => res.data);
  }
}

export default new KieServerClient();
