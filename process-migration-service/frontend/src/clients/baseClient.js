import axios from "axios";

const BACKEND_URL = "/rest";

export default class BaseClient {
  constructor(resourcePath) {
    this.instance = axios.create({
      baseURL: this.buildUrl(BACKEND_URL, resourcePath)
    });
  }

  buildUrl = (...args) => {
    return args.join("/");
  };
}
