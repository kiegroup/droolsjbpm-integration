import renderer from "react-test-renderer";
import React from "react";
import PageReview from "../../component/tabMigrationPlan/PageReview";
import migration from "../../../mock_data/migration.json";

test("PageReview renders correctly using snapshot", () => {
  const tree = renderer
    .create(<PageReview inputJsonStr={JSON.stringify(migration)} />)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
