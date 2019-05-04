import renderer from "react-test-renderer";
import React from "react";
import { MockupData_planList } from "../../component/common/MockupData";
import MigrationPlansTable from "../../component/tabMigrationPlan/MigrationPlansTable";

test("MigrationPlansTable renders correctly using snapshot", () => {
  const mockData = MockupData_planList;
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <MigrationPlansTable
        openMigrationWizard={myMock}
        openAddPlanWizard={myMock}
        openAddPlanWizardWithInitialData={myMock}
        showDeleteDialog={myMock}
        filteredPlans={mockData}
        updatePlan={myMock}
        retrieveAllPlans={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
