import renderer from "react-test-renderer";
import React from "react";
import PagePlanName from "../../component/tabMigrationPlan/wizardAddPlan/PagePlanName";

test("PagePlanName opens with an empty plan", () => {
  const plan = {};
  const mockFn = jest.fn();
  const tree = renderer
    .create(
      <PagePlanName plan={plan} onFieldChanged={mockFn} onIsValid={mockFn} />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});

test("PagePlanName opens using an existing plan", () => {
  const plan = {
    name: "plan name",
    description: "description"
  };
  const mockFn = jest.fn();
  const tree = renderer
    .create(
      <PagePlanName plan={plan} onFieldChanged={mockFn} onIsValid={mockFn} />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});