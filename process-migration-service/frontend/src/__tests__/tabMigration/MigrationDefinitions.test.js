import React from "react";
import ReactDOM from "react-dom";
import MigrationDefinitions from "../../component/tabMigration/MigrationDefinitions";

describe("Main Page for Migration Defintions", () => {
  it("renders without crashing", () => {
    const div = document.createElement("div");
    ReactDOM.render(<MigrationDefinitions />, div);
  });
});
