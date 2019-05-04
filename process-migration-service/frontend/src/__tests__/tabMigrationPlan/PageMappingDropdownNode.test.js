import renderer from "react-test-renderer";
import React from "react";
import PageMappingDropdownNode from "../../component/tabMigrationPlan/wizardAddPlan/PageMappingDropdownNode";

test("PageMappingDropdownNode renders correctly using snapshot", () => {
  const sourceNode = [
    {
      value: "_D3E17247-1D94-47D8-93AD-D645E317B736",
      label: "Self Evaluation:_D3E17247-1D94-47D8-93AD-D645E317B736"
    },
    {
      value: "_E35438DF-03AF-4D7B-9DCB-30BC70E7E92E",
      label: "PM Evaluation:_E35438DF-03AF-4D7B-9DCB-30BC70E7E92E"
    },
    {
      value: "_AB431E82-86BC-460F-9D8B-7A7617565B36",
      label: "HR Evaluation:_AB431E82-86BC-460F-9D8B-7A7617565B36"
    },
    {
      value: "_B8E4DA1E-A62B-49C2-9A94-FEE5F5FD2B4E",
      label: "Input:_B8E4DA1E-A62B-49C2-9A94-FEE5F5FD2B4E"
    }
  ];
  const myMock = jest.fn();
  const tree = renderer
    .create(
      <PageMappingDropdownNode
        options={sourceNode}
        title="Source Nodes"
        onDropdownChange={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
