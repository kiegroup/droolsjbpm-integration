import React from "react";
import ReactDOM from "react-dom";
import PageMigrationScheduler from "../../component/tabMigrationPlan/wizardExecuteMigration/PageMigrationScheduler";

describe("Main Page for PageMigrationScheduler", () => {
  it("renders without crashing", () => {
    const div = document.createElement("div");
    ReactDOM.render(<PageMigrationScheduler />, div);
  });
});
