import renderer from "react-test-renderer";
import React from "react";
import PageMapping from "../../component/tabMigrationPlan/wizardAddPlan/PageMapping";
import evaluationDef from "../../../mock_data/process_definition_evaluation.json";
import mortgageDef from "../../../mock_data/process_definition_mortgage.json";

test("PageMapping renders correctly using snapshot", () => {
  const tree = renderer
    .create(
      <PageMapping
        sourceInfo={evaluationDef}
        targetInfo={mortgageDef}
        mappings={""}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
