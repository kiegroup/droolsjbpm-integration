import renderer from "react-test-renderer";
import React from "react";
import PageDefinition from "../../component/tabMigrationPlan/wizardAddPlan/PageDefinition";

test("PageDefinition renders correctly using snapshot", () => {
  const myMock = jest.fn();
  const kieServerId = "kie server id";
  const plan = {
    source: {
      containerId: "sourceContainerId"
    },
    target: {
      containerId: "targetContainerId"
    }
  };
  const tree = renderer
    .create(
      <PageDefinition
        kieServerId={kieServerId}
        plan={plan}
        onIsValid={myMock}
        onChangeSource={myMock}
        onChangeTarget={myMock}
        setSourceDefinition={myMock}
        setTargetDefinition={myMock}
      />
    )
    .toJSON();
  expect(tree).toMatchSnapshot();
});
