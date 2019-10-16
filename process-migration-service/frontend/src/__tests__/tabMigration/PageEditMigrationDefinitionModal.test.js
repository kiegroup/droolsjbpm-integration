import renderer from "react-test-renderer";
import React from "react";
import PageEditMigrationDefinitionModal from "../../component/tabMigration/PageEditMigrationDefinitionModal";
import migration from "../../../mock_data/migration.json";

test("PageEditMigrationDefinitionModal renders correctly using snapshot", () => {
  const tree = renderer
    .create(
      <PageEditMigrationDefinitionModal rowData={migration} migrationId={2} />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
