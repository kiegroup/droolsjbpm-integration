import renderer from "react-test-renderer";
import React from "react";
import PageMappingDiagrams from "../../component/tabMigrationPlan/wizardAddPlan/PageMappingDiagrams";
import evaluationDef from "../../../mock_data/process_definition_evaluation.json";
import mortgageDef from "../../../mock_data/process_definition_mortgage.json";

test("PageMappingDiagrams renders correctly using snapshot", () => {
  const tree = renderer
    .create(
      <PageMappingDiagrams
        sourceProcess={evaluationDef}
        targetProcess={mortgageDef}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
