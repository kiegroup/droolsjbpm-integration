import React from "react";

import { Wizard } from "patternfly-react";

export const renderWizardSteps = (
  wizardSteps,
  activeStepIndex,
  activeSubStepIndex,
  onStepClick
) => {
  const activeStep = wizardSteps[activeStepIndex];
  const activeSubStep = activeStep.subSteps[activeSubStepIndex];

  return wizardSteps.map((step, stepIndex) => (
    <Wizard.Step
      key={stepIndex}
      stepIndex={stepIndex}
      step={step.step}
      label={step.label}
      title={step.title}
      activeStep={activeStep && activeStep.step}
      onClick={onStepClick}
    >
      {step.subSteps.map((sub, subStepIndex) => (
        <Wizard.SubStep
          key={subStepIndex}
          subStep={sub.subStep}
          activeSubStep={activeSubStep && activeSubStep.subStep}
        />
      ))}
    </Wizard.Step>
  ));
};
