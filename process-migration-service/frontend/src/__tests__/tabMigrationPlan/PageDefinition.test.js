import renderer from "react-test-renderer";
import React from "react";
import PageDefinition from "../../component/tabMigrationPlan/wizardAddPlan/PageDefinition";
import definitions from "../../../mock_data/definitions.json";

test("PageDefinition renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageDefinition
        sourceInfo={definitions.sourceInfo}
        targetInfo={definitions.targetInfo}
        setInfo={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
