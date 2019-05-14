import renderer from "react-test-renderer";
import React from "react";
import plans from "../../../mock_data/plans.json";
import MigrationPlansTable from "../../component/tabMigrationPlan/MigrationPlansTable";

test("MigrationPlansTable renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <MigrationPlansTable
        openMigrationWizard={myMock}
        openAddPlanWizard={myMock}
        openAddPlanWizardWithInitialData={myMock}
        showDeleteDialog={myMock}
        filteredPlans={plans}
        updatePlan={myMock}
        retrieveAllPlans={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
