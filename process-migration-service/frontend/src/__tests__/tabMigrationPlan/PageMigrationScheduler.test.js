import React from "react";
import { render, cleanup, fireEvent } from "react-testing-library";

import PageMigrationScheduler from "../../component/tabMigrationPlan/wizardExecuteMigration/PageMigrationScheduler.js";

// automatically unmount and cleanup DOM after the test is finished.
afterEach(cleanup);

test("PageMigrationScheduler renders expected components", () => {
  const callbackFn = jest.fn();
  const isValidFn = jest.fn();
  const { container, getByLabelText } = render(
    <PageMigrationScheduler onFieldChange={callbackFn} onIsValid={isValidFn} />
  );

  expect(container).toMatchSnapshot();
  const callbackInput = getByLabelText("Callback URL");
  expect(callbackInput).toHaveProperty("value", "");

  fireEvent.change(callbackInput, {
    target: { value: "some url" }
  });

  expect(callbackFn).toHaveBeenCalled();
  expect(callbackFn.mock.calls[0][1]).toBe("some url");
});

test("PageMigrationScheduler renders error for wrong url", () => {
  const callbackFn = jest.fn();
  const isValidFn = jest.fn();
  const { container, getByLabelText } = render(
    <PageMigrationScheduler
      callbackUrl="wrong url"
      onFieldChange={callbackFn}
      onIsValid={isValidFn}
    />
  );

  expect(container).toMatchSnapshot();
  const callbackInput = getByLabelText("Callback URL");
  expect(callbackInput).toHaveProperty("value", "wrong url");
});

test("PageMigrationScheduler renders callback url", () => {
  const callbackFn = jest.fn();
  const isValidFn = jest.fn();
  const callbackUrl = "https://example.com/callback";
  const { container, getByLabelText } = render(
    <PageMigrationScheduler
      callbackUrl={callbackUrl}
      onFieldChange={callbackFn}
      onIsValid={isValidFn}
    />
  );

  expect(container).toMatchSnapshot();
  const callbackInput = getByLabelText("Callback URL");
  expect(callbackInput).toHaveProperty("value", callbackUrl);
});

test("PageMigrationScheduler is scheduled", () => {
  const callbackFn = jest.fn();
  const isValidFn = jest.fn();
  const callbackUrl = "https://example.com/callback";
  const scheduledTime = "2019-7-17T20:59:22.128Z";
  const { getByLabelText } = render(
    <PageMigrationScheduler
      callbackUrl={callbackUrl}
      scheduledStartTime={scheduledTime}
      onFieldChange={callbackFn}
      onIsValid={isValidFn}
    />
  );

  expect(getByLabelText("Schedule")).toHaveProperty("checked", true);
});
