import renderer from "react-test-renderer";
import React from "react";
import { MockupData_Migrations_Definitions } from "../../component/common/MockupData";
import PageEditMigrationDefinitionModal from "../../component/tabMigration/PageEditMigrationDefinitionModal";

test("PageEditMigrationDefinitionModal renders correctly using snapshot", () => {
  const mockData = MockupData_Migrations_Definitions;
  const tree = renderer
    .create(<PageEditMigrationDefinitionModal rowData={mockData[0]} />)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
