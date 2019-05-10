import renderer from "react-test-renderer";
import React from "react";
import PageViewMigrationLogs from "../../component/tabMigration/PageViewMigrationLogs";
import migration_results from "../../../mock_data/migration_results.json";

test("PageViewMigrationLogs renders correctly using snapshot", () => {
  const tree = renderer
    .create(<PageViewMigrationLogs migrationLogs={migration_results} />)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
