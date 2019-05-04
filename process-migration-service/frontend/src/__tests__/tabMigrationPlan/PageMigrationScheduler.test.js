import React from "react";
import { render, cleanup } from "react-testing-library";
import PageMigrationScheduler from "../../component/tabMigrationPlan/wizardExecuteMigration/PageMigrationScheduler.js";

// automatically unmount and cleanup DOM after the test is finished.
afterEach(cleanup);

//Because PageMigrationScheduler's DataTime component will update everyday to disable selection of old dates, so using snap-shot wont' work.
//instead I use react-testing-library here to compare expect components which I set a testId.
test("PageMigrationScheduler renders expected components", () => {
  const myMock = jest.fn();
  const { getByTestId } = render(
    <PageMigrationScheduler
      setCallbackUrl={myMock}
      setScheduleStartTime={myMock}
    />
  );

  expect(getByTestId("testid_callback")).toBeTruthy();
  expect(getByTestId("testid_syncMode")).toBeTruthy();
  expect(getByTestId("testid_asyncMode")).toBeTruthy();
});
