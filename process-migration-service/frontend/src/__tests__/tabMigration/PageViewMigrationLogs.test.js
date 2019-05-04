import renderer from "react-test-renderer";
import React from "react";
import { MockupData_Migrations_Logs } from "../../component/common/MockupData";
import PageViewMigrationLogs from "../../component/tabMigration/PageViewMigrationLogs";

test("PageViewMigrationLogs renders correctly using snapshot", () => {
  const mockData = MockupData_Migrations_Logs;
  const tree = renderer
    .create(<PageViewMigrationLogs migrationLogs={mockData} />)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
