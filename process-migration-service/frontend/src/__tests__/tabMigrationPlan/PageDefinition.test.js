import renderer from "react-test-renderer";
import React from "react";
import PageDefinition from "../../component/tabMigrationPlan/wizardAddPlan/PageDefinition";
import evaluationDef from "../../../mock_data/process_definition_evaluation.json";
import mortgageDef from "../../../mock_data/process_definition_mortgage.json";

test("PageDefinition renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageDefinition
        sourceInfo={evaluationDef}
        targetInfo={mortgageDef}
        setInfo={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
