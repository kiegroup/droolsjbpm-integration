import React from "react";
import ReactDOM from "react-dom";
import MainPageWithPfTab from "../component/MainPageWithPfTab";

describe("Main Page", () => {
  it("renders without crashing", () => {
    const div = document.createElement("div");
    ReactDOM.render(<MainPageWithPfTab />, div);
  });
});
