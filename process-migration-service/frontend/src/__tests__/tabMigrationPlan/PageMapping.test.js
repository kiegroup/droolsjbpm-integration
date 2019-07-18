import renderer from "react-test-renderer";
import React from "react";
import PageMapping from "../../component/tabMigrationPlan/wizardAddPlan/PageMapping";
import evaluationDef from "../../../mock_data/process_definition_evaluation.json";
import mortgageDef from "../../../mock_data/process_definition_mortgage.json";

test("PageMapping renders correctly using snapshot", () => {
  const plan = {};
  const mockFn = jest.fn();
  const tree = renderer
    .create(
      <PageMapping
        plan={plan}
        sourceProcess={evaluationDef}
        targetProcess={mortgageDef}
        onMappingsChange={mockFn}
        onIsValid={mockFn}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
